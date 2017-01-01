package kml.matchers;

import kml.Utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class TextureMatcher implements URLMatcher{
    private final Pattern textureRegex = Pattern.compile("/texture/([0-9a-fA-F]+)");
    private final URL url;
    
    public TextureMatcher(URL url){
        this.url = url;
    }
    @Override
    public boolean match(){
        final String textureHost = "textures.minecraft.net";
        if (this.url.getHost().equalsIgnoreCase(textureHost)){
            Matcher m = textureRegex.matcher(this.url.getPath());
            return m.matches();
        }
        return false;
    }
    @Override
    public URLConnection handle(){
        Matcher m = textureRegex.matcher(this.url.getPath());
        if (m.matches()){
            String textureID = m.group(1);
            URL remoteURL = Utils.stringToURL("http://mc.krothium.com/textures/" + textureID);
            try {
                return remoteURL != null ? remoteURL.openConnection() : null;
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
