package kml.game.version.asset;

import com.google.gson.annotations.Expose;

import java.util.HashMap;

public class AssetIndex {
    @Expose
    private HashMap<String, AssetObject> objects;

    public HashMap<String, AssetObject> getObjects() {
        return objects;
    }
}
