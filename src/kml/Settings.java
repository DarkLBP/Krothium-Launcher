package kml;

import org.json.JSONObject;

import java.util.Objects;

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

    public boolean getKeepLauncherOpen() {
        return this.keepLauncherOpen;
    }

    public void setKeepLauncherOpen(boolean b) {
        this.keepLauncherOpen = b;
    }

    public String getLocale() {
        return this.locale;
    }

    public void setLocale(String s) {
        if (Objects.nonNull(s)) {
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

    public boolean getShowGameLog() {
        return this.showGameLog;
    }

    public void setShowGameLog(boolean b) {
        this.showGameLog = b;
    }

    public boolean getEnableAdvanced() {
        return this.enableAdvanced;
    }

    public void setEnableAdvanced(boolean b) {
        this.enableAdvanced = b;
    }

    public boolean getEnableHistorical() {
        return this.enableHistorical;
    }

    public void setEnableHistorical(boolean b) {
        this.enableHistorical = b;
    }

    public boolean getEnableSnapshots() {
        return this.enableSnapshots;
    }

    public void setEnableSnapshots(boolean b) {
        this.enableSnapshots = b;
    }

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
