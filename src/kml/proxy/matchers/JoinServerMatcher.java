package kml.proxy.matchers;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class JoinServerMatcher implements URLMatcher {
    private final String JOIN_URL = "http://session.minecraft.net/game/joinserver.jsp";

    @Override
    public final boolean match(String url) {
        return url.contains(JOIN_URL) && url.split("\\?").length == 2;
    }

    @Override
    public final String handle(String url) {
        String[] segments = url.split("\\?");
        if (url.contains(JOIN_URL) && segments.length == 2) {
            return "https://mc.krothium.com/server/joinserver?" + segments[1];
        }
        return null;
    }
}
