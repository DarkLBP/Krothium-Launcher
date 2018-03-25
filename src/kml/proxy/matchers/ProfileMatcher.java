package kml.proxy.matchers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class ProfileMatcher implements URLMatcher {
    private final Pattern profileRegex = Pattern.compile("https://sessionserver.mojang.com/session/minecraft/profile/(\\w+).+");

    @Override
    public final boolean match(String url) {
        Matcher m = profileRegex.matcher(url);
        return m.matches();
    }

    @Override
    public final String handle(String url) {
        Matcher m = profileRegex.matcher(url);
        if (m.matches()) {
            String profileID = m.group(1);
            String[] segments = url.split("\\?");
            return "https://mc.krothium.com/profiles/" + profileID + (segments.length == 2 ? '?' + segments[1] : "");
        } else {
            System.out.println("NOPE");
        }
        return null;
    }
}
