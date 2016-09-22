package kml.matchers;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import kml.Utils;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class CheckServerMatcher implements URLMatcher{
    private final String checkURL = "http://session.minecraft.net/game/checkserver.jsp";
    private final URL url;
    
    public CheckServerMatcher(URL url){
        this.url = url;
    }
    @Override
    public boolean match(){
        return (this.url.toString().contains(checkURL) && this.url.getQuery() != null);
    }
    @Override
    public URLConnection handle(){
        if ((this.url.toString().contains(checkURL) && this.url.getQuery() != null)){
            URL remoteURL = Utils.stringToURL("http://mc.krothium.com/server/checkserver?" + this.url.getQuery());
            try{
                return remoteURL.openConnection();
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
