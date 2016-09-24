package kml;

import kml.enums.VersionType;
import kml.exceptions.ObjectException;
import kml.objects.Version;
import kml.objects.VersionMeta;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public class Versions {
    private final Map<String, VersionMeta> versions = new LinkedHashMap();
    private final Map<String, Version> version_cache = new HashMap();
    private final Console console;
    private String latestSnap;
    private String latestRel;
    private String latestBeta;
    private String latestAlpha;
    private final Kernel kernel;
    public Versions(Kernel k){
        this.kernel = k;
        this.console = k.getConsole();
    }
    public void add(String name, VersionMeta m){
        if (!versions.containsKey(name)){
            versions.put(name, m);
            console.printInfo("Version " + name + " loaded.");
        }
    }
    public Version getVersion(String id){
        if (this.versions.containsKey(id)){
            if (this.version_cache.containsKey(id)){
                return this.version_cache.get(id);
            }
            VersionMeta vm = this.versions.get(id);
            if (!vm.hasURL()){
                console.printError("Version meta from version " + id + " is incomplete.");
                return null;
            }
            try{
                Version v = new Version(vm.getURL(), kernel);
                this.version_cache.put(id, v);
                return v;
            } catch (ObjectException ex){
                console.printError(ex.getMessage());
                return null;
            }
        }
        console.printError("Version id " + id + " not found.");
        return null;
    }
    public VersionMeta getVersionMeta(String id){
        if (this.versions.containsKey(id)){
            return this.versions.get(id);
        }
        return null;
    }
    public void fetchVersions(){
        console.printInfo("Fetching remote version list.");
        try {
            JSONObject root = new JSONObject(Utils.readURL(Constants.VERSION_MANIFEST_FILE));
            if (root.has("latest")){
                JSONObject latest = root.getJSONObject("latest");
                if (latest.has("snapshot")){
                    this.latestSnap = latest.getString("snapshot");
                }
                if (latest.has("release")){
                    this.latestRel = latest.getString("release");
                }
            }
            JSONArray vers = root.getJSONArray("versions");
            boolean last_rel = (this.latestRel != null);
            boolean last_snap = (this.latestSnap != null);
            boolean last_beta = false;
            boolean last_alpha = false;
            for (int i = 0; i < vers.length(); i++){
                JSONObject ver = vers.getJSONObject(i);
                String id = null;
                VersionType type = null;
                URL url = null;
                if (ver.has("id")){
                    id = ver.getString("id");
                }
                if (ver.has("type")){
                    type = VersionType.valueOf(ver.getString("type").toUpperCase());
                } else {
                    type = VersionType.RELEASE;
                    console.printError("Remote version " + id + " has no version type. Will be loaded as a RELEASE.");
                }
                if (ver.has("url")){
                    url = Utils.stringToURL(ver.getString("url"));
                }
                if (id == null || type == null || url == null){
                    continue;
                }
                VersionMeta vm = new VersionMeta(id, url, type);
                this.add(id, vm);
                if (!last_rel && type.equals(VersionType.RELEASE)){
                    this.latestRel = id;
                    last_rel = true;
                }
                if (!last_snap && type.equals(VersionType.SNAPSHOT)){
                    this.latestSnap = id;
                    last_snap = true;
                }
                if (!last_beta && type.equals(VersionType.OLD_BETA)){
                    this.latestBeta = id;
                    last_beta = true;
                }
                if (!last_alpha && type.equals(VersionType.OLD_ALPHA)){
                    this.latestAlpha = id;
                    last_alpha = true;
                }
            }
            console.printInfo("Remote version list loaded.");
        } catch (Exception ex) {
            console.printError("Failed to fetch remote version list.");
        }
        console.printInfo("Fetching local version list versions.");
        try{
            File versionsDir = new File(kernel.getWorkingDir() + File.separator + "versions");
            if (versionsDir.exists()){
                if (versionsDir.isDirectory()){
                    File[] files = versionsDir.listFiles();
                    for (File file : files){
                        if (file.isDirectory()){
                            File jsonFile = new File(file.getAbsolutePath() + File.separator + file.getName() + ".json");
                            if (jsonFile.exists()){
                                String id = file.getName();
                                URL url = jsonFile.toURI().toURL();
                                VersionType type;
                                JSONObject ver = new JSONObject(Utils.readURL(url));
                                if (ver.has("type")){
                                    type = VersionType.valueOf(ver.getString("type").toUpperCase());
                                } else {
                                    type = VersionType.RELEASE;
                                    console.printError("Local version " + id + " has no version type. Will be loaded as a RELEASE.");
                                }
                                VersionMeta vm = new VersionMeta(id, url, type);
                                if (!this.versions.containsKey(id)){
                                    this.add(id, vm);
                                }
                            }
                        }
                    }
                }
            }
            console.printInfo("Local version list loaded.");
        }catch (Exception ex){
            console.printError("Failed to fetch local version list.");
        }
    }
    public LinkedHashSet<String> getVersionsByType(VersionType t){
        LinkedHashSet<String> vers = new LinkedHashSet();
        Set keys = this.versions.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()){
            String verName = it.next().toString();
            VersionMeta vm = this.versions.get(verName);
            if (vm.hasType()){
                VersionType type = vm.getType();
                if (type.equals(t)){
                    vers.add(verName);
                }
            }
        }
        return vers;
    }
    public LinkedHashSet<String> getVersions(){
       LinkedHashSet<String> vers = new LinkedHashSet();
        Set keys = this.versions.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()){
            String verName = it.next().toString();
            vers.add(verName);
        }
        return vers; 
    }
    public String getLatestRelease(){return latestRel;}
    public String getLatestSnapshot(){return latestSnap;}
    public String getLatestBeta(){return latestBeta;}
    public Version getLatestAlpha(){return this.getVersion(latestAlpha);}
    public int versionCount(){return versions.size();}
}
