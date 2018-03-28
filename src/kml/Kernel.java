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
import javafx.stage.Stage;
import kml.auth.Authentication;
import kml.game.GameLauncher;
import kml.game.download.Downloader;
import kml.game.profile.ProfileIcon;
import kml.game.profile.Profiles;
import kml.game.version.Versions;
import kml.gui.AlertType;
import kml.gui.BrowserFX;
import kml.gui.MainFX;
import kml.gui.lang.Language;
import kml.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;


/**
 * @author DarkLBP
 * website https://krothium.com
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
    private BrowserFX webBrowser;
    private JSONObject launcherProfiles;

    private final Image profileIcons;
    private final Map<ProfileIcon, Image> iconCache;
    public static final String KERNEL_BUILD_NAME = "3.1.5";
    private static final int KERNEL_FORMAT = 21;
    private static final int KERNEL_PROFILES_FORMAT = 2;
    public static final File APPLICATION_WORKING_DIR = Utils.getWorkingDirectory();
    private static final File APPLICATION_CONFIG = new File(APPLICATION_WORKING_DIR, "launcher_profiles.json");
    public static final File APPLICATION_LOGS = new File(APPLICATION_WORKING_DIR, "logs");
    public static final File APPLICATION_CACHE = new File(APPLICATION_WORKING_DIR, "cache");
    public static File JAVA_PATH;
    public static Image APPLICATION_ICON;
    public static boolean USE_LOCAL;

    public Kernel(Stage stage, HostServices hs) {
        if (!APPLICATION_WORKING_DIR.isDirectory()) {
            APPLICATION_WORKING_DIR.mkdirs();
        }
        APPLICATION_CACHE.mkdir();
        APPLICATION_LOGS.mkdir();
        console = new Console();
        try {
            int response = Utils.testNetwork();
            USE_LOCAL = response != 204;
        } catch (IOException ex) {
            USE_LOCAL = true;
            console.print("Running offline mode.");
            ex.printStackTrace(console.getWriter());
        }
        console.print("KML v" + KERNEL_BUILD_NAME + " by DarkLBP (https://krothium.com)");
        console.print("OS: " + System.getProperty("os.name"));
        console.print("OS Version: " + System.getProperty("os.version"));
        console.print("OS Architecture: " + System.getProperty("os.arch"));
        console.print("Java Version: " + System.getProperty("java.version"));
        console.print("Java Vendor: " + System.getProperty("java.vendor"));
        console.print("Java Architecture: " + System.getProperty("sun.arch.data.model"));
        if (Utils.getPlatform().equals(OS.WINDOWS)) {
            JAVA_PATH = new File(APPLICATION_WORKING_DIR, "jre" + File.separator + "javaw.exe");
        } else {
            JAVA_PATH = new File(APPLICATION_WORKING_DIR, "jre" + File.separator + "java");
        }
        console.print("Custom Java: " + JAVA_PATH.isFile());
        try {
            Class.forName("javafx.fxml.FXMLLoader");
            console.print("JavaFX loaded.");
        } catch (ClassNotFoundException e) {
            File jfxrt = new File(System.getProperty("java.home"), "lib/jfxrt.jar");
            if (jfxrt.isFile()) {
                console.print("Attempting to load JavaFX manually...");
                try {
                    if (addToSystemClassLoader(jfxrt)) {
                        console.print("JavaFX loaded manually.");
                    } else {
                        console.print("Found JavaFX but it couldn't be loaded!");
                        warnJavaFX();
                    }
                } catch (Throwable e2) {
                    console.print("Found JavaFX but it couldn't be loaded!");
                    e2.printStackTrace(console.getWriter());
                    warnJavaFX();
                }
            } else {
                console.print("JavaFX library not found. Please update Java!");
                warnJavaFX();
            }
        }
        console.print("Using custom HTTPS certificate checker? | " + Utils.ignoreHTTPSCert());
        console.print("Reading launcher profiles...");
        try {
            if (APPLICATION_CONFIG.isFile()) {
                String data = new String(Files.readAllBytes(APPLICATION_CONFIG.toPath()), StandardCharsets.UTF_8);
                launcherProfiles = new JSONObject(data);
            } else {
                console.print("Launcher profiles file does not exists.");
            }
        } catch (MalformedURLException | JSONException e) {
            console.print("Malformed launcher profiles file.");
            e.printStackTrace(console.getWriter());
        } catch (IOException ex) {
            console.print("Failed to read launcher profiles file.");
            ex.printStackTrace(console.getWriter());
        }

        //Initialize constants
        APPLICATION_ICON = new Image("/kml/gui/textures/icon.png");
        profileIcons = new Image("/kml/gui/textures/profile_icons.png");
        iconCache = new EnumMap<>(ProfileIcon.class);

        //Prepare loader
        FXMLLoader loader = new FXMLLoader();

        //Load launcher data
        profiles = new Profiles(this);
        versions = new Versions(this);
        settings = new Settings(this);
        downloader = new Downloader(this);
        authentication = new Authentication(this);
        gameLauncher = new GameLauncher(this);
        hostServices = hs;
        settings.loadSettings();
        versions.fetchVersions();
        profiles.fetchProfiles();
        authentication.fetchUsers();

        //Load web browser
        try {
            loader.setLocation(getClass().getResource("/kml/gui/fxml/Browser.fxml"));
            Parent p = loader.load();
            Scene browser = new Scene(p);
            webBrowser = loader.getController();
            webBrowser.initialize(stage, browser);

        } catch (IOException e) {
            console.print("Failed to initialize web browser.");
            e.printStackTrace(console.getWriter());
            exitSafely();
        }

        //Load main form
        try {
            loader.setLocation(getClass().getResource("/kml/gui/fxml/Main.fxml"));
            loader.setRoot(null);
            loader.setController(null);
            Parent p = loader.load();
            Scene main = new Scene(p);
            stage.getIcons().add(APPLICATION_ICON);
            stage.setTitle("Krothium Minecraft Launcher");
            stage.setOnCloseRequest((e) -> {
                settings.setLauncherHeight(main.getWindow().getHeight());
                settings.setLauncherWidth(main.getWindow().getWidth());
                exitSafely();
            });
            stage.setScene(main);
            stage.setHeight(settings.getLauncherHeight());
            stage.setWidth(settings.getLauncherWidth());
            MainFX mainForm = loader.getController();
            mainForm.initialize(this, stage, main);
            stage.show();
        } catch (IOException e) {
            console.print("Failed to initialize main interface.");
            e.printStackTrace(console.getWriter());
            exitSafely();
        }
    }

    /**
     * Loads a JAR file dynamically
     *
     * @param file The JAR file to be loaded
     * @return A boolean indicating if the file has been loaded
     */
    private static boolean addToSystemClassLoader(File file) {
        if (ClassLoader.getSystemClassLoader() instanceof URLClassLoader) {
            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            try {
                Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(classLoader, file.toURI().toURL());
                return true;
            } catch (NoSuchMethodException | MalformedURLException | InvocationTargetException | IllegalAccessException t) {
                return false;
            }
        }
        return false;
    }

    public Console getConsole() {
        return console;
    }

    public HostServices getHostServices() {
        return hostServices;
    }

    /**
     * Saves the profiles
     */
    public void saveProfiles() {
        console.print("Saving profiles...");
        JSONObject output = new JSONObject();
        JSONObject profilesJSON = profiles.toJSON();
        JSONObject authdata = authentication.toJSON();
        Set pset = profilesJSON.keySet();
        for (Object aPset : pset) {
            String name = aPset.toString();
            output.put(name, profilesJSON.get(name));
        }
        Set aset = authdata.keySet();
        for (Object anAset : aset) {
            String name = anAset.toString();
            output.put(name, authdata.get(name));
        }
        output.put("settings", settings.toJSON());
        JSONObject launcherVersion = new JSONObject();
        launcherVersion.put("name", KERNEL_BUILD_NAME);
        launcherVersion.put("format", KERNEL_FORMAT);
        launcherVersion.put("profilesFormat", KERNEL_PROFILES_FORMAT);
        output.put("launcherVersion", launcherVersion);
        if (!Utils.writeToFile(output.toString(2), APPLICATION_CONFIG)) {
            console.print("Failed to save the profiles file!");
        } else {
            console.print("Profiles saved.");
        }
    }

    public Versions getVersions() {
        return versions;
    }

    public Profiles getProfiles() {
        return profiles;
    }

    public Settings getSettings() {
        return settings;
    }

    public Downloader getDownloader() {
        return downloader;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public JSONObject getLauncherProfiles() {
        return launcherProfiles;
    }

    public GameLauncher getGameLauncher() {
        return gameLauncher;
    }

    /**
     * Saves the profiles and shuts down the launcher
     */
    public void exitSafely() {
        console.print("Shutting down launcher...");
        console.close();
        saveProfiles();
        System.exit(0);
    }

    /**
     * Warns the user that JavaFX is not available
     */
    private void warnJavaFX() {
        JOptionPane.showMessageDialog(null, Language.get(9), "Error", JOptionPane.ERROR_MESSAGE);
        exitSafely();
    }

    public BrowserFX getBrowser() {
        return webBrowser;
    }

    /**
     * Gets a profile icon from the icons map
     *
     * @param p The desired profile icon
     * @return The image with the profile icon
     */
    public Image getProfileIcon(ProfileIcon p) {
        if (iconCache.containsKey(p)) {
            return iconCache.get(p);
        }
        WritableImage wi = new WritableImage(68, 68);
        PixelWriter pw = wi.getPixelWriter();
        int blockX = 0;
        int blockY = 0;
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
        pw.setPixels(0, 0, 68, 68, pr, blockX * 68, blockY * 68);
        iconCache.put(p, wi);
        return wi;
    }

    /**
     * Constructs an alert with the application icon
     *
     * @param type    The alert type
     * @param title   The header text
     * @param content The content text
     * @return The built alert
     */
    public int showAlert(AlertType type, String title, String content) {
        try {
            Class.forName("javafx.scene.control.Alert");
            Alert a = new Alert(Alert.AlertType.valueOf(type.name()));
            ((Stage) a.getDialogPane().getScene().getWindow()).getIcons().add(APPLICATION_ICON);
            a.setTitle(title);
            a.setHeaderText(title);
            a.setContentText(content);
            a.showAndWait();
            if (type == AlertType.CONFIRMATION) {
                if (a.getResult() == ButtonType.OK) {
                    return 1;
                }
                return 0;
            }
        } catch (ClassNotFoundException e) {
            //Using legacy
            int messageType;
            switch (type) {
                case CONFIRMATION:
                    messageType = JOptionPane.QUESTION_MESSAGE;
                    break;
                case WARNING:
                    messageType = JOptionPane.WARNING_MESSAGE;
                    break;
                case INFORMATION:
                    messageType = JOptionPane.INFORMATION_MESSAGE;
                    break;
                case ERROR:
                    messageType = JOptionPane.ERROR_MESSAGE;
                    break;
                default:
                    messageType = JOptionPane.INFORMATION_MESSAGE;

            }
            if (type != AlertType.CONFIRMATION) {
                JOptionPane.showMessageDialog(null, content, title, messageType);
            } else {
                if (JOptionPane.showConfirmDialog(null, content, title, JOptionPane.YES_NO_OPTION, messageType) == JOptionPane.YES_OPTION) {
                    return 1;
                }
                return 0;
            }
        }
        return -1;
    }
}
