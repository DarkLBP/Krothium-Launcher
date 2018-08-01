package kml.auth;

import com.google.gson.annotations.Expose;

public class AuthAgent {
    @Expose
    private String name = "Minecraft";
    @Expose
    private int version = 1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
