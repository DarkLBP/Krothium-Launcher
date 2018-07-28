package kml.game.version.rule;

import com.google.gson.annotations.Expose;
import kml.OS;
import kml.OSArch;

public class OSRule {
    @Expose
    private OS name;
    @Expose
    private String version;
    @Expose
    private OSArch arch;

    public OS getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public OSArch getArch() {
        return arch;
    }
}
