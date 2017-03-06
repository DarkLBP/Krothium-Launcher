package kml.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
class AdsConnection extends HttpURLConnection {
    private final HttpURLConnection con;
    public AdsConnection(URLConnection con) throws IOException {
        super(con.getURL());
        this.con = (HttpURLConnection)con;
    }

    @Override
    public void disconnect() {
        con.disconnect();
        connected = false;
    }

    @Override
    public boolean usingProxy() {
        return con.usingProxy();
    }

    @Override
    public void connect() throws IOException {
        con.connect();
        connected = true;
    }
    @Override
    public InputStream getInputStream() throws IOException {
        return con.getInputStream();
    }
    @Override
    public OutputStream getOutputStream() throws IOException {
        return con.getOutputStream();
    }
    @Override
    public void addRequestProperty(String key, String value) {
        con.addRequestProperty(key, value);
    }
    @Override
    public void setRequestProperty(String key, String value) {
        con.setRequestProperty(key, value);
    }
    @Override
    public String getHeaderField(String header) {
        return con.getHeaderField(header);
    }
    @Override
    public String getContentType(){
        return con.getContentType();
    }
    @Override
    public int getResponseCode() throws IOException {
        return con.getResponseCode();
    }
    @Override
    public void setDoInput(boolean doInput) {
        con.setDoInput(doInput);
    }
    @Override
    public void setDoOutput(boolean doOutput) {
        con.setDoOutput(doOutput);
    }
    @Override
    public String getRequestProperty(String key) {
        return con.getRequestProperty(key);
    }
}
