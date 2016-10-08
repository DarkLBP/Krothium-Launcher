package kml.matchers;

import kml.Utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class JoinServerMatcher implements URLMatcher{
    private final String joinURL = "http://session.minecraft.net/game/joinserver.jsp";
    private final URL url;
    
    public JoinServerMatcher(URL url){
        this.url = url;
    }
    @Override
    public boolean match(){
        return (this.url.toString().contains(joinURL) && this.url.getQuery() != null);
    }
    @Override
    public URLConnection handle(){
        if ((this.url.toString().contains(joinURL) && this.url.getQuery() != null)){
            URL remoteURL = Utils.stringToURL("http://mc.krothium.com/server/joinserver?" + this.url.getQuery());
            try{
                return remoteURL != null ? remoteURL.openConnection() : null;
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
