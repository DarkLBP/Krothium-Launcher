package kml.proxy.matchers;

import java.net.URL;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public interface URLMatcher {
    boolean match(URL url);

    URL handle(URL url);
}
