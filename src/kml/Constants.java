package kml;

import com.google.gson.Gson;
import kml.utils.Utils;

import java.net.URL;

public class Constants {
    // Gson related instances
    public static final Gson GSON = new Gson();

    // URL related instances
    public static URL VERSION_MANIFEST = Utils.makeUrl("https://launchermeta.mojang.com/mc/game/version_manifest.json");


}
