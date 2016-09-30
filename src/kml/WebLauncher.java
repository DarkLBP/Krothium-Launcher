package kml;

import com.sun.net.httpserver.HttpServer;
import kml.exceptions.AuthenticationException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            HttpsURLConnection con = (HttpsURLConnection)Constants.HANDSHAKE_URL.openConnection();
            int responseCode = con.getResponseCode();
            Constants.USE_HTTPS = (responseCode == 204);
        } catch (SSLHandshakeException ex) {
            Constants.USE_HTTPS = false;
        } catch (IOException ex){
            //
        }
        console.printInfo("Using HTTPS when available? | " + Constants.USE_HTTPS);
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
        InetSocketAddress add = new InetSocketAddress("localhost", port);
        HttpServer server;
        try {
            server = HttpServer.create(add, 0);
            server.createContext("/", new WebHandler(kernel));
            server.setExecutor(null);
            server.start();
        } catch (IOException ex) {
            Logger.getLogger(WebLauncher.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    }
}
