package kml.proxy.matchers;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class HasJoinedMatcher implements URLMatcher {
    private static final String HAS_URL = "https://sessionserver.mojang.com/session/minecraft/hasJoined";

    @Override
    public final boolean match(String url) {
        return url.contains(HAS_URL) && url.split("\\?").length == 2;
    }

    @Override
    public final String handle(String url) {
        String[] segments = url.split("\\?");
        if (url.contains(HAS_URL) && segments.length == 2) {
            return "https://mc.krothium.com/server/hasJoined?" + segments[1];
        }
        return null;
    }
}
