package kml;

import java.io.File;
import java.net.URL;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public final class Constants {
    public static final int KERNEL_BUILD = 42;
    public static final String KERNEL_BUILD_NAME = "3.0.6";
    public static final URL CHANGESKIN_URL = Utils.stringToURL("https://mc.krothium.com/changeskin");
    public static final URL CHANGECAPE_URL = Utils.stringToURL("https://mc.krothium.com/changecape");
    public static final URL GET_PROFILESID = Utils.stringToURL("https://mc.krothium.com/api/profiles/minecraft");
    public static final URL GET_PROFILESID_MOJANG = Utils.stringToURL("https://api.mojang.com/profiles/minecraft");
    public static final File APPLICATION_WORKING_DIR = Utils.getWorkingDirectory();
    public static final File APPLICATION_CONFIG = new File(APPLICATION_WORKING_DIR, "launcher_profiles.json");
    public static final File APPLICATION_LOGS = new File(APPLICATION_WORKING_DIR, "logs");
    public static boolean USE_LOCAL;
}
