package kml.proxy.matchers;

import kml.Utils;

import java.net.URL;
/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class JoinServerMatcher implements URLMatcher {
    private final String JOIN_URL = "http://session.minecraft.net/game/joinserver.jsp";

    @Override
    public final boolean match(URL url) {
        return url.toString().contains(JOIN_URL) && url.getQuery() != null;
    }

    @Override
    public final URL handle(URL url) {
        if (url.toString().contains(JOIN_URL) && url.getQuery() != null) {
            return Utils.stringToURL("http://mc.krothium.com/server/joinserver?" + url.getQuery());
        }
        return null;
    }
}
