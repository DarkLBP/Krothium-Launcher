package kml.handlers;

import kml.matchers.URLMatcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.Permission;
import java.util.List;
import java.util.Map;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
class ConnectionHandler extends HttpURLConnection {

    private HttpURLConnection relay;

    public ConnectionHandler(URL url, URLMatcher m) {
        super(url);
        try {
            this.relay = (HttpURLConnection)m.handle(url).openConnection();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        System.out.println("URL handled: " + url);
    }

    @Override
    public final int getResponseCode() throws IOException {
        return this.relay.getResponseCode();
    }

    @Override
    public final String getContentType() {
        return this.relay.getContentType();
    }

    @Override
    public final void connect() throws IOException {
        this.relay.connect();
    }

    @Override
    public final InputStream getInputStream() throws IOException {
        return this.relay.getInputStream();
    }

    @Override
    public final String getHeaderField(String header) {
        return this.relay.getHeaderField(header);
    }

    @Override
    public final void setRequestProperty(String key, String value) {
        this.relay.setRequestProperty(key, value);
    }

    @Override
    public final String getRequestProperty(String key) {
        return this.relay.getRequestProperty(key);
    }

    @Override
    public final OutputStream getOutputStream() throws IOException {
        return this.relay.getOutputStream();
    }

    @Override
    public final void disconnect() {
        this.relay.disconnect();
    }

    @Override
    public final boolean usingProxy() {
        return this.relay.usingProxy();
    }

    @Override
    public final String getHeaderFieldKey(int n) {
        return this.relay.getHeaderFieldKey(n);
    }

    @Override
    public final void setFixedLengthStreamingMode(int contentLength) {
        this.relay.setFixedLengthStreamingMode(contentLength);
    }

    @Override
    public final void setFixedLengthStreamingMode(long contentLength) {
        this.relay.setFixedLengthStreamingMode(contentLength);
    }

    @Override
    public final void setChunkedStreamingMode(int chunklen) {
        this.relay.setChunkedStreamingMode(chunklen);
    }

    @Override
    public final String getHeaderField(int n) {
        return this.relay.getHeaderField(n);
    }

    @Override
    public final boolean getInstanceFollowRedirects() {
        return this.relay.getInstanceFollowRedirects();
    }

    @Override
    public final void setInstanceFollowRedirects(boolean followRedirects) {
        this.relay.setInstanceFollowRedirects(followRedirects);
    }

    @Override
    public final String getRequestMethod() {
        return this.relay.getRequestMethod();
    }

    @Override
    public final void setRequestMethod(String method) throws ProtocolException {
        this.relay.setRequestMethod(method);
    }

    @Override
    public final String getResponseMessage() throws IOException {
        return this.relay.getResponseMessage();
    }

    @Override
    public final long getHeaderFieldDate(String name, long Default) {
        return this.relay.getHeaderFieldDate(name, Default);
    }

    @Override
    public final Permission getPermission() throws IOException {
        return this.relay.getPermission();
    }

    @Override
    public final InputStream getErrorStream() {
        return this.relay.getErrorStream();
    }

    @Override
    public final int getConnectTimeout() {
        return this.relay.getConnectTimeout();
    }

    @Override
    public final void setConnectTimeout(int timeout) {
        this.relay.setConnectTimeout(timeout);
    }

    @Override
    public final int getReadTimeout() {
        return this.relay.getReadTimeout();
    }

    @Override
    public final void setReadTimeout(int timeout) {
        this.relay.setReadTimeout(timeout);
    }

    @Override
    public final URL getURL() {
        return this.relay.getURL();
    }

    @Override
    public final int getContentLength() {
        return this.relay.getContentLength();
    }

    @Override
    public final long getContentLengthLong() {
        return this.relay.getContentLengthLong();
    }

    @Override
    public final String getContentEncoding() {
        return this.relay.getContentEncoding();
    }

    @Override
    public final long getExpiration() {
        return this.relay.getExpiration();
    }

    @Override
    public final long getDate() {
        return this.relay.getDate();
    }

    @Override
    public final long getLastModified() {
        return this.relay.getLastModified();
    }

    @Override
    public final Map<String, List<String>> getHeaderFields() {
        return this.relay.getHeaderFields();
    }

    @Override
    public final int getHeaderFieldInt(String name, int Default) {
        return this.relay.getHeaderFieldInt(name, Default);
    }

    @Override
    public final long getHeaderFieldLong(String name, long Default) {
        return this.relay.getHeaderFieldLong(name, Default);
    }

    @Override
    public final Object getContent() throws IOException {
        return this.relay.getContent();
    }

    @Override
    public final Object getContent(Class[] classes) throws IOException {
        return this.relay.getContent(classes);
    }

    @Override
    public final String toString() {
        return this.relay.toString();
    }

    @Override
    public final boolean getDoInput() {
        return this.relay.getDoInput();
    }

    @Override
    public final void setDoInput(boolean doinput) {
        this.relay.setDoInput(doinput);
    }

    @Override
    public final boolean getDoOutput() {
        return this.relay.getDoOutput();
    }

    @Override
    public final void setDoOutput(boolean dooutput) {
        this.relay.setDoOutput(dooutput);
    }

    @Override
    public final boolean getAllowUserInteraction() {
        return this.relay.getAllowUserInteraction();
    }

    @Override
    public final void setAllowUserInteraction(boolean allowuserinteraction) {
        this.relay.setAllowUserInteraction(allowuserinteraction);
    }

    @Override
    public final boolean getUseCaches() {
        return this.relay.getUseCaches();
    }

    @Override
    public final void setUseCaches(boolean usecaches) {
        this.relay.setUseCaches(usecaches);
    }

    @Override
    public final long getIfModifiedSince() {
        return this.relay.getIfModifiedSince();
    }

    @Override
    public final void setIfModifiedSince(long ifmodifiedsince) {
        this.relay.setIfModifiedSince(ifmodifiedsince);
    }

    @Override
    public final boolean getDefaultUseCaches() {
        return this.relay.getDefaultUseCaches();
    }

    @Override
    public final void setDefaultUseCaches(boolean defaultusecaches) {
        this.relay.setDefaultUseCaches(defaultusecaches);
    }

    @Override
    public final void addRequestProperty(String key, String value) {
        this.relay.addRequestProperty(key, value);
    }

    @Override
    public final Map<String, List<String>> getRequestProperties() {
        return this.relay.getRequestProperties();
    }
}
