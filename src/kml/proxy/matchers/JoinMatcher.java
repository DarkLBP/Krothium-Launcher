package kml.proxy.matchers;

import kml.Utils;

import java.net.URL;
/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class JoinMatcher implements URLMatcher {
    private final String JOIN_URL = "https://sessionserver.mojang.com/session/minecraft/join";
    private final URL JOINSERVER = Utils.stringToURL("https://mc.krothium.com/server/join");

    @Override
    public final boolean match(URL url) {
        return url.toString().equalsIgnoreCase(JOIN_URL);
    }

    @Override
    public final URL handle(URL url) {
        if (url.toString().equalsIgnoreCase(JOIN_URL)) {
            return this.JOINSERVER;
        }
        return null;
    }
}
