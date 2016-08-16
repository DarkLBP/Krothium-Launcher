package kml.matchers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kml.Utils;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class TextureMatcher implements URLMatcher{
    private String textureHost = "textures.minecraft.net";
    private Pattern textureRegex = Pattern.compile("/texture/([0-9a-fA-F]+)");
    private final URL url;
    
    public TextureMatcher(URL url){
        this.url = url;
    }
    @Override
    public boolean match(){
        if (this.url.getHost().equalsIgnoreCase(textureHost)){
            Matcher m = textureRegex.matcher(this.url.getPath());
            return m.matches();
        }
        return false;
    }
    @Override
    public HttpURLConnection handle(){
        Matcher m = textureRegex.matcher(this.url.getPath());
        if (m.matches()){
            String textureID = m.group(1);
            URL remoteURL = Utils.stringToURL("http://mc.krothium.com/textures/" + textureID);
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
