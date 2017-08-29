package kml.handlers;

import kml.Constants;
import kml.matchers.URLMatcher;
import sun.net.www.protocol.https.Handler;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
class HttpsHandler extends Handler {

    @Override
    protected final URLConnection openConnection(URL url) throws IOException {
        return this.openConnection(url, null);
    }

    @Override
    protected final URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        System.out.println("URL requested: " + url);
        for (URLMatcher m : Constants.HTTPS_MATCHERS) {
            if (m.match(url)) {
                return new ConnectionHandler(url, m);
            }
        }
        return super.openConnection(url, proxy);
    }
}
