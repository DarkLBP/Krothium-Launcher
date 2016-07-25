package kmlk;

import kmlk.enums.VersionOrigin;
import kmlk.objects.AssetIndex;
import kmlk.objects.Native;
import kmlk.objects.Version;
import kmlk.objects.Library;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import kmlk.exceptions.DownloaderException;
import kmlk.objects.Downloadable;
import kmlk.objects.Profile;
import org.json.JSONObject;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class Downloader {
    private final Console console;
    private final Kernel kernel;
    private long downloaded = 0;
    private long validated = 0;
    private long total = 0;
    private boolean downloading = false;
    public Downloader(Kernel k){
        this.kernel = k;
        this.console = k.getConsole();
    }
    public void download() throws DownloaderException{
        this.downloading = true;
        Profile p = this.kernel.getProfile(this.kernel.getSelectedProfile());
        Version v;
        Version r;
        JSONObject v_meta;
        JSONObject r_meta;
        if (p.hasVersion()){
            v = p.getVersion();
            if (!v.isPrepared()){
                v.prepare();
            }
            if (v.hasRoot()){
                r = v.getRoot();
                if (!r.isPrepared()){
                    r.prepare();
                }
            } else {
                r = v;
            }
        } else {
            v = kernel.getLatestVersion();
            v.prepare();
            r = v;
        }
        if (!v.hasMeta()){
            this.downloading = false;
            throw new DownloaderException("Failed to fetch meta from version " + v.getID());
        }
        if (!r.hasMeta()){
            this.downloading = false;
            throw new DownloaderException("Failed to fetch meta from version " + r.getID());
        }
        v_meta = v.getMeta();
        r_meta = r.getMeta();
        ExecutorService pool = Executors.newFixedThreadPool(5);
        List<Downloadable> urls = new ArrayList();
        this.downloaded = 0;
        this.validated = 0;
        this.total = 0;
        console.printInfo("Fetching asset urls..");
        if (r.hasAssetIndex()){
            AssetIndex index = r.getAssetIndex();
            String assetID = index.getID();
            long assetFileLength = index.getSize();
            boolean localIndex = false;
            File indexJSON = new File(kernel.getWorkingDir() + File.separator + index.getJSONFile());
            URL assetsURL = null;
            if (indexJSON.exists() && indexJSON.isFile()){
                if (indexJSON.length() == assetFileLength){
                    try {
                        assetsURL = indexJSON.toURI().toURL();
                    } catch (MalformedURLException ex) {
                        this.downloading = false;
                        throw new DownloaderException("Invalid asset index json file path.");
                    }
                    localIndex = true;
                } 
            }
            assetsURL = (assetsURL == null) ? index.getURL() : assetsURL;
            if (!localIndex){
                int tries = 0;
                while (!Utils.downloadFile(assetsURL, indexJSON) && (tries < Constants.DOWNLOAD_TRIES)){
                    tries++;
                }
                if (tries == Constants.DOWNLOAD_TRIES){
                    console.printError("Failed to download asset index for version " + assetID);
                }
            }
            JSONObject root;
            try {
                root = new JSONObject(new String(Files.readAllBytes(indexJSON.toPath()), "ISO-8859-1"));
            } catch (UnsupportedEncodingException ex) {
                this.downloading = false;
                throw new DownloaderException("Your computer does not support ISO-8859-1 encoding.");
            } catch (IOException ex) {
                this.downloading = false;
                throw new DownloaderException("Failed to read asset index json file.");
            }
            JSONObject objects = root.getJSONObject("objects");
            Set keys = objects.keySet();
            Iterator it2 = keys.iterator();
            List<String> processedHashes = new ArrayList();
            while (it2.hasNext()){
                String object = it2.next().toString();
                JSONObject o = objects.getJSONObject(object);
                String hash = o.getString("hash");
                long size = o.getLong("size");
                URL downloadURL = Utils.stringToURL(Constants.RESOURCES_URL + hash.substring(0,2) + "/" + hash);
                File destPath = new File(kernel.getWorkingDir() + File.separator + "assets" + File.separator + "objects" + File.separator + hash.substring(0,2) + File.separator + hash);
                boolean localValid = false;
                if (destPath.exists() && destPath.isFile()){
                    if (destPath.length() == size){
                        localValid = true;
                    }
                }
                this.total += size;
                if (!localValid){
                    Downloadable d = new Downloadable(downloadURL, size, destPath, hash);
                    urls.add(d);
                }else{
                    console.printInfo("Asset file " + object + " found locally and it is valid.");
                    if (!processedHashes.contains(hash)){
                        processedHashes.add(hash);
                        validated += size;
                    } else {
                        this.total -= size;
                    }
                }
            }
        } else {
            this.downloading = false;
            throw new DownloaderException("Version " + r.getID() + " does not have AssetIndex.");
        }
        console.printInfo("Fetching version urls..");
        JSONObject client = r_meta.getJSONObject("downloads").getJSONObject("client");
        URL jarURL = Utils.stringToURL(client.getString("url"));
        String jarSHA1 = client.getString("sha1");
        long jarSize = client.getLong("size");
        total += jarSize;
        File destPath = new File(kernel.getWorkingDir() + File.separator + "versions" + File.separator + r.getID() + File.separator + r.getID() + ".jar");
        boolean localValid = false;
        File jsonFile = new File(kernel.getWorkingDir() + File.separator + "versions" + File.separator + r.getID() + File.separator + r.getID() + ".json");
        boolean JSONValid = false;
        if (jsonFile.exists() && jsonFile.isFile()){
            try {
                if (r.getOrigin() != VersionOrigin.LOCAL){
                    if (jsonFile.length() == r.getURL(VersionOrigin.REMOTE).openConnection().getContentLength()){
                        JSONValid = true;
                    }
                }else{
                    JSONValid = true;
                }
            } catch (IOException ex) {
                console.printError("Falied to verify existing JSON integrity.");
            }
        }
        if (!JSONValid){
            int tries = 0;
            while (!Utils.downloadFile(r.getURL(VersionOrigin.REMOTE), jsonFile) && (tries < Constants.DOWNLOAD_TRIES)){
                tries++;
            }
            if (tries == Constants.DOWNLOAD_TRIES){
                console.printError("Failed to download version index " + destPath.getName());
            }
        }else{
            console.printInfo("Version " + r.getID() + " JSON file found locally and it is valid.");
        }
        if (destPath.exists() && destPath.isFile()){
            if (destPath.length() == jarSize && Utils.verifyChecksum(destPath, jarSHA1)){
                localValid = true;
            }
        }
        if (!localValid){
            Downloadable d = new Downloadable(jarURL, jarSize, destPath, jarSHA1);
            urls.add(d);
        }else{
            console.printInfo("Version file " + destPath.getName() + " found locally and it is valid.");
            validated += jarSize;
        }
        console.printInfo("Fetching library urls..");
        Version tmp = v;
        while (tmp != null){
            if (tmp.hasLibraries()){
                Map<String, Library> libs = tmp.getLibraries();
                Set set = libs.keySet();
                Iterator it = set.iterator();
                while (it.hasNext()){
                    String lib_name = it.next().toString();
                    Library lib = libs.get(lib_name);
                    if (lib.isDownloadable()){
                        String sha1 = lib.getSHA1();
                        File libPath = new File(kernel.getWorkingDir() + File.separator + lib.getPath());
                        localValid = false;
                        if (libPath.exists() && libPath.isFile()){
                            if (lib.isLegacy()){
                                try{
                                    String urlRaw = lib.getURL().toString();
                                    int responseCode = -1;
                                    int contentLength = -1;
                                    if (urlRaw.startsWith("https")){
                                        HttpsURLConnection con = (HttpsURLConnection)lib.getURL().openConnection();
                                        con.connect();
                                        responseCode = con.getResponseCode();
                                        contentLength = con.getContentLength();
                                    }else if (urlRaw.startsWith("http")){
                                        HttpURLConnection con = (HttpURLConnection)lib.getURL().openConnection();
                                        con.connect();
                                        responseCode = con.getResponseCode();
                                        contentLength = con.getContentLength();
                                    }else{
                                        console.printError("Unsupported protocol type in " + urlRaw);
                                    }
                                    if (responseCode == 200){
                                        if (libPath.length() == contentLength){
                                            localValid = true;
                                        }
                                    }else if (responseCode == 404){
                                        localValid = true;
                                        console.printInfo("Library not found remotelly so let's say it's valid.");
                                    }
                                }catch (Exception ex){
                                    localValid = false;
                                }
                            }else{
                                if (libPath.length() == lib.getSize() && Utils.verifyChecksum(libPath, sha1)){
                                    localValid = true;
                                }
                            }
                        }
                        total += lib.getSize();
                        if (!localValid){
                            Downloadable d = new Downloadable(lib.getURL(), lib.getSize(), libPath, sha1);
                            urls.add(d);
                        }else{
                            console.printInfo("Library file " + libPath.getName() + " found locally and it is valid.");
                            validated += lib.getSize();
                        }
                    }else{
                        console.printInfo("Library " + lib_name + " is not downloadable.");
                    }
                }
            }else{
                console.printInfo("Version " + tmp.getID() + " has no libraries.");
            }
            if (tmp.hasInheritedVersion()){
                tmp = tmp.getInheritedVersion();
            }else{
                tmp = null;
            }
        }
        console.printInfo("Fetching natives urls..");
        tmp = v;
        while (tmp != null){
            if (tmp.hasNatives()){
                Map<String, Native> natives = tmp.getNatives();
                Set set = natives.keySet();
                Iterator it = set.iterator();
                while (it.hasNext()){
                    String nat_name = it.next().toString();
                    Native nat = natives.get(nat_name);
                    if (nat.isDownloadable()){
                        String sha1 = nat.getSHA1();
                        File natPath = new File(kernel.getWorkingDir() + File.separator + nat.getPath());
                        localValid = false;
                        if (natPath.exists() && natPath.isFile()){
                            if (natPath.length() == nat.getSize() && Utils.verifyChecksum(natPath, sha1)){
                                localValid = true;
                            }
                        }
                        total += nat.getSize();
                        if (!localValid){
                            Downloadable d = new Downloadable(nat.getURL(), nat.getSize(), natPath, sha1);
                            urls.add(d);
                        }else{
                            console.printInfo("Native file " + natPath.getName() + " found locally and it is valid.");
                            validated += nat.getSize();
                        }
                    }else{
                        console.printInfo("Native " + nat_name + " is not downloadable.");
                    }
                }
            }else{
                console.printInfo("Version " + tmp.getID() + " has no natives.");
            }
            if (tmp.hasInheritedVersion()){
                tmp = tmp.getInheritedVersion();
            }else{
                tmp = null;
            }
        }
        console.printInfo("Downloading required game files...");
        if (urls.isEmpty()){
            console.printInfo("Nothing to download.");
        } else {
            for (Downloadable d : urls){
                Runnable thread = new Runnable(){
                    @Override
                    public void run(){
                        File path = d.getPath();
                        URL url = d.getURL();
                        int tries = 0; 
                        console.printInfo("Downloading " + path.getName() + "...");
                        while (!Utils.downloadFile(url, path) && (tries < Constants.DOWNLOAD_TRIES)){ 
                           tries++; 
                        } 
                        if (tries == Constants.DOWNLOAD_TRIES){ 
                            console.printError("Failed to download file: " + path.getName()); 
                        }
                        downloaded += d.getSize();
                    }
                };
                pool.execute(thread);                
            }
            pool.shutdown();
            try{
                pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException ex){
                this.downloading = false;
                throw new DownloaderException("Thread pool unexceptedly closed.");
            }
        }
        this.downloading = false;
    }
    public int getProgress(){
        if (!isDownloading()){
            return 0;
        }
        long sum = this.downloaded + this.validated;
        return (int)((float)(sum)/this.total*100);
    }
    public boolean isDownloading(){return this.downloading;}
}