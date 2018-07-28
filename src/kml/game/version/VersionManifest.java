package kml.game.version;

public class VersionManifest {
    private LatestVersions latest;
    private VersionMeta[] versions;

    public LatestVersions getLatest() {
        return latest;
    }

    public VersionMeta[] getVersions() {
        return versions;
    }
}
