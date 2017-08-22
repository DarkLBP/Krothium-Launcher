package kml.matchers;

import kml.Utils;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class CapeMatcher implements URLMatcher {
    private final Pattern capeRegex = Pattern.compile("/MinecraftCloaks/(.+?)\\.png");

    @Override
    public boolean match(URL url) {
        final String capeHost = "skins.minecraft.net";
        final String capeHostLegacy = "s3.amazonaws.com";
        if (url.getHost().equalsIgnoreCase(capeHost) || url.getHost().equalsIgnoreCase(capeHostLegacy)) {
            Matcher m = capeRegex.matcher(url.getPath());
            return m.matches();
        }
        return false;
    }

    @Override
    public URL handle(URL url) {
        Matcher m = capeRegex.matcher(url.getPath());
        if (m.matches()) {
            String name = m.group(1);
            return Utils.stringToURL("http://mc.krothium.com/capes/" + name + ".png");
        }
        return null;
    }
}
