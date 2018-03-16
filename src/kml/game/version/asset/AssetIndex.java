package kml.game.version.asset;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class AssetIndex {
    private final String id;
    private String sha1;
    private long size, totalSize;
    private final String url;

    public AssetIndex(String id) {
        this.id = id == null ? "legacy" : id;
        this.url = "https://s3.amazonaws.com/Minecraft.Download/indexes/" + this.id + ".json";
    }

    public AssetIndex(String id, long size, long totalSize, String url, String sha1) {
        this.id = id;
        this.size = size;
        this.totalSize = totalSize;
        this.url = url;
        this.sha1 = sha1;
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

    public final String getURL() {
        return this.url;
    }

    public final String getSHA1() {
        return this.sha1;
    }
}
