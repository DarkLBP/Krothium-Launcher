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
public class AuthenticateMatcher implements URLMatcher{
    private final String authenticateURL = "https://authserver.mojang.com/authenticate";
    private final URL url;

    public AuthenticateMatcher(URL url){
        this.url = url;
    }
    @Override
    public boolean match(){
        return this.url.toString().equalsIgnoreCase(authenticateURL);
    }
    @Override
    public URLConnection handle(){
        if (this.url.toString().equalsIgnoreCase(authenticateURL)){
            URL remoteURL;
            if (Constants.USE_HTTPS){
                remoteURL = Utils.stringToURL("https://mc.krothium.com/authenticate");
            } else {
                remoteURL = Utils.stringToURL("http://mc.krothium.com/authenticate");
            }
            try{
                return remoteURL.openConnection();
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
