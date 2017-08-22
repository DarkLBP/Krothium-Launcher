package kml.matchers;

import kml.Constants;

import java.net.URL;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class BlockedServersMatcher implements URLMatcher {
    private final String blockURL = "https://sessionserver.mojang.com/blockedservers";

    @Override
    public boolean match(URL url) {
        return url.toString().equalsIgnoreCase(blockURL);
    }

    @Override
    public URL handle(URL url) {
        if (url.toString().equalsIgnoreCase(blockURL)) {
            return Constants.BLOCKED_SERVERS;
        }
        return null;
    }
}
