package kml.handlers;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

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
        return super.openConnection(url, proxy);
    }
}
