package kml;

import com.google.gson.annotations.Expose;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class Settings {
    @Expose
    private int launcherWidth = 850;
    @Expose
    private int launcherHeight = 700;
    @Expose
    private boolean keepLauncherOpen;
    @Expose
    private boolean enableSnapshots;
    @Expose
    private boolean enableHistorical;
    @Expose
    private boolean enableAdvanced;
    @Expose
    private String locale = "en-us";

    public int getLauncherWidth() {
        return launcherWidth;
    }

    public void setLauncherWidth(int launcherWidth) {
        this.launcherWidth = launcherWidth;
    }

    public int getLauncherHeight() {
        return launcherHeight;
    }

    public void setLauncherHeight(int launcherHeight) {
        this.launcherHeight = launcherHeight;
    }

    public boolean isKeepLauncherOpen() {
        return keepLauncherOpen;
    }

    public void setKeepLauncherOpen(boolean keepLauncherOpen) {
        this.keepLauncherOpen = keepLauncherOpen;
    }

    public boolean isEnableSnapshots() {
        return enableSnapshots;
    }

    public void setEnableSnapshots(boolean enableSnapshots) {
        this.enableSnapshots = enableSnapshots;
    }

    public boolean isEnableHistorical() {
        return enableHistorical;
    }

    public void setEnableHistorical(boolean enableHistorical) {
        this.enableHistorical = enableHistorical;
    }

    public boolean isEnableAdvanced() {
        return enableAdvanced;
    }

    public void setEnableAdvanced(boolean enableAdvanced) {
        this.enableAdvanced = enableAdvanced;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
