package kml.gui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.Styleable;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;
import kml.*;
import kml.auth.Authentication;
import kml.auth.user.User;
import kml.auth.user.UserType;
import kml.exceptions.AuthenticationException;
import kml.exceptions.DownloaderException;
import kml.exceptions.GameLauncherException;
import kml.game.GameLauncher;
import kml.game.download.Downloader;
import kml.game.profile.Profile;
import kml.game.profile.ProfileType;
import kml.game.profile.Profiles;
import kml.game.version.VersionMeta;
import kml.game.version.VersionType;
import kml.game.version.Versions;
import kml.game.version.asset.TexturePreview;
import kml.gui.lang.Language;
import kml.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class MainFX {

    @FXML private Label progressText, newsLabel, skinsLabel, settingsLabel, launchOptionsLabel,
            keepLauncherOpen, outputLog, enableSnapshots, historicalVersions,
            advancedSettings, resolutionLabel, gameDirLabel, javaExecLabel, javaArgsLabel, accountButton,
            switchAccountButton, languageButton, newsTitle, newsText, slideBack, slideForward, rotateRight,
            rotateLeft, includeCape, versionLabel, usernameLabel, passwordLabel, existingLabel, launcherSettings,
            nameLabel, profileVersionLabel, skinLabel, capeLabel, modelLabel, iconLabel, helpButton, gameVersion,
            authenticationLabel, authServer;
    @FXML private Button playButton, deleteButton, changeIcon, deleteSkin, deleteCape, logoutButton,
            loginButton, registerButton, loginExisting, cancelButton, saveButton, selectSkin,
            selectCape, exportLogs, downloadServer, deleteCache, profilePopupButton;
    @FXML private Tab loginTab, newsTab, skinsTab,
            settingsTab, launchOptionsTab, profileEditorTab;
    @FXML private ProgressBar progressBar;
    @FXML private TabPane contentPane;
    @FXML private ListView<Label> languagesList, profileList, profilePopupList;
    @FXML private ListView<ImageView> iconList;
    @FXML private VBox progressPane, existingPanel, playPane, skinActions, newsContainer;
    @FXML private HBox tabMenu, slideshowBox;
    @FXML private TextField username, profileName,javaExec, gameDir, javaArgs,
            resH, resW;
    @FXML private PasswordField password;
    @FXML private ComboBox<User> existingUsers;
    @FXML private ComboBox<VersionMeta> versionList;
    @FXML private StackPane versionBlock, javaArgsBlock, javaExecBlock, iconBlock;
    @FXML private ImageView profileIcon, slideshow, skinPreview;
    @FXML private RadioButton skinClassic, skinSlim, authKrothium, authMojang;
    @FXML private Hyperlink forgotPasswordLink;

    private Kernel kernel;
    private Console console;
    private Settings settings;
    private Stage stage;
    private Scene mainScene;
    private final List<Slide> slides = new ArrayList<>();
    private int currentSlide;
    private int currentPreview; // 0 = front / 1 = right / 2 = back / 3 = left
    private final Image[] skinPreviews = new Image[4];
    private Image skin, cape, alex, steve;
    private boolean texturesLoaded;
    private String urlPrefix = "";
    private boolean iconListLoaded, versionListLoaded, languageListLoaded, loadingTextures, profileListLoaded,
            profileListPopupLoaded;

    /**
     * Initializes all required stuff from the GUI
     * @param k The Kernel instance
     * @param s The Stage instance
     */
    public final void initialize(Kernel k, Stage s, Scene scene, final Scene browser) {
        //Require to exit using Platform.exit()
        Platform.setImplicitExit(false);

        //Set kernel and stage
        kernel = k;
        console = k.getConsole();
        settings = k.getSettings();
        stage = s;
        mainScene = scene;

        //Update version label
        versionLabel.setText(Kernel.KERNEL_BUILD_NAME);

        //Load news slideshow
        slideshowBox.setVisible(false);
        slideshowBox.setManaged(false);
        newsTitle.setText("Loading news...");
        newsText.setText("Please wait a moment...");
        loadSlideshow();

        //Refresh session
        refreshSession();

        //Prepare language list
        languageButton.setText(settings.getSupportedLocales().get(settings.getLocale()));

        //Update settings labels
        toggleLabel(keepLauncherOpen, settings.getKeepLauncherOpen());
        toggleLabel(outputLog, settings.getShowGameLog());
        toggleLabel(enableSnapshots, settings.getEnableSnapshots());
        toggleLabel(historicalVersions, settings.getEnableHistorical());
        toggleLabel(advancedSettings, settings.getEnableAdvanced());

        //Prepare Spinners
        resW.setEditable(true);
        resH.setEditable(true);

        //If offline mode make play button bigger for language support and hide skins tab
        if (Kernel.USE_LOCAL) {
            skinsLabel.setVisible(false);
            skinsLabel.setManaged(false);
        }

        //Localize elements
        localizeElements();

        //Validate selected profile
        validateSelectedProfile();

        //Manual component resize binding to fix JavaFX maximize bug
        TimerTask newsResize = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!MainFX.this.stage.isMaximized()) {
                            Window w = MainFX.this.mainScene.getWindow();
                            if (w == null) {
                                w = browser.getWindow();
                            }
                            if (w != null) {
                                settings.setLauncherHeight(w.getHeight());
                                settings.setLauncherWidth(w.getWidth());
                            }
                        }
                        double computedHeight = MainFX.this.newsContainer.heightProperty().doubleValue()  * 0.7;
                        double computedWidth = MainFX.this.newsContainer.widthProperty().doubleValue()  * 0.7;
                        if (MainFX.this.slideshow.getImage() != null) {
                            if (computedHeight > MainFX.this.slideshow.getImage().getHeight()) {
                                MainFX.this.slideshow.setFitHeight(MainFX.this.slideshow.getImage().getHeight());
                            } else {
                                MainFX.this.slideshow.setFitHeight(computedHeight);
                            }
                            if (computedWidth > MainFX.this.slideshow.getImage().getWidth()) {
                                MainFX.this.slideshow.setFitWidth(MainFX.this.slideshow.getImage().getWidth());
                            } else {
                                MainFX.this.slideshow.setFitWidth(computedWidth);
                            }
                        }
                    }
                });
            }
        };
        Timer resize = new Timer();
        resize.schedule(newsResize, 0, 25);

        //Close popups on resize
        mainScene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                MainFX.this.checkPopups();
            }
        });
        mainScene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                MainFX.this.checkPopups();
            }
        });
    }

    /**
     * Load language list
     */
    private void loadLanguages() {
        console.print("Loading languages...");
        HashMap<String, String> supportedLocales = settings.getSupportedLocales();
        ObservableList<Label> languageListItems = FXCollections.observableArrayList();
        for (String key : supportedLocales.keySet()) {
            Image i = new Image("/kml/gui/textures/flags/flag_" + key + ".png");
            Label l = new Label(supportedLocales.get(key), new ImageView(i));
            l.setId(key);
            languageListItems.add(l);
        }
        languagesList.setItems(languageListItems);
        console.print("Languages loaded.");
    }

    /**
     * Fetches any advertisement available for the logged user
     */
    private void fetchAds() {
        User user = kernel.getAuthentication().getSelectedUser();
        if (user.getType() != UserType.MOJANG) {
            String profileID = user.getSelectedProfile();
            String adsCheck = "https://mc.krothium.com/ads.php?profileID=" + profileID;
            String response = Utils.readURL(adsCheck);
            if (!response.isEmpty()) {
                String[] chunks = response.split(":");
                String firstChunk = Utils.fromBase64(chunks[0]);
                urlPrefix = firstChunk == null ? "" : firstChunk;
                if (chunks.length == 2) {
                    String secondChunk = Utils.fromBase64(response.split(":")[1]);
                    String adsURL = secondChunk == null ? "" : secondChunk;
                    kernel.getBrowser().loadWebsite(adsURL);
                    kernel.getBrowser().show(mainScene);
                }
                console.print("Ads loaded.");
            } else {
                console.print("Ads info not available.");
            }
        } else {
            console.print("Ads not available for Mojang user.");
        }
    }

    /**
     * Updates all components text with its localized Strings
     */
    private void localizeElements() {
        helpButton.setText(Language.get(2));
        logoutButton.setText(Language.get(3));
        newsLabel.setText(Language.get(4));
        skinsLabel.setText(Language.get(5));
        settingsLabel.setText(Language.get(6));
        launchOptionsLabel.setText(Language.get(7));
        if (kernel.getGameLauncher().isRunning()) {
            playButton.setText(Language.get(14));
        } else {
            if (Kernel.USE_LOCAL) {
                playButton.setText(Language.get(79));
            } else {
                playButton.setText(Language.get(12));
            }
        }
        usernameLabel.setText(Language.get(18));
        passwordLabel.setText(Language.get(19));
        loginButton.setText(Language.get(20));
        loginExisting.setText(Language.get(20));
        registerButton.setText(Language.get(21));
        changeIcon.setText(Language.get(24));
        exportLogs.setText(Language.get(27));
        downloadServer.setText(Language.get(28));
        skinLabel.setText(Language.get(29));
        capeLabel.setText(Language.get(30));
        launcherSettings.setText(Language.get(45));
        keepLauncherOpen.setText(Language.get(46));
        outputLog.setText(Language.get(47));
        enableSnapshots.setText(Language.get(48));
        historicalVersions.setText(Language.get(49));
        advancedSettings.setText(Language.get(50));
        saveButton.setText(Language.get(52));
        cancelButton.setText(Language.get(53));
        deleteButton.setText(Language.get(54));
        nameLabel.setText(Language.get(63));
        profileVersionLabel.setText(Language.get(64));
        resolutionLabel.setText(Language.get(65));
        gameDirLabel.setText(Language.get(66));
        javaExecLabel.setText(Language.get(67));
        javaArgsLabel.setText(Language.get(68));
        existingLabel.setText(Language.get(85));
        switchAccountButton.setText(Language.get(86));
        selectSkin.setText(Language.get(87));
        selectCape.setText(Language.get(87));
        deleteSkin.setText(Language.get(88));
        deleteCape.setText(Language.get(88));
        modelLabel.setText(Language.get(89));
        skinClassic.setText(Language.get(90));
        skinSlim.setText(Language.get(91));
        iconLabel.setText(Language.get(92));
        includeCape.setText(Language.get(93));
        deleteCache.setText(Language.get(94));
        forgotPasswordLink.setText(Language.get(97));
        profileName.setPromptText(Language.get(98));
        authenticationLabel.setText(Language.get(99));
        if (slides.isEmpty()) {
            newsTitle.setText(Language.get(102));
            newsText.setText(Language.get(103));
        }
        updateGameVersion();
    }

    /**
     * Loads the skin preview for the logged user
     */
    private void loadTextures() {
        if (loadingTextures) {
            return;
        }
        selectSkin.setDisable(true);
        selectCape.setDisable(true);
        deleteSkin.setDisable(true);
        deleteCape.setDisable(true);
        includeCape.setDisable(true);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    console.print("Loading textures...");
                    loadingTextures = true;
                    if (alex == null || steve == null) {
                        //Load placeholder skins
                        alex = new Image("/kml/gui/textures/alex.png");
                        steve = new Image("/kml/gui/textures/steve.png");
                    }
                    User selected = kernel.getAuthentication().getSelectedUser();
                    String domain;
                    if (selected.getType() == UserType.MOJANG) {
                        domain = "sessionserver.mojang.com/session/minecraft/profile/";
                        skinActions.setVisible(false);
                        skinActions.setManaged(false);
                    } else {
                        domain = "mc.krothium.com/profiles/";
                        skinActions.setVisible(true);
                        skinActions.setManaged(true);
                    }
                    String profileURL = "https://" + domain + selected.getSelectedProfile() + "?unsigned=true";
                    JSONObject root = new JSONObject(Utils.readURL(profileURL));
                    JSONArray properties = root.getJSONArray("properties");
                    for (int i = 0; i < properties.length(); i++) {
                        JSONObject property = properties.getJSONObject(i);
                        if ("textures".equalsIgnoreCase(property.getString("name"))) {
                            JSONObject data = new JSONObject(Utils.fromBase64(property.getString("value")));
                            JSONObject textures = data.getJSONObject("textures");
                            skin = null;
                            cape = null;
                            boolean slim = false;
                            if (textures.has("SKIN")) {
                                JSONObject skinData = textures.getJSONObject("SKIN");
                                if (skinData.has("metadata")) {
                                    if ("slim".equalsIgnoreCase(skinData.getJSONObject("metadata").getString("model"))) {
                                        slim = true;
                                    }
                                }
                                InputStream stream = Utils.readCachedStream(textures.getJSONObject("SKIN").getString("url"));
                                skin = new Image(stream);
                                stream.close();
                            }
                            if (skin == null || skin.getHeight() == 0 && !slim) {
                                skin = steve;
                            } else if (skin.getHeight() == 0) {
                                skin = alex;
                            } else {
                                deleteSkin.setDisable(false);
                            }
                            if (textures.has("CAPE")) {
                                InputStream stream = Utils.readCachedStream(textures.getJSONObject("CAPE").getString("url"));
                                cape = new Image(stream);
                                stream.close();
                                includeCape.setDisable(false);
                                deleteCape.setDisable(false);
                            }
                            if (slim) {
                                skinSlim.setSelected(true);
                            } else {
                                skinClassic.setSelected(true);
                            }
                            texturesLoaded = true;
                            console.print("Textures loaded.");
                            MainFX.this.updatePreview();
                        }
                    }
                    selectSkin.setDisable(false);
                    selectCape.setDisable(false);
                } catch (Exception ex) {
                    console.print("Failed to parse remote profile textures.");
                    ex.printStackTrace(console.getWriter());
                }
                loadingTextures = false;
            }
        });
        t.start();
    }

    /**
     * Toggles the label of the toggle cape button
     */
    @FXML public final void toggleCapePreview() {
        if (includeCape.getStyleClass().contains("toggle-enabled")) {
            toggleLabel(includeCape, false);
        } else {
            toggleLabel(includeCape, true);
        }
        updatePreview();
    }

    /**
     * Changes the skin type
     */
    @FXML public final void toggleSkinType() {
        if (deleteSkin.isDisabled()) {
            if (skinClassic.isSelected()) {
                skin = steve;
            } else {
                skin = alex;
            }
            updatePreview();
        }
    }

    /**
     * Updates the skin preview
     */
    private void updatePreview() {
        if (skin == null) {
            return;
        }
        boolean slim = skinSlim.isSelected();
        if (includeCape.getStyleClass().contains("toggle-enabled")) {
            skinPreviews[0] = TexturePreview.resampleImage(TexturePreview.generateFront(skin, cape, slim), 10);
            skinPreviews[1] = TexturePreview.resampleImage(TexturePreview.generateRight(skin, cape), 10);
            skinPreviews[2] = TexturePreview.resampleImage(TexturePreview.generateBack(skin, cape, slim), 10);
            skinPreviews[3] = TexturePreview.resampleImage(TexturePreview.generateLeft(skin, cape), 10);
        } else {
            skinPreviews[0] = TexturePreview.resampleImage(TexturePreview.generateFront(skin, null, slim), 10);
            skinPreviews[1] = TexturePreview.resampleImage(TexturePreview.generateRight(skin, null), 10);
            skinPreviews[2] = TexturePreview.resampleImage(TexturePreview.generateBack(skin, null, slim), 10);
            skinPreviews[3] = TexturePreview.resampleImage(TexturePreview.generateLeft(skin, null), 10);
        }
        skinPreview.setImage(skinPreviews[currentPreview]);
    }

    /**
     * Submits a texure change
     * @param target Skin or cape
     * @param file File to be submited. Null if it's a deletion.
     */
    private void submitChange(String target, File file) {
        String CHANGESKIN_URL = "https://mc.krothium.com/changeskin";
        String CHANGECAPE_URL = "https://mc.krothium.com/changecape";
        String url = target.equals("skin") ? CHANGESKIN_URL : CHANGECAPE_URL;
        Map<String, String> params = new HashMap<>();
        params.put("Access-Token", kernel.getAuthentication().getSelectedUser().getAccessToken());
        params.put("Client-Token", kernel.getAuthentication().getClientToken());
        byte[] data = null;
        if (file != null) {
            if (file.length() > 131072) {
                if (target.equals("skin")) {
                    console.print("Skin file exceeds 128KB file size limit.");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            kernel.showAlert(Alert.AlertType.ERROR, null, Language.get(105));
                        }
                    });
                } else {
                    console.print("Cape file exceeds 128KB file size limit.");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            kernel.showAlert(Alert.AlertType.ERROR, null, Language.get(104));
                        }
                    });
                }
                return;
            }
            try {
                data = Files.readAllBytes(file.toPath());
                if (target.equals("skin")) {
                    if (skinSlim.isSelected()) {
                        params.put("Skin-Type", "alex");
                    } else {
                        params.put("Skin-Type", "steve");
                    }
                }
                params.put("Content-Type", "image/png");
            } catch (Exception ex) {
                console.print("Failed read textures.");
                ex.printStackTrace(console.getWriter());
            }
        }
        try {
            String r = Utils.sendPost(url, data, params);
            String text;
            if (!"OK".equals(r)) {
                if (target.equals("skin")) {
                    if (file != null) {
                        text = Language.get(42);
                    } else {
                        text = Language.get(33);
                    }
                } else {
                    if (file != null) {
                        text = Language.get(43);
                    } else {
                        text = Language.get(38);
                    }
                }
                console.print("Failed to " + (file != null ? "change" : "delete") + " the " + target + ".");
                console.print(r);
                final String finalText = text;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        kernel.showAlert(Alert.AlertType.ERROR, null, finalText);
                    }
                });
                return;
            }
            if (target.equals("skin")) {
                if (file != null) {
                    text = Language.get(40);
                } else {
                    text = Language.get(34);
                }
            } else {
                if (file != null) {
                    text = Language.get(41);
                } else {
                    text = Language.get(39);
                }
            }
            target = target.substring(0, 1).toUpperCase() + target.substring(1);
            console.print(target + " " + (file != null ? "changed" : "deleted") + " successfully!");
            final String finalText = text;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    kernel.showAlert(Alert.AlertType.INFORMATION, null, finalText);
                }
            });
            loadTextures();
        } catch (IOException ex) {
            console.print("Failed to perform textures post.");
            ex.printStackTrace(console.getWriter());
        }
    }

    /**
     * Changes the skin of the user
     */
    @FXML private void changeSkin() {
        final File selected = selectFile(Language.get(44), "*.png", "open");
        if (selected != null) {
            selectSkin.setDisable(true);
            deleteSkin.setDisable(true);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    MainFX.this.submitChange("skin", selected);
                }
            });
            t.start();
        }
    }

    /**
     * Changes the cape of the user
     */
    @FXML private void changeCape() {
        final File selected = selectFile(Language.get(25), "*.png", "open");
        if (selected != null) {
            selectCape.setDisable(true);
            deleteCape.setDisable(true);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    MainFX.this.submitChange("cape", selected);
                }
            });
            t.start();
        }

    }

    /**
     * Deletes the skin of the user
     */
    @FXML private void deleteSkin() {
        int result = kernel.showAlert(Alert.AlertType.CONFIRMATION, null, Language.get(31));
        if (result == 1){
            selectSkin.setDisable(true);
            deleteSkin.setDisable(true);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    MainFX.this.submitChange("skin", null);
                }
            });
            t.start();
        }
    }

    /**
     * Deletes the cape of the user
     */
    @FXML private void deleteCape() {
        int result = kernel.showAlert(Alert.AlertType.CONFIRMATION, null, Language.get(36));
        if (result == 1){
            selectCape.setDisable(true);
            deleteCape.setDisable(true);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    MainFX.this.submitChange("cape", null);
                }
            });
            t.start();
        }
    }

    /**
     * Loads the news slideshow
     */
    private void loadSlideshow() {
        console.print("Loading news slideshow...");
        try {
            String newsURL = "https://launchermeta.mojang.com/mc/news.json";
            String response = Utils.readURL(newsURL);
            if (response.isEmpty()) {
                console.print("News data returned empty response.");
                return;
            }
            JSONObject root = new JSONObject(response);
            JSONArray entries = root.getJSONArray("entries");
            for (int i = 0; i < entries.length(); i++) {
                JSONObject entry = entries.getJSONObject(i);
                boolean isDemo = false;
                JSONArray tags = entry.getJSONArray("tags");
                for (int j = 0; j < tags.length(); j++) {
                    if ("demo".equalsIgnoreCase(tags.getString(j))) {
                        isDemo = true;
                        break;
                    }
                }
                if (isDemo) {
                    continue;
                }
                JSONObject content = entry.getJSONObject("content").getJSONObject("en-us");
                Slide s = new Slide(content.getString("action"), content.getString("image"), content.getString("title"), content.getString("text"));
                slides.add(s);
            }
        } catch (Exception ex) {
            newsTitle.setText(Language.get(80));
            newsText.setText(Language.get(101));
            console.print("Couldn't parse news data.");
            ex.printStackTrace(console.getWriter());
            return;
        }
        if (!slides.isEmpty()) {
            slideshowBox.setVisible(true);
            slideshowBox.setManaged(true);
            Slide s = slides.get(0);
            Image i = s.getImage();
            if (i != null) {
                slideshow.setImage(s.getImage());
            }
            newsTitle.setText(s.getTitle());
            newsText.setText(s.getText());
        } else {
            newsTitle.setText(Language.get(102));
            newsText.setText(Language.get(103));
        }
        console.print("News slideshow loaded.");
    }

    /**
     * Changes the news slide
     * @param e The trigger event
     */
    @FXML public final void changeSlide(MouseEvent e) {
        if (slides.isEmpty()) {
            //No slides
            return;
        }
        Label source = (Label)e.getSource();
        if (source == slideBack) {
            if (currentSlide == 0) {
                currentSlide = slides.size() - 1;
            } else {
                currentSlide--;
            }
        } else if (source == slideForward) {
            if (currentSlide == slides.size() - 1) {
                currentSlide = 0;
            } else {
                currentSlide++;
            }
        }
        final Slide s = slides.get(currentSlide);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Image i = s.getImage();
                if (i != null) {
                    slideshow.setImage(i);
                }
            }
        });
        t.start();
        newsTitle.setText(s.getTitle());

        newsText.setText(s.getText());
    }

    /**
     * Performs an action when a slide is clicked
     */
    @FXML public final void performSlideAction() {
        if (slides.isEmpty()) {
            //No slides
            return;
        }
        Slide s = slides.get(currentSlide);
        kernel.getHostServices().showDocument(urlPrefix + s.getAction());
    }

    /**
     * Rotates the skin preview
     * @param e The trigger event
     */
    @FXML public final void rotatePreview(MouseEvent e) {
        Label src = (Label)e.getSource();
        if (src == rotateRight) {
            if (currentPreview < 3) {
                currentPreview++;
                skinPreview.setImage(skinPreviews[currentPreview]);
            } else {
                currentPreview = 0;
                skinPreview.setImage(skinPreviews[currentPreview]);
            }
        } else if (src == rotateLeft) {
            if (currentPreview > 0) {
                currentPreview--;
                skinPreview.setImage(skinPreviews[currentPreview]);
            } else {
                currentPreview = 3;
                skinPreview.setImage(skinPreviews[currentPreview]);
            }
        }
    }

    /**
     * Loads profiles list items
     */
    private void loadProfileList() {
        console.print("Loading profile list...");
        ObservableList<Label> profileListItems = getProfileList();

        //Add "Add New Profile" item
        profileListItems.add(0, new Label(Language.get(51), new ImageView(new Image("/kml/gui/textures/add.png"))));
        profileList.setItems(profileListItems);
        profileListLoaded = true;
        console.print("Profile list loaded.");
    }

    /**
     * Loads profiles popup list items
     */
    private void loadProfileListPopup() {
        console.print("Loading profile list popup...");
        ObservableList<Label> profileListItems = getProfileList();
        profilePopupList.setItems(profileListItems);
        profileListPopupLoaded = true;
        console.print("Profile list popup loaded.");
    }

    /**
     * Generates an ObservableList of Labels representing each profile
     * @return The profiles ObservableList
     */
    private ObservableList<Label> getProfileList() {
        ObservableList<Label> profileListItems = FXCollections.observableArrayList();
        Profiles ps = kernel.getProfiles();
        Label l;
        ImageView iv;
        String text;
        for (Profile p : ps.getProfiles()) {
            if (p.getType() == ProfileType.SNAPSHOT && !settings.getEnableSnapshots()) {
                continue;
            }
            switch (p.getType()) {
                case RELEASE:
                    iv = new ImageView(kernel.getProfileIcon("Grass"));
                    text = Language.get(59);
                    break;
                case SNAPSHOT:
                    iv = new ImageView(kernel.getProfileIcon("Crafting_Table"));
                    text = Language.get(60);
                    break;
                default:
                    text = p.hasName() ? p.getName() : Language.get(70);
                    String pi = p.hasIcon() ? p.getIcon() : "Furnace";
                    iv = new ImageView(kernel.getProfileIcon(pi));
                    break;
            }
            iv.setFitWidth(68);
            iv.setFitHeight(68);
            l = new Label(text, iv);
            //Fetch Minecraft version used by the profile
            VersionMeta verID;
            switch (p.getType()) {
                case CUSTOM:
                    Versions versions = kernel.getVersions();
                    verID = p.hasVersion() ? p.getVersionID() : versions.getLatestRelease();
                    break;
                case RELEASE:
                    verID = kernel.getVersions().getLatestRelease();
                    break;
                default:
                    verID = kernel.getVersions().getLatestSnapshot();
                    break;
            }
            l.setId(p.getID());
            if (verID != null) {
                //If profile has any known version just show it below the profile name
                if (verID.getType() == VersionType.SNAPSHOT && !settings.getEnableSnapshots()) {
                    continue;
                }
                if ((verID.getType() == VersionType.OLD_ALPHA || verID.getType() == VersionType.OLD_BETA) && !settings.getEnableHistorical()) {
                    continue;
                }
                l.setText(l.getText() + '\n' + verID.getID());
            }
            if (ps.getSelectedProfile().equals(p)) {
                l.getStyleClass().add("selectedProfile");
            }
            profileListItems.add(l);
        }
        return profileListItems;
    }

    /**
     * Updates the selected minecraft version indicator
     */
    private void updateGameVersion() {
        Profile p = kernel.getProfiles().getSelectedProfile();
        if (p != null) {
            switch (p.getType()) {
                case RELEASE:
                    gameVersion.setText(Language.get(26));
                    break;
                case SNAPSHOT:
                    gameVersion.setText(Language.get(32));
                    break;
                default:
                    if (p.isLatestRelease()) {
                        gameVersion.setText(Language.get(26));
                    } else if (p.isLatestSnapshot()) {
                        gameVersion.setText(Language.get(32));
                    } else if (p.hasVersion()) {
                        VersionMeta version = p.getVersionID();
                        gameVersion.setText("Minecraft " + version.getID());
                    }
                    break;
            }
        }  else {
            gameVersion.setText("");
        }
    }

    /**
     * Loads the profile icons
     */
    private void loadIcons() {
        console.print("Loading icons...");
        ObservableList<ImageView> icons = FXCollections.observableArrayList();
        Set<String> keys = kernel.getIcons().keySet();
        for (String key : keys) {
            if (!key.equals("Crafting_Table") && !key.equals("Grass")) {
                ImageView imv = new ImageView(kernel.getProfileIcon(key));
                imv.setFitHeight(68);
                imv.setFitWidth(68);
                imv.setId(key);
                icons.add(imv);
            }
        }
        iconList.setItems(icons);
        console.print("Icons loaded.");
    }

    /**
     * Validates the selected profile according to the constraints
     */
    private void validateSelectedProfile() {
        Profiles ps = kernel.getProfiles();

        //Check if selected profile passes the current settings
        Profile selected = ps.getSelectedProfile();
        VersionMeta selectedVersion = selected.getVersionID();

        if (selected.getType() == ProfileType.SNAPSHOT && !settings.getEnableSnapshots()) {
            ps.setSelectedProfile(ps.getReleaseProfile());
        } else if (selected.getType() == ProfileType.CUSTOM) {
            VersionType type = selectedVersion.getType();
            if (type == VersionType.SNAPSHOT && !settings.getEnableSnapshots()) {
                ps.setSelectedProfile(ps.getReleaseProfile());
            } else if (type == VersionType.OLD_ALPHA && !settings.getEnableHistorical()) {
                ps.setSelectedProfile(ps.getReleaseProfile());
            } else if (type == VersionType.OLD_BETA && !settings.getEnableHistorical()) {
                ps.setSelectedProfile(ps.getReleaseProfile());
            }
        }

        updateGameVersion();
    }

    /**
     * Selects the selected profile from the list
     */
    @FXML private void selectProfile() {
        if (profilePopupList.getSelectionModel().getSelectedIndex() == -1) {
            //Nothing has been selected
            return;
        }
        //Select profile and refresh list
        kernel.getProfiles().setSelectedProfile(kernel.getProfiles().getProfile(profilePopupList.getSelectionModel().getSelectedItem().getId()));
        updateGameVersion();
        SingleSelectionModel<Tab> selection = contentPane.getSelectionModel();
        Tab selectedTab = selection.getSelectedItem();
        if (selectedTab == launchOptionsTab) {
            loadProfileList();
        } else {
            profileListLoaded = false;
        }
        profileListPopupLoaded = false;
        profilePopupList.setVisible(false);
        kernel.saveProfiles();
    }

    /**
     * Downloads and launches the game
     */
    @FXML public final void launchGame() {
        progressPane.setVisible(true);
        playPane.setVisible(false);
        progressBar.setProgress(0);
        progressText.setText("");
        final Downloader d = kernel.getDownloader();
        final GameLauncher gl = kernel.getGameLauncher();

        //Keep track of the progress
        final TimerTask progressTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        MainFX.this.progressBar.setProgress(d.getProgress());
                        MainFX.this.progressText.setText(Language.get(13) + ' ' + d.getCurrentFile() + "...");
                    }
                });
            }
        };

        Thread runThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Begin download and game launch task
                try {
                    Timer timer = new Timer();
                    timer.schedule(progressTask, 0, 25);
                    d.download();
                    timer.cancel();
                    timer.purge();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            progressText.setText(Language.get(78));
                            progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                        }
                    });
                    gl.launch(MainFX.this);

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            progressPane.setVisible(false);
                            playPane.setVisible(true);
                            playButton.setText(Language.get(14));
                            playButton.setDisable(true);
                            profilePopupButton.setDisable(true);
                        }
                    });

                    if (!settings.getKeepLauncherOpen()) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                MainFX.this.stage.close();
                            }
                        });
                    }
                } catch (DownloaderException e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            kernel.showAlert(Alert.AlertType.ERROR, Language.get(83), Language.get(84));
                        }
                    });
                    console.print("Failed to perform game download task");
                    e.printStackTrace(console.getWriter());
                } catch (GameLauncherException e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            kernel.showAlert(Alert.AlertType.ERROR, Language.get(81), Language.get(82));
                        }
                    });
                    console.print("Failed to perform game launch task");
                    e.printStackTrace(console.getWriter());
                }
            }
        });
        runThread.start();
    }

    /**
     * Callback from Game Launcher
     * @param error True if an error happened during launch
     */
    public final void gameEnded(final boolean error) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (error) {
                    kernel.showAlert(Alert.AlertType.ERROR, Language.get(16), Language.get(15));
                }
                if (!settings.getKeepLauncherOpen()) {
                    kernel.exitSafely();
                }
                playButton.setDisable(false);
                profilePopupButton.setDisable(false);
                if (Kernel.USE_LOCAL) {
                    playButton.setText(Language.get(79));
                } else {
                    playButton.setText(Language.get(12));
                }
            }
        });
    }

    /**
     * Shows the language list
     */
    @FXML public final void showLanguages(Event e) {
        e.consume();
        if (languagesList.isVisible()) {
            languagesList.setVisible(false);
        } else {
            if (!languageListLoaded) {
                loadLanguages();
                languageListLoaded = true;
            }
            languagesList.setVisible(true);
        }
    }

    public final void checkPopups() {
        if (languagesList.isVisible()) {
            languagesList.setVisible(false);
        }
        if (switchAccountButton.isVisible()) {
            switchAccountButton.setVisible(false);
        }
        if (profilePopupList.isVisible()) {
            profilePopupList.setVisible(false);
        }
        if (iconList.isVisible()) {
            iconList.setVisible(false);
        }
    }

    /**
     * Deselects the current user and allows to select another
     */
    @FXML public final void switchAccount() {
        if (switchAccountButton.isVisible()) {
            switchAccountButton.setVisible(false);
        }
        Authentication a = kernel.getAuthentication();
        a.setSelectedUser(null);
        kernel.saveProfiles();
        showLoginPrompt(true);
        updateExistingUsers();
    }

    /**
     * Shows the profile popup list
     */
    @FXML public final void showProfiles() {
        if (profilePopupList.isVisible()) {
            profilePopupList.setVisible(false);
        } else {
            if (!profileListPopupLoaded) {
                loadProfileListPopup();
            }
            Bounds b = playButton.localToScene(playButton.getBoundsInLocal());
            profilePopupList.setTranslateX(b.getMinX() - 70);
            profilePopupList.setTranslateY(b.getMinY() - 180);
            profilePopupList.setVisible(true);
            profilePopupList.getSelectionModel().clearSelection();
        }
    }

    /**
     * Shows the profile editor profile icons
     */
    @FXML public final void showIcons(Event e) {
        e.consume();
        if (iconList.isVisible()) {
            iconList.setVisible(false);
        } else {
            if (!iconListLoaded) {
                loadIcons();
                iconListLoaded = true;
            }
            //Calculate change icon button position on scene
            Bounds b = changeIcon.localToScene(changeIcon.getBoundsInLocal());
            iconList.setTranslateX(b.getMinX());
            iconList.setTranslateY(b.getMaxY());
            iconList.setVisible(true);
        }
    }

    /**
     * Shows the Switch Account option when the user label is clicked
     */
    @FXML public final void showAccountOptions(Event e) {
        e.consume();
        if (switchAccountButton.isVisible()) {
            switchAccountButton.setVisible(false);
        } else {
            switchAccountButton.setVisible(true);
        }
    }

    /**
     * Switched the selected tab according to the clicked label
     * @param e The trigger event
     */
    @FXML public final void switchTab(Event e) {
        switchTab(e.getSource());
    }

    /**
     * Switched the selected tab according to the source
     * @param source The object that trigger the change
     */
    private void switchTab(Object source) {
        SingleSelectionModel<Tab> selection = contentPane.getSelectionModel();
        Tab oldTab = selection.getSelectedItem();
        if (oldTab == newsTab) {
            newsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == skinsTab) {
            skinsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == settingsTab) {
            settingsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == launchOptionsTab && source != profileEditorTab) {
            launchOptionsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == profileEditorTab) {
            //Show play button
            if (!kernel.getDownloader().isDownloading()) {
                playPane.setVisible(true);
            }
            launchOptionsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == loginTab) {
            newsLabel.getStyleClass().remove("selectedItem");
            skinsLabel.getStyleClass().remove("selectedItem");
            settingsLabel.getStyleClass().remove("selectedItem");
            launchOptionsLabel.getStyleClass().remove("selectedItem");
        }
        if (source == newsLabel) {
            newsLabel.getStyleClass().add("selectedItem");
            selection.select(newsTab);
        } else if (source == skinsLabel) {
            skinsLabel.getStyleClass().add("selectedItem");
            selection.select(skinsTab);
            if (!texturesLoaded) {
                loadTextures();
            }
        } else if (source == settingsLabel) {
            settingsLabel.getStyleClass().add("selectedItem");
            selection.select(settingsTab);
        } else if (source == launchOptionsLabel) {
            launchOptionsLabel.getStyleClass().add("selectedItem");
            selection.select(launchOptionsTab);
            if (!profileListLoaded) {
                loadProfileList();
            }
            profileList.getSelectionModel().clearSelection();
        } else if (source == profileEditorTab) {
            //Hide play button
            playPane.setVisible(false);
            selection.select(profileEditorTab);
        }
    }

    /**
     * Hides any open popup that triggers this method
     * @param e The event trigger
     */
    @FXML public final void hidePopup(Event e) {
        Node ls = (Node)e.getSource();
        if (ls.isVisible()) {
            ls.setVisible(false);
        }
    }

    /**
     * Updates the selected language
     */
    @FXML public final void updateLanguage() {
        if (languagesList.getSelectionModel().getSelectedIndex() == -1) {
            //Nothing has been selected
            return;
        }
        Label selected = languagesList.getSelectionModel().getSelectedItem();
        languageButton.setText(selected.getText());
        settings.setLocale(selected.getId());
        languagesList.setVisible(false);
        SingleSelectionModel<Tab> selection = contentPane.getSelectionModel();
        Tab selectedTab = selection.getSelectedItem();
        if (selectedTab == launchOptionsTab) {
            loadProfileList();
        } else {
            profileListLoaded = false;
        }
        if (selectedTab == profileEditorTab) {
            loadVersionList();
        } else {
            versionListLoaded = false;
        }
        profileListPopupLoaded = false;
        localizeElements();
    }

    /**
     * Updates the selected icon
     */
    @FXML public final void updateIcon() {
        if (iconList.getSelectionModel().getSelectedIndex() == -1) {
            //Nothing has been selected
            return;
        }
        ImageView selected = iconList.getSelectionModel().getSelectedItem();
        profileIcon.setImage(selected.getImage());
        profileIcon.setId(selected.getId());
        iconList.setVisible(false);
    }

    /**
     * Prepares the editor with the selected profile or with a new one
     */
    @FXML public final void loadEditor() {
        if (profileList.getSelectionModel().getSelectedIndex() == -1) {
            //Nothing has been selected
            return;
        }
        if (!versionListLoaded) {
            loadVersionList();
        }
        if (profileList.getSelectionModel().getSelectedIndex() == 0) {
            profileName.setEditable(true);
            profileName.setText("");
            deleteButton.setVisible(false);
            versionBlock.setVisible(true);
            versionBlock.setManaged(true);
            iconBlock.setVisible(true);
            iconBlock.setManaged(true);
            versionList.getSelectionModel().select(0);
            profileIcon.setImage(kernel.getProfileIcon("Furnace"));
            profileIcon.setId("Furnace");
            if (settings.getEnableAdvanced()) {
                javaExecBlock.setVisible(true);
                javaExecBlock.setManaged(true);
                javaArgsBlock.setVisible(true);
                javaArgsBlock.setManaged(true);
                toggleEditorOption(javaExecLabel, false);
                javaExec.setText(Utils.getJavaDir());
                toggleEditorOption(javaArgsLabel, false);
                StringBuilder jA = new StringBuilder(15);
                if (Utils.getOSArch() == OSArch.OLD) {
                    jA.append("-Xmx1G");
                } else {
                    jA.append("-Xmx2G");
                }
                jA.append(" -Xmn128M");
                javaArgs.setText(jA.toString());
            } else {
                javaExecBlock.setVisible(false);
                javaExecBlock.setManaged(false);
                javaArgsBlock.setVisible(false);
                javaArgsBlock.setManaged(false);
            }
            toggleEditorOption(resolutionLabel, false);
            resW.setText(String.valueOf(854));
            resH.setText(String.valueOf(480));
            toggleEditorOption(gameDirLabel, false);
            gameDir.setText(Utils.getWorkingDirectory().getAbsolutePath());
        } else {
            Label selectedElement = profileList.getSelectionModel().getSelectedItem();
            if (selectedElement != null) {
                Profile p = kernel.getProfiles().getProfile(selectedElement.getId());
                if (p.getType() != ProfileType.CUSTOM) {
                    profileName.setEditable(false);
                    deleteButton.setVisible(false);
                    if (p.getType() == ProfileType.RELEASE) {
                        profileName.setText(Language.get(59));
                        profileIcon.setImage(kernel.getProfileIcon("Grass"));
                    } else {
                        profileName.setText(Language.get(60));
                        profileIcon.setImage(kernel.getProfileIcon("Crafting_Table"));
                    }
                    versionBlock.setVisible(false);
                    versionBlock.setManaged(false);
                    iconBlock.setVisible(false);
                    iconBlock.setManaged(false);
                } else {
                    if (p.hasIcon()) {
                        profileIcon.setImage(kernel.getProfileIcon(p.getIcon()));
                        profileIcon.setId(p.getIcon());
                    } else {
                        profileIcon.setImage(kernel.getProfileIcon("Furnace"));
                    }
                    profileName.setEditable(true);
                    deleteButton.setVisible(true);
                    if (p.hasName()){
                        profileName.setText(p.getName());
                    } else {
                        profileName.setText("");
                    }
                    versionBlock.setVisible(true);
                    versionBlock.setManaged(true);
                    iconBlock.setVisible(true);
                    iconBlock.setManaged(true);
                    if (p.hasVersion()) {
                        if (p.isLatestRelease()) {
                            versionList.getSelectionModel().select(0);
                        } else if (p.isLatestSnapshot() && settings.getEnableSnapshots()) {
                            versionList.getSelectionModel().select(1);
                        } else if (versionList.getItems().contains(p.getVersionID())) {
                            versionList.getSelectionModel().select(p.getVersionID());
                        } else {
                            versionList.getSelectionModel().select(0);
                        }
                    } else {
                        versionList.getSelectionModel().select(0);
                    }
                }

                if (p.hasResolution()) {
                    toggleEditorOption(resolutionLabel, true);
                    resH.setText(String.valueOf(p.getResolutionHeight()));
                    resW.setText(String.valueOf(p.getResolutionWidth()));
                } else {
                    toggleEditorOption(resolutionLabel, false);
                    resW.setText(String.valueOf(854));
                    resH.setText(String.valueOf(480));
                }
                if (p.hasGameDir()) {
                    toggleEditorOption(gameDirLabel, true);
                    gameDir.setText(p.getGameDir().getAbsolutePath());
                } else {
                    toggleEditorOption(gameDirLabel, false);
                    gameDir.setText(Utils.getWorkingDirectory().getAbsolutePath());
                }
                if (settings.getEnableAdvanced()) {
                    javaExecBlock.setVisible(true);
                    javaExecBlock.setManaged(true);
                    javaArgsBlock.setVisible(true);
                    javaArgsBlock.setManaged(true);
                    if (p.hasJavaDir()){
                        toggleEditorOption(javaExecLabel, true);
                        javaExec.setText(p.getJavaDir().getAbsolutePath());
                    } else {
                        toggleEditorOption(javaExecLabel, false);
                        javaExec.setText(Utils.getJavaDir());
                    }
                    if (p.hasJavaArgs()) {
                        toggleEditorOption(javaArgsLabel, true);
                        javaArgs.setText(p.getJavaArgs());
                    } else {
                        toggleEditorOption(javaArgsLabel, false);
                        StringBuilder jA = new StringBuilder(15);
                        if (Utils.getOSArch() == OSArch.OLD) {
                            jA.append("-Xmx1G");
                        } else {
                            jA.append("-Xmx2G");
                        }
                        jA.append(" -Xmn128M");
                        javaArgs.setText(jA.toString());
                    }
                } else {
                    javaExecBlock.setVisible(false);
                    javaExecBlock.setManaged(false);
                    javaArgsBlock.setVisible(false);
                    javaArgsBlock.setManaged(false);
                }
            }
        }
        switchTab(profileEditorTab);
    }

    /**
     * Loads the list of version for the profile editor
     */
    private void loadVersionList() {
        console.print("Loading version list...");
        ObservableList<VersionMeta> vers = FXCollections.observableArrayList();
        VersionMeta latestVersion = new VersionMeta(Language.get(59), null, null);
        vers.add(latestVersion);
        if (settings.getEnableSnapshots()) {
            VersionMeta latestSnapshot = new VersionMeta(Language.get(60), null, null);
            vers.add(latestSnapshot);
        }
        for (VersionMeta v : kernel.getVersions().getVersions()) {
            if (v.getType() == VersionType.RELEASE) {
                vers.add(v);
            } else if (v.getType() == VersionType.SNAPSHOT && settings.getEnableSnapshots()) {
                vers.add(v);
            } else if ((v.getType() == VersionType.OLD_BETA || v.getType() == VersionType.OLD_ALPHA) && settings.getEnableHistorical()) {
                vers.add(v);
            }
        }

        versionList.setItems(vers);
        if (!versionListLoaded) {
            versionList.getSelectionModel().select(0);
            versionListLoaded = true;
        }
        console.print("Version list loaded.");
    }

    /**
     * Saves the profile data from the profile editor
     */
    @FXML public final void saveProfile() {
        Profile target;
        if (profileList.getSelectionModel().getSelectedIndex() == 0) {
            target = new Profile(ProfileType.CUSTOM);
            kernel.getProfiles().addProfile(target);
        } else {
            Label selectedElement = profileList.getSelectionModel().getSelectedItem();
            target = kernel.getProfiles().getProfile(selectedElement.getId());
        }
        if (target.getType() == ProfileType.CUSTOM) {
            if (!profileName.getText().isEmpty()) {
                target.setName(profileName.getText());
            } else {
                target.setName(null);
            }
            if (versionList.getSelectionModel().getSelectedIndex() == 0) {
                target.setVersionID(kernel.getVersions().getLatestRelease());
                target.setLatestRelease(true);
                target.setLatestSnapshot(false);
            } else if (versionList.getSelectionModel().getSelectedIndex() == 1 && settings.getEnableSnapshots()) {
                target.setVersionID(kernel.getVersions().getLatestSnapshot());
                target.setLatestRelease(false);
                target.setLatestSnapshot(true);
            } else {
                target.setVersionID(versionList.getSelectionModel().getSelectedItem());
                target.setLatestRelease(false);
                target.setLatestSnapshot(false);
            }
            try {
                target.setIcon(profileIcon.getId());
            } catch (IllegalArgumentException ex) {
                target.setIcon(null);
            }
        }
        if (!resW.isDisabled()) {
            try {
                int h = Integer.parseInt(resH.getText());
                int w = Integer.parseInt(resW.getText());
                target.setResolution(w, h);
            } catch (NumberFormatException ex) {
                console.print("Invalid resolution given.");
            }
        } else {
            target.setResolution(-1, -1);
        }
        if (!gameDir.isDisabled() && !gameDir.getText().isEmpty()) {
            target.setGameDir(new File(gameDir.getText()));
        } else {
            target.setGameDir(null);
        }
        if (settings.getEnableAdvanced()) {
            if (!javaExec.isDisabled() && !javaExec.getText().isEmpty()) {
                target.setJavaDir(new File(javaExec.getText()));
            } else {
                target.setJavaDir(null);
            }
            if (!javaArgs.isDisabled() && !javaArgs.getText().isEmpty()) {
                target.setJavaArgs(javaArgs.getText());
            } else {
                target.setJavaArgs(null);
            }
        }
        kernel.saveProfiles();
        if (kernel.getProfiles().getSelectedProfile() == target) {
            updateGameVersion();
        }
        kernel.showAlert(Alert.AlertType.INFORMATION, null, Language.get(57));
        profileListLoaded = false;
        profileListPopupLoaded = false;
        switchTab(launchOptionsLabel);
    }

    /**
     * Discards the changes of the profile editor
     */
    @FXML public final void cancelProfile() {
        int result = kernel.showAlert(Alert.AlertType.CONFIRMATION, null, Language.get(55));
        if (result == 1) {
            switchTab(launchOptionsLabel);
        }
    }

    /**
     * Deletes the profile loaded by the profile editor
     */
    @FXML public final void deleteProfile() {
        int result = kernel.showAlert(Alert.AlertType.CONFIRMATION, null, Language.get(61));
        if (result == 1) {
            Label selectedElement = profileList.getSelectionModel().getSelectedItem();
            Profile p = kernel.getProfiles().getProfile(selectedElement.getId());
            if (kernel.getProfiles().deleteProfile(p)) {
                kernel.saveProfiles();
                updateGameVersion();
                kernel.showAlert(Alert.AlertType.INFORMATION, null, Language.get(56));
            } else {
                kernel.showAlert(Alert.AlertType.ERROR, null, Language.get(58));
            }
            loadProfileList();
            switchTab(launchOptionsLabel);
        }
    }

    /**
     * Toggles the editor options on and off
     * @param src The object that has been clicked
     * @param newState The new state
     */
    private void toggleEditorOption(Object src, boolean newState) {
        if (src instanceof Label) {
            Label l = (Label)src;
            toggleLabel(l, newState);
        }
        if (src == resolutionLabel) {
            resW.setDisable(!newState);
            resH.setDisable(!newState);
        } else if (src == gameDirLabel) {
            gameDir.setDisable(!newState);
        } else if (src == javaExecLabel) {
            javaExec.setDisable(!newState);
        } else if (src == javaArgsLabel) {
            javaArgs.setDisable(!newState);
        }
    }

    /**
     * Update editor when clicking labels. This method fetches the adjacent sibling to determine if is disabled
     * @param e The event trigger
     */
    @FXML public final void updateEditor(MouseEvent e) {
        Label l = (Label)e.getSource();
        toggleEditorOption(l, l.getParent().getChildrenUnmodifiable().get(1).isDisable());
    }

    /**
     * Updates the existing users list
     */
    private void updateExistingUsers() {
        Authentication a = kernel.getAuthentication();
        if (!a.getUsers().isEmpty() && a.getSelectedUser() == null) {
            existingPanel.setVisible(true);
            existingPanel.setManaged(true);
            ObservableList<User> users = FXCollections.observableArrayList();
            Set<User> us = a.getUsers();
            users.addAll(us);
            existingUsers.setItems(users);
            existingUsers.getSelectionModel().select(0);
        } else {
            existingPanel.setVisible(false);
            existingPanel.setManaged(false);
        }
    }

    /**
     * Shows or hides the login prompt
     * @param showLoginPrompt The new state
     */
    private void showLoginPrompt(boolean showLoginPrompt) {
        if (showLoginPrompt) {
            contentPane.getSelectionModel().select(loginTab);
            tabMenu.setVisible(false);
            tabMenu.setManaged(false);
            accountButton.setVisible(false);
            playPane.setVisible(false);
            updateExistingUsers();
        } else {
            switchTab(newsLabel);
            tabMenu.setVisible(true);
            tabMenu.setManaged(true);
            accountButton.setVisible(true);
            //Show play button
            if (!kernel.getDownloader().isDownloading()) {
                playPane.setVisible(true);
            }
            //Set account name for current user
            accountButton.setText(kernel.getAuthentication().getSelectedUser().getDisplayName());
        }
    }

    /**
     * Performs an authenticate with the data typed in the login form
     */
    public final void authenticate() {
        if (username.getText().isEmpty()) {
            kernel.showAlert(Alert.AlertType.WARNING, null, Language.get(17));
        } else if (password.getText().isEmpty()) {
            kernel.showAlert(Alert.AlertType.WARNING, null, Language.get(23));
        } else {
            try {
                Authentication auth = kernel.getAuthentication();
                String user;
                if (authKrothium.isSelected()) {
                    user = "krothium://" + username.getText();
                } else {
                    user = username.getText();
                }
                auth.authenticate(user, password.getText());
                kernel.saveProfiles();
                username.setText("");
                password.setText("");
                showLoginPrompt(false);
                fetchAds();
                texturesLoaded = false;
            } catch (AuthenticationException ex) {
                kernel.showAlert(Alert.AlertType.ERROR, Language.get(22), ex.getMessage());
                password.setText("");
            }
        }
    }

    /**
     * Refreshes latest session
     */
    private void refreshSession() {
        console.print("Refreshing session...");
        Authentication a = kernel.getAuthentication();
        User u = a.getSelectedUser();
        try {
            if (u != null) {
                a.refresh();
                texturesLoaded = false;
                kernel.saveProfiles();
                console.print("Session refreshed.");
            } else {
                console.print("No user is selected.");
            }
        } catch (AuthenticationException ex) {
            if (u.getType() == UserType.KROTHIUM) {
                authKrothium.setSelected(true);
                username.setText(u.getUsername().replace("krothium://", ""));
            } else {
                authMojang.setSelected(true);
                username.setText(u.getUsername());
            }
            console.print("Couldn't refresh your session.");
        } finally {
            if (a.isAuthenticated()) {
                showLoginPrompt(false);
                fetchAds();
            } else {
                showLoginPrompt(true);
            }
        }
    }

    /**
     * Refreshes user selected from the existing user list
     */
    public final void refresh() {
        User selected = existingUsers.getSelectionModel().getSelectedItem();
        Authentication auth = kernel.getAuthentication();
        try {
            auth.setSelectedUser(selected);
            auth.refresh();
            kernel.saveProfiles();
            texturesLoaded = false;
            showLoginPrompt(false);
            fetchAds();
        } catch (AuthenticationException ex) {
            kernel.showAlert(Alert.AlertType.ERROR, Language.get(62), ex.getMessage());
            updateExistingUsers();
        }
    }

    /**
     * Logs out the selected user from the existing user list
     */
    public final void logout() {
        User selected = existingUsers.getSelectionModel().getSelectedItem();
        int result = kernel.showAlert(Alert.AlertType.CONFIRMATION, null, Language.get(8));
        if (result == 1) {
            Authentication auth = kernel.getAuthentication();
            auth.removeUser(selected);
            kernel.saveProfiles();
            updateExistingUsers();
        }
    }

    /**
     * Opens the register page
     */
    @FXML public final void register() {
        if (authKrothium.isSelected()) {
            kernel.getHostServices().showDocument("https://krothium.com/register");
        } else {
            kernel.getHostServices().showDocument("https://minecraft.net/");
        }
    }

    /**
     * Opens the help page
     */
    @FXML public final void openHelp() {
        kernel.getHostServices().showDocument(urlPrefix + "https://krothium.com/forum/12-soporte/");
    }

    /**
     * Opens the news page
     */
    @FXML public final void openNews() {
        kernel.getHostServices().showDocument(urlPrefix + "https://krothium.com/forum/3-noticias/");
    }

    /**
     * Performs an authenticate if the Enter key is pressed in the Username or Password field
     * @param e The trigger event
     */
    @FXML public final void triggerAuthenticate(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            authenticate();
        }
    }

    /**
     * Updates the settings according to the label clicked
     * @param e The trigger event
     */
    @FXML public final void updateSettings(MouseEvent e) {
        Label source = (Label)e.getSource();
        if (source == keepLauncherOpen) {
            settings.setKeepLauncherOpen(!settings.getKeepLauncherOpen());
            toggleLabel(source, settings.getKeepLauncherOpen());
        } else if (source == outputLog) {
            settings.setShowGameLog(!settings.getShowGameLog());
            toggleLabel(source, settings.getShowGameLog());
        } else if (source == enableSnapshots) {
            if (!settings.getEnableSnapshots()) {
                kernel.showAlert(Alert.AlertType.WARNING, null, Language.get(71) + System.lineSeparator() + Language.get(72));
            }
            settings.setEnableSnapshots(!settings.getEnableSnapshots());
            toggleLabel(source, settings.getEnableSnapshots());
            validateSelectedProfile();
            loadProfileList();
            versionListLoaded = false;
        } else if (source == historicalVersions) {
            if (!settings.getEnableHistorical()) {
                kernel.showAlert(Alert.AlertType.WARNING, null, Language.get(73) + System.lineSeparator()
                        + Language.get(74) + System.lineSeparator()
                        + Language.get(75));

            }
            settings.setEnableHistorical(!settings.getEnableHistorical());
            toggleLabel(source, settings.getEnableHistorical());
            validateSelectedProfile();
            loadProfileList();
            versionListLoaded = false;
        } else if (source == advancedSettings) {
            if (!settings.getEnableAdvanced()) {
                kernel.showAlert(Alert.AlertType.WARNING, null, Language.get(76) + System.lineSeparator() + Language.get(77));
            }
            settings.setEnableAdvanced(!settings.getEnableAdvanced());
            toggleLabel(source, settings.getEnableAdvanced());
        }
        kernel.saveProfiles();
    }

    /**
     * Changes any label icon
     * @param label The target label
     * @param state The new state
     */
    private void toggleLabel(Styleable label, boolean state) {
        Object[] classes = label.getStyleClass().toArray();
        for (Object ckl : classes) {
            if (ckl.toString().startsWith("toggle")) {
                label.getStyleClass().remove(ckl.toString());
            }
        }
        if (state) {
            label.getStyleClass().add("toggle-enabled");
        } else {
            label.getStyleClass().add("toggle-disabled");
        }
    }

    /**
     * Exports the logs to a ZIP file
     */
    @FXML private void exportLogs() {
        File selected = selectFile("ZIP", "*.zip", "save");
        if (selected != null) {
            try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(selected))) {
                File[] files = Kernel.APPLICATION_LOGS.listFiles();
                if (files != null) {
                    byte[] bytes;
                    for (File file : files) {
                        ZipEntry entry = new ZipEntry(file.getName());
                        out.putNextEntry(entry);
                        bytes = Files.readAllBytes(file.toPath());
                        out.write(bytes);
                        out.closeEntry();
                    }
                }
                kernel.showAlert(Alert.AlertType.INFORMATION, null, Language.get(35) + System.lineSeparator() + selected.getAbsolutePath());
            } catch (IOException ex) {
                kernel.showAlert(Alert.AlertType.ERROR, null, Language.get(35) + '\n' + selected.getAbsolutePath());
            }
        }
    }

    /**
     * Removes the launcher cache
     */
    @FXML private void deleteCache() {
        int result = kernel.showAlert(Alert.AlertType.CONFIRMATION, null, Language.get(10));
        if (result == 1) {
            File[] files = Kernel.APPLICATION_CACHE.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            kernel.showAlert(Alert.AlertType.INFORMATION, null, Language.get(11));
        }
    }

    /**
     * Opens the URL of the selected version server in the default user web browser
     */
    @FXML private void downloadServer() {
        VersionMeta selectedItem = versionList.getSelectionModel().getSelectedItem();
        kernel.getHostServices().showDocument(urlPrefix + "https://s3.amazonaws.com/Minecraft.Download/versions/" + selectedItem.getID() + "/minecraft_server." + selectedItem.getID() + ".jar");
    }

    /**
     * Selects a game directory for the profile editor
     */
    @FXML private void selectGameDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        if (gameDir.getText().isEmpty()) {
            chooser.setInitialDirectory(Kernel.APPLICATION_WORKING_DIR);
        } else {
            File gd = new File(gameDir.getText());
            if (gd.isDirectory()) {
                chooser.setInitialDirectory(gd);
            } else {
                chooser.setInitialDirectory(Kernel.APPLICATION_WORKING_DIR);
            }
        }
        File selectedFolder = chooser.showDialog(null);
        if (selectedFolder != null) {
            gameDir.setText(selectedFolder.getAbsolutePath());
        }
    }

    /**
     * Selects the java executable for the profile editor
     */
    @FXML private void selectJavaExecutable() {
        File selected = selectFile(null, null, "open");
        if (selected != null && selected.isFile()) {
            javaExec.setText(selected.getAbsolutePath());
        }
    }

    /**
     * Update auth server label on existing users
     */
    @FXML public void updateAuthServer() {
        User user = existingUsers.getValue();
        if (user != null) {
            if (user.getType() == UserType.KROTHIUM) {
                authServer.setText("(Krothium)");
            } else {
                authServer.setText("(Mojang)");
            }
        }
    }

    /**
     * Selects a file
     * @param extensionName The extension name
     * @param extension The extension
     * @param method Method to select the file
     * @return The selected file
     */
    private File selectFile(String extensionName, String extension, String method) {
        FileChooser chooser = new FileChooser();
        if (extension != null) {
            ExtensionFilter filter = new ExtensionFilter(extensionName, extension);
            chooser.getExtensionFilters().add(filter);
        }
        if (method.equalsIgnoreCase("open")) {
            chooser.setTitle(Language.get(95));
            return chooser.showOpenDialog(stage);
        } else if (method.equalsIgnoreCase("save")) {
            chooser.setTitle(Language.get(96));
            return chooser.showSaveDialog(stage);
        }
        return null;
    }

    /**
     * Opens the password recovery webpage
     */
    @FXML private void forgotPassword() {
        if (authKrothium.isSelected()) {
            kernel.getHostServices().showDocument("https://krothium.com/lostpassword");
        } else {
            kernel.getHostServices().showDocument("https://account.mojang.com/password");
        }
    }
}
