package kml.game.version;

import kml.Console;
import kml.Kernel;
import kml.utils.Utils;
import kml.game.download.Downloadable;
import kml.game.version.asset.AssetIndex;
import kml.game.version.library.Library;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public final class Version {

    private final String id;
    private final VersionType type;
    private final Map<String, Downloadable> downloads = new HashMap<>();
    private final List<Library> libraries = new ArrayList<>();
    private final String jsonURL;
    private String mainClass, minecraftArguments, jar;
    private AssetIndex assetIndex;
    private final File relativeJar;
    private final File relativeJSON;

    public Version(String durl, Kernel k) throws Exception {
        Console console = k.getConsole();
        jsonURL = durl;
        console.print("Getting version info from " + durl);
        JSONObject version = new JSONObject(Utils.readURL(durl));
        if (version.has("id")) {
            id = version.getString("id");
        } else {
            throw new Exception("Invalid version id.");
        }
        if (version.has("type")) {
            type = VersionType.valueOf(version.getString("type").toUpperCase(Locale.ENGLISH));
        } else {
            type = VersionType.RELEASE;
            console.print("Remote version " + id + " has no version type. Will be loaded as a RELEASE.");
        }
        if (version.has("mainClass")) {
            mainClass = version.getString("mainClass");
        }
        if (version.has("minecraftArguments")) {
            minecraftArguments = version.getString("minecraftArguments");
        } else if (version.has("arguments")) {
            JSONObject arguments = version.getJSONObject("arguments");
            JSONArray game = arguments.has("game") ? arguments.getJSONArray("game") : null;
            if (game != null) {
                StringBuilder b = new StringBuilder();
                for (int i = 0; i < game.length(); i++){
                    Object element = game.get(i);
                    if (element instanceof String) {
                        b.append(element).append(" ");
                    }
                }
                minecraftArguments = b.toString().trim();
            }
        }
        if (version.has("assetIndex")) {
            JSONObject aIndex = version.getJSONObject("assetIndex");
            String id = null;
            String url = null;
            String sha1 = null;
            if (aIndex.has("id")) {
                id = aIndex.getString("id");
            }
            if (aIndex.has("url")) {
                url = aIndex.getString("url");
            }
            if (aIndex.has("sha1")) {
                sha1 = aIndex.getString("sha1");
            }
            assetIndex = new AssetIndex(id, url, sha1);
        }
        if (assetIndex == null) {
            if (version.has("assets")) {
                assetIndex = new AssetIndex(version.getString("assets"));
            }
        }
        if (version.has("jar")) {
            jar = version.getString("jar");
        }
        if (version.has("downloads")) {
            JSONObject downloads = version.getJSONObject("downloads");
            if (downloads.has("client")) {
                JSONObject client = downloads.getJSONObject("client");
                String url = null;
                long size = 0;
                String sha1 = null;
                if (client.has("url")) {
                    url = client.getString("url");
                }
                if (client.has("size")) {
                    size = client.getLong("size");
                }
                if (client.has("sha1")) {
                    sha1 = client.getString("sha1");
                }
                File path = new File("versions" + File.separator + id + File.separator + id + ".jar");
                Downloadable d = new Downloadable(url, size, path, sha1, null);
                this.downloads.put("client", d);
            }
        }
        if (version.has("libraries")) {
            JSONArray libraries = version.getJSONArray("libraries");
            for (int i = 0; i < libraries.length(); i++) {
                Library lib = new Library(libraries.getJSONObject(i));
                this.libraries.add(lib);
            }
        }
        if (version.has("inheritsFrom")) {
            String inheritsFrom = version.getString("inheritsFrom");
            Version ver = k.getVersions().getVersion(k.getVersions().getVersionMeta(inheritsFrom));
            if (ver != null) {
                if (ver.hasLibraries()) {
                    for (Library lib : ver.libraries) {
                        if (!libraries.contains(lib)) {
                            libraries.add(lib);
                        }
                    }
                }
                if (ver.hasDownloads()) {
                    if (!downloads.containsKey("client")) {
                        downloads.put("client", ver.downloads.get("client"));
                    }
                }
                if (ver.jar != null) {
                    if (jar == null) {
                        jar = ver.jar;
                    }
                }
                if (ver.assetIndex != null) {
                    if (assetIndex == null) {
                        assetIndex = ver.assetIndex;
                    }
                }
                if (ver.hasMainClass()) {
                    if (!hasMainClass()) {
                        mainClass = ver.mainClass;
                    }
                }
                if (ver.hasMinecraftArguments()) {
                    if (!hasMinecraftArguments()) {
                        minecraftArguments = ver.minecraftArguments;
                    }
                }
            }
        }
        if (assetIndex == null) {
            assetIndex = new AssetIndex(null);
        }
        String idToUse = id;
        if (jar != null) {
            idToUse = jar;
        }
        relativeJar = new File("versions" + File.separator + idToUse + File.separator + idToUse + ".jar");
        relativeJSON = new File("versions" + File.separator + id + File.separator + id + ".json");
    }

    public File getRelativeJar() {
        return relativeJar;
    }

    public File getRelativeJSON() {
        return relativeJSON;
    }

    public String getJSONURL() {
        return jsonURL;
    }

    public String getID() {
        return id;
    }

    public VersionType getType() {
        return type;
    }

    private boolean hasMinecraftArguments() {
        return minecraftArguments != null;
    }

    private boolean hasMainClass() {
        return mainClass != null;
    }

    public String getMinecraftArguments() {
        return minecraftArguments;
    }

    public String getMainClass() {
        return mainClass;
    }

    private boolean hasLibraries() {
        return !libraries.isEmpty();
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    public AssetIndex getAssetIndex() {
        return assetIndex;
    }

    private boolean hasDownloads() {
        return !downloads.isEmpty();
    }

    public Map<String, Downloadable> getDownloads() {
        return downloads;
    }

    public boolean hasJar() {
        return jar != null;
    }

    public String getJar() {
        return jar;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Version && id.equalsIgnoreCase(((Version) obj).id);
    }

    @Override
    public String toString() {
        return id;
    }
}