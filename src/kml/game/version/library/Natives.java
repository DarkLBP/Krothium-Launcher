package kml.game.version.library;

import com.google.gson.annotations.Expose;

public class Natives {
    @Expose
    private String windows;
    @Expose
    private String linux;
    @Expose
    private String osx;

    public String getWindows() {
        return windows;
    }

    public String getLinux() {
        return linux;
    }

    public String getOsx() {
        return osx;
    }
}
