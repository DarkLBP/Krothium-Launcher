package kml;

import java.lang.reflect.Method;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import kml.handlers.URLHandler;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class ServerStarter {
    public static void main(String[] args){
        System.out.println("ServerStarter launcher with " + args.length + " arguments.");
        try {
            HttpsURLConnection con = (HttpsURLConnection)Constants.HANDSHAKE_URL.openConnection();
            int responseCode = con.getResponseCode();
            Constants.USE_HTTPS = (responseCode == 204);
        } catch (Exception ex) {
            Constants.USE_HTTPS = false;
        }
        System.out.println("Using HTTPS when available? | " + Constants.USE_HTTPS);
        URL.setURLStreamHandlerFactory(new URLHandler());
        try{
            Class<?> gameClass = Class.forName("net.minecraft.server.MinecraftServer");
            Method method = gameClass.getMethod("main", String[].class);
            method.invoke(null, (Object)args);
        } catch (Exception ex){
            System.out.println("Failed to start the server.");
            ex.printStackTrace();
        }
    }
}
