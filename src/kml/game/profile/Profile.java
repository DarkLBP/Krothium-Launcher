package kml.game.profile;

import com.google.gson.annotations.Expose;

import java.sql.Timestamp;

/**
 * @author DarkLBP
 * website https://krothium.com
 */

public class Profile {
    @Expose
    private String name;
    @Expose
    private ProfileType type;
    @Expose
    private Timestamp created;
    @Expose
    private Timestamp lastUsed;
    @Expose
    private String icon;
    @Expose
    private String lastVersionId;
    @Expose
    private Resolution resolution;
    @Expose
    private String gameDir;
    @Expose
    private String javaDir;
    @Expose
    private String javaArgs;


    public Profile(ProfileType type) {
        this.type = type;
        this.lastUsed = new Timestamp(0);
        this.created = new Timestamp(System.currentTimeMillis());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProfileType getType() {
        return type;
    }

    public void setType(ProfileType type) {
        this.type = type;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public Timestamp getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Timestamp lastUsed) {
        this.lastUsed = lastUsed;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLastVersionId() {
        return lastVersionId;
    }

    public void setLastVersionId(String lastVersionId) {
        this.lastVersionId = lastVersionId;
    }

    public Resolution getResolution() {
        return resolution;
    }

    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    public String getGameDir() {
        return gameDir;
    }

    public void setGameDir(String gameDir) {
        this.gameDir = gameDir;
    }

    public String getJavaDir() {
        return javaDir;
    }

    public void setJavaDir(String javaDir) {
        this.javaDir = javaDir;
    }

    public String getJavaArgs() {
        return javaArgs;
    }

    public void setJavaArgs(String javaArgs) {
        this.javaArgs = javaArgs;
    }
}
