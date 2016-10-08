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
public class MojangAPIMatcher implements URLMatcher{
    private final Pattern apiRegex = Pattern.compile("/(.+?)");
    private final URL url;
    
    public MojangAPIMatcher(URL url){
        this.url = url;
    }
    @Override
    public boolean match(){
        final String apiHost = "api.mojang.com";
        if (this.url.getHost().equalsIgnoreCase(apiHost)){
            Matcher m = apiRegex.matcher(this.url.getPath());
            if (m.matches()){
                String path = m.group(1);
                if (path.equalsIgnoreCase("profiles/minecraft")){
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public URLConnection handle(){
        Matcher m = apiRegex.matcher(this.url.getPath());
        if (m.matches()){
            String path = m.group(1);
            URL remoteURL;
            if (Constants.USE_HTTPS){
                remoteURL = Utils.stringToURL("https://mc.krothium.com/api/" + path);
            } else {
                remoteURL = Utils.stringToURL("http://mc.krothium.com/api/" + path);
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
