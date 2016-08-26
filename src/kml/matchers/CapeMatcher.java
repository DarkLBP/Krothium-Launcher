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
public class CapeMatcher implements URLMatcher{
    private final String capeHost = "skins.minecraft.net";
    private final String capeHostLegacy = "s3.amazonaws.com";
    private final Pattern capeRegex = Pattern.compile("/MinecraftCloaks/(.+?)\\.png");
    private final URL url;
    
    public CapeMatcher(URL url){
        this.url = url;
    }
    @Override
    public boolean match(){
        if (this.url.getHost().equalsIgnoreCase(capeHost) || this.url.getHost().equalsIgnoreCase(capeHostLegacy)){
            Matcher m = capeRegex.matcher(this.url.getPath());
            return m.matches();
        }
        return false;
    }
    @Override
    public HttpURLConnection handle(){
        Matcher m = capeRegex.matcher(this.url.getPath());
        if (m.matches()){
            String name = m.group(1);
            URL remoteURL = Utils.stringToURL("http://mc.krothium.com/capes/" + name + ".png");
            try {
                HttpURLConnection con = (HttpURLConnection)remoteURL.openConnection();
                return con;
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
