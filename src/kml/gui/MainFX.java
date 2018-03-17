package kml.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.Styleable;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
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
import kml.game.profile.ProfileIcon;
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
            selectCape, exportLogs, downloadServer;

    @FXML private Tab loginTab, newsTab, skinsTab,
            settingsTab, launchOptionsTab, profileEditorTab;

    @FXML private ProgressBar progressBar;

    @FXML private TabPane contentPane;

    @FXML private ListView<Label> languagesList, profileList, profilePopupList;

    @FXML private ListView<ImageView> iconList;

    @FXML private VBox progressPane, existingPanel, playPane, skinActions;

    @FXML private HBox tabMenu, slideshowBox;

    @FXML private TextField username, profileName,javaExec, gameDir, javaArgs,
            resH, resW;

    @FXML private PasswordField password;

    @FXML private ComboBox<User> existingUsers;

    @FXML private ComboBox<VersionMeta> versionList;

    @FXML private StackPane versionBlock, javaArgsBlock, javaExecBlock, iconBlock;

    @FXML private ImageView profileIcon, slideshow, skinPreview;

    @FXML private RadioButton skinClassic, skinSlim, authKrothium, authMojang;

    private Kernel kernel;
    private Console console;
    private Stage stage;
    private final List<Slide> slides = new ArrayList<>();
    private int currentSlide;
    private int currentPreview; // 0 = front / 1 = right / 2 = back / 3 = left
    private final Image[] skinPreviews = new Image[4];
    private Image skin, cape, alex, steve;
    private boolean texturesLoaded;
    private String urlPrefix = "";
    private final String CHANGESKIN_URL = "https://mc.krothium.com/changeskin";
    private final String CHANGECAPE_URL = "https://mc.krothium.com/changecape";
    private boolean iconListLoaded, versionListLoaded, languageListLoaded, loadingTextures, profileListLoaded, profileListPopupLoaded;

    /**
     * Initializes all required stuff from the GUI
     * @param k The Kernel instance
     * @param s The Stage instance
     */
    public final void initialize(Kernel k, Stage s) {
        //Require to exit using Platform.exit()
        Platform.setImplicitExit(false);

        //Set kernel and stage
        this.kernel = k;
        this.console = k.getConsole();
        this.stage = s;

        //Update version label
        this.versionLabel.setText(Kernel.KERNEL_BUILD_NAME);

        //Load news slideshow
        this.slideshowBox.setVisible(false);
        this.slideshowBox.setManaged(false);
        this.newsTitle.setText("Loading news...");
        this.newsText.setText("Please wait a moment...");
        this.loadSlideshow();

        //Load placeholder skins
        this.alex = new Image("/kml/gui/textures/alex.png");
        this.steve = new Image("/kml/gui/textures/steve.png");


        //Refresh session
        this.refreshSession();

        //Prepare language list
        String locale = this.kernel.getSettings().getLocale();
        this.languageButton.setText(this.kernel.getSettings().getSupportedLocales().get(locale));

        //Update settings labels
        Settings st = this.kernel.getSettings();
        this.toggleLabel(this.keepLauncherOpen, st.getKeepLauncherOpen());
        this.toggleLabel(this.outputLog, st.getShowGameLog());
        this.toggleLabel(this.enableSnapshots, st.getEnableSnapshots());
        this.toggleLabel(this.historicalVersions, st.getEnableHistorical());
        this.toggleLabel(this.advancedSettings, st.getEnableAdvanced());

        //Prepare Spinners
        this.resW.setEditable(true);
        this.resH.setEditable(true);

        //If offline mode make play button bigger for language support
        if (Kernel.USE_LOCAL) {
            this.playButton.setMinWidth(290);
        }

        //Localize elements
        this.localizeElements();

        //Show window
        if (!this.kernel.getBrowser().isVisible()) {
            this.stage.show();
        }

        //Validate selected profile
        this.validateSelectedProfile();
    }

    /**
     * Load language list
     */
    private void loadLanguages() {
        this.console.print("Loading languages...");
        HashMap<String, String> supportedLocales = this.kernel.getSettings().getSupportedLocales();
        ObservableList<Label> languageListItems = FXCollections.observableArrayList();
        for (String key : this.kernel.getSettings().getSupportedLocales().keySet()) {
            Image i = new Image("/kml/gui/textures/flags/flag_" + key + ".png");
            Label l = new Label(supportedLocales.get(key), new ImageView(i));
            l.setId(key);
            languageListItems.add(l);
        }
        this.languagesList.setItems(languageListItems);
        this.console.print("Languages loaded.");
    }

    /**
     * Fetches any advertisement available for the logged user
     */
    private void fetchAds() {
        User user = this.kernel.getAuthentication().getSelectedUser();
        if (user.getType() != UserType.MOJANG) {
            String profileID = user.getSelectedProfile();
            String adsCheck = "https://mc.krothium.com/ads.php?profileID=" + profileID;
            String response;
            try {
                response = Utils.readURL(adsCheck);
            } catch (IOException ex) {
                this.console.print("Failed to fetch ads data.");
                ex.printStackTrace(this.console.getWriter());
                return;
            }
            if (!response.isEmpty()) {
                String[] chunks = response.split(":");
                String firstChunk = Utils.fromBase64(chunks[0]);
                this.urlPrefix = firstChunk == null ? "" : firstChunk;
                if (chunks.length == 2) {
                    String secondChunk = Utils.fromBase64(response.split(":")[1]);
                    String adsURL = secondChunk == null ? "" : secondChunk;
                    this.kernel.getBrowser().loadWebsite(adsURL);
                    this.kernel.getBrowser().show(this.stage);
                }
                this.console.print("Ads loaded.");
            } else {
                this.console.print("Ads info not available.");
            }
        } else {
            this.console.print("Ads not available for Mojang user.");
        }
    }

    /**
     * Updates all components text with its localized Strings
     */
    private void localizeElements() {
        this.helpButton.setText(Language.get(2));
        this.logoutButton.setText(Language.get(3));
        this.newsLabel.setText(Language.get(4));
        this.skinsLabel.setText(Language.get(5));
        this.settingsLabel.setText(Language.get(6));
        this.launchOptionsLabel.setText(Language.get(7));
        if (Kernel.USE_LOCAL) {
            this.playButton.setText(Language.get(79));
        } else {
            this.playButton.setText(Language.get(12));
        }
        this.usernameLabel.setText(Language.get(18));
        this.passwordLabel.setText(Language.get(19));
        this.loginButton.setText(Language.get(20));
        this.loginExisting.setText(Language.get(20));
        this.registerButton.setText(Language.get(21));
        this.changeIcon.setText(Language.get(24));
        this.exportLogs.setText(Language.get(27));
        this.downloadServer.setText(Language.get(28));
        this.skinLabel.setText(Language.get(29));
        this.capeLabel.setText(Language.get(30));
        this.launcherSettings.setText(Language.get(45));
        this.keepLauncherOpen.setText(Language.get(46));
        this.outputLog.setText(Language.get(47));
        this.enableSnapshots.setText(Language.get(48));
        this.historicalVersions.setText(Language.get(49));
        this.advancedSettings.setText(Language.get(50));
        this.saveButton.setText(Language.get(52));
        this.cancelButton.setText(Language.get(53));
        this.deleteButton.setText(Language.get(54));
        this.nameLabel.setText(Language.get(63));
        this.profileVersionLabel.setText(Language.get(64));
        this.resolutionLabel.setText(Language.get(65));
        this.gameDirLabel.setText(Language.get(66));
        this.javaExecLabel.setText(Language.get(67));
        this.javaArgsLabel.setText(Language.get(68));
        this.existingLabel.setText(Language.get(85));
        this.switchAccountButton.setText(Language.get(86));
        this.selectSkin.setText(Language.get(87));
        this.selectCape.setText(Language.get(87));
        this.deleteSkin.setText(Language.get(88));
        this.deleteCape.setText(Language.get(88));
        this.modelLabel.setText(Language.get(89));
        this.skinClassic.setText(Language.get(90));
        this.skinSlim.setText(Language.get(91));
        this.iconLabel.setText(Language.get(92));
        this.includeCape.setText(Language.get(93));
        this.profileName.setPromptText(Language.get(98));
        this.authenticationLabel.setText(Language.get(99));
    }

    /**
     * Loads the skin preview for the logged user
     */
    private void loadTextures() {
        if (this.loadingTextures) {
            return;
        } else if (Kernel.USE_LOCAL) {
            this.selectSkin.setDisable(true);
            this.selectCape.setDisable(true);
            this.deleteSkin.setDisable(true);
            this.deleteCape.setDisable(true);
            return;
        }
        Thread t = new Thread(() -> {
            try {
                this.console.print("Loading textures...");
                this.loadingTextures = true;
                User selected = this.kernel.getAuthentication().getSelectedUser();
                String domain;
                if (selected.getType() == UserType.MOJANG) {
                    domain = "sessionserver.mojang.com/session/minecraft/profile/";
                    this.skinActions.setVisible(false);
                    this.skinActions.setManaged(false);
                } else {
                    domain = "mc.krothium.com/profiles/";
                    this.skinActions.setVisible(true);
                    this.skinActions.setManaged(true);
                }
                String profileURL = "https://" + domain + selected.getSelectedProfile() + "?unsigned=true";
                JSONObject root = new JSONObject(Utils.readURL(profileURL));
                JSONArray properties = root.getJSONArray("properties");
                for (int i = 0; i < properties.length(); i++) {
                    JSONObject property = properties.getJSONObject(i);
                    if ("textures".equalsIgnoreCase(property.getString("name"))) {
                        JSONObject data = new JSONObject(Utils.fromBase64(property.getString("value")));
                        JSONObject textures = data.getJSONObject("textures");
                        this.skin = null;
                        this.cape = null;
                        boolean slim = false;
                        if (textures.has("SKIN")) {
                            JSONObject skinData = textures.getJSONObject("SKIN");
                            if (skinData.has("metadata")) {
                                if ("slim".equalsIgnoreCase(skinData.getJSONObject("metadata").getString("model"))) {
                                    slim = true;
                                }
                            }
                            InputStream stream = Utils.readCachedStream(textures.getJSONObject("SKIN").getString("url"));
                            this.skin = new Image(stream);
                            stream.close();
                        }
                        if (this.skin == null || this.skin.getHeight() == 0 && !slim) {
                            this.skin = this.steve;
                            this.deleteSkin.setDisable(true);
                        } else if (this.skin.getHeight() == 0) {
                            this.skin = this.alex;
                            this.deleteSkin.setDisable(true);
                        } else {
                            this.deleteSkin.setDisable(false);
                        }
                        if (textures.has("CAPE")) {
                            InputStream stream = Utils.readCachedStream(textures.getJSONObject("CAPE").getString("url"));
                            this.cape = new Image(stream);
                            stream.close();
                            this.includeCape.setDisable(false);
                            this.deleteCape.setDisable(false);
                        } else {
                            this.includeCape.setDisable(true);
                            this.deleteCape.setDisable(true);
                        }
                        if (slim) {
                            this.skinSlim.setSelected(true);
                        } else {
                            this.skinClassic.setSelected(true);
                        }
                        this.texturesLoaded = true;
                        this.console.print("Textures loaded.");
                        this.updatePreview();
                    }
                }
            } catch (Exception ex) {
                this.console.print("Failed to parse remote profile textures.");
                ex.printStackTrace(this.console.getWriter());
            }
            this.loadingTextures = false;
        });
        t.start();
    }

    /**
     * Toggles the label of the toggle cape button
     */
    @FXML public final void toggleCapePreview() {
        if (this.includeCape.getStyleClass().contains("toggle-enabled")) {
            this.toggleLabel(this.includeCape, false);
        } else {
            this.toggleLabel(this.includeCape, true);
        }
        this.updatePreview();
    }

    /**
     * Changes the skin type
     */
    @FXML public final void toggleSkinType() {
        if (this.deleteSkin.isDisabled()) {
            if (this.skinClassic.isSelected()) {
                this.skin = this.steve;
            } else {
                this.skin = this.alex;
            }
            this.updatePreview();
        }
    }

    /**
     * Updates the skin preview
     */
    private void updatePreview() {
        boolean slim = this.skinSlim.isSelected();
        if (this.includeCape.getStyleClass().contains("toggle-enabled")) {
            this.skinPreviews[0] = this.kernel.resampleImage(TexturePreview.generateFront(this.skin, this.cape, slim), 10);
            this.skinPreviews[1] = this.kernel.resampleImage(TexturePreview.generateRight(this.skin, this.cape), 10);
            this.skinPreviews[2] = this.kernel.resampleImage(TexturePreview.generateBack(this.skin, this.cape, slim), 10);
            this.skinPreviews[3] = this.kernel.resampleImage(TexturePreview.generateLeft(this.skin, this.cape), 10);
        } else {
            this.skinPreviews[0] = this.kernel.resampleImage(TexturePreview.generateFront(this.skin, null, slim), 10);
            this.skinPreviews[1] = this.kernel.resampleImage(TexturePreview.generateRight(this.skin, null), 10);
            this.skinPreviews[2] = this.kernel.resampleImage(TexturePreview.generateBack(this.skin, null, slim), 10);
            this.skinPreviews[3] = this.kernel.resampleImage(TexturePreview.generateLeft(this.skin, null), 10);
        }
        this.skinPreview.setImage(this.skinPreviews[this.currentPreview]);
    }

    /**
     * Changes the skin of the user
     */
    @FXML private void changeSkin() {
        File selected = this.selectFile(Language.get(44), "*.png", "open");
        if (selected != null) {
            if (selected.length() > 131072) {
                this.kernel.showAlert(AlertType.ERROR, null, Language.get(105));
                this.console.print("Skin file exceeds 128KB file size limit.");
            } else {
                Map<String, String> params = new HashMap<>();
                try {
                    byte[] data = Files.readAllBytes(selected.toPath());
                    params.put("Access-Token", this.kernel.getAuthentication().getSelectedUser().getAccessToken());
                    params.put("Client-Token", this.kernel.getAuthentication().getClientToken());
                    if (this.skinSlim.isSelected()) {
                        params.put("Skin-Type", "alex");
                    } else {
                        params.put("Skin-Type", "steve");
                    }
                    params.put("Content-Type", "image/png");
                    String r = Utils.sendPost(this.CHANGESKIN_URL, data, params);
                    if (!"OK".equals(r)) {
                        this.console.print("Failed to change the skin.");
                        this.console.print(r);
                        this.kernel.showAlert(AlertType.ERROR, null, Language.get(42));
                    }
                    else {
                        this.kernel.showAlert(AlertType.INFORMATION, null, Language.get(40));
                        this.console.print("Skin changed successfully!");
                        this.loadTextures();
                    }
                } catch (Exception ex) {
                    this.console.print("Failed to change the skin.");
                    this.console.print(ex.getMessage());
                    this.kernel.showAlert(AlertType.ERROR, null, Language.get(42));
                }
            }
        }
    }

    /**
     * Changes the cape of the user
     */
    @FXML private void changeCape() {
        File selected = this.selectFile(Language.get(25), "*.png", "open");
        if (selected != null) {
            if (selected.length() > 131072) {
                this.kernel.showAlert(AlertType.ERROR, null, Language.get(104));
                this.console.print("Cape file exceeds 128KB file size limit.");
            } else {
                Map<String, String> params = new HashMap<>();
                try {
                    byte[] data = Files.readAllBytes(selected.toPath());
                    params.put("Access-Token", this.kernel.getAuthentication().getSelectedUser().getAccessToken());
                    params.put("Client-Token", this.kernel.getAuthentication().getClientToken());
                    params.put("Content-Type", "image/png");
                    String r = Utils.sendPost(this.CHANGECAPE_URL, data, params);
                    if (!"OK".equals(r)) {
                        this.console.print("Failed to change the cape.");
                        this.console.print(r);
                        this.kernel.showAlert(AlertType.ERROR, null, Language.get(43));
                    }
                    else {
                        this.kernel.showAlert(AlertType.INFORMATION, null, Language.get(41));
                        this.console.print("Cape changed successfully.");
                        this.loadTextures();
                    }
                } catch (Exception ex) {
                    this.console.print("Failed to change the cape.");
                    this.console.print(ex.getMessage());
                    this.kernel.showAlert(AlertType.ERROR, null, Language.get(43));
                }
            }
        }
    }

    /**
     * Deletes the skin of the user
     */
    @FXML private void deleteSkin() {
        int result = this.kernel.showAlert(AlertType.CONFIRMATION, null, Language.get(31));
        if (result == 1){
            Map<String, String> params = new HashMap<>();
            params.put("Access-Token", this.kernel.getAuthentication().getSelectedUser().getAccessToken());
            params.put("Client-Token", this.kernel.getAuthentication().getClientToken());
            try {
                String r = Utils.sendPost(this.CHANGESKIN_URL, null, params);
                if (!"OK".equals(r)) {
                    this.console.print("Failed to delete the skin.");
                    this.console.print(r);
                    this.kernel.showAlert(AlertType.ERROR, null, Language.get(33));
                }
                else {
                    this.kernel.showAlert(AlertType.INFORMATION, null, Language.get(34));
                    this.console.print("Skin deleted successfully!");
                    this.loadTextures();
                }
            }
            catch (Exception ex) {
                this.console.print("Failed to delete the skin.");
                this.console.print(ex.getMessage());
                this.kernel.showAlert(AlertType.ERROR, null, Language.get(33));
            }
            params.clear();
        }
    }

    /**
     * Deletes the cape of the user
     */
    @FXML private void deleteCape() {
        int result = this.kernel.showAlert(AlertType.CONFIRMATION, null, Language.get(36));
        if (result == 1){
            Map<String, String> params = new HashMap<>();
            params.put("Access-Token", this.kernel.getAuthentication().getSelectedUser().getAccessToken());
            params.put("Client-Token", this.kernel.getAuthentication().getClientToken());
            try {
                String r = Utils.sendPost(this.CHANGECAPE_URL, null, params);
                if (!"OK".equals(r)) {
                    this.console.print("Failed to delete the cape.");
                    this.console.print(r);
                    this.kernel.showAlert(AlertType.ERROR, null, Language.get(38));
                }
                else {
                    this.kernel.showAlert(AlertType.INFORMATION, null, Language.get(39));
                    this.console.print("Cape deleted successfully!");
                    this.loadTextures();
                }
            }
            catch (Exception ex) {
                this.console.print("Failed to delete the cape.");
                this.console.print(ex.getMessage());
                this.kernel.showAlert(AlertType.ERROR, null, Language.get(38));
            }
            params.clear();
        }
    }

    /**
     * Loads the news slideshow
     */
    private void loadSlideshow() {
        this.console.print("Loading news slideshow...");
        try {
            String newsURL = "https://launchermeta.mojang.com/mc/news.json";
            String response = Utils.readURL(newsURL);
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
                this.slides.add(s);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            this.newsTitle.setText(Language.get(80));
            this.newsText.setText(Language.get(101));
            this.console.print("Couldn't parse news data. (" + ex.getMessage() + ')');
            return;
        }
        if (!this.slides.isEmpty()) {
            this.slideshowBox.setVisible(true);
            this.slideshowBox.setManaged(true);
            Slide s = this.slides.get(0);
            this.slideshow.setImage(s.getImage());
            this.newsTitle.setText(s.getTitle());
            this.newsText.setText(s.getText());
        } else {
            this.newsTitle.setText(Language.get(102));
            this.newsText.setText(Language.get(103));
        }
        this.console.print("News slideshow loaded.");
    }

    /**
     * Changes the news slide
     * @param e The trigger event
     */
    @FXML public final void changeSlide(MouseEvent e) {
        if (this.slides.isEmpty()) {
            //No slides
            return;
        }
        Label source = (Label)e.getSource();
        if (source == this.slideBack) {
            if (this.currentSlide == 0) {
                this.currentSlide = this.slides.size() - 1;
            } else {
                this.currentSlide--;
            }
        } else if (source == this.slideForward) {
            if (this.currentSlide == this.slides.size() - 1) {
                this.currentSlide = 0;
            } else {
                this.currentSlide++;
            }
        }
        Slide s = this.slides.get(this.currentSlide);
        Thread t = new Thread(() -> {
            this.slideshow.setImage(s.getImage());
        });
        t.start();
        this.newsTitle.setText(s.getTitle());

        this.newsText.setText(s.getText());
    }

    /**
     * Performs an action when a slide is clicked
     */
    @FXML public final void performSlideAction() {
        if (this.slides.isEmpty()) {
            //No slides
            return;
        }
        Slide s = this.slides.get(this.currentSlide);
        this.kernel.getHostServices().showDocument(this.urlPrefix + s.getAction());
    }

    /**
     * Rotates the skin preview
     * @param e The trigger event
     */
    @FXML public final void rotatePreview(MouseEvent e) {
        Label src = (Label)e.getSource();
        if (src == this.rotateRight) {
            if (this.currentPreview < 3) {
                this.currentPreview++;
                this.skinPreview.setImage(this.skinPreviews[this.currentPreview]);
            } else {
                this.currentPreview = 0;
                this.skinPreview.setImage(this.skinPreviews[this.currentPreview]);
            }
        } else if (src == this.rotateLeft) {
            if (this.currentPreview > 0) {
                this.currentPreview--;
                this.skinPreview.setImage(this.skinPreviews[this.currentPreview]);
            } else {
                this.currentPreview = 3;
                this.skinPreview.setImage(this.skinPreviews[this.currentPreview]);
            }
        }
    }

    /**
     * Loads profiles list items
     */
    private void loadProfileList() {
        this.console.print("Loading profile list...");
        ObservableList<Label> profileListItems = this.getProfileList();

        //Add "Add New Profile" item
        profileListItems.add(0, new Label(Language.get(51), new ImageView(new Image("/kml/gui/textures/add.png"))));
        this.profileList.setItems(profileListItems);
        this.profileListLoaded = true;
        this.console.print("Profile list loaded.");
    }

    /**
     * Loads profiles popup list items
     */
    private void loadProfileListPopup() {
        this.console.print("Loading profile list popup...");
        ObservableList<Label> profileListItems = this.getProfileList();
        this.profilePopupList.setItems(profileListItems);
        this.profileListPopupLoaded = true;
        this.console.print("Profile list popup loaded.");
    }

    /**
     * Generates an ObservableList of Labels representing each profile
     * @return The profiles ObservableList
     */
    private ObservableList<Label> getProfileList() {
        ObservableList<Label> profileListItems = FXCollections.observableArrayList();
        Profiles ps = this.kernel.getProfiles();
        Settings settings = this.kernel.getSettings();
        Label l;
        ImageView iv;
        String text;
        for (Profile p : ps.getProfiles()) {
            if (p.getType() == ProfileType.SNAPSHOT && !settings.getEnableSnapshots()) {
                continue;
            }
            if (p.getType() == ProfileType.RELEASE) {
                iv = new ImageView(this.kernel.getProfileIcon(ProfileIcon.GRASS));
                text = Language.get(59);
            } else if (p.getType() == ProfileType.SNAPSHOT) {
                iv = new ImageView(this.kernel.getProfileIcon(ProfileIcon.CRAFTING_TABLE));
                text = Language.get(60);
            } else {
                text = p.hasName() ? p.getName() : Language.get(70);
                ProfileIcon pi = p.hasIcon() ? p.getIcon() : ProfileIcon.FURNACE;
                iv = new ImageView(this.kernel.getProfileIcon(pi));
            }
            iv.setFitWidth(68);
            iv.setFitHeight(68);
            l = new Label(text, iv);
            //Fetch Minecraft version used by the profile
            VersionMeta verID;
            if (p.getType() == ProfileType.CUSTOM) {
                Versions versions = this.kernel.getVersions();
                verID = p.hasVersion() ? p.getVersionID() : versions.getLatestRelease();
            } else if (p.getType() == ProfileType.RELEASE) {
                verID = this.kernel.getVersions().getLatestRelease();
            } else {
                verID = this.kernel.getVersions().getLatestSnapshot();
            }
            l.setId(p.getID());
            if (verID != null) {
                //If profile has any known version just show it below the profile name
                if (verID.getType() == VersionType.SNAPSHOT && !this.kernel.getSettings().getEnableSnapshots()) {
                    continue;
                }
                if ((verID.getType() == VersionType.OLD_ALPHA || verID.getType() == VersionType.OLD_BETA) && !this.kernel.getSettings().getEnableHistorical()) {
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
        Profile p = this.kernel.getProfiles().getSelectedProfile();
        if (p != null) {
            if (p.getType() == ProfileType.RELEASE) {
                this.gameVersion.setText(Language.get(26));
            } else if (p.getType() == ProfileType.SNAPSHOT) {
                this.gameVersion.setText(Language.get(32));
            } else {
                if (p.isLatestRelease()) {
                    this.gameVersion.setText(Language.get(26));
                } else if (p.isLatestSnapshot()) {
                    this.gameVersion.setText(Language.get(32));
                } else if (p.hasVersion()) {
                    VersionMeta version = p.getVersionID();
                    this.gameVersion.setText("Minecraft " + version.getID());
                }
            }
        }  else {
            this.gameVersion.setText("");
        }
    }

    /**
     * Loads the profile icons
     */
    private void loadIcons() {
        this.console.print("Loading icons...");
        ObservableList<ImageView> icons = FXCollections.observableArrayList();
        for (ProfileIcon p : ProfileIcon.values()) {
            if (p != ProfileIcon.CRAFTING_TABLE && p != ProfileIcon.GRASS) {
                ImageView imv = new ImageView(this.kernel.getProfileIcon(p));
                imv.setFitHeight(68);
                imv.setFitWidth(68);
                imv.setId(p.name());
                icons.add(imv);
            }
        }
        this.iconList.setItems(icons);
        this.console.print("Icons loaded.");
    }

    /**
     * Validates the selected profile according to the constraints
     */
    private void validateSelectedProfile() {
        Profiles ps = this.kernel.getProfiles();

        //Check if selected profile passes the current settings
        Profile selected = ps.getSelectedProfile();
        VersionMeta selectedVersion = selected.getVersionID();
        Settings settings = this.kernel.getSettings();

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

        this.updateGameVersion();
    }

    /**
     * Selects the selected profile from the list
     */
    @FXML private void selectProfile() {
        if (this.profilePopupList.getSelectionModel().getSelectedIndex() == -1) {
            //Nothing has been selected
            return;
        }
        //Select profile and refresh list
        this.kernel.getProfiles().setSelectedProfile(this.kernel.getProfiles().getProfile(this.profilePopupList.getSelectionModel().getSelectedItem().getId()));
        this.updateGameVersion();
        SingleSelectionModel<Tab> selection = this.contentPane.getSelectionModel();
        Tab selectedTab = selection.getSelectedItem();
        if (selectedTab == this.launchOptionsTab) {
            this.loadProfileList();
        } else {
            this.profileListLoaded = false;
        }
        this.profileListPopupLoaded = false;
        this.profilePopupList.setVisible(false);
    }

    /**
     * Downloads and launches the game
     */
    @FXML public final void launchGame() {
        this.progressPane.setVisible(true);
        this.playPane.setVisible(false);
        this.progressBar.setProgress(0);
        this.progressText.setText("");
        Downloader d = this.kernel.getDownloader();
        GameLauncher gl = this.kernel.getGameLauncher();

        //Keep track of the progress
        TimerTask progressTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    MainFX.this.progressBar.setProgress(d.getProgress());
                    MainFX.this.progressText.setText(Language.get(13) + ' ' + d.getCurrentFile() + "...");
                });
            }
        };

        Thread runThread = new Thread(() -> {
            //Begin download and game launch task
            try {
                Timer timer = new Timer();
                timer.schedule(progressTask, 0, 500);
                d.download();
                timer.cancel();
                timer.purge();
                Platform.runLater(() -> {
                    this.progressText.setText(Language.get(78));
                    this.progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                });
                gl.launch(this);

                Platform.runLater(() -> {
                    this.progressPane.setVisible(false);
                    this.playPane.setVisible(true);
                    this.playButton.setText(Language.get(14));
                    this.playButton.setDisable(true);
                });

                if (!MainFX.this.kernel.getSettings().getKeepLauncherOpen()) {
                    Platform.runLater(() -> MainFX.this.stage.close());
                }
            } catch (DownloaderException e) {
                Platform.runLater(() -> MainFX.this.kernel.showAlert(AlertType.ERROR, Language.get(83), Language.get(84)));
                MainFX.this.console.print("Failed to perform game download task");
                e.printStackTrace(MainFX.this.console.getWriter());
            } catch (GameLauncherException e) {
                Platform.runLater(() -> MainFX.this.kernel.showAlert(AlertType.ERROR, Language.get(81), Language.get(82)));
                MainFX.this.console.print("Failed to perform game launch task");
                e.printStackTrace(MainFX.this.console.getWriter());
            }
        });
        runThread.start();
    }

    /**
     * Callback from Game Launcher
     * @param error True if an error happened during launch
     */
    public final void gameEnded(boolean error) {
        if (error) {
            this.kernel.showAlert(AlertType.ERROR, Language.get(16), Language.get(15));
        }
        if (!this.kernel.getSettings().getKeepLauncherOpen()) {
            this.kernel.exitSafely();
        }
        this.playButton.setDisable(false);
        Platform.runLater(() -> {
            if (Kernel.USE_LOCAL) {
                this.playButton.setText(Language.get(79));
            } else {
                this.playButton.setText(Language.get(12));
            }
        });
    }

    /**
     * Shows the language list
     */
    @FXML public final void showLanguages(Event e) {
        e.consume();
        if (this.languagesList.isVisible()) {
            this.languagesList.setVisible(false);
        } else {
            if (!this.languageListLoaded) {
                this.loadLanguages();
                this.languageListLoaded = true;
            }
            this.languagesList.setVisible(true);
        }
    }

    public final void checkPopups(MouseEvent e) {
        if (this.languagesList.isVisible()) {
            this.languagesList.setVisible(false);
        }
        if (this.switchAccountButton.isVisible()) {
            this.switchAccountButton.setVisible(false);
        }
        if (this.profilePopupList.isVisible()) {
            this.profilePopupList.setVisible(false);
        }
        if (this.iconList.isVisible()) {
            this.iconList.setVisible(false);
        }
    }

    /**
     * Deselects the current user and allows to select another
     */
    @FXML public final void switchAccount() {
        if (this.switchAccountButton.isVisible()) {
            this.switchAccountButton.setVisible(false);
        }
        Authentication a = this.kernel.getAuthentication();
        a.setSelectedUser(null);
        this.showLoginPrompt(true);
        this.updateExistingUsers();
    }

    /**
     * Shows the profile popup list
     */
    @FXML public final void showProfiles() {
        if (this.profilePopupList.isVisible()) {
            this.profilePopupList.setVisible(false);
        } else {
            if (!this.profileListPopupLoaded) {
                this.loadProfileListPopup();
            }
            Bounds b = this.playButton.localToScene(this.playButton.getBoundsInLocal());
            this.profilePopupList.setTranslateX(b.getMinX() - 100);
            this.profilePopupList.setTranslateY(b.getMinY() - 180);
            this.profilePopupList.setVisible(true);
            this.profilePopupList.getSelectionModel().clearSelection();
        }
    }

    /**
     * Shows the profile editor profile icons
     */
    @FXML public final void showIcons(Event e) {
        e.consume();
        if (this.iconList.isVisible()) {
            this.iconList.setVisible(false);
        } else {
            if (!this.iconListLoaded) {
                this.loadIcons();
                this.iconListLoaded = true;
            }
            this.iconList.setVisible(true);
        }
    }

    /**
     * Shows the Switch Account option when the user label is clicked
     */
    @FXML public final void showAccountOptions(Event e) {
        e.consume();
        if (this.switchAccountButton.isVisible()) {
            this.switchAccountButton.setVisible(false);
        } else {
            Bounds b = this.accountButton.localToScene(this.accountButton.getBoundsInLocal());
            //Avoid the label getting out of bounds
            if (b.getMinX() - 5 + this.switchAccountButton.getWidth() > this.stage.getWidth()) {
                this.switchAccountButton.setTranslateX(this.stage.getWidth() - this.switchAccountButton.getWidth() - 5);
            } else {
                this.switchAccountButton.setTranslateX(b.getMinX() - 5);
            }
            this.switchAccountButton.setTranslateY(b.getMaxY() + 5);
            this.switchAccountButton.setVisible(true);
        }
    }

    /**
     * Switched the selected tab according to the clicked label
     * @param e The trigger event
     */
    @FXML public final void switchTab(Event e) {
        this.switchTab(e.getSource());
    }

    /**
     * Switched the selected tab according to the source
     * @param source The object that trigger the change
     */
    private void switchTab(Object source) {
        SingleSelectionModel<Tab> selection = this.contentPane.getSelectionModel();
        Tab oldTab = selection.getSelectedItem();
        if (oldTab == this.newsTab) {
            this.newsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == this.skinsTab) {
            this.skinsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == this.settingsTab) {
            this.settingsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == this.launchOptionsTab && source != this.profileEditorTab) {
            this.launchOptionsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == this.profileEditorTab) {
            //Show play button
            this.playPane.setVisible(true);
            this.launchOptionsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == this.loginTab) {
            this.newsLabel.getStyleClass().remove("selectedItem");
            this.skinsLabel.getStyleClass().remove("selectedItem");
            this.settingsLabel.getStyleClass().remove("selectedItem");
            this.launchOptionsLabel.getStyleClass().remove("selectedItem");
        }
        if (source == this.newsLabel) {
            this.newsLabel.getStyleClass().add("selectedItem");
            selection.select(this.newsTab);
        } else if (source == this.skinsLabel) {
            this.skinsLabel.getStyleClass().add("selectedItem");
            selection.select(this.skinsTab);
            if (!this.texturesLoaded) {
                this.loadTextures();
            }
        } else if (source == this.settingsLabel) {
            this.settingsLabel.getStyleClass().add("selectedItem");
            selection.select(this.settingsTab);
        } else if (source == this.launchOptionsLabel) {
            this.launchOptionsLabel.getStyleClass().add("selectedItem");
            selection.select(this.launchOptionsTab);
            if (!this.profileListLoaded) {
                this.loadProfileList();
            }
            this.profileList.getSelectionModel().clearSelection();
        } else if (source == this.profileEditorTab) {
            //Hide play button
            this.playPane.setVisible(false);
            selection.select(this.profileEditorTab);
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
        if (this.languagesList.getSelectionModel().getSelectedIndex() == -1) {
            //Nothing has been selected
            return;
        }
        Label selected = this.languagesList.getSelectionModel().getSelectedItem();
        this.languageButton.setText(selected.getText());
        this.kernel.getSettings().setLocale(selected.getId());
        this.languagesList.setVisible(false);
        SingleSelectionModel<Tab> selection = this.contentPane.getSelectionModel();
        Tab selectedTab = selection.getSelectedItem();
        if (selectedTab == this.launchOptionsTab) {
            this.loadProfileList();
        } else {
            this.profileListLoaded = false;
        }
        if (selectedTab == this.profileEditorTab) {
            this.loadVersionList();
        } else {
            this.versionListLoaded = false;
        }
        this.profileListPopupLoaded = false;
        this.localizeElements();
    }

    /**
     * Updates the selected icon
     */
    @FXML public final void updateIcon() {
        if (this.iconList.getSelectionModel().getSelectedIndex() == -1) {
            //Nothing has been selected
            return;
        }
        ImageView selected = this.iconList.getSelectionModel().getSelectedItem();
        this.profileIcon.setImage(selected.getImage());
        this.profileIcon.setId(selected.getId());
        this.iconList.setVisible(false);
    }

    /**
     * Prepares the editor with the selected profile or with a new one
     */
    @FXML public final void loadEditor() {
        if (this.profileList.getSelectionModel().getSelectedIndex() == -1) {
            //Nothing has been selected
            return;
        }
        if (!this.versionListLoaded) {
            this.loadVersionList();
        }
        if (this.profileList.getSelectionModel().getSelectedIndex() == 0) {
            this.profileName.setEditable(true);
            this.profileName.setText("");
            this.deleteButton.setVisible(false);
            this.versionBlock.setVisible(true);
            this.versionBlock.setManaged(true);
            this.iconBlock.setVisible(true);
            this.iconBlock.setManaged(true);
            this.versionList.getSelectionModel().select(0);
            this.profileIcon.setImage(this.kernel.getProfileIcon(ProfileIcon.FURNACE));
            if (this.kernel.getSettings().getEnableAdvanced()) {
                this.javaExecBlock.setVisible(true);
                this.javaExecBlock.setManaged(true);
                this.javaArgsBlock.setVisible(true);
                this.javaArgsBlock.setManaged(true);
                this.toggleEditorOption(this.javaExecLabel, false);
                this.javaExec.setText(Utils.getJavaDir());
                this.toggleEditorOption(this.javaArgsLabel, false);
                StringBuilder jA = new StringBuilder(15);
                if (Utils.getOSArch() == OSArch.OLD) {
                    jA.append("-Xmx1G");
                } else {
                    jA.append("-Xmx2G");
                }
                jA.append(" -Xmn128M");
                this.javaArgs.setText(jA.toString());
            } else {
                this.javaExecBlock.setVisible(false);
                this.javaExecBlock.setManaged(false);
                this.javaArgsBlock.setVisible(false);
                this.javaArgsBlock.setManaged(false);
            }
            this.toggleEditorOption(this.resolutionLabel, false);
            this.resW.setText(String.valueOf(854));
            this.resH.setText(String.valueOf(480));
            this.toggleEditorOption(this.gameDirLabel, false);
            this.gameDir.setText(Utils.getWorkingDirectory().getAbsolutePath());
        } else {
            Label selectedElement = this.profileList.getSelectionModel().getSelectedItem();
            if (selectedElement != null) {
                Profile p = this.kernel.getProfiles().getProfile(selectedElement.getId());
                if (p.getType() != ProfileType.CUSTOM) {
                    this.profileName.setEditable(false);
                    this.deleteButton.setVisible(false);
                    if (p.getType() == ProfileType.RELEASE) {
                        this.profileName.setText(Language.get(59));
                        this.profileIcon.setImage(this.kernel.getProfileIcon(ProfileIcon.GRASS));
                    } else {
                        this.profileName.setText(Language.get(60));
                        this.profileIcon.setImage(this.kernel.getProfileIcon(ProfileIcon.CRAFTING_TABLE));
                    }
                    this.versionBlock.setVisible(false);
                    this.versionBlock.setManaged(false);
                    this.iconBlock.setVisible(false);
                    this.iconBlock.setManaged(false);
                } else {
                    if (p.hasIcon()) {
                        this.profileIcon.setImage(this.kernel.getProfileIcon(p.getIcon()));
                        this.profileIcon.setId(p.getIcon().name());
                    } else {
                        this.profileIcon.setImage(this.kernel.getProfileIcon(ProfileIcon.FURNACE));
                    }
                    this.profileName.setEditable(true);
                    this.deleteButton.setVisible(true);
                    if (p.hasName()){
                        this.profileName.setText(p.getName());
                    } else {
                        this.profileName.setText("");
                    }
                    this.versionBlock.setVisible(true);
                    this.versionBlock.setManaged(true);
                    this.iconBlock.setVisible(true);
                    this.iconBlock.setManaged(true);
                    if (p.hasVersion()) {
                        if (p.isLatestRelease()) {
                            this.versionList.getSelectionModel().select(0);
                        } else if (p.isLatestSnapshot() && this.kernel.getSettings().getEnableSnapshots()) {
                            this.versionList.getSelectionModel().select(1);
                        } else if (this.versionList.getItems().contains(p.getVersionID())) {
                            this.versionList.getSelectionModel().select(p.getVersionID());
                        } else {
                            this.versionList.getSelectionModel().select(0);
                        }
                    } else {
                        this.versionList.getSelectionModel().select(0);
                    }
                }

                if (p.hasResolution()) {
                    this.toggleEditorOption(this.resolutionLabel, true);
                    this.resH.setText(String.valueOf(p.getResolutionHeight()));
                    this.resW.setText(String.valueOf(p.getResolutionWidth()));
                } else {
                    this.toggleEditorOption(this.resolutionLabel, false);
                    this.resW.setText(String.valueOf(854));
                    this.resH.setText(String.valueOf(480));
                }
                if (p.hasGameDir()) {
                    this.toggleEditorOption(this.gameDirLabel, true);
                    this.gameDir.setText(p.getGameDir().getAbsolutePath());
                } else {
                    this.toggleEditorOption(this.gameDirLabel, false);
                    this.gameDir.setText(Utils.getWorkingDirectory().getAbsolutePath());
                }
                if (this.kernel.getSettings().getEnableAdvanced()) {
                    this.javaExecBlock.setVisible(true);
                    this.javaExecBlock.setManaged(true);
                    this.javaArgsBlock.setVisible(true);
                    this.javaArgsBlock.setManaged(true);
                    if (p.hasJavaDir()){
                        this.toggleEditorOption(this.javaExecLabel, true);
                        this.javaExec.setText(p.getJavaDir().getAbsolutePath());
                    } else {
                        this.toggleEditorOption(this.javaExecLabel, false);
                        this.javaExec.setText(Utils.getJavaDir());
                    }
                    if (p.hasJavaArgs()) {
                        this.toggleEditorOption(this.javaArgsLabel, true);
                        this.javaArgs.setText(p.getJavaArgs());
                    } else {
                        this.toggleEditorOption(this.javaArgsLabel, false);
                        StringBuilder jA = new StringBuilder(15);
                        if (Utils.getOSArch() == OSArch.OLD) {
                            jA.append("-Xmx1G");
                        } else {
                            jA.append("-Xmx2G");
                        }
                        jA.append(" -Xmn128M");
                        this.javaArgs.setText(jA.toString());
                    }
                } else {
                    this.javaExecBlock.setVisible(false);
                    this.javaExecBlock.setManaged(false);
                    this.javaArgsBlock.setVisible(false);
                    this.javaArgsBlock.setManaged(false);
                }
            }
        }
        this.switchTab(this.profileEditorTab);
    }

    /**
     * Loads the list of version for the profile editor
     */
    private void loadVersionList() {
        this.console.print("Loading version list...");
        ObservableList<VersionMeta> vers = FXCollections.observableArrayList();
        VersionMeta latestVersion = new VersionMeta(Language.get(59), null, null);
        vers.add(latestVersion);
        if (this.kernel.getSettings().getEnableSnapshots()) {
            VersionMeta latestSnapshot = new VersionMeta(Language.get(60), null, null);
            vers.add(latestSnapshot);
        }
        for (VersionMeta v : this.kernel.getVersions().getVersions()) {
            if (v.getType() == VersionType.RELEASE) {
                vers.add(v);
            } else if (v.getType() == VersionType.SNAPSHOT && this.kernel.getSettings().getEnableSnapshots()) {
                vers.add(v);
            } else if ((v.getType() == VersionType.OLD_BETA || v.getType() == VersionType.OLD_ALPHA) && this.kernel.getSettings().getEnableHistorical()) {
                vers.add(v);
            }
        }

        this.versionList.setItems(vers);
        if (!this.versionListLoaded) {
            this.versionList.getSelectionModel().select(0);
            this.versionListLoaded = true;
        }
        this.console.print("Version list loaded.");
    }

    /**
     * Saves the profile data from the profile editor
     */
    @FXML public final void saveProfile() {
        Profile target;
        if (this.profileList.getSelectionModel().getSelectedIndex() == 0) {
            target = new Profile(ProfileType.CUSTOM);
            this.kernel.getProfiles().addProfile(target);
        } else {
            Label selectedElement = this.profileList.getSelectionModel().getSelectedItem();
            target = this.kernel.getProfiles().getProfile(selectedElement.getId());
        }
        if (target.getType() == ProfileType.CUSTOM) {
            if (!this.profileName.getText().isEmpty()) {
                target.setName(this.profileName.getText());
            } else {
                target.setName(null);
            }
            if (this.versionList.getSelectionModel().getSelectedIndex() == 0) {
                target.setVersionID(this.kernel.getVersions().getLatestRelease());
                target.setLatestRelease(true);
                target.setLatestSnapshot(false);
            } else if (this.versionList.getSelectionModel().getSelectedIndex() == 1 && this.kernel.getSettings().getEnableSnapshots()) {
                target.setVersionID(this.kernel.getVersions().getLatestSnapshot());
                target.setLatestRelease(false);
                target.setLatestSnapshot(true);
            } else {
                target.setVersionID(this.versionList.getSelectionModel().getSelectedItem());
                target.setLatestRelease(false);
                target.setLatestSnapshot(false);
            }
            try {
                target.setIcon(ProfileIcon.valueOf(this.profileIcon.getId()));
            } catch (IllegalArgumentException ex) {
                target.setIcon(null);
            }
        }
        if (!this.resW.isDisabled()) {
            try {
                int h = Integer.parseInt(this.resH.getText());
                int w = Integer.parseInt(this.resW.getText());
                target.setResolution(w, h);
            } catch (NumberFormatException ex) {
                this.console.print("Invalid resolution given.");
            }
        } else {
            target.setResolution(-1, -1);
        }
        if (!this.gameDir.isDisabled() && !this.gameDir.getText().isEmpty()) {
            target.setGameDir(new File(this.gameDir.getText()));
        } else {
            target.setGameDir(null);
        }
        if (this.kernel.getSettings().getEnableAdvanced()) {
            if (!this.javaExec.isDisabled() && !this.javaExec.getText().isEmpty()) {
                target.setJavaDir(new File(this.javaExec.getText()));
            } else {
                target.setJavaDir(null);
            }
            if (!this.javaArgs.isDisabled() && !this.javaArgs.getText().isEmpty()) {
                target.setJavaArgs(this.javaArgs.getText());
            } else {
                target.setJavaArgs(null);
            }
        }

        this.kernel.showAlert(AlertType.INFORMATION, null, Language.get(57));
        this.loadProfileList();
        this.switchTab(this.launchOptionsLabel);
    }

    /**
     * Discards the changes of the profile editor
     */
    @FXML public final void cancelProfile() {
        int result = this.kernel.showAlert(AlertType.CONFIRMATION, null, Language.get(55));
        if (result == 1) {
            this.switchTab(this.launchOptionsLabel);
        }
    }

    /**
     * Deletes the profile loaded by the profile editor
     */
    @FXML public final void deleteProfile() {
        int result = this.kernel.showAlert(AlertType.CONFIRMATION, null, Language.get(61));
        if (result == 1) {
            Label selectedElement = this.profileList.getSelectionModel().getSelectedItem();
            if (this.kernel.getProfiles().deleteProfile(this.kernel.getProfiles().getProfile(selectedElement.getId()))) {
                this.kernel.showAlert(AlertType.INFORMATION, null, Language.get(56));
            } else {
                this.kernel.showAlert(AlertType.ERROR, null, Language.get(58));
            }
            this.loadProfileList();
            this.switchTab(this.launchOptionsLabel);
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
            this.toggleLabel(l, newState);
        }
        if (src == this.resolutionLabel) {
            this.resW.setDisable(!newState);
            this.resH.setDisable(!newState);
        } else if (src == this.gameDirLabel) {
            this.gameDir.setDisable(!newState);
        } else if (src == this.javaExecLabel) {
            this.javaExec.setDisable(!newState);
        } else if (src == this.javaArgsLabel) {
            this.javaArgs.setDisable(!newState);
        }
    }

    /**
     * Update editor when clicking labels. This method fetches the adjacent sibling to determine if is disabled
     * @param e The event trigger
     */
    @FXML public final void updateEditor(MouseEvent e) {
        Label l = (Label)e.getSource();
        this.toggleEditorOption(l, l.getParent().getChildrenUnmodifiable().get(1).isDisable());
    }

    /**
     * Updates the existing users list
     */
    private void updateExistingUsers() {
        Authentication a = this.kernel.getAuthentication();
        if (!a.getUsers().isEmpty() && a.getSelectedUser() == null) {
            this.existingPanel.setVisible(true);
            this.existingPanel.setManaged(true);
            ObservableList<User> users = FXCollections.observableArrayList();
            Set<User> us = a.getUsers();
            users.addAll(us);
            this.existingUsers.setItems(users);
            this.existingUsers.getSelectionModel().select(0);
        } else {
            this.existingPanel.setVisible(false);
            this.existingPanel.setManaged(false);
        }
    }

    /**
     * Shows or hides the login prompt
     * @param showLoginPrompt The new state
     */
    private void showLoginPrompt(boolean showLoginPrompt) {
        if (showLoginPrompt) {
            this.contentPane.getSelectionModel().select(this.loginTab);
            this.tabMenu.setVisible(false);
            this.tabMenu.setManaged(false);
            this.accountButton.setVisible(false);
            this.playPane.setVisible(false);
            this.updateExistingUsers();
        } else {
            this.switchTab(this.newsLabel);
            this.tabMenu.setVisible(true);
            this.tabMenu.setManaged(true);
            this.accountButton.setVisible(true);
            this.playPane.setVisible(true);
            //Set account name for current user
            this.accountButton.setText(this.kernel.getAuthentication().getSelectedUser().getDisplayName() + " ▼");
        }
    }

    /**
     * Performs an authenticate with the data typed in the login form
     */
    public final void authenticate() {
        if (this.username.getText().isEmpty()) {
            this.kernel.showAlert(AlertType.WARNING, null, Language.get(17));
        } else if (this.password.getText().isEmpty()) {
            this.kernel.showAlert(AlertType.WARNING, null, Language.get(23));
        } else {
            try {
                Authentication auth = this.kernel.getAuthentication();
                String username;
                if (authKrothium.isSelected()) {
                    username = "krothium://" + this.username.getText();
                } else {
                    username = this.username.getText();
                }
                auth.authenticate(username, this.password.getText());
                this.username.setText("");
                this.password.setText("");
                this.showLoginPrompt(false);
                this.fetchAds();
                this.texturesLoaded = false;
            } catch (AuthenticationException ex) {
                this.kernel.showAlert(AlertType.ERROR, Language.get(22), ex.getMessage());
                this.password.setText("");
            }
        }
    }

    /**
     * Refreshes latest session
     */
    private void refreshSession() {
        this.console.print("Refreshing session...");
        Authentication a = this.kernel.getAuthentication();
        User u = a.getSelectedUser();
        try {
            if (u != null) {
                a.refresh();
                this.texturesLoaded = false;
                this.console.print("Session refreshed.");
            } else {
                this.console.print("No user is selected.");
            }
        } catch (AuthenticationException ex) {
            if (u.getType() == UserType.KROTHIUM) {
                authKrothium.setSelected(true);
                username.setText(u.getUsername().replace("krothium://", ""));
            } else {
                authMojang.setSelected(true);
                username.setText(u.getUsername());
            }
            this.console.print("Couldn't refresh your session.");
        } finally {
            if (a.isAuthenticated()) {
                this.showLoginPrompt(false);
                this.fetchAds();
            } else {
                this.showLoginPrompt(true);
            }
        }
    }

    /**
     * Refreshes user selected from the existing user list
     */
    public final void refresh() {
        User selected = this.existingUsers.getSelectionModel().getSelectedItem();
        Authentication auth = this.kernel.getAuthentication();
        try {
            auth.setSelectedUser(selected);
            auth.refresh();
            this.texturesLoaded = false;
            this.showLoginPrompt(false);
            this.fetchAds();
        } catch (AuthenticationException ex) {
            this.kernel.showAlert(AlertType.ERROR, Language.get(62), ex.getMessage());
            this.updateExistingUsers();
        }
    }

    /**
     * Logs out the selected user from the existing user list
     */
    public final void logout() {
        User selected = this.existingUsers.getSelectionModel().getSelectedItem();
        int result = this.kernel.showAlert(AlertType.CONFIRMATION, null, Language.get(8));
        if (result == 1) {
            Authentication auth = this.kernel.getAuthentication();
            auth.removeUser(selected);
            this.updateExistingUsers();
        }
    }

    /**
     * Opens the register page
     */
    @FXML public final void register() {
        //Open register page
        if (this.authKrothium.isSelected()) {
            this.kernel.getHostServices().showDocument(this.urlPrefix + "https://krothium.com/register");
        } else {
            this.kernel.getHostServices().showDocument("https://minecraft.net/");
        }
    }

    /**
     * Opens the help page
     */
    @FXML public final void openHelp() {
        //Open help page
        this.kernel.getHostServices().showDocument(this.urlPrefix + "https://krothium.com/forum/12-soporte/");
    }

    /**
     * Opens the news page
     */
    @FXML public final void openNews() {
        //Open news page
        this.kernel.getHostServices().showDocument(this.urlPrefix + "https://krothium.com/forum/3-noticias/");
    }

    /**
     * Performs an authenticate if the Enter key is pressed in the Username or Password field
     * @param e The trigger event
     */
    @FXML public final void triggerAuthenticate(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            this.authenticate();
        }
    }

    /**
     * Updates the settings according to the label cicked
     * @param e The trigger event
     */
    @FXML public final void updateSettings(MouseEvent e) {
        Label source = (Label)e.getSource();
        Settings s = this.kernel.getSettings();
        if (source == this.keepLauncherOpen) {
            s.setKeepLauncherOpen(!s.getKeepLauncherOpen());
            this.toggleLabel(source, s.getKeepLauncherOpen());
        } else if (source == this.outputLog) {
            s.setShowGameLog(!s.getShowGameLog());
            this.toggleLabel(source, s.getShowGameLog());
        } else if (source == this.enableSnapshots) {
            if (!s.getEnableSnapshots()) {
                this.kernel.showAlert(AlertType.WARNING, null, Language.get(71) + System.lineSeparator() + Language.get(72));
            }
            s.setEnableSnapshots(!s.getEnableSnapshots());
            this.toggleLabel(source, s.getEnableSnapshots());
            this.loadProfileList();
            this.versionListLoaded = false;
        } else if (source == this.historicalVersions) {
            if (!s.getEnableHistorical()) {
                this.kernel.showAlert(AlertType.WARNING, null, Language.get(73) + System.lineSeparator()
                        + Language.get(74) + System.lineSeparator()
                        + Language.get(75));

            }
            s.setEnableHistorical(!s.getEnableHistorical());
            this.toggleLabel(source, s.getEnableHistorical());
            this.loadProfileList();
            this.versionListLoaded = false;
        } else if (source == this.advancedSettings) {
            if (!s.getEnableAdvanced()) {
                this.kernel.showAlert(AlertType.WARNING, null, Language.get(76) + System.lineSeparator() + Language.get(77));
            }
            s.setEnableAdvanced(!s.getEnableAdvanced());
            this.toggleLabel(source, s.getEnableAdvanced());
        }
    }

    /**
     * Changes any label icon
     * @param label The target label
     * @param state The new state
     */
    private void toggleLabel(Styleable label, boolean state) {
        label.getStyleClass().clear();
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
        File selected = this.selectFile("ZIP", "*.zip", "save");
        if (selected != null) {
            try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(selected))) {
                File[] files = Kernel.APPLICATION_LOGS.listFiles();
                for (File file : files) {
                    ZipEntry entry = new ZipEntry(file.getName());
                    out.putNextEntry(entry);
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    out.write(bytes);
                    out.closeEntry();
                }
                this.kernel.showAlert(AlertType.INFORMATION, null, Language.get(35) + System.lineSeparator() + selected.getAbsolutePath());
            } catch (IOException ex) {
                this.kernel.showAlert(AlertType.ERROR, null, Language.get(35) + '\n' + selected.getAbsolutePath());
            }
        }
    }

    /**
     * Opens the URL of the selected version server in the default user web browser
     */
    @FXML private void downloadServer() {
        VersionMeta selectedItem = this.versionList.getSelectionModel().getSelectedItem();
        this.kernel.getHostServices().showDocument(this.urlPrefix + "https://s3.amazonaws.com/Minecraft.Download/versions/" + selectedItem.getID() + "/minecraft_server." + selectedItem.getID() + ".jar");
    }

    /**
     * Selects a game directory for the profile editor
     */
    @FXML private void selectGameDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        if (this.gameDir.getText().isEmpty()) {
            chooser.setInitialDirectory(Kernel.APPLICATION_WORKING_DIR);
        } else {
            File gd = new File(this.gameDir.getText());
            if (gd.isDirectory()) {
                chooser.setInitialDirectory(gd);
            } else {
                chooser.setInitialDirectory(Kernel.APPLICATION_WORKING_DIR);
            }
        }
        File selectedFolder = chooser.showDialog(null);
        if (selectedFolder != null) {
            this.gameDir.setText(selectedFolder.getAbsolutePath());
        }
    }

    /**
     * Selects the java executable for the profile editor
     */
    @FXML private void selectJavaExecutable() {
        File selected = this.selectFile(null, null, "open");
        if (selected != null && selected.isFile()) {
            this.javaExec.setText(selected.getAbsolutePath());
        }
    }

    /**
     * Update auth server label on existing users
     */
    @FXML public void updateAuthServer() {
        User user = this.existingUsers.getValue();
        if (user != null) {
            if (user.getType() == UserType.KROTHIUM) {
                this.authServer.setText("(Krothium)");
            } else {
                this.authServer.setText("(Mojang)");
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
            return chooser.showOpenDialog(null);
        } else if (method.equalsIgnoreCase("save")) {
            return chooser.showSaveDialog(null);
        }
        return null;
    }
}
