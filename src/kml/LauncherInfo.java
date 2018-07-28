package kml;

import com.google.gson.annotations.Expose;

public class LauncherInfo {
    @Expose(deserialize = false)
    private String name = Constants.APP_VERSION;
    @Expose(deserialize = false)
    private int format = Constants.APP_FORMAT;
    @Expose(deserialize = false)
    private int profilesFormat = Constants.APP_PROFILES_FORMAT;
}
