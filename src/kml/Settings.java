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
    private String locale = "en-us";
    private final HashMap<String, String> supportedLocales = new HashMap();
    private boolean keepLauncherOpen, showGameLog, enableAdvanced, enableHistorical, enableSnapshots;

    public Settings(Kernel k) {
        this.kernel = k;
        this.supportedLocales.put("en-us", "English - United States");
        this.supportedLocales.put("es-es", "Español - España");
        this.supportedLocales.put("val-es", "Valencià - C. Valenciana");
        this.supportedLocales.put("pt-pt", "Português - Portugal");
        this.supportedLocales.put("pt-br", "Português - Brasil");
        this.supportedLocales.put("hu-hu", "Hungarian - Magyar");
    }

    /**
     * Loads the settings from launcher_profiles.json
     */
    public final void loadSettings() {
        this.kernel.getConsole().print("Loading settings...");
        JSONObject root = this.kernel.getLauncherProfiles();
        if (root != null) {
            if (root.has("settings")) {
                JSONObject settings = root.getJSONObject("settings");
                try {
                    if (settings.has("locale")) {
                        this.setLocale(settings.getString("locale"));
                    } else {
                        this.setLocale("en-us");
                    }
                    if (settings.has("keepLauncherOpen")) {
                        this.keepLauncherOpen = settings.getBoolean("keepLauncherOpen");
                    }
                    if (settings.has("showGameLog")) {
                        this.showGameLog = settings.getBoolean("showGameLog");
                    }
                    if (settings.has("enableAdvanced")) {
                        this.enableAdvanced = settings.getBoolean("enableAdvanced");
                    }
                    if (settings.has("enableHistorical")) {
                        this.enableHistorical = settings.getBoolean("enableHistorical");
                    }
                    if (settings.has("enableSnapshots")) {
                        this.enableSnapshots = settings.getBoolean("enableSnapshots");
                    }
                } catch (JSONException ex) {
                    this.kernel.getConsole().print("Failed to load settings.");
                    ex.printStackTrace(this.kernel.getConsole().getWriter());
                }
            } else {
                this.setLocale("en-us");
            }
        } else {
            this.kernel.getConsole().print("Not settings to be loaded.");
            this.setLocale("en-us");
        }
    }

    /**
     * Checks if the launcher should be kept open
     * @return If the launcher should be kept open
     */
    public final boolean getKeepLauncherOpen() {
        return this.keepLauncherOpen;
    }

    /**
     * Changes whether the launcher should be kept open
     * @param b The new value
     */
    public final void setKeepLauncherOpen(boolean b) {
        this.keepLauncherOpen = b;
    }

    /**
     * Gets the current locale
     * @return The current locale
     */
    public final String getLocale() {
        return this.locale;
    }

    /**
     * Changes the current locale
     * @param s The locale to be selected
     */
    public final void setLocale(String s) {
        if (s != null) {
            if (this.supportedLocales.containsKey(s)) {
                this.kernel.getConsole().print("Switched language to " + s);
                this.locale = s;
                try {
                    Language.loadLang(this.locale);
                } catch (IOException e) {
                    this.kernel.getConsole().print("Failed to load language file.");
                    e.printStackTrace(this.kernel.getConsole().getWriter());
                }
            } else {
                this.kernel.getConsole().print("Switched language to en-us");
                this.locale = "en-us";
                try {
                    Language.loadLang(this.locale);
                } catch (IOException e) {
                    this.kernel.getConsole().print("Failed to load language file.");
                    e.printStackTrace(this.kernel.getConsole().getWriter());
                }
            }
        }
    }

    /**
     * Returns if the game log should be displayed
     * @return If the game log should be displayed
     */
    public final boolean getShowGameLog() {
        return this.showGameLog;
    }

    /**
     * Changes the value of whether the game log should be displayed
     * @param b The new value
     */
    public final void setShowGameLog(boolean b) {
        this.showGameLog = b;
    }

    /**
     * Returns if advanced settings are enabled
     * @return If advanced settings are enabled
     */
    public final boolean getEnableAdvanced() {
        return this.enableAdvanced;
    }

    /**
     * Changes if advanced settings are enabled
     * @param b The new value
     */
    public final void setEnableAdvanced(boolean b) {
        this.enableAdvanced = b;
    }

    /**
     * Returns if the historical versions are enabled
     * @return If the historical versions are enabled
     */
    public final boolean getEnableHistorical() {
        return this.enableHistorical;
    }

    /**
     * Returns a map of supported locales
     * @return List of supported locales
     */
    public final HashMap<String, String> getSupportedLocales() {
        return this.supportedLocales;
    }

    /**
     * Changes if the historical versions are enabled
     * @param b The new value
     */
    public final void setEnableHistorical(boolean b) {
        this.enableHistorical = b;
    }

    /**
     * Returns if the snapshots are enabled
     * @return If the snapshots are enabled
     */
    public final boolean getEnableSnapshots() {
        return this.enableSnapshots;
    }

    /**
     * Changes if the snapshots are enabled
     * @param b The new value
     */
    public final void setEnableSnapshots(boolean b) {
        this.enableSnapshots = b;
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
        return o;
    }
}
