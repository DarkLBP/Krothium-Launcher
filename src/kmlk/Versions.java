package kmlk;

import kmlk.enums.VersionOrigin;
import kmlk.enums.VersionType;
import kmlk.objects.Version;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class Versions {
    private final Map<String, Version> versions = new HashMap();
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
    public Map<String, Version> getVersions(){return this.versions;};
    public void add(String name, Version v){
        if (!versions.containsKey(name)){
            versions.put(name, v);
            console.printInfo("Version " + name + " loaded.");
        }else{
            versions.put(name, v);
            console.printInfo("Version " + name + " updated!.");
        }
    }
    public void fetchVersions(){
        console.printInfo("Fetching remote version list.");
        try {
            StringBuilder content = new StringBuilder();
            URLConnection con = Constants.JSON_FILE.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null){
              content.append(line).append("\n");
            }
            bufferedReader.close();
            JSONObject root = new JSONObject(content.toString());
            JSONObject latest = root.getJSONObject("latest");
            this.latestSnap = latest.getString("snapshot");
            this.latestRel = latest.getString("release");
            JSONArray vers = root.getJSONArray("versions");
            boolean last_beta = false;
            boolean last_alpha = false;
            for (int i = 0; i < vers.length(); i++){
                JSONObject ver = vers.getJSONObject(i);
                Version v = new Version(ver.getString("id"), VersionType.valueOf(ver.getString("type").toUpperCase()), VersionOrigin.REMOTE, Utils.stringToURL(ver.getString("url")), kernel);
                if (!last_beta && v.getType() == VersionType.OLD_BETA){
                    this.latestBeta = v.getID();
                    last_beta = true;
                }
                if (!last_alpha && v.getType() == VersionType.OLD_ALPHA){
                    this.latestAlpha = v.getID();
                    last_alpha = true;
                }
                this.add(ver.getString("id"), v);
            }
            console.printInfo("Remote version list loaded.");
        } catch (Exception ex) {
            console.printError("Failed to fetch remote version list.");
        }
        console.printInfo("Fetching local version list and inherited versions.");
        try
        {
            File versionsDir = new File(kernel.getWorkingDir() + File.separator + "versions");
            if (versionsDir.exists()){
                if (versionsDir.isDirectory()){
                    File[] files = versionsDir.listFiles();
                    for (File file : files){
                        if (file.isDirectory()){
                            File jsonFile = new File(file.getAbsolutePath() + File.separator + file.getName() + ".json");
                            if (jsonFile.exists()){
                                if (this.versions.containsKey(file.getName())){
                                    Version v = this.versions.get(file.getName());
                                    v.putURL(jsonFile.toURI().toURL(), VersionOrigin.LOCAL);
                                    this.add(file.getName(), v);
                                }else{
                                    JSONObject json = new JSONObject(Utils.readURL(jsonFile.toURI().toURL()));
                                    Version ver;
                                    if (this.versions.containsKey(file.getName())){
                                        ver = new Version(file.getName(), VersionType.valueOf(json.getString("type").toUpperCase()), VersionOrigin.BOTH, jsonFile.toURI().toURL(), kernel);
                                    }else{
                                        ver = new Version(file.getName(), VersionType.valueOf(json.getString("type").toUpperCase()), VersionOrigin.LOCAL, jsonFile.toURI().toURL(), kernel);
                                    }
                                    this.add(file.getName(), ver);
                                }
                            }
                        }
                    }
                }else{
                    versionsDir.mkdirs();
                }
            }else{
                versionsDir.mkdirs();
            }
            console.printInfo("Local version list loaded.");
        }
        catch (Exception ex)
        {
            console.printError("Failed to fetch local version list.");
        }
    }
    public Version getVersionByName(String verName){
        if (versions.containsKey(verName)){
            return versions.get(verName);
        }
        return null;
    }
    public Map<String, Version> getVersionsByType(VersionType type){
        Map<String, Version> vers = new HashMap();
        Set keys = this.versions.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()){
            String verName = it.next().toString();
            Version v = this.versions.get(verName);
            if (v.getType() == type){
                vers.put(verName, v);
            }
        }
        return vers;
    }
    public Map<String, Version> getVersionsByOrigin(VersionOrigin orig){
        Map<String, Version> vers = new HashMap();
        Set keys = this.versions.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()){
            String verName = it.next().toString();
            Version v = this.versions.get(verName);
            if (v.getOrigin() == orig){
                vers.put(verName, v);
            }
        }
        return vers;
    }
    public Version getLatestRelease(){return versions.get(latestRel);}
    public Version getLatestSnapshot(){return versions.get(latestSnap);}
    public Version getLatestBeta(){return versions.get(latestBeta);}
    public Version getLatestAlpha(){return versions.get(latestAlpha);}
    public int versionCount(){return versions.size();}
}
