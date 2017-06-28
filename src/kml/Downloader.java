package kml;

import kml.enums.ProfileType;
import kml.exceptions.DownloaderException;
import kml.objects.*;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class Downloader {
    private final Console console;
    private final Kernel kernel;
    private double downloaded, validated, total;
    private boolean downloading;
    private String currentFile = "";

    public Downloader(Kernel k) {
        this.kernel = k;
        this.console = k.getConsole();
    }

    public void download() throws DownloaderException {
        this.downloading = true;
        console.printInfo("Download work has started.");
        Profile p = this.kernel.getProfiles().getProfile(this.kernel.getProfiles().getSelectedProfile());
        String verID;
        if (p.getType() == ProfileType.CUSTOM) {
            verID = p.hasVersion() ? p.getVersionID() : kernel.getVersions().getLatestRelease();
        } else if (p.getType() == ProfileType.RELEASE) {
            verID = kernel.getVersions().getLatestRelease();
        } else {
            verID = kernel.getVersions().getLatestSnapshot();
        }
        if (Objects.isNull(verID)) {
            this.downloading = false;
            throw new DownloaderException("Version ID is null.");
        }
        console.printInfo("Using version ID: " + verID);
        Version v = kernel.getVersions().getVersion(verID);
        if (Objects.isNull(v)) {
            throw new DownloaderException("Version info could not be obtained.");
        }
        ExecutorService pool = Executors.newFixedThreadPool(5);
        List<Downloadable> urls = new ArrayList<>();
        this.downloaded = 0;
        this.validated = 0;
        this.total = 0;
        console.printInfo("Fetching asset urls..");
        File indexJSON = null;
        if (v.hasAssetIndex()) {
            AssetIndex index = v.getAssetIndex();
            String assetID = index.getID();
            indexJSON = new File(kernel.getWorkingDir() + File.separator + index.getRelativeFile());
            URL assetsURL = index.getURL();
            if (!Constants.USE_LOCAL) {
                int tries = 0;
                while (!Utils.downloadFile(assetsURL, indexJSON) && (tries < Constants.DOWNLOAD_TRIES)) {
                    tries++;
                }
                if (tries == Constants.DOWNLOAD_TRIES) {
                    console.printError("Failed to download asset index for version " + assetID);
                }
            }
        } else if (v.hasAssets()) {
            String assetIndex = v.getAssets();
            URL assetsURL = Utils.stringToURL("https://s3.amazonaws.com/Minecraft.Download/indexes/" + assetIndex + ".json");
            indexJSON = new File(kernel.getWorkingDir() + File.separator + "assets" + File.separator + "indexes" + File.separator + assetIndex + ".json");
            if (!Constants.USE_LOCAL) {
                int tries = 0;
                while (!Utils.downloadFile(assetsURL, indexJSON) && (tries < Constants.DOWNLOAD_TRIES)) {
                    tries++;
                }
                if (tries == Constants.DOWNLOAD_TRIES) {
                    console.printError("Failed to download asset index for version " + assetIndex);
                }
            }
        } else {
            console.printInfo("Version " + v.getID() + " does not have any valid assets.");
        }
        if (Objects.nonNull(indexJSON)) {
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
            List<String> processedHashes = new ArrayList<>();
            while (it2.hasNext()) {
                String object = it2.next().toString();
                JSONObject o = objects.getJSONObject(object);
                String hash = o.getString("hash");
                long size = o.getLong("size");
                URL downloadURL = Utils.stringToURL(Constants.RESOURCES_URL + hash.substring(0, 2) + "/" + hash);
                File relPath = new File("assets" + File.separator + "objects" + File.separator + hash.substring(0, 2) + File.separator + hash);
                File fullPath = new File(kernel.getWorkingDir() + File.separator + relPath);
                boolean localValid = false;
                if (fullPath.exists() && fullPath.isFile()) {
                    if (fullPath.length() == size) {
                        localValid = true;
                    }
                }
                if (!processedHashes.contains(hash)) {
                    this.total += size;
                    processedHashes.add(hash);
                    if (!localValid) {
                        Downloadable d = new Downloadable(downloadURL, size, relPath, hash);
                        urls.add(d);
                    } else {
                        validated += size;
                    }
                }
            }
        }
        console.printInfo("Fetching version urls..");
        Downloadable d = v.getClientDownload();
        if (Objects.nonNull(d)) {
            if (d.hasURL()) {
                long jarSize = d.getSize();
                String jarSHA1 = d.getHash();
                total += d.getSize();
                File destPath = new File(kernel.getWorkingDir() + File.separator + v.getRelativeJar());
                boolean localValid = false;
                File jsonFile = new File(kernel.getWorkingDir() + File.separator + v.getRelativeJSON());
                if (!Constants.USE_LOCAL) {
                    int tries = 0;
                    while (!Utils.downloadFile(v.getJSONURL(), jsonFile) && (tries < Constants.DOWNLOAD_TRIES)) {
                        tries++;
                    }
                    if (tries == Constants.DOWNLOAD_TRIES) {
                        console.printError("Failed to download version index " + destPath.getName());
                    }
                }
                if (destPath.exists() && destPath.isFile()) {
                    if (destPath.length() == jarSize && Utils.verifyChecksum(destPath, jarSHA1)) {
                        localValid = true;
                    }
                }
                if (!localValid) {
                    urls.add(d);
                } else {
                    validated += jarSize;
                }
            } else {
                console.printInfo("Incompatible version downloadable.");
            }
        } else {
            console.printInfo("Version file from " + v.getID() + " has no compatible downloadable objects.");
        }
        console.printInfo("Fetching library and native urls..");
        List<Library> libs = v.getLibraries();
        for (Library lib : libs) {
            if (lib.isCompatible()) {
                if (lib.hasArtifactDownload()) {
                    Downloadable a = lib.getArtifactDownload();
                    File completePath = new File(kernel.getWorkingDir() + File.separator + a.getRelativePath());
                    boolean valid = false;
                    if (completePath.exists()) {
                        if (completePath.isFile()) {
                            if (a.hasSize()) {
                                if (a.hasHash() && completePath.length() == a.getSize()) {
                                    valid = Utils.verifyChecksum(completePath, a.getHash());
                                }
                            } else {
                                valid = true;
                            }
                        }
                    }
                    this.total += a.getSize();
                    if (valid) {
                        this.validated += a.getSize();
                    }
                    if (!urls.contains(a) && !valid) {
                        urls.add(a);
                    }
                }
                if (lib.hasClassifierDownload()) {
                    Downloadable c = lib.getClassifierDownload();
                    File completePath = new File(kernel.getWorkingDir() + File.separator + c.getRelativePath());
                    boolean valid = false;
                    if (completePath.exists()) {
                        if (completePath.isFile()) {
                            valid = !(c.hasHash() && completePath.length() == c.getSize()) || Utils.verifyChecksum(completePath, c.getHash());
                        }
                    }
                    this.total += c.getSize();
                    if (valid) {
                        this.validated += c.getSize();
                    }
                    if (!urls.contains(c) && !valid) {
                        urls.add(c);
                    }
                }
            }
        }
        console.printInfo("Downloading required game files...");
        if (urls.isEmpty()) {
            console.printInfo("Nothing to download.");
        } else {
            for (final Downloadable dw : urls) {
                Runnable thread = () -> {
                    File path = dw.getRelativePath();
                    File fullPath = new File(kernel.getWorkingDir() + File.separator + path);
                    URL url = dw.getURL();
                    int tries = 0;
                    console.printInfo("Downloading " + path.getName() + " from " + url.toString());
                    currentFile = path.getName();
                    while (!Utils.downloadFile(url, fullPath) && (tries < Constants.DOWNLOAD_TRIES)) {
                        tries++;
                    }
                    if (tries == Constants.DOWNLOAD_TRIES) {
                        console.printError("Failed to download file " + path.getName() + " from " + url.toString());
                    }
                    downloaded += dw.getSize();
                };
                pool.execute(thread);
            }
            pool.shutdown();
            try {
                pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException ex) {
                this.downloading = false;
                throw new DownloaderException("Thread pool unexpectedly closed.");
            }
        }
        this.downloading = false;
    }

    public int getProgress() {
        if (!this.downloading) {
            return 0;
        }
        return (int) ((this.downloaded + this.validated) / this.total * 100);
    }

    public boolean isDownloading() {
        return this.downloading;
    }

    public String getCurrentFile() {
        return currentFile;
    }
}