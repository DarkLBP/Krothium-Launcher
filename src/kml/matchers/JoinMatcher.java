package kml.matchers;

import kml.Constants;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class JoinMatcher implements URLMatcher{
    private final String joinURL = "https://sessionserver.mojang.com/session/minecraft/join";

    @Override
    public boolean match(URL url){
        return url.toString().equalsIgnoreCase(joinURL);
    }
    @Override
    public URLConnection handle(URL url){
        if (url.toString().equalsIgnoreCase(joinURL)){
            try{
                return Constants.JOINSERVER.openConnection();
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
