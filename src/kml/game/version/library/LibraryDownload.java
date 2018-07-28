package kml.game.version.library;

import com.google.gson.annotations.Expose;

public class LibraryDownload {
    @Expose
    private long size;
    @Expose
    private String sha1;
    @Expose
    private String path;
    @Expose
    private String url;

    public long getSize() {
        return size;
    }

    public String getSha1() {
        return sha1;
    }

    public String getPath() {
        return path;
    }

    public String getUrl() {
        return url;
    }
}
