package kml.proxy.matchers;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public interface URLMatcher {
    boolean match(String url);

    String handle(String url);
}
