package kml.objects;

import kml.enums.VersionType;

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

    public String getID() {
        return this.id;
    }

    public URL getURL() {
        return this.url;
    }

    public VersionType getType() {
        return this.type;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof VersionMeta && id.equalsIgnoreCase(((VersionMeta)o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
