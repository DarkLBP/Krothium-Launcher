package kml.gui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import javafx.stage.Stage;
import javafx.util.Duration;
import kml.*;
import kml.enums.OSArch;
import kml.enums.ProfileIcon;
import kml.enums.ProfileType;
import kml.enums.VersionType;
import kml.exceptions.AuthenticationException;
import kml.exceptions.DownloaderException;
import kml.exceptions.GameLauncherException;
import kml.objects.Profile;
import kml.objects.Slide;
import kml.objects.User;
import kml.objects.VersionMeta;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class MainFX {

    @FXML
    private Label progressText, newsLabel, skinsLabel, settingsLabel, launchOptionsLabel,
            keepLauncherOpen, outputLog, enableSnapshots, historicalVersions,
            advancedSettings, resolutionLabel, gameDirLabel, javaExecLabel, javaArgsLabel, accountButton,
            switchAccountButton, languageButton, newsTitle, newsText, slideBack, slideForward, rotateRight,
            rotateLeft, includeCape, versionLabel, usernameLabel, passwordLabel, existingLabel, launcherSettings,
            nameLabel, profileVersionLabel, skinLabel, capeLabel, modelLabel, iconLabel, helpButton;

    @FXML
    private Button playButton, deleteButton, changeIcon, deleteSkin, deleteCape, logoutButton,
            loginButton, registerButton, loginExisting, cancelButton, saveButton, selectSkin,
            selectCape, profilePopupButton, deleteCache, exportLogs, downloadServer;

    @FXML
    private Tab loginTab, newsTab, skinsTab,
            settingsTab, launchOptionsTab, profileEditorTab;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private TabPane contentPane;

    @FXML
    private ListView<Label> languagesList, profileList, profilePopupList;

    @FXML
    private ListView<ImageView> iconList;

    @FXML
    private VBox progressPane, existingPanel;

    @FXML
    private HBox playPane, tabMenu, slideshowBox;

    @FXML
    private TextField username, profileName,javaExec, gameDir, javaArgs;

    @FXML
    private Spinner<Integer> resH, resW;

    @FXML
    private PasswordField password;

    @FXML
    private ComboBox<User> existingUsers;

    @FXML
    private ComboBox<VersionMeta> versionList;

    @FXML
    private StackPane versionBlock, javaArgsBlock, javaExecBlock, iconBlock;

    @FXML
    private ImageView profileIcon, slideshow, skinPreview;

    @FXML
    private RadioButton skinClassic, skinSlim;


    private Kernel kernel;
    private Stage stage;
    private final ArrayList<Slide> slides = new ArrayList<>();
    private int currentSlide;
    private int currentPreview = 0; // 0 = front / 1 = right / 2 = back / 3 = left
    private final Image[] skinPreviews = new Image[4];
    private Image skin, cape, alex, steve;
    private String urlPrefix = "";

    /**
     * Initializes all required stuff from the GUI
     * @param k The Kernel instance
     * @param s The Stage instance
     */
    public void initialize(Kernel k, Stage s) {
        //Require to exit using Platform.exit()
        Platform.setImplicitExit(false);

        //Set kernel and stage
        kernel = k;
        stage = s;

        //Check for updates
        Thread updateThread = new Thread(this::checkForUpdates);
        updateThread.start();

        //Update version label
        versionLabel.setText(Constants.KERNEL_BUILD_NAME);

        //Load news slideshow
        slideshowBox.setVisible(false);
        slideshowBox.setManaged(false);
        newsTitle.setText("Loading news...");
        newsText.setText("Please wait a moment...");
        loadSlideshow();

        //Refresh session
        refreshSession();

        //Prepare language list
        String locale = kernel.getSettings().getLocale();
        Image flag_es, flag_pt, flag_us, flag_val, flag_br, flag_hu;
        flag_es = new Image("/kml/gui/textures/flags/flag_es-es.png");
        flag_us = new Image("/kml/gui/textures/flags/flag_en-us.png");
        flag_pt = new Image("/kml/gui/textures/flags/flag_pt-pt.png");
        flag_val = new Image("/kml/gui/textures/flags/flag_val-es.png");
        flag_br = new Image("/kml/gui/textures/flags/flag_pt-br.png");
        flag_hu = new Image("/kml/gui/textures/flags/flag_hu-hu.png");
        final Label en = new Label("English - United States", new ImageView(flag_us));
        en.setId("en-us");
        if (locale.equalsIgnoreCase(en.getId())) {
            languageButton.setText(en.getText());
        }
        final Label es = new Label("Español - España", new ImageView(flag_es));
        es.setId("es-es");
        if (locale.equalsIgnoreCase(es.getId())) {
            languageButton.setText(es.getText());
        }
        final Label val = new Label("Valencià - C. Valenciana", new ImageView(flag_val));
        val.setId("val-es");
        if (locale.equalsIgnoreCase(val.getId())) {
            languageButton.setText(val.getText());
        }
        final Label pt = new Label("Português - Portugal", new ImageView(flag_pt));
        pt.setId("pt-pt");
        if (locale.equalsIgnoreCase(pt.getId())) {
            languageButton.setText(pt.getText());
        }
        final Label br = new Label("Português - Brasil", new ImageView(flag_br));
        br.setId("pt-br");
        if (locale.equalsIgnoreCase(br.getId())) {
            languageButton.setText(br.getText());
        }
        final Label hu = new Label("Hungarian - Magyar", new ImageView(flag_hu));
        hu.setId("hu-hu");
        if (locale.equalsIgnoreCase(hu.getId())) {
            languageButton.setText(hu.getText());
        }
        ObservableList<Label> languageListItems = FXCollections.observableArrayList(en, es, val, pt, br, hu);
        languagesList.setItems(languageListItems);

        //Set news tab as default selected
        newsLabel.getStyleClass().add("selectedItem");

        //Update settings labels
        Settings st = kernel.getSettings();
        toggleLabel(keepLauncherOpen, st.getKeepLauncherOpen());
        toggleLabel(outputLog, st.getShowGameLog());
        toggleLabel(enableSnapshots, st.getEnableSnapshots());
        toggleLabel(historicalVersions, st.getEnableHistorical());
        toggleLabel(advancedSettings, st.getEnableAdvanced());

        //Make transparent areas to not target mouse events
        playPane.pickOnBoundsProperty().setValue(false);

        //Prepare Spinners
        resH.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0));
        resW.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0));
        resW.setEditable(true);
        resH.setEditable(true);

        //Load icons
        loadIcons();

        //Load placeholder skins
        alex = new Image("/kml/gui/textures/alex.png");
        steve = new Image("/kml/gui/textures/steve.png");

        //If offline mode make play button bigger for language support
        if (Constants.USE_LOCAL) {
            playButton.setMinWidth(290);
        }

        //Load version list
        loadVersionList();

        //Localize elements
        localizeElements();

        //Show window
        if (!kernel.getBrowser().isVisible()) {
            stage.show();
        }
    }

    /**
     * Checks for launcher updates
     */
    private void checkForUpdates() {
        kernel.getConsole().printInfo("Checking for updates...");
        String update = kernel.checkForUpdates();
        if (update != null) {
            Platform.runLater(() -> {
                Alert confirm = kernel.buildAlert(Alert.AlertType.CONFIRMATION, Language.get(11), Language.get(10));
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        kernel.getHostServices().showDocument(urlPrefix + Utils.fromBase64(update));
                    }
                    catch (Exception e) {
                        kernel.getConsole().printError("Failed to open update page.\n" + e.getMessage());
                    }
                }
            });
        }
    }

    /**
     * Fetches any advertisement available for the logged user
     */
    private void fetchAds() {
        String profileID = kernel.getAuthentication().getSelectedUser().getProfileID();
        URL adsCheck = Utils.stringToURL("https://mc.krothium.com/ads.php?profileID=" + profileID);
        String response = Utils.readURL(adsCheck);
        if (response != null) {
            if (!response.isEmpty()) {
                String firstChunk = Utils.fromBase64(response.split(":")[0]);
                String secondChunk = Utils.fromBase64(response.split(":")[1]);
                urlPrefix = firstChunk == null ? "" : firstChunk;
                String adsURL = secondChunk == null ? "" : secondChunk;
                kernel.getBrowser().loadWebsite(adsURL);
                kernel.getBrowser().show(stage);
            }
            kernel.getConsole().printInfo("Ads loaded.");
        } else {
            kernel.getConsole().printInfo("Ads info not available.");
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
        if (Constants.USE_LOCAL) {
            playButton.setText(Language.get(79));
        } else {
            playButton.setText(Language.get(12));
        }
        usernameLabel.setText(Language.get(18));
        passwordLabel.setText(Language.get(19));
        loginButton.setText(Language.get(20));
        loginExisting.setText(Language.get(20));
        registerButton.setText(Language.get(21));
        changeIcon.setText(Language.get(24));
        deleteCache.setText(Language.get(26));
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
        //Load profile list
        loadProfileList();
    }

    /**
     * Loads the skin preview for the logged user
     */
    private void parseRemoteTextures() {
        try {
            URL profileURL = Utils.stringToURL("https://mc.krothium.com/profiles/" + kernel.getAuthentication().getSelectedUser().getProfileID() + "?unsigned=true");
            JSONObject root = new JSONObject(Utils.readURL(profileURL));
            JSONArray properties = root.getJSONArray("properties");
            for (int i = 0; i < properties.length(); i++) {
                JSONObject property = properties.getJSONObject(i);
                if (property.getString("name").equalsIgnoreCase("textures")) {
                    JSONObject data = new JSONObject(Utils.fromBase64(property.getString("value")));
                    JSONObject textures = data.getJSONObject("textures");
                    skin = null;
                    cape = null;
                    boolean slim = false;
                    if (textures.has("SKIN")) {
                        JSONObject skinData = textures.getJSONObject("SKIN");
                        if (skinData.has("metadata")) {
                            if (skinData.getJSONObject("metadata").getString("model").equalsIgnoreCase("slim")) {
                                slim = true;
                            }
                        }
                        File cachedFile = Utils.downloadFileCached(Utils.stringToURL(textures.getJSONObject("SKIN").getString("url")));
                        skin = new Image(cachedFile.toURI().toString());
                    }
                    if (skin == null || skin.getHeight() == 0 && !slim) {
                        skin = steve;
                        deleteSkin.setDisable(true);
                    } else if (skin.getHeight() == 0) {
                        skin = alex;
                        deleteSkin.setDisable(true);
                    } else {
                        deleteSkin.setDisable(false);
                    }
                    if (textures.has("CAPE")) {
                        File cachedFile = Utils.downloadFileCached(Utils.stringToURL(textures.getJSONObject("CAPE").getString("url")));
                        cape = new Image(cachedFile.toURI().toString());
                        includeCape.setDisable(false);
                        deleteCape.setDisable(false);
                    } else {
                        includeCape.setDisable(true);
                        deleteCape.setDisable(true);
                    }
                    if (slim) {
                        skinSlim.setSelected(true);
                    } else {
                        skinClassic.setSelected(true);
                    }
                    updatePreview();
                }
            }
        } catch (Exception ex) {
            kernel.getConsole().printError("Failed to parse remote profile textures. (" + ex.getMessage() + ")");
        }
        if (Constants.USE_LOCAL) {
            selectSkin.setDisable(true);
            selectCape.setDisable(true);
            deleteSkin.setDisable(true);
            deleteCape.setDisable(true);
        }
    }


    /**
     * Toggles the label of the toggle cape button
     */
    @FXML
    public void toggleCapePreview() {
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
    @FXML
    public void toggleSkinType() {
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
        boolean slim = skinSlim.isSelected();
        if (includeCape.getStyleClass().contains("toggle-enabled")) {
            skinPreviews[0] = kernel.resampleImage(TexturePreview.generateFront(skin, cape, slim), 10);
            skinPreviews[1] = kernel.resampleImage(TexturePreview.generateRight(skin, cape), 10);
            skinPreviews[2] = kernel.resampleImage(TexturePreview.generateBack(skin, cape, slim), 10);
            skinPreviews[3] = kernel.resampleImage(TexturePreview.generateLeft(skin, cape), 10);
        } else {
            skinPreviews[0] = kernel.resampleImage(TexturePreview.generateFront(skin, null, slim), 10);
            skinPreviews[1] = kernel.resampleImage(TexturePreview.generateRight(skin, null), 10);
            skinPreviews[2] = kernel.resampleImage(TexturePreview.generateBack(skin, null, slim), 10);
            skinPreviews[3] = kernel.resampleImage(TexturePreview.generateLeft(skin, null), 10);
        }
        skinPreview.setImage(skinPreviews[currentPreview]);
    }

    /**
     * Changes the skin of the user
     */
    @FXML
    private void changeSkin() {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(Language.get(44), "*.png");
        chooser.getExtensionFilters().add(filter);
        File selected = chooser.showOpenDialog(null);
        if (selected != null) {
            if (selected.length() > 131072) {
                Alert error = kernel.buildAlert(Alert.AlertType.ERROR, null, Language.get(105));
                kernel.getConsole().printError("Skin file exceeds 128KB file size limit.");
                error.showAndWait();
            } else {
                HashMap<String, String> params = new HashMap<>();
                try {
                    byte[] data = Files.readAllBytes(selected.toPath());
                    params.put("Access-Token", kernel.getAuthentication().getSelectedUser().getAccessToken());
                    params.put("Client-Token", kernel.getAuthentication().getClientToken());
                    if (skinSlim.isSelected()) {
                        params.put("Skin-Type", "alex");
                    } else {
                        params.put("Skin-Type", "steve");
                    }
                    params.put("Content-Type", "image/png");
                    String r = Utils.sendPost(Constants.CHANGESKIN_URL, data, params);
                    if (!r.equals("OK")) {
                        kernel.getConsole().printError("Failed to change the skin.");
                        kernel.getConsole().printError(r);
                        Alert error = kernel.buildAlert(Alert.AlertType.ERROR, null, Language.get(42));
                        error.showAndWait();
                    }
                    else {
                        Alert correct = kernel.buildAlert(Alert.AlertType.INFORMATION, null, Language.get(40));
                        kernel.getConsole().printInfo("Skin changed successfully!");
                        correct.showAndWait();
                        parseRemoteTextures();
                    }
                } catch (Exception ex) {
                    kernel.getConsole().printError("Failed to change the skin.");
                    kernel.getConsole().printError(ex.getMessage());
                    Alert error = kernel.buildAlert(Alert.AlertType.ERROR, null, Language.get(42));
                    error.showAndWait();
                }
            }
        }
    }

    /**
     * Changes the cape of the user
     */
    @FXML
    private void changeCape() {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(Language.get(25), "*.png");
        chooser.getExtensionFilters().add(filter);
        File selected = chooser.showOpenDialog(null);
        if (selected != null) {
            if (selected.length() > 131072) {
                Alert error = kernel.buildAlert(Alert.AlertType.ERROR, null, Language.get(104));
                kernel.getConsole().printError("Cape file exceeds 128KB file size limit.");
                error.showAndWait();
            } else {
                HashMap<String, String> params = new HashMap<>();
                try {
                    byte[] data = Files.readAllBytes(selected.toPath());
                    params.put("Access-Token", kernel.getAuthentication().getSelectedUser().getAccessToken());
                    params.put("Client-Token", kernel.getAuthentication().getClientToken());
                    params.put("Content-Type", "image/png");
                    String r = Utils.sendPost(Constants.CHANGECAPE_URL, data, params);
                    if (!r.equals("OK")) {
                        kernel.getConsole().printError("Failed to change the cape.");
                        kernel.getConsole().printError(r);
                        Alert error = kernel.buildAlert(Alert.AlertType.ERROR, null, Language.get(43));
                        error.showAndWait();
                    }
                    else {
                        Alert correct = kernel.buildAlert(Alert.AlertType.INFORMATION, null, Language.get(41));
                        kernel.getConsole().printInfo("Cape changed successfully.");
                        correct.showAndWait();
                        parseRemoteTextures();
                    }
                } catch (Exception ex) {
                    kernel.getConsole().printError("Failed to change the cape.");
                    kernel.getConsole().printError(ex.getMessage());
                    Alert error = kernel.buildAlert(Alert.AlertType.ERROR, null, Language.get(43));
                    error.showAndWait();
                }
            }
        }
    }

    /**
     * Deletes the skin of the user
     */
    @FXML
    private void deleteSkin() {
        Alert a = kernel.buildAlert(Alert.AlertType.CONFIRMATION, null, Language.get(31));
        Optional<ButtonType> result = a.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){
            HashMap<String, String> params = new HashMap<>();
            params.put("Access-Token", kernel.getAuthentication().getSelectedUser().getAccessToken());
            params.put("Client-Token", kernel.getAuthentication().getClientToken());
            try {
                String r = Utils.sendPost(Constants.CHANGESKIN_URL, new byte[0], params);
                if (!r.equals("OK")) {
                    kernel.getConsole().printError("Failed to delete the skin.");
                    kernel.getConsole().printError(r);
                    Alert error = kernel.buildAlert(Alert.AlertType.ERROR, null, Language.get(33));
                    error.showAndWait();
                }
                else {
                    Alert correct = kernel.buildAlert(Alert.AlertType.INFORMATION, null, Language.get(34));
                    kernel.getConsole().printInfo("Skin deleted successfully!");
                    correct.showAndWait();
                    parseRemoteTextures();
                }
            }
            catch (Exception ex) {
                kernel.getConsole().printError("Failed to delete the skin.");
                kernel.getConsole().printError(ex.getMessage());
                Alert error = kernel.buildAlert(Alert.AlertType.ERROR, null, Language.get(33));
                error.showAndWait();
            }
            params.clear();
        }
    }

    /**
     * Deletes the cape of the user
     */
    @FXML
    private void deleteCape() {
        Alert a = kernel.buildAlert(Alert.AlertType.CONFIRMATION, null, Language.get(36));
        Optional<ButtonType> result = a.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){
            HashMap<String, String> params = new HashMap<>();
            params.put("Access-Token", kernel.getAuthentication().getSelectedUser().getAccessToken());
            params.put("Client-Token", kernel.getAuthentication().getClientToken());
            try {
                String r = Utils.sendPost(Constants.CHANGECAPE_URL, new byte[0], params);
                if (!r.equals("OK")) {
                    kernel.getConsole().printError("Failed to delete the cape.");
                    kernel.getConsole().printError(r);
                    Alert error = kernel.buildAlert(Alert.AlertType.ERROR, null, Language.get(38));
                    error.showAndWait();
                }
                else {
                    Alert correct = kernel.buildAlert(Alert.AlertType.INFORMATION, null, Language.get(39));
                    kernel.getConsole().printInfo("Cape deleted successfully!");
                    correct.showAndWait();
                    parseRemoteTextures();
                }
            }
            catch (Exception ex) {
                kernel.getConsole().printError("Failed to delete the cape.");
                kernel.getConsole().printError(ex.getMessage());
                Alert error = kernel.buildAlert(Alert.AlertType.ERROR, null, Language.get(38));
                error.showAndWait();
            }
            params.clear();
        }
    }

    /**
     * Loads the news slideshow
     */
    private void loadSlideshow() {
        kernel.getConsole().printInfo("Loading news slideshow...");
        try {
            String response = Utils.readURL(Constants.NEWS_URL);
            if (response == null) {
                kernel.getConsole().printError("Failed to fetch news.");
            }
            JSONObject root = new JSONObject(response);
            JSONArray entries = root.getJSONArray("entries");
            for (int i = 0; i < entries.length(); i++) {
                JSONObject entry = entries.getJSONObject(i);
                boolean isDemo = false;
                JSONArray tags = entry.getJSONArray("tags");
                for (int j = 0; j < tags.length(); j++) {
                    if (tags.getString(j).equalsIgnoreCase("demo")) {
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
            kernel.getConsole().printError("Couldn't parse news data. (" + ex.getMessage() + ")");
            return;
        }
        if (slides.size() > 0) {
            slideshowBox.setVisible(true);
            slideshowBox.setManaged(true);
            Slide s = slides.get(0);
            slideshow.setImage(s.getImage());
            newsTitle.setText(s.getTitle());
            newsText.setText(s.getText());
        } else {
            newsTitle.setText(Language.get(102));
            newsText.setText(Language.get(103));
        }
    }

    /**
     * Changes the news slide
     * @param e The trigger event
     */
    @FXML
    public void changeSlide(MouseEvent e) {
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
        Slide s = slides.get(currentSlide);
        slideshow.setImage(s.getImage());
        newsTitle.setText(s.getTitle());
        newsText.setText(s.getText());
    }

    /**
     * Performs an action when a slide is clicked
     */
    @FXML
    public void performSlideAction() {
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
    @FXML
    public void rotatePreview(MouseEvent e) {
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
     * Loads the profile lists
     */
    @FXML
    private void loadProfileList() {
        Profiles ps = kernel.getProfiles();

        //Check if selected profile passes the current settings
        Profile selected = ps.getSelectedProfile();
        VersionMeta selectedVersion = selected.getVersionID();
        Settings settings = kernel.getSettings();

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

        //For some reason using the same label for both lists one list appear the items blank
        ObservableList<Label> profileListItems = FXCollections.observableArrayList();
        ObservableList<Label> profileListItems2 = FXCollections.observableArrayList();

        //Add "Add New Profile" item
        Label l = new Label(Language.get(51), new ImageView(new Image("/kml/gui/textures/add.png")));
        profileListItems.add(l);

        Label l2;
        for (Profile p : ps.getProfiles()) {
            if (p.getType() == ProfileType.SNAPSHOT && !settings.getEnableSnapshots()) {
                continue;
            }
            if (p.getType() == ProfileType.RELEASE) {
                Image img = kernel.getProfileIcon(ProfileIcon.GRASS);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(64);
                iv.setFitHeight(64);
                ImageView iv2 = new ImageView(img);
                iv2.setFitWidth(64);
                iv2.setFitHeight(64);
                l = new Label(Language.get(59), iv);
                l2 = new Label(Language.get(59), iv2);
            } else if (p.getType() == ProfileType.SNAPSHOT) {
                Image img = kernel.getProfileIcon(ProfileIcon.CRAFTING_TABLE);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(64);
                iv.setFitHeight(64);
                ImageView iv2 = new ImageView(img);
                iv2.setFitWidth(64);
                iv2.setFitHeight(64);
                l = new Label(Language.get(60), iv);
                l2 = new Label(Language.get(60), iv2);
            } else {
                String name = p.hasName() ? p.getName() : Language.get(70);
                ProfileIcon pi = p.hasIcon() ? p.getIcon() : ProfileIcon.FURNACE;
                Image img = kernel.getProfileIcon(pi);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(64);
                iv.setFitHeight(64);
                ImageView iv2 = new ImageView(img);
                iv2.setFitWidth(64);
                iv2.setFitHeight(64);
                l = new Label(name, iv);
                l2 = new Label(name, iv2);
            }
            //Fetch Minecraft version used by the profile
            VersionMeta verID;
            if (p.getType() == ProfileType.CUSTOM) {
                Versions versions = kernel.getVersions();
                verID = p.hasVersion() ? p.getVersionID() : versions.getLatestRelease();
            } else if (p.getType() == ProfileType.RELEASE) {
                verID = kernel.getVersions().getLatestRelease();
            } else {
                verID = kernel.getVersions().getLatestSnapshot();
            }
            l.setId(p.getID());
            l2.setId(p.getID());
            if (verID != null) {
                //If profile has any known version just show it below the profile name
                if (verID.getType() == VersionType.SNAPSHOT && !kernel.getSettings().getEnableSnapshots()) {
                    continue;
                } else if ((verID.getType() == VersionType.OLD_ALPHA || verID.getType() == VersionType.OLD_BETA) && !kernel.getSettings().getEnableHistorical()) {
                    continue;
                }
                l.setText(l2.getText() + "\n" + verID.getID());
                l2.setText(l2.getText() + "\n" + verID.getID());
            }
            if (ps.getSelectedProfile().equals(p)) {
                l.getStyleClass().add("selectedProfile");
                l2.getStyleClass().add("selectedProfile");
            }
            profileListItems.add(l);
            profileListItems2.add(l2);
        }
        profileList.setItems(profileListItems);
        profilePopupList.setItems(profileListItems2);
    }

    /**
     * Loads the profile icons
     */
    private void loadIcons() {
        kernel.getConsole().printInfo("Loading icons...");
        ObservableList<ImageView> icons = FXCollections.observableArrayList();
        for (ProfileIcon p : ProfileIcon.values()) {
            if (p != ProfileIcon.CRAFTING_TABLE && p != ProfileIcon.GRASS) {
                ImageView imv = new ImageView(kernel.getProfileIcon(p));
                imv.setFitHeight(64);
                imv.setFitWidth(64);
                imv.setId(p.name());
                icons.add(imv);
            }
        }
        iconList.setItems(icons);
    }

    /**
     * Selects the selected profile from the list
     */
    @FXML
    private void selectProfile() {
        if (profilePopupList.getSelectionModel().getSelectedIndex() == -1) {
            //Nothing has been selected
            return;
        }
        //Select profile and refresh list
        kernel.getProfiles().setSelectedProfile(kernel.getProfiles().getProfile(profilePopupList.getSelectionModel().getSelectedItem().getId()));
        loadProfileList();
        profilePopupList.setVisible(false);
    }

    /**
     * Downloads and launches the game
     */
    @FXML
    public void launchGame() {
        progressPane.setVisible(true);
        playPane.setVisible(false);
        progressBar.setProgress(0);
        progressText.setText("");
        Downloader d = kernel.getDownloader();
        GameLauncher gl = kernel.getGameLauncher();
        Console console = kernel.getConsole();
        //Begin download and game launch task
        Task runTask = new Task() {
            @Override
            protected Object call() throws Exception {
                if (!d.isDownloading() && !gl.isRunning()) {
                    //Keep track of the progress
                    Timeline task = new Timeline();
                    KeyFrame frame = new KeyFrame(Duration.millis(250), event -> {
                        progressBar.setProgress(d.getProgress() / 100.0);
                        progressText.setText(Language.get(13) + " " + d.getCurrentFile() + "...");
                    });
                    task.getKeyFrames().add(frame);
                    task.setCycleCount(Timeline.INDEFINITE);
                    task.play();
                    try {
                        d.download();
                        task.stop();
                        Platform.runLater(() -> {
                            progressText.setText(Language.get(78));
                            progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                        });
                        gl.launch();
                        progressPane.setVisible(false);
                        playPane.setVisible(true);
                        Platform.runLater(() -> playButton.setText(Language.get(14)));
                        playButton.setDisable(true);
                        //Keep track of the game process
                        Timeline task2 = new Timeline();
                        KeyFrame frame2 = new KeyFrame(Duration.millis(250), event -> {
                            if (!gl.isStarted()) {
                                task2.stop();
                            }
                        });
                        task2.statusProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
                            if (newValue == Animation.Status.STOPPED) {
                                if (gl.hasError()) {
                                    Alert a = kernel.buildAlert(Alert.AlertType.ERROR, Language.get(16), Language.get(15));
                                    a.showAndWait();
                                }
                                if (!kernel.getSettings().getKeepLauncherOpen() && !kernel.getSettings().getShowGameLog()) {
                                    kernel.exitSafely();
                                }
                                playButton.setDisable(false);
                                if (Constants.USE_LOCAL) {
                                    playButton.setText(Language.get(79));
                                } else {
                                    playButton.setText(Language.get(12));
                                }
                            }
                        }));
                        task2.getKeyFrames().add(frame2);
                        task2.setCycleCount(Timeline.INDEFINITE);
                        task2.play();
                        if (!kernel.getSettings().getKeepLauncherOpen()) {
                            Platform.runLater(() -> stage.close());
                        }
                    } catch (DownloaderException e) {
                        Alert a = kernel.buildAlert(Alert.AlertType.ERROR, Language.get(83), Language.get(84));
                        a.showAndWait();
                        console.printError("Failed to perform game download task: " + e);
                    } catch (GameLauncherException e) {
                        Alert a = kernel.buildAlert(Alert.AlertType.ERROR, Language.get(81), Language.get(82));
                        a.showAndWait();
                        console.printError("Failed to perform game launch task: " + e);
                    }
                }
                return null;
            }
        };
        Thread t = new Thread(runTask);
        t.start();
    }

    /**
     * Shows the language list
     */
    @FXML
    public void showLanguages() {
        if (languagesList.isVisible()) {
            languagesList.setVisible(false);
        } else {
            languagesList.setVisible(true);
        }
    }

    /**
     * Deselects the current user and allows to select another
     */
    @FXML
    public void switchAccount() {
        showAccountOptions();
        Authentication a = kernel.getAuthentication();
        a.setSelectedUser(null);
        showLoginPrompt(true);
        updateExistingUsers();
    }

    /**
     * Shows the profile popup list
     */
    @FXML
    public void showProfiles() {
        if (profilePopupList.isVisible()) {
            profilePopupList.setVisible(false);
        } else {
            Bounds b = playButton.localToScene(playButton.getBoundsInLocal());
            profilePopupList.setTranslateX(b.getMinX() - 100);
            profilePopupList.setTranslateY(b.getMinY() - 180);
            profilePopupList.setVisible(true);
            profilePopupList.getSelectionModel().clearSelection();
        }
    }

    /**
     * Shows the profile editor profile icons
     */
    @FXML
    public void showIcons() {
        if (iconList.isVisible()) {
            iconList.setVisible(false);
        } else {
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
    @FXML
    public void showAccountOptions() {
        if (switchAccountButton.isVisible()) {
            switchAccountButton.setVisible(false);
        } else {
            Bounds b = accountButton.localToScene(accountButton.getBoundsInLocal());
            //Avoid the label getting out of bounds
            if (b.getMinX() - 5 + switchAccountButton.getWidth() > stage.getWidth()) {
                switchAccountButton.setTranslateX(stage.getWidth() - switchAccountButton.getWidth() - 5);
            } else {
                switchAccountButton.setTranslateX(b.getMinX() - 5);
            }
            switchAccountButton.setTranslateY(b.getMaxY() + 5);
            switchAccountButton.setVisible(true);
        }
    }

    /**
     * Switched the selected tab according to the clicked label
     * @param e The trigger event
     */
    @FXML
    public void switchTab(Event e) {
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
            playButton.setVisible(true);
            profilePopupButton.setVisible(true);
            launchOptionsLabel.getStyleClass().remove("selectedItem");
        }
        if (source == newsLabel) {
            newsLabel.getStyleClass().add("selectedItem");
            selection.select(newsTab);
        } else if (source == skinsLabel) {
            skinsLabel.getStyleClass().add("selectedItem");
            selection.select(skinsTab);
        } else if (source == settingsLabel) {
            settingsLabel.getStyleClass().add("selectedItem");
            selection.select(settingsTab);
        } else if (source == launchOptionsLabel) {
            launchOptionsLabel.getStyleClass().add("selectedItem");
            selection.select(launchOptionsTab);
            profileList.getSelectionModel().clearSelection();
        } else if (source == profileEditorTab) {
            //Hide play button
            playButton.setVisible(false);
            profilePopupButton.setVisible(false);
            selection.select(profileEditorTab);
        }
    }

    /**
     * Hides any open popup that triggers this method
     * @param e The event trigger
     */
    @FXML
    public void hidePopup(Event e) {
        Node ls = (Node)e.getSource();
        if (ls.isVisible()) {
            ls.setVisible(false);
        }
    }

    /**
     * Updates the selected language
     */
    @FXML
    public void updateLanguage() {
        if (languagesList.getSelectionModel().getSelectedIndex() == -1) {
            //Nothing has been selected
            return;
        }
        Label selected = languagesList.getSelectionModel().getSelectedItem();
        languageButton.setText(selected.getText());
        kernel.getSettings().setLocale(selected.getId());
        languagesList.setVisible(false);
        localizeElements();
    }

    /**
     * Updates the selected icon
     */
    @FXML
    public void updateIcon() {
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
    @FXML
    public void loadEditor() {
        if (profileList.getSelectionModel().getSelectedIndex() == -1) {
            //Nothing has been selected
            return;
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
            profileIcon.setImage(kernel.getProfileIcon(ProfileIcon.FURNACE));
            if (kernel.getSettings().getEnableAdvanced()) {
                javaExecBlock.setVisible(true);
                javaExecBlock.setManaged(true);
                javaArgsBlock.setVisible(true);
                javaArgsBlock.setManaged(true);
                toggleEditorOption(javaExecLabel, false);
                javaExec.setText(Utils.getJavaDir());
                toggleEditorOption(javaArgsLabel, false);
                StringBuilder jA = new StringBuilder();
                if (Utils.getOSArch().equals(OSArch.OLD)) {
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
            resW.getEditor().setText(String.valueOf(854));
            resW.getValueFactory().setValue(854);
            resH.getEditor().setText(String.valueOf(480));
            resH.getValueFactory().setValue(480);
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
                        profileIcon.setImage(kernel.getProfileIcon(ProfileIcon.GRASS));
                    } else {
                        profileName.setText(Language.get(60));
                        profileIcon.setImage(kernel.getProfileIcon(ProfileIcon.CRAFTING_TABLE));
                    }
                    versionBlock.setVisible(false);
                    versionBlock.setManaged(false);
                    iconBlock.setVisible(false);
                    iconBlock.setManaged(false);
                } else {
                    if (p.hasIcon()) {
                        profileIcon.setImage(kernel.getProfileIcon(p.getIcon()));
                        profileIcon.setId(p.getIcon().name());
                    } else {
                        profileIcon.setImage(kernel.getProfileIcon(ProfileIcon.FURNACE));
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
                        } else if (p.isLatestSnapshot() && kernel.getSettings().getEnableSnapshots()) {
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
                    resH.getEditor().setText(String.valueOf(p.getResolutionHeight()));
                    resH.getValueFactory().setValue(p.getResolutionHeight());
                    resW.getEditor().setText(String.valueOf(p.getResolutionWidth()));
                    resW.getValueFactory().setValue(p.getResolutionWidth());
                } else {
                    toggleEditorOption(resolutionLabel, false);
                    resW.getEditor().setText(String.valueOf(854));
                    resW.getValueFactory().setValue(854);
                    resH.getEditor().setText(String.valueOf(480));
                    resH.getValueFactory().setValue(480);
                }
                if (p.hasGameDir()) {
                    toggleEditorOption(gameDirLabel, true);
                    gameDir.setText(p.getGameDir().getAbsolutePath());
                } else {
                    toggleEditorOption(gameDirLabel, false);
                    gameDir.setText(Utils.getWorkingDirectory().getAbsolutePath());
                }
                if (kernel.getSettings().getEnableAdvanced()) {
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
                        StringBuilder jA = new StringBuilder();
                        if (Utils.getOSArch().equals(OSArch.OLD)) {
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
        ObservableList<VersionMeta> vers = FXCollections.observableArrayList();
        VersionMeta latestVersion = new VersionMeta(Language.get(59), null, null);
        vers.add(latestVersion);
        if (kernel.getSettings().getEnableSnapshots()) {
            VersionMeta latestSnapshot = new VersionMeta(Language.get(60), null, null);
            vers.add(latestSnapshot);
        }
        for (VersionMeta v : kernel.getVersions().getVersions()) {
            if (v.getType() == VersionType.RELEASE) {
                vers.add(v);
            } else if (v.getType() == VersionType.SNAPSHOT && kernel.getSettings().getEnableSnapshots()) {
                vers.add(v);
            } else if ((v.getType() == VersionType.OLD_BETA || v.getType() == VersionType.OLD_ALPHA) && kernel.getSettings().getEnableHistorical()) {
                vers.add(v);
            }
        }
        versionList.setItems(vers);
        versionList.getSelectionModel().select(0);
    }

    /**
     * Saves the profile data from the profile editor
     */
    @FXML
    public void saveProfile() {
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
            } else if (versionList.getSelectionModel().getSelectedIndex() == 1 && kernel.getSettings().getEnableSnapshots()) {
                target.setVersionID(kernel.getVersions().getLatestSnapshot());
                target.setLatestRelease(false);
                target.setLatestSnapshot(true);
            } else {
                target.setVersionID(versionList.getSelectionModel().getSelectedItem());
            }
            try {
                target.setIcon(ProfileIcon.valueOf(profileIcon.getId()));
            } catch (IllegalArgumentException ex) {
                target.setIcon(null);
            }
        }
        if (!resW.isDisabled()) {
            try {
                int h = Integer.parseInt(resH.getEditor().getText());
                int w = Integer.parseInt(resW.getEditor().getText());
                target.setResolution(w, h);
            } catch (NumberFormatException ex) {
                kernel.getConsole().printError("Invalid resolution given.");
            }
        } else {
            target.setResolution(-1, -1);
        }
        if (!gameDir.isDisabled() && !gameDir.getText().isEmpty()) {
            target.setGameDir(new File(gameDir.getText()));
        } else {
            target.setGameDir(null);
        }
        if (kernel.getSettings().getEnableAdvanced()) {
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
        Alert a = kernel.buildAlert(Alert.AlertType.INFORMATION, null, Language.get(57));
        a.showAndWait();
        loadProfileList();
        switchTab(launchOptionsLabel);
    }

    /**
     * Discards the changes of the profile editor
     */
    @FXML
    public void cancelProfile() {
        Alert a = kernel.buildAlert(Alert.AlertType.CONFIRMATION, null, Language.get(55));
        Optional<ButtonType> result = a.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            switchTab(launchOptionsLabel);
        }
    }

    /**
     * Deletes the profile loaded by the profile editor
     */
    @FXML
    public void deleteProfile() {
        Alert a = kernel.buildAlert(Alert.AlertType.CONFIRMATION, null, Language.get(61));
        Optional<ButtonType> result = a.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Label selectedElement = profileList.getSelectionModel().getSelectedItem();
            if (kernel.getProfiles().deleteProfile(kernel.getProfiles().getProfile(selectedElement.getId()))) {
                a.setAlertType(Alert.AlertType.INFORMATION);
                a.setContentText(Language.get(56));
                a.showAndWait();
            } else {
                a.setAlertType(Alert.AlertType.ERROR);
                a.setContentText(Language.get(58));
                a.showAndWait();
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
    @FXML
    public void updateEditor(MouseEvent e) {
        Label l = (Label)e.getSource();
        toggleEditorOption(l, l.getParent().getChildrenUnmodifiable().get(1).isDisable());
    }

    /**
     * Updates the existing users list
     */
    private void updateExistingUsers() {
        Authentication a = kernel.getAuthentication();
        if (a.getUsers().size() > 0 && a.getSelectedUser() == null) {
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
            contentPane.getSelectionModel().select(newsTab);
            tabMenu.setVisible(true);
            tabMenu.setManaged(true);
            accountButton.setVisible(true);
            playPane.setVisible(true);
            //Set account name for current user
            accountButton.setText(kernel.getAuthentication().getSelectedUser().getDisplayName() + " ▼");
        }
    }

    /**
     * Performs an authenticate with the data typed in the login form
     */
    public void authenticate() {
        Alert a = kernel.buildAlert(Alert.AlertType.WARNING, null, null);
        if (username.getText().isEmpty()) {
            a.setContentText(Language.get(17));
            a.show();
        } else if (password.getText().isEmpty()) {
            a.setContentText(Language.get(23));
            a.show();
        } else {
            try {
                Authentication auth = kernel.getAuthentication();
                auth.authenticate(username.getText(), password.getText());
                username.setText("");
                password.setText("");
                showLoginPrompt(false);
                fetchAds();
                parseRemoteTextures();
            } catch (AuthenticationException ex) {
                a.setAlertType(Alert.AlertType.ERROR);
                a.setHeaderText(Language.get(22));
                a.setContentText(ex.getMessage());
                a.show();
                password.setText("");
            }
        }
    }

    /**
     * Refreshes latest session
     */
    private void refreshSession() {
        try {
            if (kernel.getAuthentication().getSelectedUser() != null) {
                kernel.getAuthentication().refresh();
            } else {
                kernel.getConsole().printInfo("No user is selected.");
            }
        } catch (AuthenticationException ex) {
            kernel.getConsole().printInfo("Couldn't refresh your session.");
        } finally {
            if (kernel.getAuthentication().isAuthenticated()) {
                showLoginPrompt(false);
                fetchAds();
                parseRemoteTextures();
            } else {
                showLoginPrompt(true);
            }
        }
    }

    /**
     * Refreshes user selected from the existing user list
     */
    public void refresh() {
        User selected = existingUsers.getSelectionModel().getSelectedItem();
        Authentication auth = kernel.getAuthentication();
        try {
            auth.setSelectedUser(selected);
            auth.refresh();
            showLoginPrompt(false);
            fetchAds();
            parseRemoteTextures();
        } catch (AuthenticationException ex) {
            Alert a = kernel.buildAlert(Alert.AlertType.ERROR, Language.get(62), ex.getMessage());
            a.showAndWait();
            updateExistingUsers();
        }
    }

    /**
     * Logs out the selected user from the existing user list
     */
    public void logout() {
        User selected = existingUsers.getSelectionModel().getSelectedItem();
        Alert a = kernel.buildAlert(Alert.AlertType.CONFIRMATION, null, Language.get(8));
        Optional<ButtonType> result = a.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Authentication auth = kernel.getAuthentication();
            auth.removeUser(selected);
            updateExistingUsers();
        }
    }

    /**
     * Opens the register page
     */
    @FXML
    public void register() {
        //Open register page
        kernel.getHostServices().showDocument(urlPrefix + "https://krothium.com/register");
    }

    /**
     * Opens the help page
     */
    @FXML
    public void openHelp() {
        //Open help page
        kernel.getHostServices().showDocument(urlPrefix + "https://krothium.com/forum/12-soporte/");
    }

    /**
     * Opens the news page
     */
    @FXML
    public void openNews() {
        //Open news page
        kernel.getHostServices().showDocument(urlPrefix + "https://krothium.com/forum/3-noticias/");
    }

    /**
     * Performs an authenticate if the Enter key is pressed in the Username or Password field
     * @param e The trigger event
     */
    @FXML
    public void triggerAuthenticate(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            authenticate();
        }
    }

    /**
     * Updates the settings according to the label cicked
     * @param e The trigger event
     */
    @FXML
    public void updateSettings(MouseEvent e) {
        Label source = (Label)e.getSource();
        Settings s = kernel.getSettings();
        if (source == keepLauncherOpen) {
            s.setKeepLauncherOpen(!s.getKeepLauncherOpen());
            toggleLabel(source, s.getKeepLauncherOpen());
        } else if (source == outputLog) {
            s.setShowGameLog(!s.getShowGameLog());
            toggleLabel(source, s.getShowGameLog());
        } else if (source == enableSnapshots) {
            if (!s.getEnableSnapshots()) {
                Alert a = kernel.buildAlert(Alert.AlertType.WARNING, null, Language.get(71) + System.lineSeparator() + Language.get(72));
                a.showAndWait();
            }
            s.setEnableSnapshots(!s.getEnableSnapshots());
            toggleLabel(source, s.getEnableSnapshots());
            loadProfileList();
            loadVersionList();
        } else if (source == historicalVersions) {
            if (!s.getEnableHistorical()) {
                Alert a = kernel.buildAlert(Alert.AlertType.WARNING, null, Language.get(73) + System.lineSeparator()
                        + Language.get(74) + System.lineSeparator()
                        + Language.get(75));
                a.showAndWait();
            }
            s.setEnableHistorical(!s.getEnableHistorical());
            toggleLabel(source, s.getEnableHistorical());
            loadProfileList();
            loadVersionList();
        } else if (source == advancedSettings) {
            if (!s.getEnableAdvanced()) {
                Alert a = kernel.buildAlert(Alert.AlertType.WARNING, null, Language.get(76) + System.lineSeparator() + Language.get(77));
                a.showAndWait();
            }
            s.setEnableAdvanced(!s.getEnableAdvanced());
            toggleLabel(source, s.getEnableAdvanced());
        }
    }

    /**
     * Changes any label icon
     * @param label The target label
     * @param state The new state
     */
    private void toggleLabel(Label label, boolean state) {
        label.getStyleClass().clear();
        if (state) {
            label.getStyleClass().add("toggle-enabled");
        } else {
            label.getStyleClass().add("toggle-disabled");
        }
    }

    /**
     * Deletes the cache with confirmation
     */
    @FXML
    private void deleteCache() {
        Alert a = kernel.buildAlert(Alert.AlertType.CONFIRMATION, null, Language.get(98) + System.lineSeparator() +
                Language.get(99) + System.lineSeparator() +
                Language.get(100));
        Optional<ButtonType> response = a.showAndWait();
        if (response.isPresent() && response.get() == ButtonType.OK) {
            File[] cacheFiles = Constants.APPLICATION_CACHE.listFiles();
            for (File file : cacheFiles) {
                file.delete();
            }
            a.setAlertType(Alert.AlertType.INFORMATION);
            a.setContentText(Language.get(32));
            a.showAndWait();
        }
    }

    /**
     * Exports the logs to a ZIP file
     */
    @FXML
    private void exportLogs() {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("ZIP", "*.zip");
        chooser.getExtensionFilters().add(filter);
        File selected = chooser.showSaveDialog(null);
        if (selected != null) {
            try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(selected))) {
                File[] files = Constants.APPLICATION_LOGS.listFiles();
                for (File file : files) {
                    ZipEntry entry = new ZipEntry(file.getName());
                    out.putNextEntry(entry);
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    out.write(bytes);
                    out.closeEntry();
                }
                Alert a = kernel.buildAlert(Alert.AlertType.INFORMATION, null, Language.get(35) + System.lineSeparator() + selected.getAbsolutePath());
                a.showAndWait();
            } catch (IOException ex) {
                Alert a = kernel.buildAlert(Alert.AlertType.ERROR, null, Language.get(35) + "\n" + selected.getAbsolutePath());
                a.showAndWait();
            }
        }
    }

    /**
     * Opens the URL of the selected version server in the default user web browser
     */
    @FXML
    private void downloadServer() {
        VersionMeta selectedItem = versionList.getSelectionModel().getSelectedItem();
        kernel.getHostServices().showDocument(urlPrefix + "https://s3.amazonaws.com/Minecraft.Download/versions/" + selectedItem.getID() + "/minecraft_server." + selectedItem.getID() + ".jar");
    }

    /**
     * Selects a game directory for the profile editor
     */
    @FXML
    private void selectGameDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        if (gameDir.getText().isEmpty()) {
            chooser.setInitialDirectory(Constants.APPLICATION_WORKING_DIR);
        } else {
            File gd = new File(gameDir.getText());
            if (gd.exists() && gd.isDirectory()) {
                chooser.setInitialDirectory(gd);
            } else {
                chooser.setInitialDirectory(Constants.APPLICATION_WORKING_DIR);
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
    @FXML
    private void selectJavaExecutable() {
        FileChooser chooser = new FileChooser();
        File je;
        if (javaExec.getText().isEmpty()) {
            je = new File(Utils.getJavaDir());
        } else {
            je = new File(javaExec.getText());
        }
        if (je.exists() && je.isFile()) {
            File jf = je.getParentFile();
            if (jf.exists() && jf.isDirectory()) {
                chooser.setInitialDirectory(jf);
            }
        }
        File selected = chooser.showOpenDialog(null);
        if (selected != null && selected.isFile()) {
            javaExec.setText(selected.getAbsolutePath());
        }
    }
}
