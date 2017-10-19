package kml.proxy;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class URLHandler implements URLStreamHandlerFactory {
    private final HttpsHandler HTTPS_HANDLER = new HttpsHandler();
    private final HttpHandler HTTP_HANDLER = new HttpHandler();

    @Override
    public final URLStreamHandler createURLStreamHandler(String protocol) {
        if ("https".equalsIgnoreCase(protocol)) {
            return this.HTTPS_HANDLER;
        }
        if ("http".equalsIgnoreCase(protocol)) {
            return this.HTTP_HANDLER;
        }
        return null;
    }

}
