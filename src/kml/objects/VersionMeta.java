package kml.objects;

import kml.enums.VersionType;

import java.net.URL;
import java.util.Objects;

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

    public boolean hasURL() {
        return Objects.nonNull(this.url);
    }

    public boolean hasID() {
        return Objects.nonNull(this.id);
    }

    public boolean hasType() {
        return Objects.nonNull(this.type);
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
}
