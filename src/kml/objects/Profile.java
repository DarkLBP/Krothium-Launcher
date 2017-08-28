package kml.objects;

import kml.enums.ProfileIcon;
import kml.enums.ProfileType;

import java.io.File;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class Profile implements Comparable<Profile>{
    private final String id;
    private String name, javaArgs;
    private ProfileType type;
    private File gameDir, javaDir;
    private Timestamp created, lastUsed;
    private Map<String, Integer> resolution = new HashMap<>();
    private ProfileIcon icon;
    private VersionMeta lastVersionId;
    private boolean latestRelease, latestSnapshot;

    public Profile(ProfileType type) {
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.type = type;
        if (type == ProfileType.RELEASE) {
            this.lastUsed = new Timestamp(0);
        } else if (type == ProfileType.SNAPSHOT) {
            this.lastUsed = new Timestamp(0);
        } else {
            this.created = new Timestamp(System.currentTimeMillis());
            this.lastUsed = new Timestamp(0);
        }
    }

    public Profile(String id, String name, ProfileType type, String created, String lastUsed, VersionMeta lastVersionId,
                   String gameDir, String javaDir, String javaArgs, Map<String, Integer> resolution, ProfileIcon icon, boolean latestRelease, boolean latestSnapshot) {

        this.id = id;
        this.name = name;
        this.lastVersionId = lastVersionId;

        if (gameDir != null) {
            this.gameDir = new File(gameDir);
            if (!this.gameDir.exists() || !this.gameDir.isDirectory()) {
                this.gameDir = null;
            }
        }
        if (javaDir != null) {
            this.javaDir = new File(javaDir);
            if (!this.javaDir.exists() || !this.javaDir.isFile()) {
                this.javaDir = null;
            }
        }

        this.javaArgs = javaArgs;
        this.resolution = resolution;

        if (lastUsed == null) {
            this.lastUsed = new Timestamp(0);
        } else {
            try {
                this.lastUsed = Timestamp.valueOf(lastUsed.replace("T", " ").replace("Z", ""));
            } catch (Exception ex) {
                this.lastUsed = new Timestamp(0);
            }
        }

        this.type = type;

        if (this.type == ProfileType.CUSTOM) {
            if (created == null) {
                this.created = new Timestamp(0);
            } else {
                try {
                    this.created = Timestamp.valueOf(created.replace("T", " ").replace("Z", ""));
                } catch (Exception ex) {
                    this.created = new Timestamp(0);
                }
            }
        }

        this.icon = icon;

        this.latestRelease = latestRelease;
        this.latestSnapshot = latestSnapshot;
    }

    public String getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public boolean hasName() {
        return this.name != null && !this.name.isEmpty();
    }

    public ProfileType getType() {
        return this.type;
    }

    public void setType(ProfileType type) {
        this.type = type;
    }

    public VersionMeta getVersionID() {
        return this.lastVersionId;
    }

    public void setVersionID(VersionMeta ver) {
        this.lastVersionId = ver;
    }

    public boolean hasVersion() {
        return this.lastVersionId != null;
    }

    public File getGameDir() {
        return this.gameDir;
    }

    public void setGameDir(File dir) {
        this.gameDir = dir;
    }

    public boolean hasGameDir() {
        return this.gameDir != null;
    }

    public File getJavaDir() {
        return this.javaDir;
    }

    public void setJavaDir(File dir) {
        this.javaDir = dir;
    }

    public boolean hasJavaDir() {
        return this.javaDir != null;
    }

    public String getJavaArgs() {
        return this.javaArgs;
    }

    public void setJavaArgs(String args) {
        this.javaArgs = args;
    }

    public boolean hasJavaArgs() {
        return this.javaArgs != null;
    }

    public Timestamp getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Timestamp used) {
        this.lastUsed = used;
    }

    public boolean hasCreated() {
        return this.created != null;
    }

    public Timestamp getCreated() {
        return this.created;
    }

    public boolean hasResolution() {
        return this.resolution != null && this.resolution.size() == 2;
    }

    public int getResolutionHeight() {
        if (resolution.containsKey("height")) {
            return resolution.get("height");
        }
        return 0;
    }

    public int getResolutionWidth() {
        if (resolution.containsKey("width")) {
            return resolution.get("width");
        }
        return 0;
    }

    public void setResolution(int w, int h) {
        if (w < 1 || h < 1) {
            resolution = null;
        } else {
            if (resolution == null) {
                resolution = new HashMap<>();
            }
            resolution.put("width", w);
            resolution.put("height", h);
        }
    }

    public boolean hasIcon() {
        return this.icon != null;
    }

    public ProfileIcon getIcon() {
        return this.icon;
    }

    public void setIcon(ProfileIcon icon) {
        this.icon = icon;
    }

    public boolean isLatestRelease() {
        return latestRelease;
    }

    public boolean isLatestSnapshot() {
        return latestSnapshot;
    }

    public void setLatestRelease(boolean latestRelease) {
        this.latestRelease = latestRelease;
    }

    public void setLatestSnapshot(boolean latestSnapshot) {
        this.latestSnapshot = latestSnapshot;
    }

    @Override
    public String toString() {
        return this.id;
    }

    @Override
    public int compareTo(Profile o) {
        if (this.equals(o)) {
            return 0;
        }
        if (this.type == ProfileType.RELEASE && o.type == ProfileType.SNAPSHOT) {
            return -1;
        } else if (this.type == ProfileType.SNAPSHOT && o.type == ProfileType.RELEASE) {
            return 1;
        } else if (o.type != ProfileType.CUSTOM && this.type == ProfileType.CUSTOM) {
            return 1;
        } else if (this.type != ProfileType.CUSTOM && o.type == ProfileType.CUSTOM) {
            return -1;
        }
        int timeCompare = this.created.compareTo(o.created);
        if (timeCompare == 0) {
            return this.id.compareTo(o.id);
        }
        return timeCompare;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Profile && id.equalsIgnoreCase(((Profile) obj).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
