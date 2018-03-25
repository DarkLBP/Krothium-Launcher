package kml.game.download;

import java.io.File;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class Downloadable {
    private final long size;
    private final String url;
    private final File relPath;
    private final String hash;
    private final String fakePath;

    public Downloadable(String url, long size, File path, String hash, String fakePath) {
        this.url = url;
        this.size = size;
        this.relPath = path;
        this.hash = hash;
        this.fakePath = fakePath;
    }

    public final long getSize() {
        return size;
    }

    public final boolean hasURL() {
        return url != null;
    }

    public final String getURL() {
        return url;
    }

    public final File getRelativePath() {
        return relPath;
    }

    public final String getHash() {
        return hash;
    }

    public final boolean hasFakePath() {
        return fakePath != null;
    }

    public final String getFakePath() {
        return fakePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Downloadable that = (Downloadable) o;

        return url != null ? url.equals(that.url) : that.url == null;
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }
}
