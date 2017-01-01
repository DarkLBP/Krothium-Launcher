package kml.handlers;

import kml.matchers.*;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
class HttpHandler extends sun.net.www.protocol.http.Handler{

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return openConnection(url, null);
    }
    
    @Override
    protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        System.out.println("URL requested: " + url.toString());
        URLMatcher[] matchers = new URLMatcher[]{new TextureMatcher(url), new SkinMatcher(url), new CapeMatcher(url), new JoinServerMatcher(url), new CheckServerMatcher(url)};
        for (URLMatcher m : matchers){
            if (m.match()){
                return new ConnectionHandler(url, m);
            }
        }
        return super.openConnection(url, proxy);
    }
}
