package kml.matchers;

import kml.Constants;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class RefreshMatcher implements URLMatcher {
    private final String refreshURL = "https://authserver.mojang.com/refresh";

    @Override
    public boolean match(URL url) {
        return url.toString().equalsIgnoreCase(refreshURL);
    }

    @Override
    public URLConnection handle(URL url) {
        if (url.toString().equalsIgnoreCase(refreshURL)) {
            try {
                return Constants.REFRESH_URL.openConnection();
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
