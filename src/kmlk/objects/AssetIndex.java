package kmlk.objects;


import java.io.File;
import java.net.URL;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class AssetIndex {
    private final String id;
    private final long size;
    private final long totalSize;
    private final URL url;
    private final String sha1;
    private final File jsonPath;
    public AssetIndex(String id, long size, long totalSize, URL url, String sha1){
        this.id = id;
        this.size = size;
        this.totalSize = totalSize;
        this.url = url;
        this.sha1 = sha1;
        this.jsonPath = new File("assets" + File.separator + "indexes" + File.separator + id + ".json");
    }
    public String getID(){return this.id;}
    public long getSize(){return this.size;}
    public long getTotalSize(){return this.totalSize;}
    public URL getURL(){return this.url;}
    public String getSHA1(){return this.sha1;}
    public File getJSONFile(){return this.jsonPath;}
}
