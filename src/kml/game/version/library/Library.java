package kml.game.version.library;

import kml.*;
import kml.game.download.Downloadable;
import kml.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public final class Library {
    private final String name;
    private final String url;
    private final Map<String, Downloadable> downloads = new HashMap<>();
    private final Map<OS, LibraryRule> rules = new EnumMap<>(OS.class);
    private final File relativePath, relativeNativePath;
    private final List<String> exclude = new ArrayList<>();
    private final Map<OS, String> natives = new EnumMap<>(OS.class);

    public Library(JSONObject lib) throws Exception {
        if (lib.has("name")) {
            name = lib.getString("name");
        } else {
            throw new Exception("Invalid name for a library.");
        }
        if (lib.has("url")) {
            url = lib.getString("url");
        } else {
            url = null;
        }
        if (lib.has("rules")) {
            JSONArray rules = lib.getJSONArray("rules");
            OS[] oses = OS.values();
            for (OS os : oses) {
                this.rules.put(os, LibraryRule.NOSET);
            }
            for (int i = 0; i < rules.length(); i++) {
                JSONObject rule = rules.getJSONObject(i);
                LibraryRule action = null;
                OS o = null;
                if (rule.has("action")) {
                    action = LibraryRule.valueOf(rule.getString("action").toUpperCase(Locale.ENGLISH));
                }
                if (rule.has("os")) {
                    JSONObject os = rule.getJSONObject("os");
                    if (os.has("name")) {
                        o = OS.valueOf(os.getString("name").toUpperCase(Locale.ENGLISH));
                    }
                }
                if (o != null && action != null) {
                    this.rules.put(o, action);
                } else if (o == null && action != null) {
                    for (OS os : oses) {
                        if (this.rules.get(Utils.getPlatform()) == LibraryRule.NOSET) {
                            this.rules.put(os, action);
                        }
                    }
                }
            }
        }
        if (lib.has("natives")) {
            JSONObject natives = lib.getJSONObject("natives");
            String osArch = Utils.getOSArch() == OSArch.NEW ? "64" : "32";
            Iterator it = natives.keys();
            while (it.hasNext()) {
                String index = it.next().toString();
                OS os = OS.valueOf(index.toUpperCase(Locale.ENGLISH));
                String value = natives.getString(index).replace("${arch}", osArch);
                this.natives.put(os, value);
            }
        }
        relativePath = new File("libraries" + File.separator + Utils.getArtifactPath(name, "jar"));
        if (isNative()) {
            relativeNativePath = new File("libraries" + File.separator + Utils.getArtifactPath(name, "jar").replace(".jar", '-' + getNativeTag() + ".jar"));
        } else {
            relativeNativePath = null;
        }
        if (lib.has("extract")) {
            JSONObject extract = lib.getJSONObject("extract");
            if (extract.has("exclude")) {
                JSONArray exclude = extract.getJSONArray("exclude");
                for (int i = 0; i < exclude.length(); i++) {
                    this.exclude.add(exclude.getString(i));
                }
            }
        }
        if (lib.has("downloads")) {
            JSONObject downloads = lib.getJSONObject("downloads");
            if (downloads.has("artifact")) {
                JSONObject artifact = downloads.getJSONObject("artifact");
                String url = null;
                long size = 0;
                String sha1 = null;
                if (artifact.has("url")) {
                    url = artifact.getString("url");
                }
                if (artifact.has("size")) {
                    size = artifact.getLong("size");
                }
                if (artifact.has("sha1")) {
                    sha1 = artifact.getString("sha1");
                }
                Downloadable d = new Downloadable(url, size, relativePath, sha1, null);
                this.downloads.put("artifact", d);
            }
            if (downloads.has("classifiers")) {
                JSONObject classif = downloads.getJSONObject("classifiers");
                String current = getNativeTag();
                if (current != null) {
                    if (classif.has(current)) {
                        JSONObject download = classif.getJSONObject(current);
                        String url = null;
                        long size = 0;
                        String sha1 = null;
                        if (download.has("url")) {
                            url = download.getString("url");
                        }
                        if (download.has("size")) {
                            size = download.getLong("size");
                        }
                        if (download.has("sha1")) {
                            sha1 = download.getString("sha1");
                        }
                        File relPath = new File(relativePath.toString().replace(".jar", '-' + current + ".jar"));
                        Downloadable d = new Downloadable(url, size, relPath, sha1, null);
                        this.downloads.put("classifier", d);
                    }
                }
            }
        } else {
            if (isCompatible()) {
                if (isNative() && natives.containsKey(Utils.getPlatform())) {
                    String url = "https://libraries.minecraft.net/" + Utils.getArtifactPath(name, "jar").replace(".jar", '-' + getNativeTag() + ".jar");
                    Downloadable d = new Downloadable(url, 0, relativeNativePath, null, null);
                    downloads.put("classifier", d);
                } else if (hasURL()) {
                    String url = this.url + Utils.getArtifactPath(name, "jar");
                    Downloadable d = new Downloadable(url, 0, relativePath, null, null);
                    downloads.put("artifact", d);
                } else {
                    String url = "https://libraries.minecraft.net/" + Utils.getArtifactPath(name, "jar");
                    Downloadable d = new Downloadable(url, 0, relativePath, null, null);
                    downloads.put("artifact", d);
                }
            }
        }
    }

    public List<String> getExtractExclusions() {
        return exclude;
    }

    public File getRelativePath() {
        return relativePath;
    }

    public File getRelativeNativePath() {
        return relativeNativePath;
    }

    public String getName() {
        return name;
    }

    private boolean hasURL() {
        return url != null;
    }

    private boolean hasDownloads() {
        return !downloads.isEmpty();
    }

    public boolean hasArtifactDownload() {
        return hasDownloads() && downloads.containsKey("artifact");
    }

    public Downloadable getArtifactDownload() {
        return downloads.get("artifact");
    }

    public boolean hasClassifierDownload() {
        return hasDownloads() && downloads.containsKey("classifier");
    }

    public Downloadable getClassifierDownload() {
        return downloads.get("classifier");
    }

    public boolean isCompatible() {
        if (!rules.isEmpty()) {
            if (rules.containsKey(Utils.getPlatform())) {
                return rules.get(Utils.getPlatform()) != LibraryRule.DISALLOW && rules.get(Utils.getPlatform()) != LibraryRule.NOSET;
            }
        }
        return true;
    }

    public boolean isNative() {
        return !natives.isEmpty();
    }

    private String getNativeTag() {
        if (natives.containsKey(Utils.getPlatform())) {
            return natives.get(Utils.getPlatform());
        }
        return null;
    }
}
