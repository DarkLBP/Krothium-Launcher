package kml.matchers;

import kml.Utils;

import java.net.URL;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class HasJoinedMatcher implements URLMatcher {
    private final String hasURL = "https://sessionserver.mojang.com/session/minecraft/hasJoined";

    @Override
    public boolean match(URL url) {
        return url.toString().contains(hasURL) && url.getQuery() != null;
    }

    @Override
    public URL handle(URL url) {
        if (url.toString().contains(hasURL) && url.getQuery() != null) {
            return Utils.stringToURL("https://mc.krothium.com/server/hasJoined?" + url.getQuery());
        }
        return null;
    }
}
