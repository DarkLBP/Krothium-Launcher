package kml.game.profile;

import kml.game.version.VersionMeta;

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
    private final ProfileType type;
    private File gameDir, javaDir;
    private Timestamp created, lastUsed;
    private Map<String, Integer> resolution = new HashMap<>();
    private String icon;
    private VersionMeta lastVersionId;
    private boolean latestRelease, latestSnapshot;

    public Profile(ProfileType type) {
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.type = type;
        this.lastUsed = new Timestamp(0);
        this.created = new Timestamp(System.currentTimeMillis());
    }

    public Profile(String id, String name, ProfileType type, String created, String lastUsed, VersionMeta lastVersionId,
                   String gameDir, String javaDir, String javaArgs, Map<String, Integer> resolution, String icon, boolean latestRelease, boolean latestSnapshot) {

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
        return id;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String newName) {
        name = newName;
    }

    public final boolean hasName() {
        return name != null && !name.isEmpty();
    }

    public final ProfileType getType() {
        return type;
    }

    public final VersionMeta getVersionID() {
        return lastVersionId;
    }

    public final void setVersionID(VersionMeta ver) {
        lastVersionId = ver;
    }

    public final boolean hasVersion() {
        return lastVersionId != null;
    }

    public final File getGameDir() {
        return gameDir;
    }

    public final void setGameDir(File dir) {
        gameDir = dir;
    }

    public final boolean hasGameDir() {
        return gameDir != null;
    }

    public final File getJavaDir() {
        return javaDir;
    }

    public final void setJavaDir(File dir) {
        javaDir = dir;
    }

    public final boolean hasJavaDir() {
        return javaDir != null;
    }

    public final String getJavaArgs() {
        return javaArgs;
    }

    public final void setJavaArgs(String args) {
        javaArgs = args;
    }

    public final boolean hasJavaArgs() {
        return javaArgs != null;
    }

    public final Timestamp getLastUsed() {
        return lastUsed;
    }

    public final void setLastUsed(Timestamp used) {
        lastUsed = used;
    }

    public final boolean hasCreated() {
        return created != null;
    }

    public final Timestamp getCreated() {
        return created;
    }

    public final boolean hasResolution() {
        return resolution != null && resolution.size() == 2;
    }

    public final int getResolutionHeight() {
        if (resolution.containsKey("height")) {
            return resolution.get("height");
        }
        return 0;
    }

    public final int getResolutionWidth() {
        if (resolution.containsKey("width")) {
            return resolution.get("width");
        }
        return 0;
    }

    public final void setResolution(int w, int h) {
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

    public final boolean hasIcon() {
        return icon != null;
    }

    public final String getIcon() {
        return icon;
    }

    public final void setIcon(String icon) {
        this.icon = icon;
    }

    public final boolean isLatestRelease() {
        return latestRelease;
    }

    public final boolean isLatestSnapshot() {
        return latestSnapshot;
    }

    public final void setLatestRelease(boolean latestRelease) {
        this.latestRelease = latestRelease;
    }

    public final void setLatestSnapshot(boolean latestSnapshot) {
        this.latestSnapshot = latestSnapshot;
    }

    @Override
    public final String toString() {
        return id;
    }

    @Override
    public final int compareTo(Profile o) {
        if (equals(o)) {
            return 0;
        }
        if (type == ProfileType.RELEASE && o.type == ProfileType.SNAPSHOT) {
            return -1;
        }
        if (type == ProfileType.SNAPSHOT && o.type == ProfileType.RELEASE) {
            return 1;
        }
        if (o.type != ProfileType.CUSTOM && type == ProfileType.CUSTOM) {
            return 1;
        }
        if (type != ProfileType.CUSTOM && o.type == ProfileType.CUSTOM) {
            return -1;
        }
        int timeCompare = created.compareTo(o.created);
        if (timeCompare == 0) {
            return id.compareTo(o.id);
        }
        return timeCompare;
    }

    @Override
    public final boolean equals(Object obj) {
        return obj instanceof Profile && id.equalsIgnoreCase(((Profile) obj).id);
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }
}
