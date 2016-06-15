package kmlk;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class Native {
    private final String name;
    private final URL url;
    private final String sha1;
    private final long size;
    private final List<String> exclude;
    private final File path;
    public Native(String name, URL url, String sha1, long size, List<String> exclude, String classifier)
    {
        this.name = name;
        this.url = url;
        this.sha1 = sha1;
        this.size = size;
        this.exclude = exclude;
        String name_tmp = Utils.getArtifactPath(this.name, "jar");
        this.path = new File("libraries" + File.separator + name_tmp.replace(".jar", "-" + classifier + ".jar"));
    }
    public String getName()
    {
        return this.name;
    }
    public List<String> getExclusions()
    {
        return this.exclude;
    }
    public URL getURL()
    {
        return this.url;
    }
    public String getSHA1()
    {
        return this.sha1;
    }
    public long getSize()
    {
        return this.size;
    }
    public File getPath()
    {
        return this.path;
    }
    public boolean isDownloadable()
    {
        return (this.url != null);
    }
}
