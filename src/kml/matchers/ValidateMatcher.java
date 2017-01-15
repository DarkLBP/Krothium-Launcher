package kml.matchers;

import kml.Constants;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class ValidateMatcher implements URLMatcher{
    private final String validateURL = "https://authserver.mojang.com/validate";
    private final URL url;

    public ValidateMatcher(URL url){
        this.url = url;
    }
    @Override
    public boolean match(){
        return this.url.toString().equalsIgnoreCase(validateURL);
    }
    @Override
    public URLConnection handle(){
        if (this.url.toString().equalsIgnoreCase(validateURL)){
            URL remoteURL = Constants.VALIDATE_URL;
            try{
                return remoteURL != null ? remoteURL.openConnection() : null;
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
