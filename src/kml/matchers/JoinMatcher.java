package kml.matchers;

import java.io.IOException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import kml.Utils;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class JoinMatcher implements URLMatcher{
    private final String joinURL = "https://sessionserver.mojang.com/session/minecraft/join";
    private final URL url;
    
    public JoinMatcher(URL url){
        this.url = url;
    }
    @Override
    public boolean match(){
        return this.url.toString().equalsIgnoreCase(joinURL);
    }
    @Override
    public HttpsURLConnection handle(){
        if (this.url.toString().equalsIgnoreCase(joinURL)){
            URL remoteURL = Utils.stringToURL("https://mc.krothium.com/server/join");
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
