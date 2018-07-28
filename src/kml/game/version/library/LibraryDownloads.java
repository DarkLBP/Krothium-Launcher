package kml.game.version.library;

import com.google.gson.annotations.Expose;

import java.util.HashMap;

public class LibraryDownloads {
    @Expose
    private HashMap<String, LibraryDownload> classifiers;
    @Expose
    private LibraryDownload artifact;

    public HashMap<String, LibraryDownload> getClassifiers() {
        return classifiers;
    }

    public LibraryDownload getArtifact() {
        return artifact;
    }
}
