package kmlk;

import java.net.URL;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class Constants {
    public static final int kernelRevision = 1;
    public static final URL authAuthenticate = Utils.stringToURL("https://authserver.mojang.com/authenticate");
    public static final URL authRefresh = Utils.stringToURL("https://authserver.mojang.com/refresh");
    public static final URL authValidate = Utils.stringToURL("https://authserver.mojang.com/validate");
    public static final URL versionsJSON = Utils.stringToURL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
    public static final String resourcesRoot = "http://resources.download.minecraft.net/";
    public static final int downloadTries = 5;
}
