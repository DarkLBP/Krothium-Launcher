package kml.matchers;

import java.net.URLConnection;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public interface URLMatcher {
    public boolean match();
    public URLConnection handle();
}
