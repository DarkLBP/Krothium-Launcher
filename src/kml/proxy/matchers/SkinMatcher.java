package kml.proxy.matchers;

import kml.Utils;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class SkinMatcher implements URLMatcher {
    private final Pattern skinRegex = Pattern.compile("/MinecraftSkins/(.+?)\\.png");

    @Override
    public final boolean match(URL url) {
        String skinHost = "skins.minecraft.net";
        if (url.getHost().equalsIgnoreCase(skinHost)) {
            Matcher m = this.skinRegex.matcher(url.getPath());
            return m.matches();
        }
        return false;
    }

    @Override
    public final URL handle(URL url) {
        Matcher m = this.skinRegex.matcher(url.getPath());
        if (m.matches()) {
            String name = m.group(1);
            return Utils.stringToURL("http://mc.krothium.com/skins/" + name + ".png");
        }
        return null;
    }
}
