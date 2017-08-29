package kml.matchers;

import kml.Constants;

import java.net.URL;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class BlockedServersMatcher implements URLMatcher {
    private static final String BLOCK_URL = "https://sessionserver.mojang.com/blockedservers";

    @Override
    public final boolean match(URL url) {
        return url.toString().equalsIgnoreCase(BLOCK_URL);
    }

    @Override
    public final URL handle(URL url) {
        if (url.toString().equalsIgnoreCase(BLOCK_URL)) {
            return Constants.BLOCKED_SERVERS;
        }
        return null;
    }
}
