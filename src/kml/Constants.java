package kml;

import java.io.File;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public final class Constants {
    public static final int KERNEL_BUILD = 46;
    public static final String KERNEL_BUILD_NAME = "3.1.1";
    public static final int KERNEL_FORMAT = 20;
    public static final int KERNEL_PROFILES_FORMAT = 2;
    public static final File APPLICATION_WORKING_DIR = Utils.getWorkingDirectory();
    public static final File APPLICATION_CONFIG = new File(APPLICATION_WORKING_DIR, "launcher_profiles.json");
    public static final File APPLICATION_LOGS = new File(APPLICATION_WORKING_DIR, "logs");
    public static boolean USE_LOCAL;
}
