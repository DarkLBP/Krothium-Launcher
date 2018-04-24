package kml;

import javafx.application.HostServices;
import javafx.event.EventHandler;
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
import javafx.stage.WindowEvent;
import kml.auth.Authentication;
import kml.game.GameLauncher;
import kml.game.download.Downloader;
import kml.game.profile.Profiles;
import kml.game.version.Versions;
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
import java.util.HashMap;
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
    private final Map<String, int[]> icons = new HashMap<>();
    private final Map<String, Image> iconCache = new HashMap<>();

    private final Image profileIcons;
    public static final String KERNEL_BUILD_NAME = "3.2.2";
    private static final int KERNEL_FORMAT = 21;
    private static final int KERNEL_PROFILES_FORMAT = 2;
    public static final File APPLICATION_WORKING_DIR = Utils.getWorkingDirectory();
    private static final File APPLICATION_CONFIG = new File(APPLICATION_WORKING_DIR, "launcher_profiles.json");
    public static final File APPLICATION_LOGS = new File(APPLICATION_WORKING_DIR, "logs");
    public static final File APPLICATION_CACHE = new File(APPLICATION_WORKING_DIR, "cache");
    public static Image APPLICATION_ICON;
    public static boolean USE_LOCAL;

    public Kernel(Stage stage, HostServices hs) {
        if (!APPLICATION_WORKING_DIR.isDirectory()) {
            APPLICATION_WORKING_DIR.mkdirs();
        }
        APPLICATION_CACHE.mkdir();
        APPLICATION_LOGS.mkdir();
        console = new Console();
        console.print("KML v" + KERNEL_BUILD_NAME + " by DarkLBP (https://krothium.com)");
        console.print("OS: " + System.getProperty("os.name"));
        console.print("OS Version: " + System.getProperty("os.version"));
        console.print("OS Architecture: " + System.getProperty("os.arch"));
        console.print("Java Version: " + System.getProperty("java.version"));
        console.print("Java Vendor: " + System.getProperty("java.vendor"));
        console.print("Java Architecture: " + System.getProperty("sun.arch.data.model"));
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
            final Scene main = new Scene(p);
            stage.getIcons().add(APPLICATION_ICON);
            stage.setTitle("Krothium Minecraft Launcher");
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    if (main.getWindow() != null) {
                        settings.setLauncherHeight(main.getWindow().getHeight());
                        settings.setLauncherWidth(main.getWindow().getWidth());
                    }
                    exitSafely();
                }
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

    private void loadIcons() {
        icons.put("Leaves_Oak", new int[]{0, 0});
        icons.put("Bedrock", new int[]{1, 0});
        icons.put("Clay", new int[]{2, 0});
        icons.put("Diamond_Block", new int[]{3, 0});
        icons.put("End_Stone", new int[]{4, 0});
        icons.put("Gravel", new int[]{5, 0});
        icons.put("Log_Birch", new int[]{6, 0});
        icons.put("Planks_Oak", new int[]{7, 0});
        icons.put("TNT", new int[]{8, 0});
        icons.put("Brick", new int[]{0, 1});
        icons.put("Chest", new int[]{1, 1});
        icons.put("Coal_Block", new int[]{2, 1});
        icons.put("Diamond_Ore", new int[]{3, 1});
        icons.put("Farmland", new int[]{4, 1});
        icons.put("Hardened_Clay", new int[]{5, 1});
        icons.put("Log_DarkOak", new int[]{6, 1});
        icons.put("Planks_Spruce", new int[]{7, 1});
        icons.put("Wool", new int[]{8, 1});
        icons.put("Coal_Ore", new int[]{0, 1});
        icons.put("Cobblestone", new int[]{1, 2});
        icons.put("Crafting_Table", new int[]{2, 2}); //Default for Latest Snapshot
        icons.put("Dirt", new int[]{3, 2});
        icons.put("Furnace", new int[]{4, 2}); //Default for custom profiles
        icons.put("Ice_Packed", new int[]{5, 2});
        icons.put("Log_Jungle", new int[]{6, 2});
        icons.put("Quartz_Ore", new int[]{7, 2});
        icons.put("Dirt_Podzol", new int[]{0, 3});
        icons.put("Dirt_Snow", new int[]{1, 3});
        icons.put("Emerald_Block", new int[]{2, 3});
        icons.put("Emerald_Ore", new int[]{3, 3});
        icons.put("Furnace_On", new int[]{4, 3});
        icons.put("Iron_Block", new int[]{5, 3});
        icons.put("Log_Oak", new int[]{6, 3});
        icons.put("Red_Sand", new int[]{7, 3});
        icons.put("Glass", new int[]{0, 4});
        icons.put("Glowstone", new int[]{1, 4});
        icons.put("Gold_Block", new int[]{2, 4});
        icons.put("Gold_Ore", new int[]{3, 4});
        icons.put("Grass", new int[]{4, 4}); //Default for Latest Release
        icons.put("Iron_Ore", new int[]{5, 4});
        icons.put("Log_Spruce", new int[]{6, 4});
        icons.put("Red_Sandstone", new int[]{7, 4});
        icons.put("Lapis_Ore", new int[]{0, 5});
        icons.put("Leaves_Birch", new int[]{1, 5});
        icons.put("Leaves_Jungle", new int[]{2, 5});
        icons.put("Bookshelf", new int[]{3, 5});
        icons.put("Leaves_Spruce", new int[]{4, 5});
        icons.put("Log_Acacia", new int[]{5, 5});
        icons.put("Mycelium", new int[]{6, 5});
        icons.put("Redstone_Block", new int[]{7, 5});
        icons.put("Nether_Brick", new int[]{0, 6});
        icons.put("Netherrack", new int[]{1, 6});
        icons.put("Obsidian", new int[]{2, 6});
        icons.put("Planks_Acacia", new int[]{3, 6});
        icons.put("Planks_Birch", new int[]{4, 6});
        icons.put("Planks_DarkOak", new int[]{5, 6});
        icons.put("Planks_Jungle", new int[]{6, 6});
        icons.put("Redstone_Ore", new int[]{7, 6});
        icons.put("Sand", new int[]{0, 7});
        icons.put("Sandstone", new int[]{1, 7});
        icons.put("Snow", new int[]{2, 7});
        icons.put("Soul_Sand", new int[]{3, 7});
        icons.put("Stone", new int[]{4, 7});
        icons.put("Stone_Andesite", new int[]{5, 7});
        icons.put("Stone_Diorite", new int[]{6, 7});
        icons.put("Stone_Granite", new int[]{7, 7});
    }

    public Map<String, int[]> getIcons() {
        return icons;
    }
    /**
     * Gets a profile icon from the icons map
     *
     * @param profileIcon The desired profile icon
     * @return The image with the profile icon
     */
    public Image getProfileIcon(String profileIcon) {
        if (iconCache.containsKey(profileIcon)) {
            return iconCache.get(profileIcon);
        } else if (icons.isEmpty()) {
            loadIcons();
        }
        int[] coords = icons.get(profileIcon);
        if (coords == null) {
            if (iconCache.containsKey("Furnace")) {
                return iconCache.get("Furnace");
            }
            coords = icons.get("Furnace");
        }
        WritableImage wi = new WritableImage(68, 68);
        PixelWriter pw = wi.getPixelWriter();
        PixelReader pr = profileIcons.getPixelReader();
        pw.setPixels(0, 0, 68, 68, pr, coords[0] * 68, coords[1] * 68);
        iconCache.put(profileIcon, wi);
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
    public int showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        ((Stage) a.getDialogPane().getScene().getWindow()).getIcons().add(APPLICATION_ICON);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(content);
        a.showAndWait();
        if (type == Alert.AlertType.CONFIRMATION) {
            if (a.getResult() == ButtonType.OK) {
                return 1;
            }
            return 0;
        }
        return -1;
    }
}
