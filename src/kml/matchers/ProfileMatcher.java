package kml.matchers;

import kml.Constants;
import kml.Utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class ProfileMatcher implements URLMatcher{
    private final Pattern profileRegex = Pattern.compile("/session/minecraft/profile/([0-9a-fA-F]+?)");
    private final URL url;
    
    public ProfileMatcher(URL url){
        this.url = url;
    }
    @Override
    public boolean match(){
        final String profileHost = "sessionserver.mojang.com";
        if (this.url.getHost().equalsIgnoreCase(profileHost)){
            Matcher m = profileRegex.matcher(this.url.getPath());
            return m.matches();
        }
        return false;
    }
    @Override
    public URLConnection handle(){
        Matcher m = profileRegex.matcher(this.url.getPath());
        if (m.matches()){
            String profileID = m.group(1);
            URL remoteURL;
            if (Constants.USE_HTTPS){
                remoteURL = Utils.stringToURL("https://mc.krothium.com/profiles/" + profileID + (this.url.getQuery() != null ? "?" + this.url.getQuery() : ""));
            } else {
                remoteURL = Utils.stringToURL("http://mc.krothium.com/profiles/" + profileID + (this.url.getQuery() != null ? "?" + this.url.getQuery() : ""));
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
