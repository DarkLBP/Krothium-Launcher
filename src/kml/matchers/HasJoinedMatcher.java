package kml.matchers;

import kml.Utils;

import java.net.URL;
import java.util.Objects;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class HasJoinedMatcher implements URLMatcher {
    private final String hasURL = "https://sessionserver.mojang.com/session/minecraft/hasJoined";

    @Override
    public boolean match(URL url) {
        return url.toString().contains(hasURL) && Objects.nonNull(url.getQuery());
    }

    @Override
    public URL handle(URL url) {
        if (url.toString().contains(hasURL) && Objects.nonNull(url.getQuery())) {
            return Utils.stringToURL("https://mc.krothium.com/server/hasJoined?" + url.getQuery());
        }
        return null;
    }
}
