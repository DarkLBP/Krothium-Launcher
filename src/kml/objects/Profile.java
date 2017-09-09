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
            if (!this.gameDir.isDirectory()) {
                this.gameDir = null;
            }
        }
        if (javaDir != null) {
            this.javaDir = new File(javaDir);
            if (!this.javaDir.isFile()) {
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

    public final String getID() {
        return this.id;
    }

    public final String getName() {
        return this.name;
    }

    public final void setName(String newName) {
        this.name = newName;
    }

    public final boolean hasName() {
        return this.name != null && !this.name.isEmpty();
    }

    public final ProfileType getType() {
        return this.type;
    }

    public final void setType(ProfileType type) {
        this.type = type;
    }

    public final VersionMeta getVersionID() {
        return this.lastVersionId;
    }

    public final void setVersionID(VersionMeta ver) {
        this.lastVersionId = ver;
    }

    public final boolean hasVersion() {
        return this.lastVersionId != null;
    }

    public final File getGameDir() {
        return this.gameDir;
    }

    public final void setGameDir(File dir) {
        this.gameDir = dir;
    }

    public final boolean hasGameDir() {
        return this.gameDir != null;
    }

    public final File getJavaDir() {
        return this.javaDir;
    }

    public final void setJavaDir(File dir) {
        this.javaDir = dir;
    }

    public final boolean hasJavaDir() {
        return this.javaDir != null;
    }

    public final String getJavaArgs() {
        return this.javaArgs;
    }

    public final void setJavaArgs(String args) {
        this.javaArgs = args;
    }

    public final boolean hasJavaArgs() {
        return this.javaArgs != null;
    }

    public final Timestamp getLastUsed() {
        return this.lastUsed;
    }

    public final void setLastUsed(Timestamp used) {
        this.lastUsed = used;
    }

    public final boolean hasCreated() {
        return this.created != null;
    }

    public final Timestamp getCreated() {
        return this.created;
    }

    public final boolean hasResolution() {
        return this.resolution != null && this.resolution.size() == 2;
    }

    public final int getResolutionHeight() {
        if (this.resolution.containsKey("height")) {
            return this.resolution.get("height");
        }
        return 0;
    }

    public final int getResolutionWidth() {
        if (this.resolution.containsKey("width")) {
            return this.resolution.get("width");
        }
        return 0;
    }

    public final void setResolution(int w, int h) {
        if (w < 1 || h < 1) {
            this.resolution = null;
        } else {
            if (this.resolution == null) {
                this.resolution = new HashMap<>();
            }
            this.resolution.put("width", w);
            this.resolution.put("height", h);
        }
    }

    public final boolean hasIcon() {
        return this.icon != null;
    }

    public final ProfileIcon getIcon() {
        return this.icon;
    }

    public final void setIcon(ProfileIcon icon) {
        this.icon = icon;
    }

    public final boolean isLatestRelease() {
        return this.latestRelease;
    }

    public final boolean isLatestSnapshot() {
        return this.latestSnapshot;
    }

    public final void setLatestRelease(boolean latestRelease) {
        this.latestRelease = latestRelease;
    }

    public final void setLatestSnapshot(boolean latestSnapshot) {
        this.latestSnapshot = latestSnapshot;
    }

    @Override
    public final String toString() {
        return this.id;
    }

    @Override
    public final int compareTo(Profile o) {
        if (this.equals(o)) {
            return 0;
        }
        if (this.type == ProfileType.RELEASE && o.type == ProfileType.SNAPSHOT) {
            return -1;
        }
        if (this.type == ProfileType.SNAPSHOT && o.type == ProfileType.RELEASE) {
            return 1;
        }
        if (o.type != ProfileType.CUSTOM && this.type == ProfileType.CUSTOM) {
            return 1;
        }
        if (this.type != ProfileType.CUSTOM && o.type == ProfileType.CUSTOM) {
            return -1;
        }
        int timeCompare = this.created.compareTo(o.created);
        if (timeCompare == 0) {
            return this.id.compareTo(o.id);
        }
        return timeCompare;
    }

    @Override
    public final boolean equals(Object obj) {
        return obj instanceof Profile && this.id.equalsIgnoreCase(((Profile) obj).id);
    }

    @Override
    public final int hashCode() {
        return this.id.hashCode();
    }
}
