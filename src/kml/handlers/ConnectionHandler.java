package kml.handlers;

import kml.matchers.URLMatcher;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class ConnectionHandler extends HttpURLConnection{
    
    private final URLMatcher matcher;
    private final URLConnection relay;
    
    public ConnectionHandler(URL url, URLMatcher m){
        super(url);
        this.matcher = m;
        this.relay = this.matcher.handle();
        System.out.println("URL handled: " + super.url.toString() + " | " + (this.relay != null));
    }
    @Override
    public int getResponseCode(){
        try {
            if (relay instanceof HttpsURLConnection){
                return ((HttpsURLConnection)relay).getResponseCode();
            } else if (relay instanceof HttpURLConnection){
                return ((HttpURLConnection)relay).getResponseCode();
            }
            return -1;
        } catch (IOException ex) {
            return -1;
        }
    }
    @Override
    public String getContentType(){
        return this.relay.getContentType();
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
            return new ByteArrayInputStream(new byte[0]);
        }
    }
    @Override
    public OutputStream getOutputStream(){
       try{
            this.relay.setDoOutput(true);
            return this.relay.getOutputStream();
        } catch (IOException ex) {
            return new ByteArrayOutputStream();
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
