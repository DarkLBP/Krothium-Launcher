package kml.matchers;

import kml.Utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class CheckServerMatcher implements URLMatcher{
    private final String checkURL = "http://session.minecraft.net/game/checkserver.jsp";

    @Override
    public boolean match(URL url) {
        return url.toString().contains(checkURL) && url.getQuery() != null;
    }
    @Override
    public URLConnection handle(URL url){
        if (url.toString().contains(checkURL) && url.getQuery() != null){
            try{
                return Utils.stringToURL("http://mc.krothium.com/server/checkserver?" + url.getQuery()).openConnection();
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }
}
