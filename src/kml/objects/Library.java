package kml.objects;

import kml.Console;
import kml.Kernel;
import kml.Utils;
import kml.enums.LibraryRule;
import kml.enums.OS;
import kml.enums.OSArch;
import kml.exceptions.ObjectException;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public final class Library {
    private final String name;
    private final URL url;
    private final Map<String, Downloadable> downloads = new HashMap<>();
    private final Map<OS, LibraryRule> rules = new HashMap<>();
    private final File relativePath;
    private final File relativeNativePath;
    private final List<String> exclude = new ArrayList<>();
    private final Map<OS, String> natives = new HashMap<>();
    
    public Library(JSONObject lib, Kernel k) throws ObjectException{
        final Kernel kernel = k;
        final Console console = k.getConsole();
        if (lib.has("name")){
            this.name = lib.getString("name");
        } else {
           throw new ObjectException("Invalid name for a library."); 
        }
        if (lib.has("url")){
            this.url = Utils.stringToURL(lib.getString("url"));
        } else {
            this.url = null;
        }
        if (lib.has("rules")){
            JSONArray rules = lib.getJSONArray("rules");
            OS[] oses = OS.values();
            for (OS os : oses){
                this.rules.put(os, LibraryRule.NOSET);
            }
            for (int i = 0; i < rules.length(); i++){
                JSONObject rule = rules.getJSONObject(i);
                LibraryRule action = null;
                OS o = null;
                if (rule.has("action")){
                    action = LibraryRule.valueOf(rule.getString("action").toUpperCase());
                }
                if (rule.has("os")){
                    JSONObject os = rule.getJSONObject("os");
                    if (os.has("name")){
                        o = OS.valueOf(os.getString("name").toUpperCase());
                    }
                }
                if (o != null && action != null){
                    this.rules.put(o, action);
                } else if (o == null && action != null) {
                    for (OS os : oses){
                        if (this.rules.get(Utils.getPlatform()).equals(LibraryRule.NOSET)){
                            this.rules.put(os, action);
                        }
                    }
                }
            }
        }
        if (lib.has("natives")){
            JSONObject natives = lib.getJSONObject("natives");
            String osArch = (Utils.getOSArch().equals(OSArch.NEW) ? "64" : "32");
            Iterator it = natives.keys();
            while (it.hasNext()){
                String index = it.next().toString();
                OS os = OS.valueOf(index.toUpperCase());
                String value = natives.getString(index).replace("${arch}", osArch);
                this.natives.put(os, value);
            }
        }
        this.relativePath = new File("libraries" + File.separator + Utils.getArtifactPath(this.name, "jar"));
        if (this.isNative()){
            this.relativeNativePath = new File("libraries" + File.separator + Utils.getArtifactPath(this.name, "jar").replace(".jar", "-" + this.getNativeTag() + ".jar"));
        } else {
            this.relativeNativePath = null;
        }
        if (lib.has("extract")){
            JSONObject extract = lib.getJSONObject("extract");
            if (extract.has("exclude")){
                JSONArray exclude = extract.getJSONArray("exclude");
                for (int i = 0; i < exclude.length(); i++){
                    this.exclude.add(exclude.getString(i));
                }
            }
        }
        if (lib.has("downloads")){
            JSONObject downloads = lib.getJSONObject("downloads");
            if (downloads.has("artifact")){
                JSONObject artifact = downloads.getJSONObject("artifact");
                URL url = null;
                long size = 0;
                String sha1 = null;
                if (artifact.has("url")){
                    url = Utils.stringToURL(artifact.getString("url"));
                }
                if (artifact.has("size")){
                    size = artifact.getLong("size");
                }
                if (artifact.has("sha1")){
                    sha1 = artifact.getString("sha1");
                }
                Downloadable d = new Downloadable(url, size, this.relativePath, sha1);
                this.downloads.put("artifact", d);
            }
            if (downloads.has("classifiers")){
                JSONObject classif = downloads.getJSONObject("classifiers");
                String current = this.getNativeTag();
                if (current != null){
                    if (classif.has(current)){
                        JSONObject download = classif.getJSONObject(current);
                        URL url = null;
                        long size = 0;
                        String sha1 = null;
                        if (download.has("url")){
                            url = Utils.stringToURL(download.getString("url"));
                        }
                        if (download.has("size")){
                            size = download.getLong("size");
                        }
                        if (download.has("sha1")){
                            sha1 = download.getString("sha1");
                        }
                        File relPath = new File(this.relativePath.toString().replace(".jar", "-" + current + ".jar"));
                        Downloadable d = new Downloadable(url, size, relPath, sha1);
                        this.downloads.put("classifier", d);
                    }
                }
            }
        } else {
            if (this.isCompatible()){
                if (this.hasURL()){
                    try {
                        URL url = new URL(this.url + Utils.getArtifactPath(this.name, "jar"));
                        String urlRaw = url.toString();
                        int responseCode = -1;
                        int contentLength = -1;
                        if (urlRaw.startsWith("https")){
                            HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
                            con.setConnectTimeout(2500);
                            con.connect();
                            responseCode = con.getResponseCode();
                            contentLength = con.getContentLength();
                        }else if (urlRaw.startsWith("http")){
                            HttpURLConnection con = (HttpURLConnection)url.openConnection();
                            con.setConnectTimeout(2500);
                            con.connect();
                            responseCode = con.getResponseCode();
                            contentLength = con.getContentLength();
                        }else{
                            console.printError("Unsupported protocol type in " + urlRaw);
                        }
                        if (responseCode == 200){
                            Downloadable d = new Downloadable(url, contentLength, this.relativePath, null);
                            this.downloads.put("artifact", d);
                        }
                    } catch (MalformedURLException ex) {
                       console.printError("Invalid " + this.name + " url.");
                    } catch (IOException ex) {
                        console.printError("Failed to establish connection in library " + this.name);
                    }
                } else {
                    try {
                        URL url = new URL("https://libraries.minecraft.net/" + Utils.getArtifactPath(this.name, "jar"));
                        HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
                        con.connect();
                        int responseCode = con.getResponseCode();
                        int contentLength = con.getContentLength();
                        if (responseCode == 200){
                            Downloadable d = new Downloadable(url, contentLength, this.relativePath, null);
                            this.downloads.put("artifact", d);
                        }
                    } catch (MalformedURLException ex) {
                        console.printError("Invalid " + this.name + " url.");
                    } catch (IOException ex) {
                        console.printError("Failed to establish connection in library " + this.name);
                    }
                }
                if (this.isNative()){
                    if (this.natives.containsKey(Utils.getPlatform())){
                        try{
                            URL url = new URL("https://libraries.minecraft.net/" + Utils.getArtifactPath(this.name, "jar").replace(".jar", "-" + this.getNativeTag() + ".jar"));
                            HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
                            con.connect();
                            int responseCode = con.getResponseCode();
                            int contentLength = con.getContentLength();
                            if (responseCode == 200){
                                Downloadable d = new Downloadable(url, contentLength, this.relativePath, null);
                                this.downloads.put("classifier", d);
                            }
                        } catch (MalformedURLException ex) {
                            console.printError("Invalid " + this.name + " url.");
                        } catch (IOException ex) {
                            console.printError("Failed to establish connection in library " + this.name);
                        }
                    }
                }
            }
        }
    }
    public boolean hasExtractExclusions(){return (this.exclude.size() > 0);}
    public List<String> getExtractExclusions(){return this.exclude;}
    public File getRelativePath(){return this.relativePath;}
    public File getRelativeNativePath(){return this.relativeNativePath;}
    public String getName(){return this.name;}
    private boolean hasURL(){return (this.url != null);}
    public URL getURL(){return this.url;}
    private boolean hasDownloads(){return (this.downloads.size() > 0);}
    public boolean hasArtifactDownload(){
        if (!this.hasDownloads()){
            return false;
        }
        return this.downloads.containsKey("artifact");
    }
    public Downloadable getArtifactDownload(){
        return this.downloads.get("artifact");
    }
    public boolean hasClassifierDownload(){
        if (!this.hasDownloads()){
            return false;
        }
        return this.downloads.containsKey("classifier");
    }
    public Downloadable getClassifierDownload(){
        return this.downloads.get("classifier");
    }
    public boolean isCompatible(){
        if (this.rules.size() > 0){
            if (this.rules.containsKey(Utils.getPlatform())){
                if (this.rules.get(Utils.getPlatform()).equals(LibraryRule.DISALLOW)){
                    return false;
                } else if (this.rules.get(Utils.getPlatform()).equals(LibraryRule.NOSET)){
                    return false;
                }
            }
        }
        return true;
    }
    public boolean isNative(){return (this.natives.size() > 0);}
    private String getNativeTag(){
        if (this.natives.containsKey(Utils.getPlatform())){
            return this.natives.get(Utils.getPlatform());
        }
        return null;
    }
}
