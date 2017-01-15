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
            URL remoteURL = Constants.JOINSERVER;
            try{
                return remoteURL != null ? remoteURL.openConnection() : null;
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
