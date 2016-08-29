package kml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import kml.exceptions.AuthenticationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Random;
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
        } catch (Exception e) {
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
        Thread keepAlive = new Thread(){
            @Override
            public void run(){
                long diff = (System.nanoTime() - lastKeepAlive);
                long result = TimeUnit.MILLISECONDS.convert(diff, TimeUnit.NANOSECONDS);
                while (result < Constants.KEEPALIVE_TIMEOUT){
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ex) {
                        console.printError("Error in KeepAlive thread.");
                    }
                    diff = (System.nanoTime() - lastKeepAlive);
                    result = TimeUnit.MILLISECONDS.convert(diff, TimeUnit.NANOSECONDS);
                }
                console.printInfo("KeepAlive timeout exceeded. Saving logs and closing launcher...");
                try {
                    File log;
                    console.setEnabled(false);
                    if (console.usesCompression()){
                        console.stopCompressing();
                        log = new File(kernel.getWorkingDir() + File.separator + "logs" + File.separator + "weblauncher.log.gz");
                    } else {
                        log = new File(kernel.getWorkingDir() + File.separator + "logs" + File.separator + "weblauncher.log");
                    }
                    if (log.exists()){
                        log.delete();
                    }
                    if (!log.getParentFile().exists()){
                        log.getParentFile().mkdirs();
                    }
                    FileOutputStream out = new FileOutputStream(log);
                    out.write(console.getData());
                    out.close();
                } catch (Exception ex) {}
                System.exit(0);
            }
        };
        keepAlive.start();
        while (status){
            Socket s;
            try {
                s = ss.accept();
                WebLauncherThread thread = new WebLauncherThread(s, kernel);
                thread.start();
            } catch (IOException ex) {
                console.printError("Failed to accept connection.\n" + ex.getMessage());
            }
        }
    }
}
