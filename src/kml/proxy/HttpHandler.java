package kml.proxy;

import kml.proxy.matchers.CapeMatcher;
import kml.proxy.matchers.JoinServerMatcher;
import kml.proxy.matchers.SkinMatcher;
import kml.proxy.matchers.URLMatcher;
import sun.net.www.protocol.http.Handler;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
class HttpHandler extends Handler{

    private final URLMatcher[] urlMatchers;

    public HttpHandler() {
        this.urlMatchers = new URLMatcher[]{new SkinMatcher(), new CapeMatcher(), new JoinServerMatcher()};
    }

    @Override
    protected final URLConnection openConnection(URL url) throws IOException {
        return this.openConnection(url, null);
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
