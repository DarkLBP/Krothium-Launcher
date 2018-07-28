package kml;

import com.google.gson.annotations.Expose;

public class LauncherInfo {
    @Expose(serialize = false) private String name;
    @Expose(serialize = false) private int format;
    @Expose(serialize = false) private int profilesFormat;
}
