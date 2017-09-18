package kml.handlers;

import kml.matchers.*;
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
        this.urlMatchers = new URLMatcher[]{new ProfileMatcher(), new JoinMatcher(), new HasJoinedMatcher(), new BlockedServersMatcher(), new RealmsMatcher()};
    }

    @Override
    protected final URLConnection openConnection(URL url) throws IOException {
        return this.openConnection(url, null);
    }

    @Override
    protected final URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        System.out.println("URL requested: " + url);
        for (URLMatcher m : this.urlMatchers) {
            if (m.match(url)) {
                return new ConnectionHandler(url, m);
            }
        }
        return super.openConnection(url, proxy);
    }
}
