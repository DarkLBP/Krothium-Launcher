package kml.matchers;

import kml.Constants;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class ValidateMatcher implements URLMatcher {
    private final String validateURL = "https://authserver.mojang.com/validate";

    @Override
    public boolean match(URL url) {
        return url.toString().equalsIgnoreCase(validateURL);
    }

    @Override
    public URLConnection handle(URL url) {
        if (url.toString().equalsIgnoreCase(validateURL)) {
            try {
                return Constants.VALIDATE_URL.openConnection();
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }
}
