package kml.matchers;

import kml.Utils;

import java.net.URL;
import java.net.URLConnection;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class JoinServerMatcher implements URLMatcher{
    private final String joinURL = "http://session.minecraft.net/game/joinserver.jsp";

    @Override
    public boolean match(URL url){
        return url.toString().contains(joinURL) && url.getQuery() != null;
    }
    @Override
    public URLConnection handle(URL url){
        if (url.toString().contains(joinURL) && url.getQuery() != null){
            try{
                return Utils.stringToURL("http://mc.krothium.com/server/joinserver?" + url.getQuery()).openConnection();
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }
}
