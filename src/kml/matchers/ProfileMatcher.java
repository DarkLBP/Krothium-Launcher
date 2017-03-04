package kml.matchers;

import kml.Utils;

import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class ProfileMatcher implements URLMatcher{
    private final Pattern profileRegex = Pattern.compile("/session/minecraft/profile/([0-9a-fA-F]+?)");

    @Override
    public boolean match(URL url){
        final String profileHost = "sessionserver.mojang.com";
        if (url.getHost().equalsIgnoreCase(profileHost)){
            Matcher m = profileRegex.matcher(url.getPath());
            return m.matches();
        }
        return false;
    }
    @Override
    public URLConnection handle(URL url){
        Matcher m = profileRegex.matcher(url.getPath());
        if (m.matches()){
            String profileID = m.group(1);
            try{
                return Utils.stringToURL("https://mc.krothium.com/profiles/" + profileID + (url.getQuery() != null ? "?" + url.getQuery() : "")).openConnection();
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }
}
