package kml.handlers;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class URLHandler implements URLStreamHandlerFactory{

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equalsIgnoreCase("https")){
            return new HttpsHandler();
        } else if (protocol.equalsIgnoreCase("http")){
            return new HttpHandler();
        }
        return null;
    }

}
