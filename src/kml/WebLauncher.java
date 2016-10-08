package kml;

import com.sun.net.httpserver.HttpServer;
import javafx.application.Application;
import kml.exceptions.AuthenticationException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */

class WebLauncher {

    public static final Kernel kernel = new Kernel();
    public static void load(){
        final Console console = kernel.getConsole();
        console.includeTimestamps(true);
        try {
            HttpsURLConnection con = (HttpsURLConnection)Constants.HANDSHAKE_URL.openConnection();
            int responseCode = con.getResponseCode();
            Constants.USE_HTTPS = (responseCode == 204);
        } catch (SSLHandshakeException ex) {
            Constants.USE_HTTPS = false;
        } catch (IOException ex){
            Constants.USE_LOCAL = true;
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
        InetSocketAddress add = new InetSocketAddress("localhost", Constants.USED_PORT);
        HttpServer server;
        try {
            server = HttpServer.create(add, 0);
            server.createContext("/", new WebHandler(kernel));
            server.setExecutor(null);
            server.start();
        } catch (IOException ex) {
            Logger.getLogger(WebLauncher.class.getName()).log(Level.SEVERE, null, ex);
        }
        console.printInfo("Started bundled web server in port " + Constants.USED_PORT);
        try{
            Application.launch(WebBrowser.class);
        } catch (Exception ex){
            console.printError("Failed to open web browser.\n" + ex.getMessage());
        }
    }
}
