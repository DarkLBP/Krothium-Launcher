package kmlk;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public final class Library {
    private final String name;
    private final String sha1;
    private final long size;
    private final URL url;
    private final Map<OS, String> natives = new HashMap();
    private final File path;
    private final Map<OS, LibraryRule> rules;
    private final Kernel kernel;
    private final Console console;
    
    public Library(Kernel k, String name, URL url, String sha1, long size, Map<OS, LibraryRule> rules)
    {
        this.kernel = k;
        this.console = k.getConsole();
        this.name = name;
        this.url = url;
        this.sha1 = sha1;
        this.size = size;
        this.path = new File("libraries" + File.separator + Utils.getArtifactFile(this.name, "jar"));
        this.rules = rules;
    }
    public File getPath()
    {
        return this.path;
    }
    public String getName()
    {
        return this.name;
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
    public LibraryRule getRuleByOS(OS os)
    {
        if (this.rules.containsKey(os))
        {
            return this.rules.get(os);
        }
        else
        {
            return LibraryRule.ALLOW;
        }
    }
    public boolean hasRules()
    {
        return (this.rules.size() > 0);
    }
    public boolean isDownloadable()
    {
        return (this.url != null);
    }
}