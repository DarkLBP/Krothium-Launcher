package kml.matchers;

import kml.Constants;
import kml.Utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class BlockedServersMatcher implements URLMatcher{
    private final String blockURL = "https://sessionserver.mojang.com/blockedservers";
    private final URL url;
    
    public BlockedServersMatcher(URL url){
        this.url = url;
    }
    @Override
    public boolean match(){
        return this.url.toString().equalsIgnoreCase(blockURL);
    }
    @Override
    public URLConnection handle(){
        if (this.url.toString().equalsIgnoreCase(blockURL)){
            URL remoteURL;
            if (Constants.USE_HTTPS){
                remoteURL = Utils.stringToURL("https://mc.krothium.com/server/blockedservers");
            } else {
                remoteURL = Utils.stringToURL("http://mc.krothium.com/server/blockedservers");
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
