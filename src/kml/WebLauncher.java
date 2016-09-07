package kml;

import java.io.BufferedInputStream;
import kml.exceptions.AuthenticationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public class WebLauncher {
    
    public static long lastKeepAlive;
    public static void load(){
        final Kernel kernel = new Kernel();
        final Console console = kernel.getConsole();
        console.includeTimestamps(true);
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            Path ksPath = Paths.get(System.getProperty("java.home"), "lib", "security", "cacerts");
            keyStore.load(Files.newInputStream(ksPath), "changeit".toCharArray());
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            try (InputStream caInput = new BufferedInputStream(
                WebLauncher.class.getResourceAsStream("/mc_server_cert.der"))) {
                Certificate crt = cf.generateCertificate(caInput);
                console.printInfo("Added certificate for " + ((X509Certificate) crt).getSubjectDN());
                keyStore.setCertificateEntry("DSTRootCAX3", crt);
            }
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            SSLContext.setDefault(sslContext);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | KeyManagementException e) {
            console.printError("Failed to trust mc.krothium.com certificate.\n" + e.getMessage());
        }
        kernel.loadVersions();
        kernel.loadProfiles();
        kernel.loadUsers();
        Authentication a = kernel.getAuthentication();
        if (a.hasSelectedUser()){
            try{
                a.refresh();
            }catch(AuthenticationException ex){
                console.printError(ex.getMessage());
            }
            kernel.saveProfiles();
        }
        Random rand = new Random();
        int portStart = 24000;
        int portEnd = 25000;
        int port = rand.nextInt((portEnd - portStart) + 1) + portStart;
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port, 100, InetAddress.getLoopbackAddress());
        } catch (IOException ex) {
            console.printError("Failed to initialize bundled server.\n" + ex.getMessage());
        }
        boolean status = true;
        console.printInfo("Started bundled web server in port " + port);
        try{
            Utils.openWebsite("http://mc.krothium.com/launcher/?p=" + port);
        } catch (Exception ex){
            console.printError("Failed to open web browser.\n" + ex.getMessage());
        }
        WebLauncher.lastKeepAlive = System.nanoTime();
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                long diff = (System.nanoTime() - lastKeepAlive);
                long result = TimeUnit.MILLISECONDS.convert(diff, TimeUnit.NANOSECONDS);
                if (result >= Constants.KEEPALIVE_TIMEOUT){
                    console.printInfo("KeepAlive timeout exceeded. Launcher closed.");
                    kernel.exitSafely();
                }
            }
        }, 0, 1000);
        while (status){
            try {
                Socket s = ss.accept();
                WebLauncherThread thread = new WebLauncherThread(s, kernel);
                thread.start();
            } catch (IOException | NullPointerException ex) {
                console.printError("Failed to accept connection.\n" + ex.getMessage());
            }
        }
    }
}
