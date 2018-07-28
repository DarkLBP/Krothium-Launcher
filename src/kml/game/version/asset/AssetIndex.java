package kml.game.version.asset;

/**
 * @author DarkLBP
 * website https://krothium.com
 */

public class AssetIndex {
    private String id;
    private String sha1;
    private long size;
    private String url;
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
