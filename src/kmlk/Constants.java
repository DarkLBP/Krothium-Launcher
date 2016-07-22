package kmlk;

import java.net.URL;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class Constants {
    public static final int KERNEL_REVISION = 1;
    public static final URL AUTHENTICATE_URL = Utils.stringToURL("https://authserver.mojang.com/authenticate");
    public static final URL REFRESH_URL = Utils.stringToURL("https://authserver.mojang.com/refresh");
    public static final URL VALIDATE_URL = Utils.stringToURL("https://authserver.mojang.com/validate");
    public static final URL JSON_FILE = Utils.stringToURL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
    public static final String RESOURCES_URL = "http://resources.download.minecraft.net/";
    public static final int DOWNLOAD_TRIES = 5;
    public static final int KEEPALIVE_TIMEOUT = 8000;
}
