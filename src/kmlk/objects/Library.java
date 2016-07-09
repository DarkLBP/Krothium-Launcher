package kmlk.objects;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import kmlk.Console;
import kmlk.Kernel;
import kmlk.enums.LibraryRule;
import kmlk.enums.OS;
import kmlk.Utils;

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
    private final Console console;
    private final boolean legacy;
    
    public Library(String name, URL url, Map<OS, LibraryRule> rules){
        this.console = Kernel.getKernel().getConsole();
        this.name = name;
        this.url = url;
        this.sha1 = null;
        this.size = -1;
        this.path = new File("libraries" + File.separator + Utils.getArtifactPath(this.name, "jar"));
        this.legacy = true;
    }
    public Library(String name, URL url, String sha1, long size, Map<OS, LibraryRule> rules){
        this.console = Kernel.getKernel().getConsole();
        this.name = name;
        this.url = url;
        this.sha1 = sha1;
        this.size = size;
        this.path = new File("libraries" + File.separator + Utils.getArtifactPath(this.name, "jar"));
        this.legacy = false;
    }
    public File getPath(){return this.path;}
    public String getName(){return this.name;}
    public URL getURL(){return this.url;}
    public String getSHA1(){return this.sha1;}
    public long getSize(){return this.size;}
    public boolean isDownloadable(){return (this.url != null);}
    public boolean isLegacy(){return this.legacy;}
}
