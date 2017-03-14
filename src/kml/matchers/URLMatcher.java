package kml.matchers;

import java.net.URL;
import java.net.URLConnection;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public interface URLMatcher
{
	boolean match(URL url);

	URLConnection handle(URL url);
}
