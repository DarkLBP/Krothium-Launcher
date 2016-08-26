package kml.matchers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kml.Utils;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class ProfileMatcher implements URLMatcher{
    private final String profileHost = "sessionserver.mojang.com";
    private final Pattern profileRegex = Pattern.compile("/session/minecraft/profile/([0-9a-fA-F]+?)");
    private final URL url;
    
    public ProfileMatcher(URL url){
        this.url = url;
    }
    @Override
    public boolean match(){
        if (this.url.getHost().equalsIgnoreCase(profileHost)){
            Matcher m = profileRegex.matcher(this.url.getPath());
            return m.matches();
        }
        return false;
    }
    @Override
    public HttpURLConnection handle(){
        Matcher m = profileRegex.matcher(this.url.getPath());
        if (m.matches()){
            String profileID = m.group(1);
            URL remoteURL = Utils.stringToURL("https://mc.krothium.com/profiles/" + profileID);
            try{
                HttpURLConnection con = (HttpURLConnection)remoteURL.openConnection();
                return con;
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
