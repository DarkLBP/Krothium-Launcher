package kml.handlers;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class URLHandler implements URLStreamHandlerFactory
{
	private final HttpsHandler HTTPS_HANDLER = new HttpsHandler();
	private final HttpHandler  HTTP_HANDLER  = new HttpHandler();

	@Override
	public URLStreamHandler createURLStreamHandler(String protocol)
	{
		if (protocol.equalsIgnoreCase("https")) {
			return HTTPS_HANDLER;
		}
		else if (protocol.equalsIgnoreCase("http")) {
			return HTTP_HANDLER;
		}
		return null;
	}

}
