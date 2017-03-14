package kml.matchers;

import kml.Constants;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class BlockedServersMatcher implements URLMatcher
{
	private final String blockURL = "https://sessionserver.mojang.com/blockedservers";

	@Override
	public boolean match(URL url)
	{
		return url.toString().equalsIgnoreCase(blockURL);
	}

	@Override
	public URLConnection handle(URL url)
	{
		if (url.toString().equalsIgnoreCase(blockURL)) {
			try {
				return Constants.BLOCKED_SERVERS.openConnection();
			}
			catch (IOException ex) {
				return null;
			}
		}
		return null;
	}
}
