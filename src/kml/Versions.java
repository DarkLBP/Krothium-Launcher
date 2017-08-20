package kml;

import kml.enums.VersionType;
import kml.objects.Version;
import kml.objects.VersionMeta;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class Versions {
    private final Map<String, VersionMeta> versions = new LinkedHashMap<>();
    private final Map<String, Version> version_cache = new HashMap<>();
    private final Console console;
    private final Kernel kernel;
    private String latestSnap, latestRel;

    public Versions(Kernel k) {
        this.kernel = k;
        this.console = k.getConsole();
    }

    private void add(String name, VersionMeta m) {
        if (!versions.containsKey(name)) {
            versions.put(name, m);
        }
    }

    public Version getVersion(String id) {
        if (this.versions.containsKey(id)) {
            if (this.version_cache.containsKey(id)) {
                return this.version_cache.get(id);
            }
            VersionMeta vm = this.versions.get(id);
            if (!vm.hasURL()) {
                console.printError("Version meta from version " + id + " is incomplete.");
                return null;
            }
            try {
                Version v = new Version(vm.getURL(), kernel);
                this.version_cache.put(id, v);
                return v;
            } catch (Exception ex) {
                console.printError(ex.getMessage());
                return null;
            }
        } else if (id.equals("latest-release")) {
            return this.getVersion(this.getLatestRelease());
        } else if (id.equals("latest-snapshot")) {
            return this.getVersion(this.getLatestSnapshot());
        }
        console.printError("Version id " + id + " not found.");
        return null;
    }

    public void fetchVersions() {
        boolean last_rel = Objects.nonNull(this.latestRel);
        boolean last_snap = Objects.nonNull(this.latestSnap);
        console.printInfo("Fetching remote version list.");
        try {
            JSONObject root = new JSONObject(Utils.readURL(Constants.VERSION_MANIFEST_FILE));
            if (root.has("latest")) {
                JSONObject latest = root.getJSONObject("latest");
                if (latest.has("snapshot")) {
                    this.latestSnap = latest.getString("snapshot");
                    last_snap = true;
                }
                if (latest.has("release")) {
                    this.latestRel = latest.getString("release");
                    last_rel = true;
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
                if (ver.has("type")) {
                    type = VersionType.valueOf(ver.getString("type").toUpperCase());
                } else {
                    type = VersionType.RELEASE;
                    console.printError("Remote version " + id + " has no version type. Will be loaded as a RELEASE.");
                }
                if (ver.has("url")) {
                    url = Utils.stringToURL(ver.getString("url"));
                }
                if (Objects.isNull(id) || Objects.isNull(url)) {
                    continue;
                }
                VersionMeta vm = new VersionMeta(id, url, type);
                this.add(id, vm);
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
            File versionsDir = new File(Constants.APPLICATION_WORKING_DIR + File.separator + "versions");
            if (versionsDir.exists()) {
                if (versionsDir.isDirectory()) {
                    File[] files = versionsDir.listFiles();
                    for (File file : files) {
                        if (file.isDirectory()) {
                            File jsonFile = new File(file.getAbsolutePath() + File.separator + file.getName() + ".json");
                            if (jsonFile.exists()) {
                                String id = file.getName();
                                URL url = jsonFile.toURI().toURL();
                                VersionType type;
                                JSONObject ver = new JSONObject(Utils.readURL(url));
                                if (ver.has("type")) {
                                    type = VersionType.valueOf(ver.getString("type").toUpperCase());
                                } else {
                                    type = VersionType.RELEASE;
                                    console.printError("Local version " + id + " has no version type. Will be loaded as a RELEASE.");
                                }
                                VersionMeta vm = new VersionMeta(id, url, type);
                                this.add(id, vm);
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
            }
            if (!last_rel && Objects.nonNull(lastRelease)) {
                this.latestRel = lastRelease.getID();
            }
            if (!last_snap && Objects.nonNull(lastSnapshot)) {
                this.latestSnap = lastSnapshot.getID();
            }
            console.printInfo("Local version list loaded.");
        } catch (Exception ex) {
            console.printError("Failed to fetch local version list.");
        }
    }

    public Map<String, VersionMeta> getVersions() {
        return versions;
    }

    public String getLatestRelease() {
        return latestRel;
    }

    public String getLatestSnapshot() {
        return latestSnap;
    }

    public int versionCount() {
        return versions.size();
    }
}
