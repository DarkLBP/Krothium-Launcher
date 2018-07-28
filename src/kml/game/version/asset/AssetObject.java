package kml.game.version.asset;

import com.google.gson.annotations.Expose;

public class AssetObject {
    @Expose
    private String hash;
    @Expose
    private long size;

    public String getHash() {
        return hash;
    }

    public long getSize() {
        return size;
    }
}
