package kml;

import java.net.URL;

/**
 * @author DarkLBP
 * website https://krothium.com
 */

public class Constants {
    public static final int KERNEL_BUILD = 24;
    public static final String KERNEL_BUILD_NAME = "2.0.0";
    public static final URL AUTHENTICATE_URL = Utils.stringToURL("https://mc.krothium.com/authenticate");
    public static final URL REFRESH_URL = Utils.stringToURL("https://mc.krothium.com/refresh");
    public static final URL VALIDATE_URL = Utils.stringToURL("https://mc.krothium.com/validate");
    public static final URL CHANGESKIN_URL = Utils.stringToURL("https://mc.krothium.com/changeskin");
    public static final URL CHANGECAPE_URL = Utils.stringToURL("https://mc.krothium.com/changecape");
    public static final URL GETLATEST_URL = Utils.stringToURL("https://mc.krothium.com/latestversion");
    public static final URL VERSION_MANIFEST_FILE = Utils.stringToURL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
    public static final String RESOURCES_URL = "http://resources.download.minecraft.net/";
    public static final URL HANDSHAKE_URL = Utils.stringToURL("https://mc.krothium.com/hello");
    public static final URL PROFILE_ICONS = Constants.class.getResource("/kml/gui/textures/profile-icons.png");
    public static final int DOWNLOAD_TRIES = 5;
    public static final int KEEP_OLD_LOGS = 2;
    public static boolean USE_HTTPS = false;
    public static boolean USE_LOCAL = false;
}