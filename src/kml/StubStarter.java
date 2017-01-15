package kml;

import kml.handlers.URLHandler;

import java.lang.reflect.Method;
import java.net.URL;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
class StubStarter {
    public static void main(String[] args){
        System.out.println("StubStarter launcher with " + args.length + " arguments.");
        if (!Utils.ignoreHTTPSCert()){
            System.err.println("Failed load custom HTTPS certificate checker.");
        }
        Utils.testNetwork();
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
