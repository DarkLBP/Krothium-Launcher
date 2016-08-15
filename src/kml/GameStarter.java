package kml;

import java.lang.reflect.Method;
import java.net.URL;
import kml.handlers.URLHandler;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class GameStarter {
    public static void main(String[] args){
        System.out.println("GameStarter launcher with " + args.length + " arguments.");
        URL.setURLStreamHandlerFactory(new URLHandler());
        String mainClass = args[0];
        String[] gameArgs = new String[args.length - 1];
        for (int i = 1; i < args.length; i++){
            gameArgs[i - 1] = args[i];
        }
        try{
            Class<?> gameClass = Class.forName(mainClass);
            Method method = gameClass.getMethod("main", String[].class);
            method.invoke(null, (Object)gameArgs);
        } catch (Exception ex){
            System.out.println("Failed to start the game.");
            ex.printStackTrace();
        }
    }
}
