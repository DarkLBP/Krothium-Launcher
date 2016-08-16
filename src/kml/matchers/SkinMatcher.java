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
public class SkinMatcher implements URLMatcher{
    private String skinHost = "skins.minecraft.net";
    private Pattern skinRegex = Pattern.compile("/MinecraftSkins/(.+?)\\.png");
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
    public HttpURLConnection handle(){
        Matcher m = skinRegex.matcher(this.url.getPath());
        if (m.matches()){
            String name = m.group(1);
            URL remoteURL = Utils.stringToURL("http://mc.krothium.com/skins/" + name + ".png");
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
