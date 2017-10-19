package kml.proxy.matchers;

import kml.Utils;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class ProfileMatcher implements URLMatcher {
    private final Pattern profileRegex = Pattern.compile("/session/minecraft/profile/([0-9a-fA-F]+?)");

    @Override
    public final boolean match(URL url) {
        String profileHost = "sessionserver.mojang.com";
        if (url.getHost().equalsIgnoreCase(profileHost)) {
            Matcher m = this.profileRegex.matcher(url.getPath());
            return m.matches();
        }
        return false;
    }

    @Override
    public final URL handle(URL url) {
        Matcher m = this.profileRegex.matcher(url.getPath());
        if (m.matches()) {
            String profileID = m.group(1);
            return Utils.stringToURL("https://mc.krothium.com/profiles/" + profileID + (url.getQuery() != null ? '?' + url.getQuery() : ""));
        }
        return null;
    }
}
