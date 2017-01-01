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
public class RefreshMatcher implements URLMatcher{
    private final String refreshURL = "https://authserver.mojang.com/refresh";
    private final URL url;

    public RefreshMatcher(URL url){
        this.url = url;
    }
    @Override
    public boolean match(){
        return this.url.toString().equalsIgnoreCase(refreshURL);
    }
    @Override
    public URLConnection handle(){
        if (this.url.toString().equalsIgnoreCase(refreshURL)){
            URL remoteURL;
            if (Constants.USE_HTTPS){
                remoteURL = Utils.stringToURL("https://mc.krothium.com/refresh");
            } else {
                remoteURL = Utils.stringToURL("http://mc.krothium.com/refresh");
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
