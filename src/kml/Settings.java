package kml;

import org.json.JSONObject;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class Settings {
    private final Kernel kernel;
    private String locale = "en-us";
    private boolean keepLauncherOpen, showGameLog, enableAdvanced, enableHistorical, enableSnapshots;

    public Settings(Kernel k) {
        kernel = k;
    }

    /**
     * Loads the settings from launcher_profiles.json
     */
    public void loadSettings() {
        kernel.getConsole().printInfo("Loading settings...");
        JSONObject root = kernel.getLauncherProfiles();
        if (root != null) {
            if (root.has("settings")) {
                JSONObject settings = root.getJSONObject("settings");
                if (settings.has("locale")) {
                    setLocale(settings.getString("locale"));
                } else {
                    setLocale("en-us");
                }
                if (settings.has("keepLauncherOpen")) {
                    keepLauncherOpen = settings.getBoolean("keepLauncherOpen");
                }
                if (settings.has("showGameLog")) {
                    showGameLog = settings.getBoolean("showGameLog");
                }
                if (settings.has("enableAdvanced")) {
                    enableAdvanced = settings.getBoolean("enableAdvanced");
                }
                if (settings.has("enableHistorical")) {
                    enableHistorical = settings.getBoolean("enableHistorical");
                }
                if (settings.has("enableSnapshots")) {
                    enableSnapshots = settings.getBoolean("enableSnapshots");
                }
            } else {
                setLocale("en-us");
            }
        } else {
            kernel.getConsole().printError("Not settings to be loaded.");
            setLocale("en-us");
        }
    }

    /**
     * Checks if the launcher should be kept open
     * @return If the launcher should be kept open
     */
    public boolean getKeepLauncherOpen() {
        return this.keepLauncherOpen;
    }

    /**
     * Changes whether the launcher should be kept open
     * @param b The new value
     */
    public void setKeepLauncherOpen(boolean b) {
        this.keepLauncherOpen = b;
    }

    /**
     * Gets the current locale
     * @return The current locale
     */
    public String getLocale() {
        return this.locale;
    }

    /**
     * Changes the current locale
     * @param s The locale to be selected
     */
    public void setLocale(String s) {
        if (s != null) {
            if (s.equals("es-es") || s.equals("en-us") || s.equals("pt-pt") || s.equals("pt-br") || s.equals("val-es") || s.equals("hu-hu")) {
                kernel.getConsole().printInfo("Switched language to " + s);
                this.locale = s;
                Language.loadLang(this.locale);
            } else {
                kernel.getConsole().printInfo("Switched language to en-us");
                this.locale = "en-us";
                Language.loadLang(this.locale);
            }
        }
    }

    /**
     * Returns if the game log should be displayed
     * @return If the game log should be displayed
     */
    public boolean getShowGameLog() {
        return this.showGameLog;
    }

    /**
     * Changes the value of whether the game log should be displayed
     * @param b The new value
     */
    public void setShowGameLog(boolean b) {
        this.showGameLog = b;
    }

    /**
     * Returns if advanced settings are enabled
     * @return If advanced settings are enabled
     */
    public boolean getEnableAdvanced() {
        return this.enableAdvanced;
    }

    /**
     * Changes if advanced settings are enabled
     * @param b The new value
     */
    public void setEnableAdvanced(boolean b) {
        this.enableAdvanced = b;
    }

    /**
     * Returns if the historical versions are enabled
     * @return If the historical versions are enabled
     */
    public boolean getEnableHistorical() {
        return this.enableHistorical;
    }

    /**
     * Changes if the historical versions are enabled
     * @param b The new value
     */
    public void setEnableHistorical(boolean b) {
        this.enableHistorical = b;
    }

    /**
     * Returns if the snapshots are enabled
     * @return If the snapshots are enabled
     */
    public boolean getEnableSnapshots() {
        return this.enableSnapshots;
    }

    /**
     * Changes if the snapshots are enabled
     * @param b The new value
     */
    public void setEnableSnapshots(boolean b) {
        this.enableSnapshots = b;
    }

    /**
     * Converts the settings to JSON
     * @return The json conversion of the settings
     */
    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        o.put("locale", getLocale());
        o.put("keepLauncherOpen", getKeepLauncherOpen());
        o.put("showGameLog", getShowGameLog());
        o.put("enableAdvanced", getEnableAdvanced());
        o.put("enableHistorical", getEnableHistorical());
        o.put("enableSnapshots", getEnableSnapshots());
        return o;
    }
}
