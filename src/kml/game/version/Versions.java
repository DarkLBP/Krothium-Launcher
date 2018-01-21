package kml.game.version;

import kml.Console;
import kml.Constants;
import kml.Kernel;
import kml.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class Versions {
    private final Set<VersionMeta> versions = new LinkedHashSet<>();
    private final Collection<Version> version_cache = new HashSet<>();
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
        if (!this.versions.contains(m)) {
            this.versions.add(m);
        }
    }

    /**
     * Gets the version meta by id
     * @param id The id to fetch the version meta
     * @return The version meta from the specified id or from the latest release
     */
    public final VersionMeta getVersionMeta(String id) {
        if (id != null) {
            switch (id) {
                case "latest-release":
                    return this.latestRel;
                case "latest-snapshot":
                    return this.latestSnap;
            }
            for (VersionMeta m : this.versions) {
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
    public final Version getVersion(VersionMeta vm) {
        if (this.versions.contains(vm)) {
            for (Version v : this.version_cache) {
                if (v.getID().equalsIgnoreCase(vm.getID())) {
                    return v;
                }
            }
            try {
                Version v = new Version(vm.getURL(), this.kernel);
                this.version_cache.add(v);
                return v;
            } catch (Exception ex) {
                this.console.print(ex.getMessage());
                return null;
            }
        }
        this.console.print("Version id " + vm.getID() + " not found.");
        return null;
    }

    /**
     * Loads versions from Mojang servers or from local versions
     */
    public final void fetchVersions() {
        String lr = "", ls = "";
        this.console.print("Fetching remote version list.");
        try {
            URL versionManifest = Utils.stringToURL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
            JSONObject root = new JSONObject(Utils.readURL(versionManifest));
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
                    try {
                        type = VersionType.valueOf(ver.getString("type").toUpperCase(Locale.ENGLISH));
                    } catch (IllegalArgumentException ex) {
                        type = VersionType.RELEASE;
                        this.console.print("Invalid type for version " + id);
                    }
                } else {
                    type = VersionType.RELEASE;
                    this.console.print("Remote version " + id + " has no version type. Will be loaded as a RELEASE.");
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
            this.console.print("Remote version list loaded.");
        } catch (JSONException | IOException ex) {
            this.console.print("Failed to fetch remote version list.");
            ex.printStackTrace(this.console.getWriter());
        }
        this.console.print("Fetching local version list versions.");
        VersionMeta lastRelease = null, lastSnapshot = null;
        String latestRelease = "", latestSnapshot = "";
        try {
            File versionsDir = new File(Constants.APPLICATION_WORKING_DIR, "versions");
            if (versionsDir.isDirectory()) {
                File[] files = versionsDir.listFiles();
                for (File file : files) {
                    if (file.isDirectory()) {
                        File jsonFile = new File(file.getAbsolutePath(), file.getName() + ".json");
                        if (jsonFile.isFile()) {
                            String id;
                            URL url = jsonFile.toURI().toURL();
                            VersionType type;
                            JSONObject ver = new JSONObject(Utils.readURL(url));
                            if (ver.has("id")) {
                                id = ver.getString("id");
                            } else {
                                continue;
                            }
                            if (ver.has("type")) {
                                try {
                                    type = VersionType.valueOf(ver.getString("type").toUpperCase(Locale.ENGLISH));
                                } catch (IllegalArgumentException ex) {
                                    type = VersionType.RELEASE;
                                    this.console.print("Invalid type for version " + id);
                                }
                            } else {
                                type = VersionType.RELEASE;
                                this.console.print("Local version " + id + " has no version type. Will be loaded as a RELEASE.");
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
            this.console.print("Local version list loaded.");
        } catch (JSONException | IOException ex) {
            this.console.print("Failed to fetch local version list.");
            ex.printStackTrace(this.console.getWriter());
        }
    }

    /**
     * Returns the version database
     * @return The version database
     */
    public final Iterable<VersionMeta> getVersions() {
        return this.versions;
    }

    /**
     * Returns the latest release
     * @return The latest release
     */
    public final VersionMeta getLatestRelease() {
        return this.latestRel;
    }

    /**
     * Returns the latest snapshot
     * @return The latest snapshot
     */
    public final VersionMeta getLatestSnapshot() {
        return this.latestSnap;
    }
}
