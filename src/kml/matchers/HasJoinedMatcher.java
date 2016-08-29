package kml.matchers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import kml.Utils;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class HasJoinedMatcher implements URLMatcher{
    private final String hasURL = "https://sessionserver.mojang.com/session/minecraft/hasJoined";
    private final URL url;
    
    public HasJoinedMatcher(URL url){
        this.url = url;
    }
    @Override
    public boolean match(){
        return (this.url.toString().contains(hasURL) && this.url.getQuery() != null);
    }
    @Override
    public HttpURLConnection handle(){
        if ((this.url.toString().contains(hasURL) && this.url.getQuery() != null)){
            URL remoteURL = Utils.stringToURL("https://mc.krothium.com/server/hasJoined?" + this.url.getQuery());
            try{
                HttpsURLConnection con = (HttpsURLConnection)remoteURL.openConnection();
                return con;
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
