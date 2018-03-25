package kml.proxy;

import kml.proxy.matchers.*;
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

    private final URLMatcher[] urlMatchers;

    public HttpsHandler() {
        urlMatchers = new URLMatcher[]{new ProfileMatcher(), new JoinMatcher(), new HasJoinedMatcher(), new BlockedServersMatcher(), new RealmsMatcher()};
    }

    @Override
    protected final URLConnection openConnection(URL url) throws IOException {
        return openConnection(url, null);
    }

    @Override
    protected final URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        System.out.println("URL requested: " + url);
        for (URLMatcher m : urlMatchers) {
            if (m.match(url.toString())) {
                return new ConnectionHandler(url, m);
            }
        }
        return super.openConnection(url, proxy);
    }
}
