package kml;

import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import kml.enums.ProfileIcon;
import kml.gui.BrowserFX;
import kml.gui.MainFX;
import org.json.JSONObject;

import javax.swing.*;
import java.beans.IntrospectionException;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;


/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public final class Kernel {
    private final Console console;
    private final Profiles profiles;
    private final Versions versions;
    private final Settings settings;
    private final Downloader downloader;
    private final Authentication authentication;
    private final GameLauncher gameLauncher;
    private final HostServices hostServices;
    private final BrowserFX webBrowser;
    private JSONObject launcherProfiles;
    private final Image applicationIcon, profileIcons;
    private final HashMap<ProfileIcon, Image> iconCache;

    public Kernel(Stage stage, HostServices hs) {
        if (!Constants.APPLICATION_WORKING_DIR.exists()) {
            Constants.APPLICATION_WORKING_DIR.mkdirs();
        }
        Constants.APPLICATION_CACHE.mkdir();
        this.console = new Console();
        this.console.printInfo("KML v" + Constants.KERNEL_BUILD_NAME + " by DarkLBP (https://krothium.com)");
        this.console.printInfo("Kernel build: " + Constants.KERNEL_BUILD);
        this.console.printInfo("OS: " + System.getProperty("os.name"));
        this.console.printInfo("OS Version: " + System.getProperty("os.version"));
        this.console.printInfo("OS Architecture: " + System.getProperty("os.arch"));
        this.console.printInfo("Java Version: " + System.getProperty("java.version"));
        this.console.printInfo("Java Vendor: " + System.getProperty("java.vendor"));
        this.console.printInfo("Java Architecture: " + System.getProperty("sun.arch.data.model"));
        try {
            this.getClass().getClassLoader().loadClass("javafx.embed.swing.JFXPanel");
            this.console.printInfo("JavaFX loaded.");
        } catch (ClassNotFoundException e) {
            final File jfxrt = new File(System.getProperty("java.home"), "lib/jfxrt.jar");
            if (jfxrt.isFile()) {
                this.console.printInfo("Attempting to load JavaFX manually...");
                try {
                    if (addToSystemClassLoader(jfxrt)) {
                        this.console.printInfo("JavaFX loaded manually.");
                    } else {
                        this.console.printError("Found JavaFX but it couldn't be loaded!");
                        warnJavaFX();
                    }
                } catch (Throwable e2) {
                    this.console.printError("Found JavaFX but it couldn't be loaded!");
                    warnJavaFX();
                }
            } else {
                this.console.printError("JavaFX library not found. Please update Java!");
                warnJavaFX();
            }
        }
        console.printInfo("Using custom HTTPS certificate checker? | " + Utils.ignoreHTTPSCert());
        console.printInfo("Reading launcher profiles...");
        try {
            if (Constants.APPLICATION_CONFIG.exists() && Constants.APPLICATION_CONFIG.isFile()) {
                launcherProfiles = new JSONObject(Utils.readURL(Constants.APPLICATION_CONFIG.toURI().toURL()));
            } else {
                console.printError("Launcher profiles file does not exists.");
            }
        } catch (Exception e) {
            console.printError("Malformed launcher profiles file.");
        }
        this.profiles = new Profiles(this);
        this.versions = new Versions(this);
        this.settings = new Settings(this);
        this.downloader = new Downloader(this);
        this.authentication = new Authentication(this);
        this.gameLauncher = new GameLauncher(this);
        this.hostServices = hs;
        this.loadSettings();
        this.loadVersions();
        this.loadProfiles();
        this.loadUsers();

        //Initialize constants
        profileIcons = new Image("/kml/gui/textures/profile_icons.png");
        applicationIcon = new Image("/kml/gui/textures/icon.png");
        iconCache = new HashMap<>();

        //Load web browser
        Stage s = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/kml/gui/fxml/Browser.fxml"));
        Parent p;
        try {
            p = loader.load();
        } catch (IOException e) {
            p = null;
            this.console.printError("Failed to initialize JavaFX GUI!");
            this.console.printError(e.getMessage());
            exitSafely();
        }
        s.getIcons().add(applicationIcon);
        s.setTitle("Krothium Minecraft Launcher");
        s.setScene(new Scene(p));
        s.setResizable(false);
        s.setMaximized(false);
        s.setOnCloseRequest(e -> {
            e.consume();
            Alert ask = buildAlert(Alert.AlertType.CONFIRMATION, null, Language.get(94) + System.lineSeparator() + Language.get(95) + System.lineSeparator() +
                    Language.get(96) + System.lineSeparator() + Language.get(97));
            Optional<ButtonType> response = ask.showAndWait();
            if (response.isPresent() && response.get() == ButtonType.OK) {
                getHostServices().showDocument("https://krothium.com/donaciones/");
            }
        });
        webBrowser = loader.getController();
        webBrowser.initialize(s);

        //Load main form
        FXMLLoader loader2 = new FXMLLoader();
        loader2.setLocation(getClass().getResource("/kml/gui/fxml/Main.fxml"));
        try {
            p = loader2.load();
        } catch (IOException e) {
            p = null;
            this.console.printError("Failed to initialize JavaFX GUI!");
            this.console.printError(e.getMessage());
            exitSafely();
        }
        stage.getIcons().add(applicationIcon);
        stage.setTitle("Krothium Minecraft Launcher");
        stage.setScene(new Scene(p));
        stage.setResizable(false);
        stage.setMaximized(false);
        stage.setOnCloseRequest(e -> exitSafely());
        MainFX mainForm = loader2.getController();
        mainForm.initialize(this, stage);
        stage.show();
    }

    public static boolean addToSystemClassLoader(final File file) throws IntrospectionException {
        if (ClassLoader.getSystemClassLoader() instanceof URLClassLoader) {
            final URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            try {
                final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(classLoader, file.toURI().toURL());
                return true;
            } catch (Exception t) {
                return false;
            }
        }
        return false;
    }

    public Console getConsole() {
        return this.console;
    }

    public HostServices getHostServices() {
        return this.hostServices;
    }

    public void saveProfiles() {
        JSONObject output = new JSONObject();
        JSONObject profiles = this.profiles.toJSON();
        JSONObject authdata = this.authentication.toJSON();
        Set pset = profiles.keySet();
        for (Object aPset : pset) {
            String name = aPset.toString();
            output.put(name, profiles.get(name));
        }
        Set aset = authdata.keySet();
        for (Object anAset : aset) {
            String name = anAset.toString();
            output.put(name, authdata.get(name));
        }
        output.put("settings", this.settings.toJSON());
        if (!Utils.writeToFile(output.toString(4), Constants.APPLICATION_CONFIG)) {
            console.printError("Failed to save the profiles file!");
        }
    }

    private void loadProfiles() {
        profiles.fetchProfiles();
    }

    private void loadVersions() {
        versions.fetchVersions();
    }

    private void loadUsers() {
        authentication.fetchUsers();
    }

    private void loadSettings() {
        settings.loadSettings();
    }

    public Versions getVersions() {
        return this.versions;
    }

    public Profiles getProfiles() {
        return this.profiles;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public Downloader getDownloader() {
        return this.downloader;
    }

    public Authentication getAuthentication() {
        return this.authentication;
    }

    public JSONObject getLauncherProfiles() {
        return launcherProfiles;
    }

    public GameLauncher getGameLauncher() {
        return this.gameLauncher;
    }

    public Image getApplicationIcon() {
        return applicationIcon;
    }

    public Image getProfileIcons() {
        return profileIcons;
    }

    public HashMap<ProfileIcon, Image> getIconCache() {
        return iconCache;
    }

    public void exitSafely() {
        this.saveProfiles();
        this.console.printInfo("Shutting down launcher...");
        this.console.close();
        System.exit(0);
    }

    public String checkForUpdates() {
        try {
            String r = Utils.readURL(Constants.GETLATEST_URL);
            String[] data = r.split(":");
            int version = Integer.parseInt(Utils.fromBase64(data[0]));
            if (version > Constants.KERNEL_BUILD) {
                console.printInfo("New kernel build available: " + version);
                return data[1];
            }
        } catch (Exception ex) {
            console.printError("Failed to check for updates: " + ex);
            return null;
        }
        return null;
    }

    private void warnJavaFX() {
        JOptionPane.showMessageDialog(null, Language.get(9), "Error", JOptionPane.ERROR_MESSAGE);
        exitSafely();
    }

    public BrowserFX getBrowser() {
        return this.webBrowser;
    }

    public Image getProfileIcon(ProfileIcon p) {
        if (iconCache.containsKey(p)) {
            return iconCache.get(p);
        }
        WritableImage wi = new WritableImage(136, 136);
        PixelWriter pw = wi.getPixelWriter();
        int blockX = 0;
        int blockY = 0;
        try {
            PixelReader pr = profileIcons.getPixelReader();
            switch (p) {
                case LEAVES_OAK:
                    blockX = 0;
                    blockY = 0;
                    break;
                case BEDROCK:
                    blockX = 1;
                    blockY = 0;
                    break;
                case CLAY:
                    blockX = 2;
                    blockY = 0;
                    break;
                case DIAMOND_BLOCK:
                    blockX = 3;
                    blockY = 0;
                    break;
                case END_STONE:
                    blockX = 4;
                    blockY = 0;
                    break;
                case GRAVEL:
                    blockX = 5;
                    blockY = 0;
                    break;
                case LOG_BIRCH:
                    blockX = 6;
                    blockY = 0;
                    break;
                case PLANKS_OAK:
                    blockX = 7;
                    blockY = 0;
                    break;
                case TNT:
                    blockX = 8;
                    blockY = 0;
                    break;
                case BRICK:
                    blockX = 0;
                    blockY = 1;
                    break;
                case CHEST:
                    blockX = 1;
                    blockY = 1;
                    break;
                case COAL_BLOCK:
                    blockX = 2;
                    blockY = 1;
                    break;
                case DIAMOND_ORE:
                    blockX = 3;
                    blockY = 1;
                    break;
                case FARMLAND:
                    blockX = 4;
                    blockY = 1;
                    break;
                case HARDENED_CLAY:
                    blockX = 5;
                    blockY = 1;
                    break;
                case LOG_DARKOAK:
                    blockX = 6;
                    blockY = 1;
                    break;
                case PLANKS_SPRUCE:
                    blockX = 7;
                    blockY = 1;
                    break;
                case WOOL:
                    blockX = 8;
                    blockY = 1;
                    break;
                case COAL_ORE:
                    blockX = 0;
                    blockY = 2;
                    break;
                case COBBLESTONE:
                    blockX = 1;
                    blockY = 2;
                    break;
                case CRAFTING_TABLE: //Default for Latest Snapshot
                    blockX = 2;
                    blockY = 2;
                    break;
                case DIRT:
                    blockX = 3;
                    blockY = 2;
                    break;
                case FURNACE: //Default for custom profiles
                    blockX = 4;
                    blockY = 2;
                    break;
                case ICE_PACKED:
                    blockX = 5;
                    blockY = 2;
                    break;
                case LOG_JUNGLE:
                    blockX = 6;
                    blockY = 2;
                    break;
                case QUARTZ_ORE:
                    blockX = 7;
                    blockY = 2;
                    break;
                case DIRT_PODZOL:
                    blockX = 0;
                    blockY = 3;
                    break;
                case DIRT_SNOW:
                    blockX = 1;
                    blockY = 3;
                    break;
                case EMERALD_BLOCK:
                    blockX = 2;
                    blockY = 3;
                    break;
                case EMERALD_ORE:
                    blockX = 3;
                    blockY = 3;
                    break;
                case FURNACE_ON:
                    blockX = 4;
                    blockY = 3;
                    break;
                case IRON_BLOCK:
                    blockX = 5;
                    blockY = 3;
                    break;
                case LOG_OAK:
                    blockX = 6;
                    blockY = 3;
                    break;
                case RED_SAND:
                    blockX = 7;
                    blockY = 3;
                    break;
                case GLASS:
                    blockX = 0;
                    blockY = 4;
                    break;
                case GLOWSTONE:
                    blockX = 1;
                    blockY = 4;
                    break;
                case GOLD_BLOCK:
                    blockX = 2;
                    blockY = 4;
                    break;
                case GOLD_ORE:
                    blockX = 3;
                    blockY = 4;
                    break;
                case GRASS: //Default for Latest Release
                    blockX = 4;
                    blockY = 4;
                    break;
                case IRON_ORE:
                    blockX = 5;
                    blockY = 4;
                    break;
                case LOG_SPRUCE:
                    blockX = 6;
                    blockY = 4;
                    break;
                case RED_SANDSTONE:
                    blockX = 7;
                    blockY = 4;
                    break;
                case LAPIS_ORE:
                    blockX = 0;
                    blockY = 5;
                    break;
                case LEAVES_BIRCH:
                    blockX = 1;
                    blockY = 5;
                    break;
                case LEAVES_JUNGLE:
                    blockX = 2;
                    blockY = 5;
                    break;
                case BOOKSHELF:
                    blockX = 3;
                    blockY = 5;
                    break;
                case LEAVES_SPRUCE:
                    blockX = 4;
                    blockY = 5;
                    break;
                case LOG_ACACIA:
                    blockX = 5;
                    blockY = 5;
                    break;
                case MYCELIUM:
                    blockX = 6;
                    blockY = 5;
                    break;
                case REDSTONE_BLOCK:
                    blockX = 7;
                    blockY = 5;
                    break;
                case NETHER_BRICK:
                    blockX = 0;
                    blockY = 6;
                    break;
                case NETHERRACK:
                    blockX = 1;
                    blockY = 6;
                    break;
                case OBSIDIAN:
                    blockX = 2;
                    blockY = 6;
                    break;
                case PLANKS_ACACIA:
                    blockX = 3;
                    blockY = 6;
                    break;
                case PLANKS_BIRCH:
                    blockX = 4;
                    blockY = 6;
                    break;
                case PLANKS_DARKOAK:
                    blockX = 5;
                    blockY = 6;
                    break;
                case PLANKS_JUNGLE:
                    blockX = 6;
                    blockY = 6;
                    break;
                case REDSTONE_ORE:
                    blockX = 7;
                    blockY = 6;
                    break;
                case SAND:
                    blockX = 0;
                    blockY = 7;
                    break;
                case SANDSTONE:
                    blockX = 1;
                    blockY = 7;
                    break;
                case SNOW:
                    blockX = 2;
                    blockY = 7;
                    break;
                case SOUL_SAND:
                    blockX = 3;
                    blockY = 7;
                    break;
                case STONE:
                    blockX = 4;
                    blockY = 7;
                    break;
                case STONE_ANDESITE:
                    blockX = 5;
                    blockY = 7;
                    break;
                case STONE_DIORITE:
                    blockX = 6;
                    blockY = 7;
                    break;
                case STONE_GRANITE:
                    blockX = 7;
                    blockY = 7;
                    break;
            }

            pw.setPixels(0, 0, 136, 136, pr, blockX * 136, blockY * 136);
        } catch (Exception e) {
            e.printStackTrace();
        }
        iconCache.put(p, wi);
        return wi;
    }

    public Image resampleImage(Image input, int scaleFactor) {
        final int W = (int) input.getWidth();
        final int H = (int) input.getHeight();

        WritableImage output = new WritableImage(
                W * scaleFactor,
                H * scaleFactor
        );

        PixelReader reader = input.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                final int argb = reader.getArgb(x, y);
                for (int dy = 0; dy < scaleFactor; dy++) {
                    for (int dx = 0; dx < scaleFactor; dx++) {
                        writer.setArgb(x * scaleFactor + dx, y * scaleFactor + dy, argb);
                    }
                }
            }
        }

        return output;
    }

    public Alert buildAlert(Alert.AlertType type, String header, String content) {
        Alert a = new Alert(type);
        a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        a.setContentText(content);
        a.setHeaderText(header);
        Stage s = (Stage)a.getDialogPane().getScene().getWindow();
        s.getIcons().add(applicationIcon);
        return a;
    }
}
