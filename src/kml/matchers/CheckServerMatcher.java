package kml.matchers;

import kml.Utils;

import java.net.URL;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class CheckServerMatcher implements URLMatcher {
    private final String checkURL = "http://session.minecraft.net/game/checkserver.jsp";

    @Override
    public boolean match(URL url) {
        return url.toString().contains(checkURL) && url.getQuery() != null;
    }

    @Override
    public URL handle(URL url) {
        if (url.toString().contains(checkURL) && url.getQuery() != null) {
            Utils.stringToURL("http://mc.krothium.com/server/checkserver?" + url.getQuery());
        }
        return null;
    }
}
