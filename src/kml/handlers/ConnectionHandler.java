package kml.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import kml.matchers.URLMatcher;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class ConnectionHandler extends HttpURLConnection{
    
    private final URLMatcher matcher;
    private final HttpURLConnection relay;
    
    public ConnectionHandler(URL url, URLMatcher m){
        super(url);
        this.matcher = m;
        this.relay = this.matcher.handle();
        System.out.println("URL handled: " + super.url.toString() + " | " + (this.relay == null));
    }
    @Override
    public int getResponseCode(){
        try {
            return relay.getResponseCode();
        } catch (IOException ex) {
            return -1;
        }
    }
    @Override
    public String getContentType(){
        if (relay == null){
            return null;
        } else {
            return this.relay.getContentType();
        }
    }
    @Override
    public void connect() throws IOException {
        connected = true;
    }
    @Override
    public InputStream getInputStream(){
        try{
            return this.relay.getInputStream();
        } catch (IOException ex) { 
            return null;
        }
    }
    @Override
    public void disconnect() {
        connected = false;
    }
    @Override
    public boolean usingProxy() {
        return false;
    }
}
