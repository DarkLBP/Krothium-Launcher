package kml.handlers;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;


/**
 * @author DarkLBP
 * website https://krothium.com
 */
class AdsHandlerSecure extends sun.net.www.protocol.https.Handler{
    private final String userAgent;

    public AdsHandlerSecure(String userAgent) {
        this.userAgent = userAgent;
    }
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return openConnection(url, null);
    }

    @Override
    protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        if (!url.getHost().toLowerCase().contains("minecraft.net") && !url.getHost().toLowerCase().contains("mojang.com")) {
            System.out.println("Browser requested: " + url.toString());
            AdsConnection connection = new AdsConnection(super.openConnection(url, proxy));
            connection.setRequestProperty("Referer", "https://krothium.com");
            connection.setRequestProperty("User-Agent", this.userAgent);
            return connection;
        }
        return super.openConnection(url, proxy);
    }

}

