package kml.handlers;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import kml.matchers.HasJoinedMatcher;
import kml.matchers.JoinMatcher;
import kml.matchers.MojangAPIMatcher;
import kml.matchers.ProfileMatcher;
import kml.matchers.URLMatcher;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class HttpsHandler extends sun.net.www.protocol.https.Handler{

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return openConnection(url, null);
    }
    
    @Override
    protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        System.out.println("URL requested: " + url.toString());
        URLMatcher[] matchers = new URLMatcher[]{new ProfileMatcher(url), new JoinMatcher(url), new MojangAPIMatcher(url), new HasJoinedMatcher(url)};
        for (URLMatcher m : matchers){
            if (m.match()){
                return new ConnectionHandler(url, m);
            }
        }
        return super.openConnection(url, proxy);
    }
}
