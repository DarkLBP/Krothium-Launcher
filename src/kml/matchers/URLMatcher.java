package kml.matchers;

import java.net.HttpURLConnection;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public interface URLMatcher {
    public boolean match();
    public HttpURLConnection handle();
}
