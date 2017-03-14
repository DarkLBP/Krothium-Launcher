package kml.objects;

import java.io.File;
import java.net.URL;
import java.util.Objects;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class Downloadable
{
	private final long   size;
	private final URL    url;
	private final File   relPath;
	private final String hash;

	public Downloadable(URL url, long size, File path, String hash)
	{
		this.url = url;
		this.size = size;
		this.relPath = path;
		this.hash = hash;
	}

	public long getSize() {return this.size;}

	public boolean hasURL() {return Objects.nonNull(this.url);}

	public URL getURL() {return this.url;}

	public File getRelativePath() {return this.relPath;}

	public String getHash() {return this.hash;}

	public boolean hasHash() {return Objects.nonNull(this.hash);}

	public boolean hasSize() {return (this.size != -1);}
}
