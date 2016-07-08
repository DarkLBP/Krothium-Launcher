package kmlk;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
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
import org.json.JSONObject;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class Downloader {
    private final Console console;
    private int progressDownload = 0;
    private int progressValid = 0;
    private long downloaded = 0;
    private long validated = 0;
    public Downloader()
    {
        this.console = Kernel.getKernel().getConsole();
    }
    public void downloadAssets(Version v)
    {
        ExecutorService pool = Executors.newFixedThreadPool(5);
        Version ver = (v.getRoot() == null) ? v : v.getRoot();
        console.printInfo("Downloading assets for version: " + ver.getID());
        try
        {
            if (ver.hasAssetIndex())
            {
                JSONObject verMeta = ver.getMeta();
                AssetIndex index = ver.getAssetIndex();
                String assetID = index.getID();
                long length = index.getTotalSize();
                long assetFileLength = index.getSize();
                this.downloaded = 0;
                this.validated = 0;
                boolean localIndex = false;
                File indexJSON = new File(Kernel.getKernel().getWorkingDir() + File.separator + index.getJSONFile());
                URL assetsURL = null;
                if (indexJSON.exists() && indexJSON.isFile())
                {
                    if (indexJSON.length() == assetFileLength)
                    {
                        assetsURL = indexJSON.toURI().toURL();
                        localIndex = true;
                    } 
                }
                assetsURL = (assetsURL == null) ? index.getURL() : assetsURL;
                if (!localIndex)
                {
                    if (Utils.downloadFile(assetsURL, indexJSON))
                    {
                        console.printInfo("Asset index for version " + assetID + " created.");
                    }
                    else
                    {
                        console.printError("Failed to create asset index for version " + assetID);
                    }
                }
                JSONObject root = new JSONObject(new String(Files.readAllBytes(indexJSON.toPath()), "ISO-8859-1"));
                JSONObject objects = root.getJSONObject("objects");
                Set keys = objects.keySet();
                Iterator it2 = keys.iterator();
                List<String> processedHashes = new ArrayList();
                while (it2.hasNext())
                {
                    String object = it2.next().toString();
                    JSONObject o = objects.getJSONObject(object);
                    String hash = o.getString("hash");
                    long size = o.getLong("size");
                    URL downloadURL = Utils.stringToURL(Constants.resourcesRoot + hash.substring(0,2) + "/" + hash);
                    File destPath = new File(Kernel.getKernel().getWorkingDir() + File.separator + "assets" + File.separator + "objects" + File.separator + hash.substring(0,2) + File.separator + hash);
                    boolean localValid = false;
                    if (destPath.exists() && destPath.isFile())
                    {
                        if (destPath.length() == size)
                        {
                            localValid = true;
                        }
                    }
                    if (!localValid)
                    {
                        Runnable t = new Runnable(){
                        @Override
                            public void run()
                            {
                                console.printInfo("Downloading " + object);
                                int tries = 0;
                                while (!Utils.downloadFile(downloadURL, destPath) && (tries < Constants.downloadTries))
                                {
                                    tries++;
                                }
                                if (tries == Constants.downloadTries)
                                {
                                    console.printError("Failed to download asset file: " + object);
                                }
                                else
                                {
                                    downloaded += size;
                                }
                                progressDownload = (int)(downloaded * 100 / length);
                                progressValid = (int)(validated * 100 / length);
                                console.printInfo("Downloaded: " + progressDownload + "% | Validated " + progressValid + "% | Total: " + (progressDownload + progressValid) + "%");
                            }
                        };
                        pool.execute(t);
                    }
                    else
                    {
                        console.printInfo("Asset file " + object + " found locally and it is valid.");
                        if (!processedHashes.contains(hash))
                        {
                            processedHashes.add(hash);
                            validated += size;
                        }
                        progressDownload = (int)(downloaded * 100 / length);
                        progressValid = (int)(validated * 100 / length);
                        console.printInfo("Downloaded: " + progressDownload + "% | Validated " + progressValid + "% | Total: " + (progressDownload + progressValid) + "%");
                    }
                }
                pool.shutdown();
                try {
                    pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    console.printError("Error has produced in dowload pool.");
                }
            }
            else
            {
                console.printError("Root version " + ver.getID() + " doesn't have AssetIndex.");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            console.printError("Failed to download assets for version: " + ver.getID());
        }
    }
    public void downloadVersion(Version ver)
    {
        Version v = (ver.getRoot() == null) ? ver : ver.getRoot();
        console.printInfo("Downloading version file: " + ver.getID() + ".jar");
        JSONObject root = v.getMeta();
        JSONObject client = root.getJSONObject("downloads").getJSONObject("client");
        URL jarURL = Utils.stringToURL(client.getString("url"));
        String jarSHA1 = client.getString("sha1");
        long jarSize = client.getLong("size");
        File destPath = new File(Kernel.getKernel().getWorkingDir() + File.separator + "versions" + File.separator + ver.getID() + File.separator + ver.getID() + ".jar");
        boolean localValid = false;
        if (destPath.exists() && destPath.isFile())
        {
            if (destPath.length() == jarSize && Utils.verifyChecksum(destPath, jarSHA1))
            {
                localValid = true;
            }
        }
        if (!localValid)
        {
            int tries = 0;
            while (!Utils.downloadFile(jarURL, destPath) && (tries < Constants.downloadTries))
            {
                tries++;
            }
            if (tries == Constants.downloadTries)
            {
                console.printError("Failed to download version jar: " + destPath.getName());
            }
        }
        else
        {
            console.printInfo("Version file " + destPath.getName() + " found locally and it is valid.");
        }
        console.printInfo("Creating version " + ver.getID() + " JSON file.");
        File jsonFile = new File(Kernel.getKernel().getWorkingDir() + File.separator + "versions" + File.separator + ver.getID() + File.separator + ver.getID() + ".json");
        boolean JSONValid = false;
        if (jsonFile.exists() && jsonFile.isFile())
        {
            try {
                if (ver.getOrigin() != VersionOrigin.LOCAL)
                {
                    if (jsonFile.length() == ver.getURL(VersionOrigin.REMOTE).openConnection().getContentLength())
                    {
                        JSONValid = true;
                    }
                }
                else
                {
                    JSONValid = true;
                }
            } catch (IOException ex) {
                console.printError("Falied to verify existing JSON integrity.");
            }
        }
        if (!JSONValid)
        {
            int tries = 0;
            while (!Utils.downloadFile(ver.getURL(VersionOrigin.REMOTE), jsonFile) && (tries < Constants.downloadTries))
            {
                tries++;
            }
            if (tries == Constants.downloadTries)
            {
                console.printError("Failed to download version jar: " + destPath.getName());
            }
        }
        else
        {
            console.printInfo("Version " + ver.getID() + " JSON file found locally and it is valid.");
        }
    }
    public void downloadLibraries(Version ver)
    {
        Version v = ver;
        while (v != null)
        {
            console.printInfo("Downloading required libraries for version " + v.getID());
            if (v.hasLibraries())
            {
                Map<String, Library> libs = v.getLibraries();
                Set set = libs.keySet();
                Iterator it = set.iterator();
                while (it.hasNext())
                {
                    String lib_name = it.next().toString();
                    Library lib = libs.get(lib_name);
                    console.printInfo("Downloading library " + lib_name);
                    if (lib.isDownloadable())
                    {
                        String sha1 = lib.getSHA1();
                        File libPath = new File(Kernel.getKernel().getWorkingDir() + File.separator + lib.getPath());
                        boolean localValid = false;
                        if (libPath.exists() && libPath.isFile())
                        {
                            if (lib.isLegacy())
                            {
                                try
                                {
                                    String urlRaw = lib.getURL().toString();
                                    int responseCode = -1;
                                    int contentLength = -1;
                                    if (urlRaw.startsWith("https"))
                                    {
                                        HttpsURLConnection con = (HttpsURLConnection)lib.getURL().openConnection();
                                        con.connect();
                                        responseCode = con.getResponseCode();
                                        contentLength = con.getContentLength();
                                    }
                                    else if (urlRaw.startsWith("http"))
                                    {
                                        HttpURLConnection con = (HttpURLConnection)lib.getURL().openConnection();
                                        con.connect();
                                        responseCode = con.getResponseCode();
                                        contentLength = con.getContentLength();
                                    }
                                    else
                                    {
                                        console.printError("Unsupported protocol type in " + urlRaw);
                                    }
                                    if (responseCode == 200)
                                    {
                                        if (libPath.length() == contentLength)
                                        {
                                            localValid = true;
                                        }
                                    }
                                    else if (responseCode == 404)
                                    {
                                        localValid = true;
                                        console.printInfo("Library not found remotelly so let's say it's valid.");
                                    }
                                }
                                catch (Exception ex)
                                {
                                    localValid = false;
                                }
                            }
                            else
                            {
                                if (libPath.length() == lib.getSize() && Utils.verifyChecksum(libPath, sha1))
                                {
                                    localValid = true;
                                }
                            }
                        }
                        if (!localValid)
                        {
                            int tries = 0;
                            while (!Utils.downloadFile(lib.getURL(), libPath) && (tries < Constants.downloadTries))
                            {
                                tries++;
                            }
                            if (tries == Constants.downloadTries)
                            {
                                console.printError("Failed to download library jar: " + libPath.getName());
                            }
                        }
                        else
                        {
                            console.printInfo("Library file " + libPath.getName() + " found locally and it is valid.");
                        }
                    }
                    else
                    {
                        console.printInfo("Library " + lib_name + " is not downloadable.");
                    }
                }
            }
            else
            {
                console.printInfo("Version " + v.getID() + " has no libraries.");
            }
            if (v.hasInheritedVersion())
            {
                v = v.getInheritedVersion();
            }
            else
            {
                v = null;
            }
        }
    }
    public void downloadNatives(Version ver)
    {
        Version v = ver;
        while (v != null)
        {
            if (!v.isPrepared())
            {
                v.prepare();
            }
            console.printInfo("Downloading required natives for version " + v.getID());
            if (v.hasNatives())
            {
                Map<String, Native> natives = v.getNatives();
                Set set = natives.keySet();
                Iterator it = set.iterator();
                while (it.hasNext())
                {
                    String nat_name = it.next().toString();
                    Native nat = natives.get(nat_name);
                    console.printInfo("Downloading native " + nat_name);
                    if (nat.isDownloadable())
                    {
                        String sha1 = nat.getSHA1();
                        File natPath = new File(Kernel.getKernel().getWorkingDir() + File.separator + nat.getPath());
                        boolean localValid = false;
                        if (natPath.exists() && natPath.isFile())
                        {
                            if (natPath.length() == nat.getSize() && Utils.verifyChecksum(natPath, sha1))
                            {
                                localValid = true;
                            }
                        }
                        if (!localValid)
                        {
                            int tries = 0;
                            while (!Utils.downloadFile(nat.getURL(), natPath) && (tries < Constants.downloadTries))
                            {
                                tries++;
                            }
                            if (tries == Constants.downloadTries)
                            {
                                console.printError("Failed to download native jar: " + natPath.getName());
                            }
                        }
                        else
                        {
                            console.printInfo("Native file " + natPath.getName() + " found locally and it is valid.");
                        }
                    }
                    else
                    {
                        console.printInfo("Native " + nat_name + " is not downloadable.");
                    }
                }
            }
            else
            {
                console.printInfo("Version " + v.getID() + " has no natives.");
            }
            if (v.hasInheritedVersion())
            {
                v = v.getInheritedVersion();
            }
            else
            {
                v = null;
            }
        }
    }
}