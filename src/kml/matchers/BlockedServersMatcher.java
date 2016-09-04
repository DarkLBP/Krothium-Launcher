package kml.matchers;

import java.io.IOException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import kml.Utils;

/**
 * @website https://krothium.com
 * @author DarkLBP
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
    public HttpsURLConnection handle(){
        if (this.url.toString().equalsIgnoreCase(blockURL)){
            URL remoteURL = Utils.stringToURL("https://mc.krothium.com/server/blockedservers");
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
