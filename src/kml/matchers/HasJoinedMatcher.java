package kml.matchers;

import kml.Utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class HasJoinedMatcher implements URLMatcher{
    private final String hasURL = "https://sessionserver.mojang.com/session/minecraft/hasJoined";

    @Override
    public boolean match(URL url){
        return url.toString().contains(hasURL) && url.getQuery() != null;
    }
    @Override
    public URLConnection handle(URL url){
        if (url.toString().contains(hasURL) && url.getQuery() != null){
            try{
                return Utils.stringToURL("https://mc.krothium.com/server/hasJoined?" + url.getQuery()).openConnection();
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }
}
