package kml.matchers;

import kml.Utils;

import java.net.URL;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class BlockedServersMatcher implements URLMatcher {
    private final String blockURL = "https://sessionserver.mojang.com/blockedservers";
    private final URL blockedServersURL = Utils.stringToURL("https://mc.krothium.com/server/blockedservers");

    @Override
    public final boolean match(URL url) {
        return url.toString().equalsIgnoreCase(this.blockURL);
    }

    @Override
    public final URL handle(URL url) {
        if (url.toString().equalsIgnoreCase(this.blockURL)) {
            return this.blockedServersURL;
        }
        return null;
    }
}
