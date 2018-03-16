package kml.proxy.matchers;
/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class JoinMatcher implements URLMatcher {
    private final String JOIN_URL = "https://sessionserver.mojang.com/session/minecraft/join";
    private final String JOINSERVER = "https://mc.krothium.com/server/join";

    @Override
    public final boolean match(String url) {
        return url.equalsIgnoreCase(JOIN_URL);
    }

    @Override
    public final String handle(String url) {
        if (url.equalsIgnoreCase(JOIN_URL)) {
            return this.JOINSERVER;
        }
        return null;
    }
}
