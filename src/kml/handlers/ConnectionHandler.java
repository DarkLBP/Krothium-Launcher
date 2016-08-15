package kml.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import kml.Utils;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class ConnectionHandler extends URLConnection{
    
    private ByteArrayInputStream inputStream;
    private ByteArrayOutputStream outputStream;
    
    public ConnectionHandler(URL url){
        super(url);
        this.outputStream = new ByteArrayOutputStream();
    }

    @Override
    public void connect() throws IOException {
        //
    }
    
    @Override
    public InputStream getInputStream(){
        System.out.println("URL requested: " + super.url.toString());
        String s = Utils.readURL(super.url);
        this.inputStream = new ByteArrayInputStream(s.getBytes());
        return this.inputStream;
    }
}
