package kml.handlers;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;


/**
 * @author DarkLBP
 *         website https://krothium.com
 */
class AdsHandlerSecure extends sun.net.www.protocol.https.Handler {

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return openConnection(url, null);
    }

    @Override
    protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        if (url.getHost().toLowerCase().contains("sh.st") || url.getHost().toLowerCase().contains("adf.ly") || url.getHost().toLowerCase().contains("go.krothium.com")) {
            System.out.println("Browser requested: " + url.toString());
            return new AdsConnection(super.openConnection(url, proxy));
        }
        return super.openConnection(url, proxy);
    }

}

