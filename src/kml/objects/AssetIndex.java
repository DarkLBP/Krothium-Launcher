package kml.objects;


import java.io.File;
import java.net.URL;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class AssetIndex {
    private final String id, sha1;
    private final long size, totalSize;
    private final URL url;
    private final File relFile;

    public AssetIndex(String id, long size, long totalSize, URL url, String sha1) {
        this.id = id;
        this.size = size;
        this.totalSize = totalSize;
        this.url = url;
        this.sha1 = sha1;
        this.relFile = new File("assets" + File.separator + "indexes" + File.separator + id + ".json");
    }

    public final boolean hasID() {
        return this.id != null;
    }

    public final boolean hasSize() {
        return this.size >= 0;
    }

    public final boolean hasTotalSize() {
        return this.totalSize >= 0;
    }

    public final boolean hasURL() {
        return this.url != null;
    }

    public final boolean hasSHA1() {
        return this.sha1 != null;
    }

    public final String getID() {
        return this.id;
    }

    public final long getSize() {
        return this.size;
    }

    public final long getTotalSize() {
        return this.totalSize;
    }

    public final URL getURL() {
        return this.url;
    }

    public final String getSHA1() {
        return this.sha1;
    }

    public final File getRelativeFile() {
        return this.relFile;
    }
}
