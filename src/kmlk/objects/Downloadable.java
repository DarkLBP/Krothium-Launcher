package kmlk.objects;

import java.io.File;
import java.net.URL;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class Downloadable {
    private final long size;
    private final URL url;
    private final File path;
    private final String hash;
    public Downloadable(URL url, long size, File path, String hash){
        this.url = url;
        this.size = size;
        this.path = path;
        this.hash = hash;
    }
    public long getSize(){return this.size;}
    public URL getURL(){return this.url;}
    public File getPath(){return this.path;}
    public String getHash(){return this.hash;}
}
