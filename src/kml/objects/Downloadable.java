package kml.objects;

import java.io.File;
import java.net.URL;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class Downloadable {
    private final long size;
    private final URL url;
    private final File relPath;
    private final String hash;
    private final String fakePath;

    public Downloadable(URL url, long size, File path, String hash, String fakePath) {
        this.url = url;
        this.size = size;
        this.relPath = path;
        this.hash = hash;
        this.fakePath = fakePath;
    }

    public final long getSize() {
        return this.size;
    }

    public final boolean hasURL() {
        return this.url != null;
    }

    public final URL getURL() {
        return this.url;
    }

    public final File getRelativePath() {
        return this.relPath;
    }

    public final String getHash() {
        return this.hash;
    }

    public final boolean hasHash() {
        return this.hash != null;
    }

    public final boolean hasSize() {
        return this.size != -1;
    }

    public final boolean hasFakePath() {
        return this.fakePath != null;
    }

    public final String getFakePath() {
        return this.fakePath;
    }
}
