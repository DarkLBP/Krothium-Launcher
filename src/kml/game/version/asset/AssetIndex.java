package kml.game.version.asset;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class AssetIndex {
    private final String id;
    private String sha1;
    private final String url;

    public AssetIndex(String id) {
        this.id = id == null ? "legacy" : id;
        url = "https://s3.amazonaws.com/Minecraft.Download/indexes/" + this.id + ".json";
    }

    public AssetIndex(String id, String url, String sha1) {
        this.id = id;
        this.url = url;
        this.sha1 = sha1;
    }

    public final String getID() {
        return id;
    }

    public final String getURL() {
        return url;
    }

    public final String getSHA1() {
        return sha1;
    }
}
