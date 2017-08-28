package kml;

import kml.enums.VersionType;
import kml.objects.Version;
import kml.objects.VersionMeta;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class Versions {
    private final Set<VersionMeta> versions = new LinkedHashSet<>();
    private final Set<Version> version_cache = new HashSet<>();
    private final Console console;
    private final Kernel kernel;
    private VersionMeta latestSnap, latestRel;

    public Versions(Kernel k) {
        this.kernel = k;
        this.console = k.getConsole();
    }

    /**
     * Add a version to the database
     * @param m The versions to be added
     */
    private void add(VersionMeta m) {
        if (!versions.contains(m)) {
            versions.add(m);
        }
    }

    /**
     * Gets the version meta by id
     * @param id The id to fetch the version meta
     * @return The version meta from the specified id or from the latest release
     */
    public VersionMeta getVersionMeta(String id) {
        if (id != null) {
            switch (id) {
                case "latest-release":
                    return this.latestRel;
                case "latest-snapshot":
                    return this.latestSnap;
            }
            for (VersionMeta m : versions) {
                if (m.getID().equalsIgnoreCase(id)) {
                    return m;
                }
            }
        }
        return this.latestRel;
    }

    /**
     * Gets the version data from a version meta
     * @param vm The target version meta
     * @return The version data from the specified version meta or null if an error happened
     */
    public Version getVersion(VersionMeta vm) {
        if (this.versions.contains(vm)) {
            for (Version v : version_cache) {
                if (v.getID().equalsIgnoreCase(vm.getID())) {
                    return v;
                }
            }
            try {
                Version v = new Version(vm.getURL(), kernel);
                this.version_cache.add(v);
                return v;
            } catch (Exception ex) {
                console.printError(ex.getMessage());
                return null;
            }
        }
        console.printError("Version id " + vm.getID() + " not found.");
        return null;
    }

    /**
     * Loads versions from Mojang servers or from local versions
     */
    public void fetchVersions() {
        String lr = "", ls = "";
        console.printInfo("Fetching remote version list.");
        try {
            JSONObject root = new JSONObject(Utils.readURL(Constants.VERSION_MANIFEST_FILE));
            if (root.has("latest")) {
                JSONObject latest = root.getJSONObject("latest");
                if (latest.has("snapshot")) {
                    ls = latest.getString("snapshot");
                }
                if (latest.has("release")) {
                    lr = latest.getString("release");
                }
            }
            JSONArray vers = root.getJSONArray("versions");
            for (int i = 0; i < vers.length(); i++) {
                JSONObject ver = vers.getJSONObject(i);
                String id = null;
                VersionType type;
                URL url = null;
                if (ver.has("id")) {
                    id = ver.getString("id");
                }
                if (ver.has("url")) {
                    url = Utils.stringToURL(ver.getString("url"));
                }
                if (id == null || url == null) {
                    continue;
                }
                if (ver.has("type")) {
                    type = VersionType.valueOf(ver.getString("type").toUpperCase());
                } else {
                    type = VersionType.RELEASE;
                    console.printError("Remote version " + id + " has no version type. Will be loaded as a RELEASE.");
                }
                VersionMeta vm = new VersionMeta(id, url, type);
                if (lr.equalsIgnoreCase(id)) {
                    this.latestRel = vm;
                }
                if (ls.equalsIgnoreCase(id)) {
                    this.latestSnap = vm;
                }
                this.add(vm);
            }
            console.printInfo("Remote version list loaded.");
        } catch (Exception ex) {
            console.printError("Failed to fetch remote version list.");
            console.printError(ex.getMessage());
        }
        console.printInfo("Fetching local version list versions.");
        VersionMeta lastRelease = null, lastSnapshot = null;
        String latestRelease = "", latestSnapshot = "";
        try {
            File versionsDir = new File(Constants.APPLICATION_WORKING_DIR, "versions");
            if (versionsDir.exists() && versionsDir.isDirectory()) {
                File[] files = versionsDir.listFiles();
                for (File file : files) {
                    if (file.isDirectory()) {
                        File jsonFile = new File(file.getAbsolutePath(), file.getName() + ".json");
                        if (jsonFile.exists()) {
                            String id = null;
                            URL url = jsonFile.toURI().toURL();
                            VersionType type;
                            JSONObject ver = new JSONObject(Utils.readURL(url));
                            if (ver.has("id")) {
                                id = ver.getString("id");
                            } else {
                                continue;
                            }
                            if (ver.has("type")) {
                                type = VersionType.valueOf(ver.getString("type").toUpperCase());
                            } else {
                                type = VersionType.RELEASE;
                                console.printError("Local version " + id + " has no version type. Will be loaded as a RELEASE.");
                            }
                            VersionMeta vm = new VersionMeta(id, url, type);
                            this.add(vm);
                            if (ver.has("releaseTime")) {
                                if (type == VersionType.RELEASE && ver.getString("releaseTime").compareTo(latestRelease) > 0) {
                                    lastRelease = vm;
                                    latestRelease = ver.getString("releaseTime");
                                } else if (type == VersionType.SNAPSHOT && ver.getString("releaseTime").compareTo(latestSnapshot) > 0) {
                                    lastSnapshot = vm;
                                    latestSnapshot = ver.getString("releaseTime");
                                }
                            }
                        }
                    }
                }
            }
            if (this.latestRel == null && lastRelease != null) {
                this.latestRel = lastRelease;
            }
            if (this.latestSnap == null && lastSnapshot != null) {
                this.latestSnap = lastSnapshot;
            }
            console.printInfo("Local version list loaded.");
        } catch (Exception ex) {
            console.printError("Failed to fetch local version list.");
        }
    }

    /**
     * Returns the version database
     * @return The version database
     */
    public Set<VersionMeta> getVersions() {
        return versions;
    }

    /**
     * Returns the latest release
     * @return The latest release
     */
    public VersionMeta getLatestRelease() {
        return latestRel;
    }

    /**
     * Returns the latest snapshot
     * @return The latest snapshot
     */
    public VersionMeta getLatestSnapshot() {
        return latestSnap;
    }
}
