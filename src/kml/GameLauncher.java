package kml;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import kml.enums.OSArch;
import kml.enums.ProfileType;
import kml.exceptions.GameLauncherException;
import kml.gui.OutputFX;
import kml.objects.Library;
import kml.objects.Profile;
import kml.objects.User;
import kml.objects.Version;
import org.json.JSONObject;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class GameLauncher {

    private final Console console;
    private final Kernel kernel;
    private Process process;
    private boolean error;
    private boolean started;

    public GameLauncher(Kernel k) {
        this.kernel = k;
        this.console = k.getConsole();
    }

    public void launch() throws GameLauncherException {
        started = true;
        error = false;
        console.printInfo("Game launch work has started.");
        Profile p = kernel.getProfiles().getProfile(kernel.getProfiles().getSelectedProfile());
        if (this.isRunning()) {
            throw new GameLauncherException("Game is already started!");
        }
        String verID;
        Version ver;
        if (p.getType() == ProfileType.CUSTOM) {
            verID = p.hasVersion() ? p.getVersionID() : kernel.getVersions().getLatestRelease();
        } else if (p.getType() == ProfileType.RELEASE) {
            verID = kernel.getVersions().getLatestRelease();
        } else {
            verID = kernel.getVersions().getLatestSnapshot();
        }
        ver = kernel.getVersions().getVersion(verID);
        File workingDir = kernel.getWorkingDir();
        console.printInfo("Deleting old natives.");
        File nativesRoot = new File(workingDir + File.separator + "versions" + File.separator + ver.getID());
        if (nativesRoot.exists()) {
            if (nativesRoot.isDirectory()) {
                File[] files = nativesRoot.listFiles();
                if (Objects.nonNull(files)) {
                    for (File f : files) {
                        if (f.isDirectory() && f.getName().contains("natives")) {
                            Utils.deleteDirectory(f);
                        }
                    }
                }
            }
        }
        final File nativesDir = new File(workingDir + File.separator + "versions" + File.separator + ver.getID() + File.separator + ver.getID() + "-natives-" + System.nanoTime());
        if (!nativesDir.exists() || !nativesDir.isDirectory()) {
            nativesDir.mkdirs();
        }
        console.printInfo("Launching Minecraft " + ver.getID() + " on " + workingDir.getAbsolutePath());
        console.printInfo("Using natives dir: " + nativesDir);
        console.printInfo("Extracting natives.");
        List<String> gameArgs = new ArrayList<>();
        if (p.hasJavaDir()) {
            gameArgs.add(p.getJavaDir().getAbsolutePath());
        } else {
            gameArgs.add(Utils.getJavaDir());
        }
        if (!p.hasJavaArgs()) {
            if (Utils.getOSArch().equals(OSArch.OLD)) {
                gameArgs.add("-Xmx512M");
            } else {
                gameArgs.add("-Xmx1G");
            }
            gameArgs.add("-XX:+UseConcMarkSweepGC");
            gameArgs.add("-XX:+CMSIncrementalMode");
            gameArgs.add("-XX:-UseAdaptiveSizePolicy");
            gameArgs.add("-Xmn128M");
        } else {
            String javaArgs = p.getJavaArgs();
            String[] args = javaArgs.split(" ");
            Collections.addAll(gameArgs, args);
        }
        gameArgs.add("-Djava.library.path=" + nativesDir.getAbsolutePath());
        gameArgs.add("-cp");
        StringBuilder libraries = new StringBuilder();
        List<Library> libs = ver.getLibraries();
        String separator = System.getProperty("path.separator");
        try {
            File launchPath = new File(GameLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            libraries.append(launchPath.getAbsolutePath()).append(separator);
        } catch (URISyntaxException ex) {
            console.printError("Failed to load GameStarter.");
        }
        for (Library lib : libs) {
            if (lib.isCompatible()) {
                if (lib.isNative()) {
                    try {
                        File completePath = new File(kernel.getWorkingDir() + File.separator + lib.getRelativeNativePath());
                        ZipFile zip = new ZipFile(completePath);
                        final Enumeration<? extends ZipEntry> entries = zip.entries();
                        while (entries.hasMoreElements()) {
                            final ZipEntry entry = entries.nextElement();
                            if (entry.isDirectory()) {
                                continue;
                            }
                            final File targetFile = new File(nativesDir, entry.getName());
                            boolean excluded = false;
                            if (lib.hasExtractExclusions()) {
                                List<String> exclude = lib.getExtractExclusions();
                                for (String e : exclude) {
                                    if (entry.getName().startsWith(e)) {
                                        excluded = true;
                                    }
                                }
                            }
                            if (excluded) {
                                continue;
                            }
                            final BufferedInputStream inputStream = new BufferedInputStream(zip.getInputStream(entry));
                            final byte[] buffer = new byte[8192];
                            final FileOutputStream outputStream = new FileOutputStream(targetFile);
                            final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                            int length;
                            while ((length = inputStream.read(buffer, 0, buffer.length)) != -1) {
                                bufferedOutputStream.write(buffer, 0, length);
                            }
                            bufferedOutputStream.close();
                            outputStream.close();
                            inputStream.close();
                        }
                        zip.close();
                    } catch (IOException ex) {
                        console.printError("Failed to extract native: " + lib.getName());
                    }
                } else {
                    File completePath = new File(kernel.getWorkingDir() + File.separator + lib.getRelativePath());
                    libraries.append(completePath.getAbsolutePath()).append(separator);
                }
            }
        }
        console.printInfo("Preparing game args.");
        File verPath = new File(kernel.getWorkingDir() + File.separator + ver.getRelativeJar());
        libraries.append(verPath.getAbsolutePath());
        String assetsID = null;
        File assetsDir;
        File assetsRoot = new File(workingDir + File.separator + "assets");
        if (ver.hasAssets()) {
            assetsID = ver.getAssets();
            if (assetsID.equals("legacy")) {
                assetsDir = new File(assetsRoot + File.separator + "virtual" + File.separator + "legacy");
                if (!assetsDir.exists() || !assetsDir.isDirectory()) {
                    assetsDir.mkdirs();
                }
                console.printInfo("Building virtual asset folder.");
                File indexJSON = new File(assetsRoot + File.separator + "indexes" + File.separator + assetsID + ".json");
                try {
                    JSONObject o = new JSONObject(new String(Files.readAllBytes(indexJSON.toPath()), "ISO-8859-1"));
                    JSONObject objects = o.getJSONObject("objects");
                    Set s = objects.keySet();
                    for (Object value : s) {
                        String name = value.toString();
                        File assetFile = new File(assetsDir + File.separator + name);
                        JSONObject asset = objects.getJSONObject(name);
                        long size = asset.getLong("size");
                        String sha = asset.getString("hash");
                        boolean valid = false;
                        if (assetFile.exists()) {
                            if (assetFile.length() == size && Utils.verifyChecksum(assetFile, sha, "SHA-1")) {
                                valid = true;
                            }
                        }
                        if (!valid) {
                            File objectFile = new File(assetsRoot + File.separator + "objects" + File.separator + sha.substring(0, 2) + File.separator + sha);
                            if (Objects.nonNull(assetFile.getParentFile())) {
                                assetFile.getParentFile().mkdirs();
                            }
                            Files.copy(objectFile.toPath(), assetFile.toPath());
                        }
                    }
                } catch (Exception ex) {
                    console.printError("Failed to create virtual asset folder.");
                }
            } else {
                assetsDir = assetsRoot;
            }
        } else {
            assetsDir = new File(assetsRoot + File.separator + "virtual" + File.separator + "legacy");
        }
        gameArgs.add(libraries.toString());
        gameArgs.add("kml.GameStarter");
        if (p.hasGameDir()) {
            File gameDir = p.getGameDir();
            if (!gameDir.exists() || !gameDir.isDirectory()) {
                gameDir.mkdirs();
            }
            gameArgs.add(gameDir.getAbsolutePath());
        } else {
            gameArgs.add(workingDir.getAbsolutePath());
        }
        Authentication a = kernel.getAuthentication();
        User u = a.getSelectedUser();
        gameArgs.add(u.getProfileID());
        gameArgs.add(u.getAccessToken());
        gameArgs.add(ver.getMainClass());
        console.printInfo("Full game launcher parameters: ");
        String[] versionArgs = ver.getMinecraftArguments().split(" ");
        for (int i = 0; i < versionArgs.length; i++) {
            if (versionArgs[i].startsWith("$")) {
                switch (versionArgs[i]) {
                    case "${auth_player_name}":
                        versionArgs[i] = versionArgs[i].replace("${auth_player_name}", u.getDisplayName());
                        break;
                    case "${version_name}":
                        versionArgs[i] = versionArgs[i].replace("${version_name}", ver.getID());
                        break;
                    case "${game_directory}":
                        if (p.hasGameDir()) {
                            File gameDir = p.getGameDir();
                            if (!gameDir.exists() || !gameDir.isDirectory()) {
                                gameDir.mkdirs();
                            }
                            versionArgs[i] = versionArgs[i].replace("${game_directory}", gameDir.getAbsolutePath());
                        } else {
                            versionArgs[i] = versionArgs[i].replace("${game_directory}", workingDir.getAbsolutePath());
                        }
                        break;
                    case "${assets_root}":
                        versionArgs[i] = versionArgs[i].replace("${assets_root}", assetsDir.getAbsolutePath());
                        break;
                    case "${game_assets}":
                        versionArgs[i] = versionArgs[i].replace("${game_assets}", assetsDir.getAbsolutePath());
                        break;
                    case "${assets_index_name}":
                        if (ver.hasAssetIndex()) {
                            versionArgs[i] = versionArgs[i].replace("${assets_index_name}", assetsID);
                        } else if (ver.hasAssets()) {
                            versionArgs[i] = versionArgs[i].replace("${assets_index_name}", assetsID);
                        }
                        break;
                    case "${auth_uuid}":
                        versionArgs[i] = versionArgs[i].replace("${auth_uuid}", u.getProfileID());
                        break;
                    case "${auth_access_token}":
                        versionArgs[i] = versionArgs[i].replace("${auth_access_token}", u.getAccessToken());
                        break;
                    case "${version_type}":
                        versionArgs[i] = versionArgs[i].replace("${version_type}", ver.getType().name());
                        break;
                    case "${user_properties}":
                        versionArgs[i] = versionArgs[i].replace("${user_properties}", "{}");
                        break;
                    case "${user_type}":
                        versionArgs[i] = versionArgs[i].replace("${user_type}", "mojang");
                        break;
                    case "${auth_session}":
                        versionArgs[i] = versionArgs[i].replace("${auth_session}", "token:" + u.getAccessToken() + ":" + u.getProfileID().replace("-", ""));
                        break;
                }
            }
        }
        Collections.addAll(gameArgs, versionArgs);
        if (p.hasResolution()) {
            gameArgs.add("--width");
            gameArgs.add(String.valueOf(p.getResolutionWidth()));
            gameArgs.add("--height");
            gameArgs.add(String.valueOf(p.getResolutionHeight()));
        }
        for (String arg : gameArgs) {
            console.printInfo(arg);
        }
        ProcessBuilder pb = new ProcessBuilder(gameArgs);
        pb.directory(workingDir);
        try {
            this.process = pb.start();
            final OutputFX[] out = new OutputFX[1];
            if (kernel.getSettings().getShowGameLog()) {
                Platform.runLater(() -> {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/kml/gui/fxml/Output.fxml"));
                    Parent parent;
                    try {
                        parent = loader.load();
                    } catch (IOException e) {
                        parent = null;
                        this.console.printError("Failed to initialize Output GUI!");
                        this.console.printError(e.getMessage());
                    }
                    Stage stage = new Stage();
                    stage.getIcons().add(new Image("/kml/gui/textures/icon.png"));
                    stage.setTitle("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME + " - Game Output");
                    stage.setScene(new Scene(parent));
                    stage.setResizable(true);
                    stage.setMaximized(false);
                    stage.show();
                    out[0] = loader.getController();
                });
            }
            Thread log_info = new Thread(() -> {
                InputStreamReader isr = new InputStreamReader(GameLauncher.this.getInputStream(), Charset.forName("ISO-8859-1"));
                BufferedReader br = new BufferedReader(isr);
                try {
                    while (GameLauncher.this.isRunning()) {
                        final String lineRead = br.readLine();
                        if (Objects.nonNull(lineRead)) {
                            if (kernel.getSettings().getShowGameLog()) {
                                Platform.runLater(() -> {
                                    out[0].pushString(lineRead);
                                });
                            }
                            console.printInfo(lineRead);
                        }
                    }
                    if (process.exitValue() != 0) {
                        error = true;
                        console.printError("Game stopped unexpectedly.");
                    }
                } catch (Exception ex) {
                    error = true;
                    console.printError("Game stopped unexpectedly.");
                }
                started = false;
                console.printInfo("Deleteting natives dir.");
                Utils.deleteDirectory(nativesDir);
            });
            log_info.start();
            Thread log_error = new Thread(() -> {
                InputStreamReader isr = new InputStreamReader(GameLauncher.this.getErrorStream(), Charset.forName("ISO-8859-1"));
                BufferedReader br = new BufferedReader(isr);
                try {
                    while (GameLauncher.this.isRunning()) {
                        final String lineRead = br.readLine();
                        if (Objects.nonNull(lineRead)) {
                            if (kernel.getSettings().getShowGameLog()) {
                                Platform.runLater(() -> {
                                    out[0].pushString(lineRead);
                                });
                            }
                            console.printInfo(lineRead);
                        }
                    }
                } catch (Exception ignored) {
                    console.printError("Failed to read game error stream.");
                }
            });
            log_error.start();
        } catch (Exception ex) {
            error = true;
            started = false;
            ex.printStackTrace();
            console.printError("Game returned an error code.");
        }
    }

    public boolean isStarted() {
        return this.started;
    }

    public boolean isRunning() {
        if (Objects.nonNull(process)) {
            try {
                process.exitValue();
                return false;
            } catch (Exception ex) {
                return true;
            }
        }
        return false;
    }

    public boolean hasError() {
        boolean current = this.error;
        this.error = false;
        return current;
    }

    private InputStream getInputStream() {
        return this.process.getInputStream();
    }

    private InputStream getErrorStream() {
        return this.process.getErrorStream();
    }
}
