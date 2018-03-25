package kml.proxy;

import kml.proxy.matchers.URLMatcher;

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
            relay = (HttpURLConnection)new URL(m.handle(url.toString())).openConnection();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        System.out.println("URL handled: " + url);
    }

    @Override
    public final int getResponseCode() throws IOException {
        return relay.getResponseCode();
    }

    @Override
    public final String getContentType() {
        return relay.getContentType();
    }

    @Override
    public final void connect() throws IOException {
        relay.connect();
    }

    @Override
    public final InputStream getInputStream() throws IOException {
        return relay.getInputStream();
    }

    @Override
    public final String getHeaderField(String header) {
        return relay.getHeaderField(header);
    }

    @Override
    public final void setRequestProperty(String key, String value) {
        relay.setRequestProperty(key, value);
    }

    @Override
    public final String getRequestProperty(String key) {
        return relay.getRequestProperty(key);
    }

    @Override
    public final OutputStream getOutputStream() throws IOException {
        return relay.getOutputStream();
    }

    @Override
    public final void disconnect() {
        relay.disconnect();
    }

    @Override
    public final boolean usingProxy() {
        return relay.usingProxy();
    }

    @Override
    public final String getHeaderFieldKey(int n) {
        return relay.getHeaderFieldKey(n);
    }

    @Override
    public final void setFixedLengthStreamingMode(int contentLength) {
        relay.setFixedLengthStreamingMode(contentLength);
    }

    @Override
    public final void setFixedLengthStreamingMode(long contentLength) {
        relay.setFixedLengthStreamingMode(contentLength);
    }

    @Override
    public final void setChunkedStreamingMode(int chunklen) {
        relay.setChunkedStreamingMode(chunklen);
    }

    @Override
    public final String getHeaderField(int n) {
        return relay.getHeaderField(n);
    }

    @Override
    public final boolean getInstanceFollowRedirects() {
        return relay.getInstanceFollowRedirects();
    }

    @Override
    public final void setInstanceFollowRedirects(boolean followRedirects) {
        relay.setInstanceFollowRedirects(followRedirects);
    }

    @Override
    public final String getRequestMethod() {
        return relay.getRequestMethod();
    }

    @Override
    public final void setRequestMethod(String method) throws ProtocolException {
        relay.setRequestMethod(method);
    }

    @Override
    public final String getResponseMessage() throws IOException {
        return relay.getResponseMessage();
    }

    @Override
    public final long getHeaderFieldDate(String name, long Default) {
        return relay.getHeaderFieldDate(name, Default);
    }

    @Override
    public final Permission getPermission() throws IOException {
        return relay.getPermission();
    }

    @Override
    public final InputStream getErrorStream() {
        return relay.getErrorStream();
    }

    @Override
    public final int getConnectTimeout() {
        return relay.getConnectTimeout();
    }

    @Override
    public final void setConnectTimeout(int timeout) {
        relay.setConnectTimeout(timeout);
    }

    @Override
    public final int getReadTimeout() {
        return relay.getReadTimeout();
    }

    @Override
    public final void setReadTimeout(int timeout) {
        relay.setReadTimeout(timeout);
    }

    @Override
    public final URL getURL() {
        return relay.getURL();
    }

    @Override
    public final int getContentLength() {
        return relay.getContentLength();
    }

    @Override
    public final long getContentLengthLong() {
        return relay.getContentLengthLong();
    }

    @Override
    public final String getContentEncoding() {
        return relay.getContentEncoding();
    }

    @Override
    public final long getExpiration() {
        return relay.getExpiration();
    }

    @Override
    public final long getDate() {
        return relay.getDate();
    }

    @Override
    public final long getLastModified() {
        return relay.getLastModified();
    }

    @Override
    public final Map<String, List<String>> getHeaderFields() {
        return relay.getHeaderFields();
    }

    @Override
    public final int getHeaderFieldInt(String name, int Default) {
        return relay.getHeaderFieldInt(name, Default);
    }

    @Override
    public final long getHeaderFieldLong(String name, long Default) {
        return relay.getHeaderFieldLong(name, Default);
    }

    @Override
    public final Object getContent() throws IOException {
        return relay.getContent();
    }

    @Override
    public final Object getContent(Class[] classes) throws IOException {
        return relay.getContent(classes);
    }

    @Override
    public final String toString() {
        return relay.toString();
    }

    @Override
    public final boolean getDoInput() {
        return relay.getDoInput();
    }

    @Override
    public final void setDoInput(boolean doinput) {
        relay.setDoInput(doinput);
    }

    @Override
    public final boolean getDoOutput() {
        return relay.getDoOutput();
    }

    @Override
    public final void setDoOutput(boolean dooutput) {
        relay.setDoOutput(dooutput);
    }

    @Override
    public final boolean getAllowUserInteraction() {
        return relay.getAllowUserInteraction();
    }

    @Override
    public final void setAllowUserInteraction(boolean allowuserinteraction) {
        relay.setAllowUserInteraction(allowuserinteraction);
    }

    @Override
    public final boolean getUseCaches() {
        return relay.getUseCaches();
    }

    @Override
    public final void setUseCaches(boolean usecaches) {
        relay.setUseCaches(usecaches);
    }

    @Override
    public final long getIfModifiedSince() {
        return relay.getIfModifiedSince();
    }

    @Override
    public final void setIfModifiedSince(long ifmodifiedsince) {
        relay.setIfModifiedSince(ifmodifiedsince);
    }

    @Override
    public final boolean getDefaultUseCaches() {
        return relay.getDefaultUseCaches();
    }

    @Override
    public final void setDefaultUseCaches(boolean defaultusecaches) {
        relay.setDefaultUseCaches(defaultusecaches);
    }

    @Override
    public final void addRequestProperty(String key, String value) {
        relay.addRequestProperty(key, value);
    }

    @Override
    public final Map<String, List<String>> getRequestProperties() {
        return relay.getRequestProperties();
    }
}
