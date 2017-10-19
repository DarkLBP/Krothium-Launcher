package kml.proxy.matchers;

import kml.Utils;

import java.net.URL;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class HasJoinedMatcher implements URLMatcher {
    private static final String HAS_URL = "https://sessionserver.mojang.com/session/minecraft/hasJoined";

    @Override
    public final boolean match(URL url) {
        return url.toString().contains(HAS_URL) && url.getQuery() != null;
    }

    @Override
    public final URL handle(URL url) {
        if (url.toString().contains(HAS_URL) && url.getQuery() != null) {
            return Utils.stringToURL("https://mc.krothium.com/server/hasJoined?" + url.getQuery());
        }
        return null;
    }
}
