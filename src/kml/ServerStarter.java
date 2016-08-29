package kml;

import java.lang.reflect.Method;
import java.net.URL;
import kml.handlers.URLHandler;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class ServerStarter {
    public static void main(String[] args){
        System.out.println("ServerStarter launcher with " + args.length + " arguments.");
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
