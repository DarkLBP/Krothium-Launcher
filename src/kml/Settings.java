package kml;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by darkl on 18/12/2016.
 */
public class Settings {
    private String locale = "en-us";
    private boolean keepLauncherOpen = false;
    private boolean showGameLog = false;
    private boolean enableAdvanced = false;
    private boolean enableHistorical = false;
    private boolean enableSnapshots = false;
    private final Kernel kernel;

    public Settings(Kernel k){
        kernel = k;
    }

    public void loadSettings(){
        try {
            File launcherProfiles = kernel.getConfigFile();
            JSONObject root = new JSONObject(Utils.readURL(launcherProfiles.toURI().toURL()));
            if (root.has("settings")){
                JSONObject settings = root.getJSONObject("settings");
                if (settings.has("locale")){
                    locale = settings.getString("locale");
                }
                if (settings.has("keepLauncherOpen")){
                    keepLauncherOpen = settings.getBoolean("keepLauncherOpen");
                }
                if (settings.has("showGameLog")){
                    showGameLog = settings.getBoolean("showGameLog");
                }
                if (settings.has("enableAdvanced")){
                    enableAdvanced = settings.getBoolean("enableAdvanced");
                }
                if (settings.has("enableHistorical")){
                    enableHistorical = settings.getBoolean("enableHistorical");
                }
                if (settings.has("enableSnapshots")){
                    enableSnapshots = settings.getBoolean("enableSnapshots");
                }
            }
        } catch (Exception ex) {
            kernel.getConsole().printError("Failed to load settings data. Using defaults...");
            locale = "en-us";
            keepLauncherOpen = false;
            showGameLog = false;
            enableAdvanced = false;
            enableHistorical = false;
            enableSnapshots = false;
        }
    }
    public boolean keepLauncherOpen(){return this.keepLauncherOpen;}
    public String getLocale(){return this.locale;}
    public boolean showGameLog(){return this.showGameLog;}
    public boolean enableAdvanced(){return this.enableAdvanced;}
    public boolean enableHistorical(){return this.enableHistorical;}
    public boolean enableSnapshots(){return this.enableSnapshots;}

    public JSONObject toJSON(){
        JSONObject o = new JSONObject();
        o.put("locale", getLocale());
        o.put("keepLauncherOpen", keepLauncherOpen());
        o.put("showGameLog", showGameLog());
        o.put("enableAdvanced", enableAdvanced());
        o.put("enableHistorical", enableHistorical());
        o.put("enableSnapshots", enableSnapshots());
        return o;
    }
}
