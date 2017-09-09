package kml;

import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
import java.util.EnumMap;
import java.util.Map;
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
    private final Map<ProfileIcon, Image> iconCache;

    public Kernel(Stage stage, HostServices hs) {
        if (!Constants.APPLICATION_WORKING_DIR.isDirectory()) {
            Constants.APPLICATION_WORKING_DIR.mkdirs();
        }
        Constants.APPLICATION_CACHE.mkdir();
        this.console = new Console();
        try {
            int response = Utils.testNetwork();
            Constants.USE_LOCAL = response != 204;
        } catch (IOException ex) {
            Constants.USE_LOCAL = true;
            this.console.print("Running offline mode.");
            ex.printStackTrace(this.console.getWriter());
        }
        this.console.print("KML v" + Constants.KERNEL_BUILD_NAME + " by DarkLBP (https://krothium.com)");
        this.console.print("Kernel build: " + Constants.KERNEL_BUILD);
        this.console.print("OS: " + System.getProperty("os.name"));
        this.console.print("OS Version: " + System.getProperty("os.version"));
        this.console.print("OS Architecture: " + System.getProperty("os.arch"));
        this.console.print("Java Version: " + System.getProperty("java.version"));
        this.console.print("Java Vendor: " + System.getProperty("java.vendor"));
        this.console.print("Java Architecture: " + System.getProperty("sun.arch.data.model"));
        try {
            this.getClass().getClassLoader().loadClass("javafx.embed.swing.JFXPanel");
            this.console.print("JavaFX loaded.");
        } catch (ClassNotFoundException e) {
            File jfxrt = new File(System.getProperty("java.home"), "lib/jfxrt.jar");
            if (jfxrt.isFile()) {
                this.console.print("Attempting to load JavaFX manually...");
                try {
                    if (addToSystemClassLoader(jfxrt)) {
                        this.console.print("JavaFX loaded manually.");
                    } else {
                        this.console.print("Found JavaFX but it couldn't be loaded!");
                        this.warnJavaFX();
                    }
                } catch (Throwable e2) {
                    this.console.print("Found JavaFX but it couldn't be loaded!");
                    e2.printStackTrace(this.console.getWriter());
                    this.warnJavaFX();
                }
            } else {
                this.console.print("JavaFX library not found. Please update Java!");
                this.warnJavaFX();
            }
        }
        this.console.print("Using custom HTTPS certificate checker? | " + Utils.ignoreHTTPSCert());
        this.console.print("Reading launcher profiles...");
        try {
            if (Constants.APPLICATION_CONFIG.isFile()) {
                this.launcherProfiles = new JSONObject(Utils.readURL(Constants.APPLICATION_CONFIG.toURI().toURL()));
            } else {
                this.console.print("Launcher profiles file does not exists.");
            }
        } catch (MalformedURLException | JSONException e) {
            this.console.print("Malformed launcher profiles file.");
            e.printStackTrace(this.console.getWriter());
        } catch (IOException ex) {
            this.console.print("Failed to read launcher profiles file.");
            ex.printStackTrace(this.console.getWriter());
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
        this.profileIcons = new Image("/kml/gui/textures/profile_icons.png");
        this.applicationIcon = new Image("/kml/gui/textures/icon.png");
        this.iconCache = new EnumMap<>(ProfileIcon.class);

        //Load web browser
        Stage s = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("/kml/gui/fxml/Browser.fxml"));
        Parent p;
        try {
            p = loader.load();
        } catch (IOException e) {
            p = null;
            this.console.print("Failed to initialize JavaFX GUI!");
            e.printStackTrace(this.console.getWriter());
            this.exitSafely();
        }
        s.getIcons().add(this.applicationIcon);
        s.setTitle("Krothium Minecraft Launcher");
        s.setScene(new Scene(p));
        s.setResizable(false);
        s.setMaximized(false);
        s.setOnCloseRequest(e -> {
            e.consume();
            Alert ask = this.buildAlert(AlertType.CONFIRMATION, null, Language.get(94) + System.lineSeparator() + Language.get(95) + System.lineSeparator() +
                    Language.get(96) + System.lineSeparator() + Language.get(97));
            Optional<ButtonType> response = ask.showAndWait();
            if (response.isPresent() && response.get() == ButtonType.OK) {
                this.hostServices.showDocument("https://krothium.com/donaciones/");
            }
        });
        this.webBrowser = loader.getController();
        this.webBrowser.initialize(s);

        //Load main form
        FXMLLoader loader2 = new FXMLLoader();
        loader2.setLocation(this.getClass().getResource("/kml/gui/fxml/Main.fxml"));
        try {
            p = loader2.load();
        } catch (IOException e) {
            p = null;
            this.console.print("Failed to initialize JavaFX GUI!");
            e.printStackTrace(this.console.getWriter());
            this.exitSafely();
        }
        stage.getIcons().add(this.applicationIcon);
        stage.setTitle("Krothium Minecraft Launcher");
        stage.setScene(new Scene(p));
        stage.setResizable(false);
        stage.setMaximized(false);
        stage.setOnCloseRequest(e -> this.exitSafely());
        MainFX mainForm = loader2.getController();
        mainForm.initialize(this, stage);
    }

    /**
     * Loads a JAR file dynamically
     * @param file The JAR file to be laoded
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
        return this.console;
    }

    public HostServices getHostServices() {
        return this.hostServices;
    }

    /**
     * Saves the profiles
     */
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
            this.console.print("Failed to save the profiles file!");
        }
    }

    private void loadProfiles() {
        this.profiles.fetchProfiles();
    }

    private void loadVersions() {
        this.versions.fetchVersions();
    }

    private void loadUsers() {
        this.authentication.fetchUsers();
    }

    private void loadSettings() {
        this.settings.loadSettings();
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
        return this.launcherProfiles;
    }

    public GameLauncher getGameLauncher() {
        return this.gameLauncher;
    }

    /**
     * Saves the profiles and shuts down the launcher
     */
    public void exitSafely() {
        this.saveProfiles();
        this.console.print("Shutting down launcher...");
        this.console.close();
        System.exit(0);
    }

    /**
     * Checks for launcher updates
     * @return The update url
     */
    public String checkForUpdates() {
        String r;
        try {
            r = Utils.readURL(Constants.GETLATEST_URL);
        } catch (IOException e) {
            e.printStackTrace(this.console.getWriter());
            r = null;
        }
        if (r == null) {
            this.console.print("Failed to check for updates");
            return null;
        }
        String[] data = r.split(":");
        if (data.length == 2) {
            try {
                int version = Integer.parseInt(Utils.fromBase64(data[0]));
                if (version > Constants.KERNEL_BUILD) {
                    this.console.print("New kernel build available: " + version);
                    return data[1];
                } else {
                    this.console.print("No updates found.");
                }
            } catch (NumberFormatException e) {
                this.console.print("Invalid check for updates reponse from the server.");
            }
        } else {
            this.console.print("Invalid check for updates reponse from the server.");
        }
        return null;
    }

    /**
     * Warns the user that JavaFX is not available
     */
    private void warnJavaFX() {
        JOptionPane.showMessageDialog(null, Language.get(9), "Error", JOptionPane.ERROR_MESSAGE);
        this.exitSafely();
    }

    public BrowserFX getBrowser() {
        return this.webBrowser;
    }

    /**
     * Gets a profile icon from the icons map
     * @param p The desired profile icon
     * @return The image with the profile icon
     */
    public Image getProfileIcon(ProfileIcon p) {
        if (this.iconCache.containsKey(p)) {
            return this.iconCache.get(p);
        }
        WritableImage wi = new WritableImage(136, 136);
        PixelWriter pw = wi.getPixelWriter();
        int blockX = 0;
        int blockY = 0;
        PixelReader pr = this.profileIcons.getPixelReader();
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
        this.iconCache.put(p, wi);
        return wi;
    }

    /**
     * This method receives an image scales it
     * @param input Input image
     * @param scaleFactor Output scale factor
     * @return The resampled image
     */
    public Image resampleImage(Image input, int scaleFactor) {
        int W = (int) input.getWidth();
        int H = (int) input.getHeight();

        WritableImage output = new WritableImage(
                W * scaleFactor,
                H * scaleFactor
        );

        PixelReader reader = input.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int argb = reader.getArgb(x, y);
                for (int dy = 0; dy < scaleFactor; dy++) {
                    for (int dx = 0; dx < scaleFactor; dx++) {
                        writer.setArgb(x * scaleFactor + dx, y * scaleFactor + dy, argb);
                    }
                }
            }
        }

        return output;
    }

    /**
     * Constructs an alert with the application icon
     * @param type The alert type
     * @param header The header text
     * @param content The content text
     * @return The built alert
     */
    public Alert buildAlert(AlertType type, String header, String content) {
        Alert a = new Alert(type);
        a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        a.setContentText(content);
        a.setHeaderText(header);
        Stage s = (Stage)a.getDialogPane().getScene().getWindow();
        s.getIcons().add(this.applicationIcon);
        return a;
    }
}
