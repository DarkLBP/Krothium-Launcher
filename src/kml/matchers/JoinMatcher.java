package kml.matchers;

import kml.Constants;
import kml.Utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

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
    public URLConnection handle(){
        if (this.url.toString().equalsIgnoreCase(joinURL)){
            URL remoteURL;
            if (Constants.USE_HTTPS){
                remoteURL = Utils.stringToURL("https://mc.krothium.com/server/join");
            } else {
                remoteURL = Utils.stringToURL("http://mc.krothium.com/server/join");
            }
            try{
                return remoteURL != null ? remoteURL.openConnection() : null;
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
