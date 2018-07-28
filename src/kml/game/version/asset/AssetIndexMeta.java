package kml.game.version.asset;

import com.google.gson.annotations.Expose;

/**
 * @author DarkLBP
 * website https://krothium.com
 */

public class AssetIndexMeta {
    @Expose
    private String id;
    @Expose
    private String sha1;
    @Expose
    private long size;
    @Expose
    private String url;
    @Expose
    private long totalSize;

    public final String getId() {
        return id;
    }

    public final String getSha1() {
        return sha1;
    }

    public long getSize() {
        return size;
    }

    public final String getUrl() {
        return url;
    }

    public long getTotalSize() {
        return totalSize;
    }
}
