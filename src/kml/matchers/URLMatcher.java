package kml.matchers;

import java.net.URLConnection;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public interface URLMatcher {
    boolean match();
    URLConnection handle();
}
