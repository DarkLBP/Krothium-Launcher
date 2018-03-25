package kml;

import kml.gui.lang.Language;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class Settings {
    private final Kernel kernel;
    private final Console console;
    private String locale = "en-us";
    private final HashMap<String, String> supportedLocales = new HashMap<>();
    private boolean keepLauncherOpen, showGameLog, enableAdvanced, enableHistorical, enableSnapshots;
    private double launcherWidth, launcherHeight;


    public Settings(Kernel k) {
        kernel = k;
        console = kernel.getConsole();
        supportedLocales.put("en-us", "English - United States");
        supportedLocales.put("es-es", "Español - España");
        supportedLocales.put("val-es", "Valencià - C. Valenciana");
        supportedLocales.put("pt-pt", "Português - Portugal");
        supportedLocales.put("pt-br", "Português - Brasil");
        supportedLocales.put("hu-hu", "Hungarian - Magyar");
    }

    /**
     * Loads the settings from launcher_profiles.json
     */
    public void loadSettings() {
        console.print("Loading settings...");
        JSONObject root = this.kernel.getLauncherProfiles();
        if (root != null) {
            if (root.has("settings")) {
                JSONObject settings = root.getJSONObject("settings");
                try {
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
                    if (settings.has("launcherWidth")) {
                        launcherWidth = settings.getDouble("launcherWidth");
                    } else {
                        launcherWidth = 850;
                    }
                    if (settings.has("launcherHeight")) {
                        launcherHeight = settings.getDouble("launcherHeight");
                    } else {
                        launcherHeight = 700;
                    }
                } catch (JSONException ex) {
                    console.print("Failed to load settings.");
                    ex.printStackTrace(console.getWriter());
                }
            } else {
                setDefaults();
            }
        } else {
            console.print("Not settings to be loaded.");
            setDefaults();
        }
    }

    private void setDefaults() {
        setLocale("en-us");
        launcherWidth = 850;
        launcherHeight = 700;
    }

    /**
     * Checks if the launcher should be kept open
     * @return If the launcher should be kept open
     */
    public boolean getKeepLauncherOpen() {
        return keepLauncherOpen;
    }

    /**
     * Changes whether the launcher should be kept open
     * @param b The new value
     */
    public void setKeepLauncherOpen(boolean b) {
        keepLauncherOpen = b;
    }

    /**
     * Gets the current locale
     * @return The current locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Changes the current locale
     * @param s The locale to be selected
     */
    public void setLocale(String s) {
        if (s != null) {
            if (!this.supportedLocales.containsKey(s)) {
                locale = "en-us";
            } else {
                locale = s;
            }
            console.print("Switched language to " + this.locale);
            try {
                Language.loadLang(locale);
            } catch (IOException e) {
                console.print("Failed to load language file.");
                e.printStackTrace(console.getWriter());
            }
        }
    }

    /**
     * Returns if the game log should be displayed
     * @return If the game log should be displayed
     */
    public boolean getShowGameLog() {
        return showGameLog;
    }

    /**
     * Changes the value of whether the game log should be displayed
     * @param b The new value
     */
    public void setShowGameLog(boolean b) {
        showGameLog = b;
    }

    /**
     * Returns if advanced settings are enabled
     * @return If advanced settings are enabled
     */
    public boolean getEnableAdvanced() {
        return enableAdvanced;
    }

    /**
     * Changes if advanced settings are enabled
     * @param b The new value
     */
    public void setEnableAdvanced(boolean b) {
        enableAdvanced = b;
    }

    /**
     * Returns if the historical versions are enabled
     * @return If the historical versions are enabled
     */
    public boolean getEnableHistorical() {
        return enableHistorical;
    }

    /**
     * Returns a map of supported locales
     * @return List of supported locales
     */
    public HashMap<String, String> getSupportedLocales() {
        return supportedLocales;
    }

    /**
     * Changes if the historical versions are enabled
     * @param b The new value
     */
    public void setEnableHistorical(boolean b) {
        enableHistorical = b;
    }

    /**
     * Returns if the snapshots are enabled
     * @return If the snapshots are enabled
     */
    public boolean getEnableSnapshots() {
        return enableSnapshots;
    }

    /**
     * Changes if the snapshots are enabled
     * @param b The new value
     */
    public void setEnableSnapshots(boolean b) {
        enableSnapshots = b;
    }

    /**
     * Returns launcher width
     * @return Launcher width
     */
    public double getLauncherWidth() {
        return launcherWidth;
    }

    /**
     * Returns launcher height
     * @return Launcher height
     */
    public double getLauncherHeight() {
        return launcherHeight;
    }

    /**
     * Set the launcher width
     * @param launcherWidth Launcher width
     */
    public void setLauncherWidth(double launcherWidth) {
        this.launcherWidth = launcherWidth;
    }

    /**
     * Sets the launcher height
     * @param launcherHeight Launcher height
     */
    public void setLauncherHeight(double launcherHeight) {
        this.launcherHeight = launcherHeight;
    }

    /**
     * Converts the settings to JSON
     * @return The json conversion of the settings
     */
    public final JSONObject toJSON() {
        JSONObject o = new JSONObject();
        o.put("locale", this.locale);
        o.put("keepLauncherOpen", this.keepLauncherOpen);
        o.put("showGameLog", this.showGameLog);
        o.put("enableAdvanced", this.enableAdvanced);
        o.put("enableHistorical", this.enableHistorical);
        o.put("enableSnapshots", this.enableSnapshots);
        o.put("launcherWidth", this.launcherWidth);
        o.put("launcherHeight", this.launcherHeight);
        return o;
    }
}
