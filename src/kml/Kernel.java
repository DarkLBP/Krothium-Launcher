package kml;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import kml.gui.BrowserFX;
import kml.gui.MainFX;
import org.json.JSONObject;

import javax.swing.*;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
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
    private final MainFX mainForm;
    private final BrowserFX webBrowser;
    private JSONObject launcherProfiles;
    public static Kernel instance;

    public Kernel(Stage stage, HostServices hs) {
        instance = this;
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
        s.getIcons().add(Constants.APPLICATION_ICON);
        s.setTitle("Krothium Minecraft Launcher");
        s.setScene(new Scene(p));
        s.setResizable(false);
        s.setMaximized(false);
        s.setOnCloseRequest(e -> {
            e.consume();
            Alert ask = new Alert(Alert.AlertType.CONFIRMATION);
            Stage st = (Stage) ask.getDialogPane().getScene().getWindow();
            st.getIcons().add(Constants.APPLICATION_ICON);
            ask.setContentText("This ads makes this service alive.\nTo close the ad you need to wait 5 seconds and click Skip Ad.\n" +
                    "If you don't want to wait you can always make a donation.\nDo you want to donate now?");
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
        stage.getIcons().add(Constants.APPLICATION_ICON);
        stage.setTitle("Krothium Minecraft Launcher");
        stage.setScene(new Scene(p));
        stage.setResizable(false);
        stage.setMaximized(false);
        stage.setOnCloseRequest(e -> exitSafely());
        mainForm = loader2.getController();
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
            } catch (Throwable t) {
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

    public void exitSafely() {
        this.saveProfiles();
        this.console.printInfo("Shutting down launcher...");
        this.console.close();
        Platform.exit();
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
        JOptionPane.showMessageDialog(null, "Failed to load JavaFX. Please update Java.", "Error", JOptionPane.ERROR_MESSAGE);
        exitSafely();
    }

    public MainFX getGUI() {
        return this.mainForm;
    }

    public BrowserFX getBrowser() {
        return this.webBrowser;
    }
}
