package kml.handlers;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * @author DarkLBP
 * website https://krothium.com
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
