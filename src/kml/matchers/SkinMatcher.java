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
public class SkinMatcher implements URLMatcher{
    private final String skinHost = "skins.minecraft.net";
    private final Pattern skinRegex = Pattern.compile("/MinecraftSkins/(.+?)\\.png");
    private final URL url;
    
    public SkinMatcher(URL url){
        this.url = url;
    }
    @Override
    public boolean match(){
        if (this.url.getHost().equalsIgnoreCase(skinHost)){
            Matcher m = skinRegex.matcher(this.url.getPath());
            return m.matches();
        }
        return false;
    }
    @Override
    public URLConnection handle(){
        Matcher m = skinRegex.matcher(this.url.getPath());
        if (m.matches()){
            String name = m.group(1);
            URL remoteURL = Utils.stringToURL("http://mc.krothium.com/skins/" + name + ".png");
            try {
                return remoteURL.openConnection();
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
