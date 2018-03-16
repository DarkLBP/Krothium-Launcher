package kml.proxy.matchers;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class BlockedServersMatcher implements URLMatcher {
    private final String blockURL = "https://sessionserver.mojang.com/blockedservers";
    private final String blockedServersURL = "https://mc.krothium.com/server/blockedservers";

    @Override
    public final boolean match(String url) {
        return url.equalsIgnoreCase(this.blockURL);
    }

    @Override
    public final String handle(String url) {
        if (url.equalsIgnoreCase(this.blockURL)) {
            return this.blockedServersURL;
        }
        return null;
    }
}
