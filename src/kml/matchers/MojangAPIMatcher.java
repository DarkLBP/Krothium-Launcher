package kml.matchers;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import kml.Utils;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class MojangAPIMatcher implements URLMatcher{
    private final String apiHost = "api.mojang.com";
    private final Pattern apiRegex = Pattern.compile("/(.+?)");
    private final URL url;
    
    public MojangAPIMatcher(URL url){
        this.url = url;
    }
    @Override
    public boolean match(){
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
    public HttpsURLConnection handle(){
        Matcher m = apiRegex.matcher(this.url.getPath());
        if (m.matches()){
            String path = m.group(1);
            URL remoteURL = Utils.stringToURL("https://mc.krothium.com/api/" + path);
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
