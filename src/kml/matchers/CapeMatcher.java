package kml.matchers;

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
public class CapeMatcher implements URLMatcher{
    private final Pattern capeRegex = Pattern.compile("/MinecraftCloaks/(.+?)\\.png");
    private final URL url;
    
    public CapeMatcher(URL url){
        this.url = url;
    }
    @Override
    public boolean match(){
        final String capeHost = "skins.minecraft.net";
        final String capeHostLegacy = "s3.amazonaws.com";
        if (this.url.getHost().equalsIgnoreCase(capeHost) || this.url.getHost().equalsIgnoreCase(capeHostLegacy)){
            Matcher m = capeRegex.matcher(this.url.getPath());
            return m.matches();
        }
        return false;
    }
    @Override
    public URLConnection handle(){
        Matcher m = capeRegex.matcher(this.url.getPath());
        if (m.matches()){
            String name = m.group(1);
            URL remoteURL = Utils.stringToURL("http://mc.krothium.com/capes/" + name + ".png");
            try {
                return remoteURL != null ? remoteURL.openConnection() : null;
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
