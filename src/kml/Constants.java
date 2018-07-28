package kml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kml.console.Console;
import kml.utils.Utils;

import java.io.File;
import java.net.URL;

public class Constants {

    public static final File APP_WORKING_DIR = Utils.getWorkingDirectory();
    public static final File APP_PROFILES = new File(APP_WORKING_DIR, "launcher_profiles.json");
    public static final File APP_LOGS = new File(APP_WORKING_DIR, "logs");
    public static final File APP_VERSIONS = new File(Constants.APP_WORKING_DIR, "versions");

    static {
        boolean ready = true;
        if (!APP_WORKING_DIR.isDirectory()) {
            ready = ready && APP_WORKING_DIR.mkdirs();
        }
        if (!APP_LOGS.isDirectory()) {
            ready = ready && APP_LOGS.mkdir();
        }
        if (!ready) {
            throw new RuntimeException("Failed to create root directories.");
        }
    }

    public static final Console CONSOLE = new Console();
    public static final String APP_VERSION = "3.3.0";
    public static final int APP_FORMAT = 21;
    public static final int APP_PROFILES_FORMAT = 2;
    public static final int APP_KEEP_LOGS = 10;

    public static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static final URL VERSION_MANIFEST = Utils.makeUrl("https://launchermeta.mojang.com/mc/game/version_manifest.json");


}
