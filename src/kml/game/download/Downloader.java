package kml.game.download;

import kml.*;
import kml.Console;
import kml.OS;
import kml.OSArch;
import kml.game.profile.ProfileType;
import kml.exceptions.DownloaderException;
import kml.game.profile.Profile;
import kml.game.version.*;
import kml.game.version.asset.AssetIndex;
import kml.game.version.library.Library;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

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
    private final int DOWNLOAD_TRIES = 5;

    public Downloader(Kernel k) {
        this.kernel = k;
        this.console = k.getConsole();
    }

    /**
     * Downloads all requires game files
     * @throws DownloaderException If the download fails
     */
    public final void download() throws DownloaderException {
        //Initial values
        this.downloading = true;
        this.downloaded = 0;
        this.validated = 0;
        this.total = 0;
        int tries;

        this.console.print("Download work has started.");
        if (Constants.USE_LOCAL) {
            this.console.print("You are in offline mode.");
            this.downloading = false;
            return;
        }

        //Fetch version used by profile
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
            this.downloading = false;
            throw new DownloaderException("Version info could not be obtained.");
        }

        //Get required files to be downloaded
        Set<Downloadable> urls = new HashSet<>();

        //Fetch assets
        this.console.print("Fetching asset urls..");
        AssetIndex index = v.getAssetIndex();
        File indexJSON = new File(Constants.APPLICATION_WORKING_DIR, "assets" + File.separator + "indexes" + File.separator + index.getID() + ".json");
        tries = 0;
        if (!Utils.verifyChecksum(indexJSON, index.getSHA1(), "SHA-1")) {
            while (tries < this.DOWNLOAD_TRIES) {
                try {
                    Utils.downloadFile(index.getURL(), indexJSON);
                    break;
                } catch (IOException ex) {
                    this.console.print("Failed to download file " + indexJSON.getName() + " (try " + tries + ')');
                    ex.printStackTrace(this.console.getWriter());
                    tries++;
                }
            }
        }
        if (tries == this.DOWNLOAD_TRIES) {
            this.console.print("Failed to download asset index for version " + index.getID());
        } else {
            //Load assets
            try {
                JSONObject root;
                try {
                    root = new JSONObject(new String(Files.readAllBytes(indexJSON.toPath()), StandardCharsets.UTF_8));
                } catch (JSONException | IOException ex) {
                    this.downloading = false;
                    throw new DownloaderException("Failed to read asset index json file.");
                }
                JSONObject objects = root.getJSONObject("objects");
                Set<String> keys = objects.keySet();
                Collection<String> processedHashes = new ArrayList<>();
                File objectsRoot = new File("assets" + File.separator + "objects");
                for (String key : keys) {
                    JSONObject o = objects.getJSONObject(key);
                    String hash = o.getString("hash");
                    long size = o.getLong("size");
                    URL downloadURL = Utils.stringToURL("http://resources.download.minecraft.net/" + hash.substring(0, 2) + '/' + hash);
                    File relPath = new File(objectsRoot, hash.substring(0, 2) + File.separator + hash);
                    File fullPath = new File(Constants.APPLICATION_WORKING_DIR + File.separator + relPath);
                    if (!processedHashes.contains(hash)) {
                        this.total += size;
                        processedHashes.add(hash);
                        if (!Utils.verifyChecksum(fullPath, hash, "SHA-1")) {
                            Downloadable d = new Downloadable(downloadURL, size, relPath, hash, key);
                            urls.add(d);
                        } else {
                            this.validated += size;
                        }
                    }
                }
            } catch (JSONException ex) {
                this.console.print("Failed to parse asset index.");
            }
        }

        //Fetch version
        this.console.print("Fetching version urls..");
        Downloadable d = v.getClientDownload();
        if (d != null) {
            if (d.hasURL()) {
                long jarSize = d.getSize();
                String jarSHA1 = d.getHash();
                this.total += d.getSize();
                File destPath = new File(Constants.APPLICATION_WORKING_DIR + File.separator + v.getRelativeJar());
                File jsonFile = new File(Constants.APPLICATION_WORKING_DIR + File.separator + v.getRelativeJSON());
                tries = 0;
                while (tries < this.DOWNLOAD_TRIES) {
                    try {
                        Utils.downloadFile(v.getJSONURL(), jsonFile);
                        break;
                    } catch (IOException ex) {
                        this.console.print("Failed to download file " + jsonFile.getName() + " (try " + tries + ')');
                        ex.printStackTrace(this.console.getWriter());
                        tries++;
                    }
                }
                if (tries == this.DOWNLOAD_TRIES) {
                    this.console.print("Failed to download version index " + destPath.getName());
                }
                if (!Utils.verifyChecksum(destPath, jarSHA1, "SHA-1")) {
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

        //Fetch libraries and natives
        this.console.print("Fetching library and native urls..");
        List<Library> libs = v.getLibraries();
        for (Library lib : libs) {
            if (lib.isCompatible()) {
                //Standard download
                if (lib.hasArtifactDownload()) {
                    Downloadable a = lib.getArtifactDownload();
                    File completePath = new File(Constants.APPLICATION_WORKING_DIR + File.separator + a.getRelativePath());
                    if (completePath.isFile() && a.getHash() == null) {
                        this.console.print("File " + completePath + " has no hash. So let's assume the local one is valid.");
                    } else {
                        this.total += a.getSize();
                        if (Utils.verifyChecksum(completePath, a.getHash(), "SHA-1")) {
                            this.validated += a.getSize();
                        } else {
                            urls.add(a);
                        }
                    }

                }
                //Native download
                if (lib.hasClassifierDownload()) {
                    Downloadable c = lib.getClassifierDownload();
                    File completePath = new File(Constants.APPLICATION_WORKING_DIR + File.separator + c.getRelativePath());
                    this.total += c.getSize();
                    if (completePath.isFile() && c.getHash() == null) {
                        this.console.print("File " + completePath + " has no hash. So let's assume the local one is valid.");
                    } else {
                        if (Utils.verifyChecksum(completePath, c.getHash(), "SHA-1")) {
                            this.validated += c.getSize();
                        } else {
                            urls.add(c);
                        }
                    }
                }
            }
        }
        this.console.print("Downloading required game files...");
        if (urls.isEmpty()) {
            this.console.print("Nothing to download.");
        } else {
            //Download required files
            downloadFiles(urls);
        }
        this.downloading = false;
    }

    /**
     * Performs the download of a Downloadable
     * @param dw The target Downloadable
     */
    private void downloadFile(Downloadable dw) {
        File path = dw.getRelativePath();
        File fullPath = new File(Constants.APPLICATION_WORKING_DIR + File.separator + path);
        if (fullPath.getParentFile() != null) {
            fullPath.getParentFile().mkdirs();
        }
        URL url = dw.getURL();
        int tries = 0;
        if (dw.hasFakePath()) {
            this.currentFile = dw.getFakePath();
        } else {
            this.currentFile = path.toString();
        }
        this.console.print("Downloading " + this.currentFile + " from " + url);
        if (dw.getSize() == 0) {
            this.console.print(dw.getURL() + " has no expected size.");
            try {
                URLConnection con = url.openConnection();
                long length = con.getContentLength();
                this.total += length;
            } catch (IOException ex) {
                this.console.print("Failed to determine size from " + dw.getURL());
            }
        }
        while (tries < this.DOWNLOAD_TRIES) {
            int totalRead = 0;
            try (InputStream in = url.openStream();
                 OutputStream out = new FileOutputStream(fullPath)){
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    this.downloaded += read;
                    totalRead += read;
                }
                break;
            } catch (IOException ex) {
                this.console.print("Failed to download file " + this.currentFile + " (try " + tries + ')');
                ex.printStackTrace(this.console.getWriter());
                this.downloaded -= totalRead;
                tries++;
            }
        }
        if (tries == this.DOWNLOAD_TRIES) {
            this.console.print("Failed to download file " + path.getName() + " from " + url);
        }
    }

    /**
     * Downloads an entire set of Downloadables
     * @param list The set of Downloadables
     */
    private void downloadFiles(Set<Downloadable> list) {
        for (Downloadable d : list) {
            downloadFile(d);
        }
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