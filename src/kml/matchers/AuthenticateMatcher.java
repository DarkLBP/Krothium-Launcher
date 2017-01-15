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
            URL remoteURL = Constants.AUTHENTICATE_URL;
            try{
                return remoteURL != null ? remoteURL.openConnection() : null;
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
