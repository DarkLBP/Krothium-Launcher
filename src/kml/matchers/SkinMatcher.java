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
public class SkinMatcher implements URLMatcher{
    private final Pattern skinRegex = Pattern.compile("/MinecraftSkins/(.+?)\\.png");

    @Override
    public boolean match(URL url){
        final String skinHost = "skins.minecraft.net";
        if (url.getHost().equalsIgnoreCase(skinHost)){
            Matcher m = skinRegex.matcher(url.getPath());
            return m.matches();
        }
        return false;
    }
    @Override
    public URLConnection handle(URL url){
        Matcher m = skinRegex.matcher(url.getPath());
        if (m.matches()){
            try {
                String name = m.group(1);
                return Utils.stringToURL("http://mc.krothium.com/skins/" + name + ".png").openConnection();
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }
}
