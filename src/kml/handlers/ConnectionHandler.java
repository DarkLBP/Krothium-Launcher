package kml.handlers;

import kml.Constants;
import kml.Utils;
import kml.matchers.URLMatcher;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.security.Permission;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
class ConnectionHandler extends HttpURLConnection {

    private HttpURLConnection relay;
    private ByteArrayInputStream cached;

    public ConnectionHandler(URL url, URLMatcher m) {
        super(url);
        try {
            relay = (HttpURLConnection)m.handle(url).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("URL handled: " + url + " | " + this.relay != null);
    }

    @Override
    public int getResponseCode() throws IOException {
        if (Constants.USE_LOCAL) {
            //Fake response code so cache can be pulled
            return 200;
        }
        return relay.getResponseCode();
    }

    @Override
    public String getContentType() {
        return relay.getContentType();
    }

    @Override
    public void connect() throws IOException {
        if (!Constants.USE_LOCAL) {
            //Connect only when offline
            relay.connect();
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (relay.getRequestMethod().equalsIgnoreCase("GET") && cached == null) {
            String etag = this.relay.getHeaderField("ETag");
            String hash = Utils.calculateChecksum(relay.getURL().toString(), "sha1");
            File cachedFile = new File(Constants.APPLICATION_CACHE, hash);
            if (!Constants.USE_LOCAL) {
                if (etag != null) {
                    if (!cachedFile.exists() || !cachedFile.isFile() || !Utils.verifyChecksum(cachedFile, etag.replace("\"", ""), "MD5")) {
                        System.out.println("Caching file for " + relay.getURL());
                        if (!Utils.downloadFile(this.relay, cachedFile)) {
                            return null;
                        }
                    }
                } else {
                    System.out.println("Caching file without ETAG for " + relay.getURL());
                    if (!Utils.downloadFile(this.relay, cachedFile)) {
                        return null;
                    }
                }
            }
            try {
                if (cachedFile.exists() && cachedFile.isFile()) {
                    cached = new ByteArrayInputStream(Files.readAllBytes(cachedFile.toPath()));
                } else if (Constants.USE_LOCAL) {
                    System.out.println("No cache available for " + relay.getURL());
                }
            } catch (IOException e) {
                System.out.println("Failed to load cached version for " + relay.getURL());
            }
        }
        if (cached != null) {
            System.out.println("Serving from cache " + this.getURL());
            return cached;
        }
        return relay.getInputStream();
    }

    @Override
    public String getHeaderField(String header) {
        return relay.getHeaderField(header);
    }

    @Override
    public void setRequestProperty(String key, String value) {
        relay.setRequestProperty(key, value);
    }

    @Override
    public String getRequestProperty(String key) {
        return relay.getRequestProperty(key);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return relay.getOutputStream();
    }

    @Override
    public void disconnect() {
        relay.disconnect();
    }

    @Override
    public boolean usingProxy() {
        return relay.usingProxy();
    }

    @Override
    public String getHeaderFieldKey(int n) {
        return relay.getHeaderFieldKey(n);
    }

    @Override
    public void setFixedLengthStreamingMode(int contentLength) {
        relay.setFixedLengthStreamingMode(contentLength);
    }

    @Override
    public void setFixedLengthStreamingMode(long contentLength) {
        relay.setFixedLengthStreamingMode(contentLength);
    }

    @Override
    public void setChunkedStreamingMode(int chunklen) {
        relay.setChunkedStreamingMode(chunklen);
    }

    @Override
    public String getHeaderField(int n) {
        return relay.getHeaderField(n);
    }

    @Override
    public boolean getInstanceFollowRedirects() {
        return relay.getInstanceFollowRedirects();
    }

    @Override
    public void setInstanceFollowRedirects(boolean followRedirects) {
        relay.setInstanceFollowRedirects(followRedirects);
    }

    @Override
    public String getRequestMethod() {
        return relay.getRequestMethod();
    }

    @Override
    public void setRequestMethod(String method) throws ProtocolException {
        relay.setRequestMethod(method);
    }

    @Override
    public String getResponseMessage() throws IOException {
        return relay.getResponseMessage();
    }

    @Override
    public long getHeaderFieldDate(String name, long Default) {
        return relay.getHeaderFieldDate(name, Default);
    }

    @Override
    public Permission getPermission() throws IOException {
        return relay.getPermission();
    }

    @Override
    public InputStream getErrorStream() {
        return relay.getErrorStream();
    }

    @Override
    public int getConnectTimeout() {
        return relay.getConnectTimeout();
    }

    @Override
    public void setConnectTimeout(int timeout) {
        relay.setConnectTimeout(timeout);
    }

    @Override
    public int getReadTimeout() {
        return relay.getReadTimeout();
    }

    @Override
    public void setReadTimeout(int timeout) {
        relay.setReadTimeout(timeout);
    }

    @Override
    public URL getURL() {
        return relay.getURL();
    }

    @Override
    public int getContentLength() {
        return relay.getContentLength();
    }

    @Override
    public long getContentLengthLong() {
        return relay.getContentLengthLong();
    }

    @Override
    public String getContentEncoding() {
        return relay.getContentEncoding();
    }

    @Override
    public long getExpiration() {
        return relay.getExpiration();
    }

    @Override
    public long getDate() {
        return relay.getDate();
    }

    @Override
    public long getLastModified() {
        return relay.getLastModified();
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return relay.getHeaderFields();
    }

    @Override
    public int getHeaderFieldInt(String name, int Default) {
        return relay.getHeaderFieldInt(name, Default);
    }

    @Override
    public long getHeaderFieldLong(String name, long Default) {
        return relay.getHeaderFieldLong(name, Default);
    }

    @Override
    public Object getContent() throws IOException {
        return relay.getContent();
    }

    @Override
    public Object getContent(Class[] classes) throws IOException {
        return relay.getContent(classes);
    }

    @Override
    public String toString() {
        return relay.toString();
    }

    @Override
    public boolean getDoInput() {
        return relay.getDoInput();
    }

    @Override
    public void setDoInput(boolean doinput) {
        relay.setDoInput(doinput);
    }

    @Override
    public boolean getDoOutput() {
        return relay.getDoOutput();
    }

    @Override
    public void setDoOutput(boolean dooutput) {
        relay.setDoOutput(dooutput);
    }

    @Override
    public boolean getAllowUserInteraction() {
        return relay.getAllowUserInteraction();
    }

    @Override
    public void setAllowUserInteraction(boolean allowuserinteraction) {
        relay.setAllowUserInteraction(allowuserinteraction);
    }

    @Override
    public boolean getUseCaches() {
        return relay.getUseCaches();
    }

    @Override
    public void setUseCaches(boolean usecaches) {
        relay.setUseCaches(usecaches);
    }

    @Override
    public long getIfModifiedSince() {
        return relay.getIfModifiedSince();
    }

    @Override
    public void setIfModifiedSince(long ifmodifiedsince) {
        relay.setIfModifiedSince(ifmodifiedsince);
    }

    @Override
    public boolean getDefaultUseCaches() {
        return relay.getDefaultUseCaches();
    }

    @Override
    public void setDefaultUseCaches(boolean defaultusecaches) {
        relay.setDefaultUseCaches(defaultusecaches);
    }

    @Override
    public void addRequestProperty(String key, String value) {
        relay.addRequestProperty(key, value);
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        return relay.getRequestProperties();
    }
}
