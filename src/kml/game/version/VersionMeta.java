package kml.game.version;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class VersionMeta {
    private final String id;
    private final String url;
    private final VersionType type;

    public VersionMeta(String id, String url, VersionType type) {
        this.id = id;
        this.url = url;
        this.type = type;
    }

    public final String getID() {
        return id;
    }

    public final String getURL() {
        return url;
    }

    public final VersionType getType() {
        return type;
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
