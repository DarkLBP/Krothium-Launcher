package kml.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;


/**
 * @author DarkLBP
 * website https://krothium.com
 */
class AdsHandler extends sun.net.www.protocol.http.Handler{
    private final String userAgent;

    public AdsHandler(String userAgent) {
        this.userAgent = userAgent;
    }
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return openConnection(url, null);
    }
    
    @Override
    protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        System.out.println("Browser requested: " + url.toString());
        AdsConnection connection = new AdsConnection(super.openConnection(url, proxy));
        connection.setRequestProperty("Referer", "https://krothium.com");
        connection.setRequestProperty("User-Agent", this.userAgent);
        return connection;
    }

}
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
    public String getRequestProperty(String key) {
        return con.getRequestProperty(key);
    }
}
