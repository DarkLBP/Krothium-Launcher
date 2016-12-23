package kml.objects;

import kml.Console;
import kml.Kernel;
import kml.Utils;
import kml.enums.VersionType;
import kml.exceptions.ObjectException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public final class Version {

    private final String id;
    private String mainClass;
    private String minecraftArguments;
    private final VersionType type;
    private String assets;
    private AssetIndex assetIndex;
    private final Map<String, Downloadable> downloads = new HashMap<>();
    private final List<Library> libraries = new ArrayList<>();
    private String inheritsFrom;
    private String jar;
    private File relativeJar;
    private File relativeJSON;
    private final URL jsonURL;
    
    public Version(URL durl, Kernel k) throws ObjectException{
        final Kernel kernel = k;
        final Console console = k.getConsole();
        this.jsonURL = durl;
        JSONObject version = new JSONObject(Utils.readURL(durl));
        if (version.has("id")){
            this.id = version.getString("id");
        } else {
            throw new ObjectException("Invalid version id.");
        }
        if (version.has("type")){
            this.type = VersionType.valueOf(version.getString("type").toUpperCase());
        } else {
            this.type = VersionType.RELEASE;
            console.printError("Remote version " + id + " has no version type. Will be loaded as a RELEASE.");
        }
        if (version.has("mainClass")){
            this.mainClass = version.getString("mainClass");
        }
        if (version.has("minecraftArguments")){
            this.minecraftArguments = version.getString("minecraftArguments");
        }
        if (version.has("assets")){
            this.assets = version.getString("assets");
        }
        if (version.has("jar")){
            this.jar = version.getString("jar");
        }
        if (version.has("assetIndex")){
            JSONObject aIndex = version.getJSONObject("assetIndex");
            String id = null;
            long totalSize = -1;
            long size = -1;
            URL url = null;
            String sha1 = null;
            if (aIndex.has("id")){
                id = aIndex.getString("id");
            }
            if (aIndex.has("totalSize")){
                totalSize = aIndex.getLong("totalSize");
            }
            if (aIndex.has("size")){
                size = aIndex.getLong("size");
            }
            if (aIndex.has("url")){
                url = Utils.stringToURL(aIndex.getString("url"));
            }
            if (aIndex.has("sha1")){
                sha1 = aIndex.getString("sha1");
            }
            this.assetIndex = new AssetIndex(id, size, totalSize, url, sha1);
        }
        if (version.has("downloads")){
            JSONObject downloads = version.getJSONObject("downloads");
            if (downloads.has("client")){
                JSONObject client = downloads.getJSONObject("client");
                URL url = null;
                long size = 0;
                String sha1 = null;
                if (client.has("url")){
                    url = Utils.stringToURL(client.getString("url"));
                }
                if (client.has("size")){
                    size = client.getLong("size");
                }
                if (client.has("sha1")){
                    sha1 = client.getString("sha1");
                }
                File path = new File("versions" + File.separator + this.id + File.separator + this.id + ".jar");
                Downloadable d = new Downloadable(url, size, path, sha1);
                this.downloads.put("client", d);
            }
            if (downloads.has("server")){
                JSONObject server = downloads.getJSONObject("server");
                URL url = null;
                long size = 0;
                String sha1 = null;
                if (server.has("url")){
                    url = Utils.stringToURL(server.getString("url"));
                }
                if (server.has("size")){
                    size = server.getLong("size");
                }
                if (server.has("sha1")){
                    sha1 = server.getString("sha1");
                }
                File path = new File("versions" + File.separator + id + File.separator + id + "_server.jar");
                Downloadable d = new Downloadable(url, size, path, sha1);
                this.downloads.put("server", d);
            }
            if (downloads.has("windows_server")){
                JSONObject windows_server = downloads.getJSONObject("windows_server");
                URL url = null;
                long size = 0;
                String sha1 = null;
                if (windows_server.has("url")){
                    url = Utils.stringToURL(windows_server.getString("url"));
                }
                if (windows_server.has("size")){
                    size = windows_server.getLong("size");
                }
                if (windows_server.has("sha1")){
                    sha1 = windows_server.getString("sha1");
                }
                File path = new File("versions" + File.separator + id + File.separator + id + "_server.exe");
                Downloadable d = new Downloadable(url, size, path, sha1);
                this.downloads.put("server", d);
            }
        }
        if (version.has("libraries")){
            JSONArray libraries = version.getJSONArray("libraries");
            for (int i = 0; i < libraries.length(); i++){
                Library lib = new Library(libraries.getJSONObject(i), kernel);
                this.libraries.add(lib);
            }
        }
        if (version.has("inheritsFrom")){
            String inheritsFrom = version.getString("inheritsFrom");
            Version ver = kernel.getVersions().getVersion(inheritsFrom);
            if (ver.hasLibraries()){
                for (Library lib : ver.libraries){
                    if (!this.libraries.contains(lib)){
                        this.libraries.add(lib);
                    }
                }
            }
            if (ver.hasDownloads()){
                Map<String, Downloadable> downloads = ver.downloads;
                if (!this.downloads.containsKey("client")){
                    this.downloads.put("client", downloads.get("client"));
                }
                if (!this.downloads.containsKey("server")){
                    this.downloads.put("server", downloads.get("server"));
                }
                if (!this.downloads.containsKey("windows_server")){
                    this.downloads.put("windows_server", downloads.get("windows_server"));
                }
            }
            if (ver.hasJar()){
                if (!this.hasJar()){
                    this.jar = ver.jar;
                }
            }
            if (ver.hasAssets()){
                if (!this.hasAssets()){
                    this.assets = ver.assets;
                }
            }
            if (ver.hasAssetIndex()){
                if (!this.hasAssetIndex()){
                    this.assetIndex = ver.assetIndex;
                }
            }
            if (ver.hasMainClass()){
                if (!this.hasMainClass()){
                    this.mainClass = ver.mainClass;
                }
            }
            if (ver.hasMinecraftArguments()){
                if (!this.hasMinecraftArguments()){
                    this.minecraftArguments = ver.minecraftArguments;
                }
            }
        }
        String idToUse = this.id;
        if (this.hasJar()){
            idToUse = this.jar;
        }
        this.relativeJar = new File("versions" + File.separator + idToUse + File.separator + idToUse + ".jar");
        this.relativeJSON = new File("versions" + File.separator + idToUse + File.separator + idToUse + ".json");
        if (this.hasClientDownload() && this.hasJar()){
            Downloadable d = this.getClientDownload();
            Downloadable dnew = new Downloadable(d.getURL(), d.getSize(), this.relativeJar, d.getHash());
            this.downloads.remove("client");
            this.downloads.put("client", dnew);
        }
    }
    public File getRelativeJar(){return this.relativeJar;}
    public File getRelativeJSON(){return this.relativeJSON;}
    public URL getJSONURL(){return this.jsonURL;}
    public String getID(){return this.id;}
    public VersionType getType(){return this.type;}
    private boolean hasMinecraftArguments(){return (this.minecraftArguments != null);}
    private boolean hasMainClass(){return (this.mainClass != null);}
    public String getMinecraftArguments(){return this.minecraftArguments;}
    public String getMainClass(){return this.mainClass;}
    public String getInheritedVersion(){return this.inheritsFrom;}
    public boolean hasInheritedVersion(){return (this.inheritsFrom != null);}
    private boolean hasLibraries(){return (this.libraries.size() > 0);}
    public List<Library> getLibraries(){return this.libraries;}
    public boolean hasAssetIndex(){return (this.assetIndex != null);}
    public boolean hasAssets(){return (this.assets != null);}
    public String getAssets(){return this.assets;}
    public AssetIndex getAssetIndex(){return this.assetIndex;}
    private boolean hasDownloads(){return (this.downloads.size() > 0);}
    public Map<String, Downloadable> getDownloads(){return this.downloads;}
    private boolean hasJar(){return (this.jar != null);}
    public String getJar(){return this.jar;}
    private boolean hasClientDownload(){
        if (!this.hasDownloads()){
            return false;
        }
        return this.downloads.containsKey("client");
    }
    public Downloadable getClientDownload(){
        return this.downloads.get("client");
    }
}