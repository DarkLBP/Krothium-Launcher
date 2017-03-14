package kml.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.List;
import java.util.Map;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
class AdsConnection extends HttpURLConnection
{
	private final HttpURLConnection con;

	public AdsConnection(URLConnection con) throws IOException
	{
		super(con.getURL());
		this.con = (HttpURLConnection) con;

	}

	@Override
	public void disconnect()
	{
		con.disconnect();
	}

	@Override
	public boolean usingProxy()
	{
		return con.usingProxy();
	}

	@Override
	public void connect() throws IOException
	{
		con.setRequestProperty("User-Agent", BrowserHandler.USER_AGENT);
		con.connect();
	}

	@Override
	public String getHeaderField(int n)
	{
		return con.getHeaderField(n);
	}

	@Override
	public String getHeaderFieldKey(int n)
	{
		return con.getHeaderFieldKey(n);
	}

	@Override
	public boolean getInstanceFollowRedirects()
	{
		return con.getInstanceFollowRedirects();
	}

	@Override
	public void setInstanceFollowRedirects(boolean followRedirects)
	{
		this.con.setInstanceFollowRedirects(followRedirects);
	}

	@Override
	public String getRequestMethod()
	{
		return con.getRequestMethod();
	}

	@Override
	public void setRequestMethod(String method) throws ProtocolException
	{
		con.setRequestMethod(method);
	}

	@Override
	public long getHeaderFieldDate(String name, long Default)
	{
		return con.getHeaderFieldDate(name, Default);
	}

	@Override
	public String getResponseMessage() throws IOException
	{
		return con.getResponseMessage();
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return con.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException
	{
		return con.getOutputStream();
	}

	@Override
	public void addRequestProperty(String key, String value)
	{
		con.addRequestProperty(key, value);
	}

	@Override
	public void setRequestProperty(String key, String value)
	{
		con.setRequestProperty(key, value);
	}

	@Override
	public String getHeaderField(String header)
	{
		return con.getHeaderField(header);
	}

	@Override
	public String getContentType()
	{
		return con.getContentType();
	}

	@Override
	public int getResponseCode() throws IOException
	{
		return con.getResponseCode();
	}

	@Override
	public String getRequestProperty(String key)
	{
		return con.getRequestProperty(key);
	}

	@Override
	public Permission getPermission() throws IOException
	{
		return con.getPermission();
	}

	@Override
	public InputStream getErrorStream()
	{
		return con.getErrorStream();
	}

	@Override
	public void setFixedLengthStreamingMode(int contentLength)
	{
		con.setFixedLengthStreamingMode(contentLength);
	}

	@Override
	public void setFixedLengthStreamingMode(long contentLength)
	{
		con.setFixedLengthStreamingMode(contentLength);
	}

	@Override
	public void setChunkedStreamingMode(int chunklen)
	{
		con.setChunkedStreamingMode(chunklen);
	}

	@Override
	public Map<String, List<String>> getHeaderFields()
	{
		return con.getHeaderFields();
	}

	@Override
	public int getConnectTimeout()
	{
		return con.getConnectTimeout();
	}

	@Override
	public void setConnectTimeout(int timeout)
	{
		con.setConnectTimeout(timeout);
	}

	@Override
	public int getReadTimeout()
	{
		return con.getReadTimeout();
	}

	@Override
	public void setReadTimeout(int timeout)
	{
		con.setReadTimeout(timeout);
	}

	@Override
	public URL getURL()
	{
		return con.getURL();
	}

	@Override
	public int getContentLength()
	{
		return con.getContentLength();
	}

	@Override
	public long getContentLengthLong()
	{
		return con.getContentLengthLong();
	}

	@Override
	public String getContentEncoding()
	{
		return con.getContentEncoding();
	}

	@Override
	public long getExpiration()
	{
		return con.getExpiration();
	}

	@Override
	public long getDate()
	{
		return con.getDate();
	}

	@Override
	public long getLastModified()
	{
		return con.getLastModified();
	}

	@Override
	public int getHeaderFieldInt(String name, int Default)
	{
		return con.getHeaderFieldInt(name, Default);
	}

	@Override
	public long getHeaderFieldLong(String name, long Default)
	{
		return con.getHeaderFieldLong(name, Default);
	}

	@Override
	public Object getContent() throws IOException
	{
		return con.getContent();
	}

	@Override
	public Object getContent(Class[] classes) throws IOException
	{
		return con.getContent(classes);
	}

	@Override
	public String toString()
	{
		return con.toString();
	}

	@Override
	public boolean getDoInput()
	{
		return con.getDoInput();
	}

	@Override
	public void setDoInput(boolean doInput)
	{
		con.setDoInput(doInput);
	}

	@Override
	public boolean getDoOutput()
	{
		return con.getDoOutput();
	}

	@Override
	public void setDoOutput(boolean doOutput)
	{
		con.setDoOutput(doOutput);
	}

	@Override
	public boolean getAllowUserInteraction()
	{
		return con.getAllowUserInteraction();
	}

	@Override
	public void setAllowUserInteraction(boolean allowuserinteraction)
	{
		con.setAllowUserInteraction(allowuserinteraction);
	}

	@Override
	public boolean getUseCaches()
	{
		return con.getUseCaches();
	}

	@Override
	public void setUseCaches(boolean usecaches)
	{
		con.setUseCaches(usecaches);
	}

	@Override
	public long getIfModifiedSince()
	{
		return con.getIfModifiedSince();
	}

	@Override
	public void setIfModifiedSince(long ifmodifiedsince)
	{
		con.setIfModifiedSince(ifmodifiedsince);
	}

	@Override
	public boolean getDefaultUseCaches()
	{
		return con.getDefaultUseCaches();
	}

	@Override
	public void setDefaultUseCaches(boolean defaultusecaches)
	{
		con.setDefaultUseCaches(defaultusecaches);
	}

	@Override
	public Map<String, List<String>> getRequestProperties()
	{
		return con.getRequestProperties();
	}
}
