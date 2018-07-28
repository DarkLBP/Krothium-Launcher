package kml.game.version;

import java.sql.Timestamp;

/**
 * @author DarkLBP
 * website https://krothium.com
 */

public class VersionMeta {
    private String id;
    private VersionType type;
    private Timestamp time;
    private Timestamp releaseTime;
    private String url;

    public VersionMeta(String id, String url, VersionType type) {
//        this.id = id;
//        this.url = url;
//        this.type = type;
    }

    public String getId() {
        return id;
    }

    public Timestamp getTime() {
        return time;
    }

    public Timestamp getReleaseTime() {
        return releaseTime;
    }

    public VersionType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public final boolean equals(Object o) {
        return o instanceof VersionMeta && id.equalsIgnoreCase(((VersionMeta) o).id);
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }

    @Override
    public final String toString() {
        return id;
    }
}
