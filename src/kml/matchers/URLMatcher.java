package kml.matchers;

import java.net.URLConnection;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public interface URLMatcher {
    boolean match();
    URLConnection handle();
}
