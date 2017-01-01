package kml.objects;


import java.io.File;
import java.net.URL;

/**
 * @author DarkLBP
 * website https://krothium.com
*/

public class AssetIndex {
    private final String id;
    private final long size;
    private final long totalSize;
    private final URL url;
    private final String sha1;
    private final File relFile;
    public AssetIndex(String id, long size, long totalSize, URL url, String sha1){
        this.id = id;
        this.size = size;
        this.totalSize = totalSize;
        this.url = url;
        this.sha1 = sha1;
        this.relFile = new File("assets" + File.separator + "indexes" + File.separator + id + ".json");
    }
    public boolean hasID(){return (this.id != null);}
    public boolean hasSize(){return (this.size >= 0);}
    public boolean hasTotalSize(){return (this.totalSize >= 0);}
    public boolean hasURL(){return (this.url != null);}
    public boolean hasSHA1(){return (this.sha1 != null);}
    public String getID(){return this.id;}
    public long getSize(){return this.size;}
    public long getTotalSize(){return this.totalSize;}
    public URL getURL(){return this.url;}
    public String getSHA1(){return this.sha1;}
    public File getRelativeFile(){return this.relFile;}
}
