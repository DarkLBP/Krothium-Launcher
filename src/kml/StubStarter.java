package kml;

import kml.handlers.URLHandler;

import javax.net.ssl.HttpsURLConnection;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
class StubStarter {
    public static void main(String[] args){
        System.out.println("StubStarter launcher with " + args.length + " arguments.");
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
            if (args.length > 0){
                Class<?> gameClass = Class.forName(args[0]);
                Method method = gameClass.getMethod("main", String[].class);
                method.invoke(null, (Object)args);
            } else {
                System.out.println("No main class specified!");
            }
        } catch (Exception ex){
            System.out.println("Failed to start the server.");
            ex.printStackTrace();
        }
    }
}
