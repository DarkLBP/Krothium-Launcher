package kml.matchers;

import kml.Constants;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class AuthenticateMatcher implements URLMatcher {
    private final String authenticateURL = "https://authserver.mojang.com/authenticate";

    @Override
    public boolean match(URL url) {
        return url.toString().equalsIgnoreCase(authenticateURL);
    }

    @Override
    public URLConnection handle(URL url) {
        if (url.toString().equalsIgnoreCase(authenticateURL)) {
            try {
                return Constants.AUTHENTICATE_URL.openConnection();
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
