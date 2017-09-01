package kml;

import kml.enums.OS;
import kml.enums.OSArch;
import kml.enums.ProfileType;
import kml.exceptions.DownloaderException;
import kml.objects.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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

    /**
     * Downloads all requires game files
     * @throws DownloaderException If the download fails
     */
    public final void download() throws DownloaderException {
        this.downloading = true;
        this.console.print("Download work has started.");
        Profile p = this.kernel.getProfiles().getSelectedProfile();
        Versions versions = this.kernel.getVersions();
        VersionMeta verID;
        if (p.getType() == ProfileType.CUSTOM) {
            verID = p.hasVersion() ? p.getVersionID() : versions.getLatestRelease();
        } else if (p.getType() == ProfileType.RELEASE) {
            verID = versions.getLatestRelease();
        } else {
            verID = versions.getLatestSnapshot();
        }
        if (verID == null) {
            this.downloading = false;
            throw new DownloaderException("Version ID is null.");
        }
        this.console.print("Using version ID: " + verID);
        Version v = versions.getVersion(verID);
        if (v == null) {
            throw new DownloaderException("Version info could not be obtained.");
        }
        ExecutorService pool = Executors.newFixedThreadPool(1);
        Collection<Downloadable> urls = new ArrayList<>();
        this.downloaded = 0;
        this.validated = 0;
        this.total = 0;
        if (Utils.getPlatform() == OS.WINDOWS && !p.hasJavaDir()) {
            this.console.print("Fetching runtime...");
            try {
                JSONObject root = new JSONObject(Utils.readURL(Constants.RUNTIME_URL));
                JSONObject windows = root.getJSONObject("windows");
                JSONObject arch;
                if (Utils.getOSArch() == OSArch.OLD) {
                    arch = windows.getJSONObject("32");
                } else {
                    arch = windows.getJSONObject("64");
                }
                JSONObject jre = arch.getJSONObject("jre");
                String sha1 = jre.getString("sha1");
                URL jreURL = Utils.stringToURL(jre.getString("url"));
                long length = jreURL.openConnection().getContentLength();
                this.total += length;
                File jreFile = new File(Constants.APPLICATION_WORKING_DIR, "jre.lzma");
                if (jreFile.exists() && jreFile.isFile() && Utils.verifyChecksum(jreFile, sha1, "sha1")) {
                    this.console.print("Runtime already downloaded and valid.");
                    this.validated += length;
                } else {
                    Downloadable d = new Downloadable(jreURL, length, new File("jre.lzma"), sha1, null);
                    urls.add(d);
                    File markFile = new File(Constants.APPLICATION_WORKING_DIR, "jre" + File.separator + "OK");
                    if (markFile.exists() && markFile.isFile()) {
                        markFile.delete();
                    }
                }
            } catch (IOException | JSONException ex) {
                this.console.print("Failed to fetch runtime.");
            }
        }
        this.console.print("Fetching asset urls..");
        File indexJSON = null;
        URL assetsURL;
        String assetID;
        //Fetch asset index
        if (v.hasAssetIndex() || v.hasAssets()) {
            if (v.hasAssetIndex()) {
                AssetIndex index = v.getAssetIndex();
                assetID = index.getID();
                indexJSON = new File(Constants.APPLICATION_WORKING_DIR + File.separator + index.getRelativeFile());
                assetsURL = index.getURL();
            } else {
                assetID = v.getAssets();
                assetsURL = Utils.stringToURL("https://s3.amazonaws.com/Minecraft.Download/indexes/" + assetID + ".json"); //Might be deprecated soon
                indexJSON = new File(Constants.APPLICATION_WORKING_DIR + File.separator + "assets" + File.separator + "indexes" + File.separator + assetID + ".json");
            }
            if (!Constants.USE_LOCAL) {
                int tries = 0;
                while (!Utils.downloadFile(assetsURL, indexJSON) && tries < Constants.DOWNLOAD_TRIES) {
                    tries++;
                }
                if (tries == Constants.DOWNLOAD_TRIES) {
                    this.console.print("Failed to download asset index for version " + assetID);
                }
            }
        } else {
            this.console.print("Version " + v.getID() + " does not have any valid assets.");
        }
        //Fetch assets
        if (indexJSON != null) {
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
            Set<String> keys = objects.keySet();
            Collection<String> processedHashes = new ArrayList<>();
            for (String key : keys) {
                JSONObject o = objects.getJSONObject(key);
                String hash = o.getString("hash");
                long size = o.getLong("size");
                URL downloadURL = Utils.stringToURL(Constants.RESOURCES_URL + hash.substring(0, 2) + '/' + hash);
                File relPath = new File("assets" + File.separator + "objects" + File.separator + hash.substring(0, 2) + File.separator + hash);
                File fullPath = new File(Constants.APPLICATION_WORKING_DIR + File.separator + relPath);
                boolean localValid = false;
                if (fullPath.exists() && fullPath.isFile()) {
                    if (fullPath.length() == size && Utils.verifyChecksum(fullPath, hash, "SHA-1")) {
                        localValid = true;
                    }
                }
                if (!processedHashes.contains(hash)) {
                    this.total += size;
                    processedHashes.add(hash);
                    if (!localValid) {
                        Downloadable d = new Downloadable(downloadURL, size, relPath, hash, key);
                        urls.add(d);
                    } else {
                        this.validated += size;
                    }
                }
            }
        }
        this.console.print("Fetching version urls..");
        Downloadable d = v.getClientDownload();
        if (d != null) {
            if (d.hasURL()) {
                long jarSize = d.getSize();
                String jarSHA1 = d.getHash();
                this.total += d.getSize();
                File destPath = new File(Constants.APPLICATION_WORKING_DIR + File.separator + v.getRelativeJar());
                boolean localValid = false;
                File jsonFile = new File(Constants.APPLICATION_WORKING_DIR + File.separator + v.getRelativeJSON());
                if (!Constants.USE_LOCAL) {
                    int tries = 0;
                    while (!Utils.downloadFile(v.getJSONURL(), jsonFile) && tries < Constants.DOWNLOAD_TRIES) {
                        tries++;
                    }
                    if (tries == Constants.DOWNLOAD_TRIES) {
                        this.console.print("Failed to download version index " + destPath.getName());
                    }
                }
                if (destPath.exists() && destPath.isFile()) {
                    if (destPath.length() == jarSize && d.hasHash()) {
                        localValid = Utils.verifyChecksum(destPath, jarSHA1, "SHA-1");
                    }
                }
                if (!localValid) {
                    urls.add(d);
                } else {
                    this.validated += jarSize;
                }
            } else {
                this.console.print("Incompatible version downloadable.");
            }
        } else {
            this.console.print("Version file from " + v.getID() + " has no compatible downloadable objects.");
        }
        this.console.print("Fetching library and native urls..");
        List<Library> libs = v.getLibraries();
        for (Library lib : libs) {
            if (lib.isCompatible()) {
                if (lib.hasArtifactDownload()) {
                    Downloadable a = lib.getArtifactDownload();
                    File completePath = new File(Constants.APPLICATION_WORKING_DIR + File.separator + a.getRelativePath());
                    boolean valid = false;
                    if (completePath.exists()) {
                        if (completePath.isFile()) {
                            if (a.hasSize()) {
                                if (a.hasHash() && completePath.length() == a.getSize()) {
                                    valid = Utils.verifyChecksum(completePath, a.getHash(), "SHA-1");
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
                    File completePath = new File(Constants.APPLICATION_WORKING_DIR + File.separator + c.getRelativePath());
                    boolean valid = false;
                    if (completePath.exists()) {
                        if (completePath.isFile()) {
                            valid = !(c.hasHash() && completePath.length() == c.getSize()) || Utils.verifyChecksum(completePath, c.getHash(), "SHA-1");
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
        this.console.print("Downloading required game files...");
        if (urls.isEmpty()) {
            this.console.print("Nothing to download.");
        } else {
            //Download required files
            for (Downloadable dw : urls) {
                Runnable thread = () -> {
                    File path = dw.getRelativePath();
                    File fullPath = new File(Constants.APPLICATION_WORKING_DIR + File.separator + path);
                    URL url = dw.getURL();
                    int tries = 0;
                    if (dw.hasFakePath()) {
                        this.currentFile = dw.getFakePath();
                    } else {
                        this.currentFile = path.getName();
                    }
                    this.console.print("Downloading " + this.currentFile + " from " + url);
                    while (!Utils.downloadFile(url, fullPath) && tries < Constants.DOWNLOAD_TRIES) {
                        tries++;
                    }
                    if (tries == Constants.DOWNLOAD_TRIES) {
                        this.console.print("Failed to download file " + path.getName() + " from " + url);
                    }
                    this.downloaded += dw.getSize();
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

    /**
     * Returns the current download progress
     * @return The download progress
     */
    public final double getProgress() {
        if (!this.downloading) {
            return 0;
        }
        return (this.downloaded + this.validated) / this.total * 100;
    }

    /**
     * Checks if the download task still running
     * @return A boolean with the current status
     */
    public final boolean isDownloading() {
        return this.downloading;
    }

    /**
     * Gets the latest file that has been pushed to the download queue
     * @return The current file name
     */
    public final String getCurrentFile() {
        return this.currentFile;
    }
}