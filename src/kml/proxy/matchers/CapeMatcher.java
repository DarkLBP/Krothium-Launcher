package kml.proxy.matchers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class CapeMatcher implements URLMatcher {
    private final Pattern capeRegex = Pattern.compile("http://skins.minecraft.net/MinecraftCloaks/(.+)\\.png");

    @Override
    public final boolean match(String url) {
        Matcher m = capeRegex.matcher(url);
        return m.matches();
    }

    @Override
    public final String handle(String url) {
        Matcher m = capeRegex.matcher(url);
        if (m.matches()) {
            String name = m.group(1);
            return "https://mc.krothium.com/capes/" + name + ".png";
        }
        return null;
    }
}
