package kml.game.version;

import java.net.URL;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class VersionMeta {
    private final String id;
    private final URL url;
    private final VersionType type;

    public VersionMeta(String id, URL url, VersionType type) {
        this.id = id;
        this.url = url;
        this.type = type;
    }

    public final String getID() {
        return this.id;
    }

    public final URL getURL() {
        return this.url;
    }

    public final VersionType getType() {
        return this.type;
    }

    @Override
    public final boolean equals(Object o) {
        return o instanceof VersionMeta && this.id.equalsIgnoreCase(((VersionMeta) o).id);
    }

    @Override
    public final int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public final String toString() {
        return this.id;
    }
}
