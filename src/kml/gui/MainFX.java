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
import java.util.*;

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
            rotateLeft;

    @FXML
    private Button playButton, deleteButton, changeIcon;

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
    private HBox playPane, tabMenu, profilePopup;

    @FXML
    private TextField username, profileName,javaExec, gameDir, javaArgs;

    @FXML
    private Spinner<Integer> resH, resW;

    @FXML
    private PasswordField password;

    @FXML
    private ComboBox<User> existingUsers;

    @FXML
    private ComboBox<String> versionList;

    @FXML
    private StackPane versionBlock, javaArgsBlock, javaExecBlock, iconBlock;

    @FXML
    private ImageView profileIcon, slideshow, skinPreview;

    private Kernel kernel;
    private Stage stage;
    private ArrayList<Slide> slides = new ArrayList<>();
    private int currentSlide;
    private int currentPreview = 0; // 0 = front / 1 = right / 2 = back / 3 = left
    private Image[] skinPreviews = new Image[4];

    public void initialize(Kernel k, Stage s) {
        //Require to exit using Platform.exit()
        Platform.setImplicitExit(false);

        //Set kernel and stage
        kernel = k;
        stage = s;

        Constants.PROFILE_ICONS = new Image("/kml/gui/textures/profile_icons.png");

        //Load news slideshow
        loadSlideshow();

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
        contentPane.getSelectionModel().select(newsTab);
        newsLabel.getStyleClass().add("selectedItem");

        //Update settings labels
        Settings st = kernel.getSettings();
        toggleLabel(keepLauncherOpen, st.getKeepLauncherOpen());
        toggleLabel(outputLog, st.getShowGameLog());
        toggleLabel(enableSnapshots, st.getEnableSnapshots());
        toggleLabel(historicalVersions, st.getEnableHistorical());
        toggleLabel(advancedSettings, st.getEnableAdvanced());

        //Load profile list
        loadProfileList();

        //Make transparent areas to not target mouse events
        playPane.pickOnBoundsProperty().setValue(false);
        profilePopup.pickOnBoundsProperty().setValue(false);

        //Prepare Spinners
        resH.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0));
        resW.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0));
        resW.setEditable(true);
        resH.setEditable(true);

        //Load icons
        loadIcons();

        //For skin preview for testing purposes
        Image skin = new Image("http://textures.minecraft.net/texture/a116e69a845e227f7ca1fdde8c357c8c821ebd4ba619382ea4a1f87d4ae94");
        skinPreviews[0] = Utils.resampleImage(TexturePreview.generateFront(skin, false), 10);
        skinPreviews[1] = Utils.resampleImage(TexturePreview.generateRight(skin), 10);
        skinPreviews[2] = Utils.resampleImage(TexturePreview.generateBack(skin, false), 10);
        skinPreviews[3] = Utils.resampleImage(TexturePreview.generateLeft(skin), 10);
        skinPreview.setImage(skinPreviews[0]);
    }

    private void loadSlideshow() {
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
            kernel.getConsole().printError("Couldn't parse news data. (" + ex.getMessage() + ")");
        }
        if (slides.size() > 0) {
            Slide s = slides.get(0);
            slideshow.setImage(s.getImage());
            newsTitle.setText(s.getTitle());
            newsText.setText(s.getText());
        } else {
            slideBack.setVisible(false);
            slideForward.setVisible(false);
        }
    }

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

    @FXML
    public void performSlideAction() {
        if (slides.isEmpty()) {
            //No slides
            return;
        }
        Slide s = slides.get(currentSlide);
        kernel.getHostServices().showDocument(s.getAction());
    }

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

    @FXML
    private void loadProfileList() {
        Profiles ps = kernel.getProfiles();
        Set<String> profiles = ps.getProfiles().keySet();
        //For some reason using the same label for both lists one list appear the items blank
        ObservableList<Label> profileListItems = FXCollections.observableArrayList();
        ObservableList<Label> profileListItems2 = FXCollections.observableArrayList();

        //Add "Add New Profile" item
        Label l = new Label("Add New Profile", new ImageView(new Image("/kml/gui/textures/add.png")));
        profileListItems.add(l);

        Label l2;
        for (String id : profiles) {
            Profile p = ps.getProfile(id);
            if (p.getType() == ProfileType.RELEASE) {
                Image img = Utils.getProfileIcon(ProfileIcon.GRASS);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(64);
                iv.setFitHeight(64);
                ImageView iv2 = new ImageView(img);
                iv2.setFitWidth(64);
                iv2.setFitHeight(64);
                l = new Label("Latest Release", iv);
                l2 = new Label("Latest Release", iv2);
            } else if (p.getType() == ProfileType.SNAPSHOT) {
                Image img = Utils.getProfileIcon(ProfileIcon.CRAFTING_TABLE);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(64);
                iv.setFitHeight(64);
                ImageView iv2 = new ImageView(img);
                iv2.setFitWidth(64);
                iv2.setFitHeight(64);
                l = new Label("Latest Snapshot", iv);
                l2 = new Label("Latest Snapshot", iv2);
            } else {
                String name = p.hasName() ? p.getName() : "Unnamed Profile";
                ProfileIcon pi = p.hasIcon() ? p.getIcon() : ProfileIcon.FURNACE;
                Image img = Utils.getProfileIcon(pi);
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
            String verID;
            if (p.getType() == ProfileType.CUSTOM) {
                verID = p.hasVersion() ? p.getVersionID() : kernel.getVersions().getLatestRelease();
                if (verID != null) {
                    switch (verID) {
                        case "latest-release":
                            verID = kernel.getVersions().getLatestRelease();
                            break;
                        case "latest-snapshot":
                            verID = kernel.getVersions().getLatestSnapshot();
                            break;
                    }
                }
            } else if (p.getType() == ProfileType.RELEASE) {
                verID = kernel.getVersions().getLatestRelease();
            } else {
                verID = kernel.getVersions().getLatestSnapshot();
            }
            l.setId(p.getID());
            l2.setId(p.getID());
            if (verID != null) {
                //If profile has any known version just show it below the profile name
                l.setText(l2.getText() + "\n" + verID);
                l2.setText(l2.getText() + "\n" + verID);
            }
            if (kernel.getProfiles().getSelectedProfile().equals(p.getID())) {
                l.getStyleClass().add("selectedProfile");
                l2.getStyleClass().add("selectedProfile");
            }
            profileListItems.add(l);
            profileListItems2.add(l2);
        }
        profileList.setItems(profileListItems);
        profilePopupList.setItems(profileListItems2);
    }

    private void loadIcons() {
        ObservableList<ImageView> icons = FXCollections.observableArrayList();
        for (ProfileIcon p : ProfileIcon.values()) {
            if (p != ProfileIcon.CRAFTING_TABLE && p != ProfileIcon.GRASS) {
                ImageView imv = new ImageView(Utils.getProfileIcon(p));
                imv.setFitHeight(64);
                imv.setFitWidth(64);
                imv.setId(p.name());
                icons.add(imv);
            }
        }
        iconList.setItems(icons);
    }

    @FXML
    private void selectProfile() {
        if (profilePopupList.getSelectionModel().getSelectedIndex() == -1) {
            //Nothing has been selected
            return;
        }
        //Select profile and refresh list
        kernel.getProfiles().setSelectedProfile(profilePopupList.getSelectionModel().getSelectedItem().getId());
        loadProfileList();
        profilePopupList.setVisible(false);
    }

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
                        progressText.setText("Downloading " + d.getCurrentFile() + "...");
                    });
                    task.getKeyFrames().add(frame);
                    task.setCycleCount(Timeline.INDEFINITE);
                    task.play();
                    try {
                        d.download();
                        task.stop();
                        gl.launch();
                        progressPane.setVisible(false);
                        playPane.setVisible(true);
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
                                    Alert a = new Alert(Alert.AlertType.ERROR);
                                    Stage s = (Stage) a.getDialogPane().getScene().getWindow();
                                    s.getIcons().add(new Image("/kml/gui/textures/icon.png"));
                                    a.setContentText("The game has crashed!");
                                    a.showAndWait();
                                }
                                if (!kernel.getSettings().getKeepLauncherOpen() && !kernel.getSettings().getShowGameLog()) {
                                    kernel.exitSafely();
                                }
                                playButton.setDisable(false);
                            }
                        }));
                        task2.getKeyFrames().add(frame2);
                        task2.setCycleCount(Timeline.INDEFINITE);
                        task2.play();
                        if (!kernel.getSettings().getKeepLauncherOpen()) {
                            Platform.runLater(() -> {
                                setVisible(false);
                            });
                        }
                    } catch (DownloaderException e) {
                        Alert a = new Alert(Alert.AlertType.ERROR);
                        Stage s = (Stage) a.getDialogPane().getScene().getWindow();
                        s.getIcons().add(new Image("/kml/gui/textures/icon.png"));
                        a.setContentText("Failed to perform game download task: " + e);
                        a.showAndWait();
                        console.printError("Failed to perform game download task: " + e);
                    } catch (GameLauncherException e) {
                        Alert a = new Alert(Alert.AlertType.ERROR);
                        Stage s = (Stage) a.getDialogPane().getScene().getWindow();
                        s.getIcons().add(new Image("/kml/gui/textures/icon.png"));
                        a.setContentText("Failed to perform game launch task: " + e);
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

    @FXML
    public void showLanguages() {
        if (languagesList.isVisible()) {
            languagesList.setVisible(false);
        } else {
            languagesList.setVisible(true);
        }
    }

    @FXML
    public void switchAccount() {
        showAccountOptions();
        Authentication a = kernel.getAuthentication();
        a.setSelectedUser(null);
        showLoginPrompt(true);
        updateExistingUsers();
    }

    @FXML
    public void showProfiles() {
        if (profilePopupList.isVisible()) {
            profilePopupList.setVisible(false);
        } else {
            profilePopupList.setVisible(true);
            profilePopupList.getSelectionModel().clearSelection();
        }
    }

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

    @FXML
    public void showAccountOptions() {
        if (switchAccountButton.isVisible()) {
            switchAccountButton.setVisible(false);
        } else {
            switchAccountButton.setVisible(true);
        }
    }

    @FXML
    public void switchTab(Event e) {
        switchTab(e.getSource());
    }

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
            selection.select(profileEditorTab);
        }
    }

    @FXML
    public void hidePopup(Event e) {
        Node ls = (Node)e.getSource();
        if (ls.isVisible()) {
            ls.setVisible(false);
        }
    }

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
    }

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

    //Load profile editor for clicked profile
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
            loadVersionList();
            versionList.getSelectionModel().select(0);
            profileIcon.setImage(Utils.getProfileIcon(ProfileIcon.FURNACE));
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
                    jA.append("-Xmx512M");
                } else {
                    jA.append("-Xmx1G");
                }
                jA.append(" -XX:+UseConcMarkSweepGC");
                jA.append(" -XX:+CMSIncrementalMode");
                jA.append(" -XX:-UseAdaptiveSizePolicy");
                jA.append(" -Xmn128M");
                javaArgs.setText(jA.toString());
            } else {
                javaExecBlock.setVisible(false);
                javaExecBlock.setManaged(false);
                javaArgsBlock.setVisible(false);
                javaArgsBlock.setManaged(false);
            }
            toggleEditorOption(resolutionLabel, false);
            resW.getValueFactory().setValue(854);
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
                        profileName.setText("Latest Release");
                        profileIcon.setImage(Utils.getProfileIcon(ProfileIcon.GRASS));
                    } else {
                        profileName.setText("Latest Snapshot");
                        profileIcon.setImage(Utils.getProfileIcon(ProfileIcon.CRAFTING_TABLE));
                    }
                    versionBlock.setVisible(false);
                    versionBlock.setManaged(false);
                    iconBlock.setVisible(false);
                    iconBlock.setManaged(false);
                } else {
                    if (p.hasIcon()) {
                        profileIcon.setImage(Utils.getProfileIcon(p.getIcon()));
                    } else {
                        profileIcon.setImage(Utils.getProfileIcon(ProfileIcon.FURNACE));
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
                    loadVersionList();
                    if (p.hasVersion()) {
                        String versionID = p.getVersionID();
                        if (versionID.equalsIgnoreCase("lastest-release")) {
                            versionList.getSelectionModel().select(0);
                        } else if (versionID.equalsIgnoreCase("latest-snapshot") && kernel.getSettings().getEnableSnapshots()) {
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
                    resH.getValueFactory().setValue(p.getResolutionHeight());
                    resW.getValueFactory().setValue(p.getResolutionWidth());
                } else {
                    toggleEditorOption(resolutionLabel, false);
                    resW.getValueFactory().setValue(854);
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
                            jA.append("-Xmx512M");
                        } else {
                            jA.append("-Xmx1G");
                        }
                        jA.append(" -XX:+UseConcMarkSweepGC");
                        jA.append(" -XX:+CMSIncrementalMode");
                        jA.append(" -XX:-UseAdaptiveSizePolicy");
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

    private void loadVersionList() {
        ObservableList<String> vers = FXCollections.observableArrayList();
        vers.add("Latest Release");
        if (kernel.getSettings().getEnableSnapshots()) {
            vers.add("Latest Snapshot");
        }
        for (VersionMeta v : kernel.getVersions().getVersions().values()) {
            if (v.getType() == VersionType.RELEASE) {
                vers.add(v.getID());
            } else if (v.getType() == VersionType.SNAPSHOT && kernel.getSettings().getEnableSnapshots()) {
                vers.add(v.getID());
            } else if ((v.getType() == VersionType.OLD_BETA || v.getType() == VersionType.OLD_ALPHA) && kernel.getSettings().getEnableHistorical()) {
                vers.add(v.getID());
            }
        }
        versionList.setItems(vers);
    }

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
                target.setVersionID("latest-release");
            } else if (versionList.getSelectionModel().getSelectedIndex() == 1 && kernel.getSettings().getEnableSnapshots()) {
                target.setVersionID("latest-snapshot");
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
            target.setResolution(resW.getValue(), resH.getValue());
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
                target.setJavaDir(null);
            }
        }
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        Stage s = (Stage) a.getDialogPane().getScene().getWindow();
        s.getIcons().add(new Image("/kml/gui/textures/icon.png"));
        a.setContentText("Profile save successfully!");
        a.showAndWait();
        loadProfileList();
        switchTab(launchOptionsLabel);
    }

    @FXML
    public void cancelProfile() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        Stage s = (Stage) a.getDialogPane().getScene().getWindow();
        s.getIcons().add(new Image("/kml/gui/textures/icon.png"));
        a.setContentText("Are you sure you want to cancel?\nAny saved changes won't be applied!");
        Optional<ButtonType> result = a.showAndWait();
        if (result.get() == ButtonType.OK) {
            switchTab(launchOptionsLabel);
        }
    }

    @FXML
    public void deleteProfile() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        Stage s = (Stage) a.getDialogPane().getScene().getWindow();
        s.getIcons().add(new Image("/kml/gui/textures/icon.png"));
        a.setContentText("Are you sure you want to delete this profile?\nThis action cannot be undone!");
        Optional<ButtonType> result = a.showAndWait();
        if (result.get() == ButtonType.OK) {
            Label selectedElement = profileList.getSelectionModel().getSelectedItem();
            if (kernel.getProfiles().deleteProfile(selectedElement.getId())) {
                a.setAlertType(Alert.AlertType.INFORMATION);
                a.setContentText("Profile deleted successfully.");
                a.showAndWait();
            } else {
                a.setAlertType(Alert.AlertType.ERROR);
                a.setContentText("Failed to delete the profile.");
                a.showAndWait();
            }
            loadProfileList();
            switchTab(launchOptionsLabel);
        }
    }

    //Toggle editor options
    private void toggleEditorOption(Object src, boolean newState) {
        if (src instanceof Label) {
            Label l = (Label)src;
            if (newState) {
                l.getStyleClass().remove("toggle-disabled");
                l.getStyleClass().add("toggle-enabled");
            } else {
                l.getStyleClass().remove("toggle-enabled");
                l.getStyleClass().add("toggle-disabled");
            }
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

    /*
        Update editor when clicking labels
        This method fetches the adjacent sibling to determine if is disabled
     */
    @FXML
    public void updateEditor(MouseEvent e) {
        Label l = (Label)e.getSource();
        toggleEditorOption(l, l.getParent().getChildrenUnmodifiable().get(1).isDisable());
    }


    public void updateExistingUsers() {
        Authentication a = kernel.getAuthentication();
        if (a.getUsers().size() > 0 && !a.hasSelectedUser()) {
            existingPanel.setVisible(true);
            existingPanel.setManaged(true);
            ObservableList<User> users = FXCollections.observableArrayList();
            Map<String, User> us = a.getUsers();
            Set<String> keys = us.keySet();
            for (String key : keys) {
                users.add(us.get(key));
            }
            existingUsers.setItems(users);
        } else {
            existingPanel.setVisible(false);
            existingPanel.setManaged(false);
        }

    }

    public void showLoginPrompt(boolean showLoginPrompt) {
        if (showLoginPrompt) {
            contentPane.getSelectionModel().select(loginTab);
            tabMenu.setVisible(false);
            tabMenu.setManaged(false);
            accountButton.setVisible(false);
            playPane.setVisible(false);
            Authentication a = kernel.getAuthentication();
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

    public void authenticate() {
        Alert a = new Alert(Alert.AlertType.WARNING);
        Stage s = (Stage) a.getDialogPane().getScene().getWindow();
        s.getIcons().add(new Image("/kml/gui/textures/icon.png"));
        if (username.getText().isEmpty()) {
            a.setContentText("You cannot leave the username field empty!");
            a.show();
        } else if (password.getText().isEmpty()) {
            a.setContentText("You cannot leave the password field empty!");
            a.show();
        } else {
            try {
                Authentication auth = kernel.getAuthentication();
                auth.authenticate(username.getText(), password.getText());
                username.setText("");
                password.setText("");
                showLoginPrompt(false);
            } catch (AuthenticationException ex) {
                a.setAlertType(Alert.AlertType.ERROR);
                a.setHeaderText("Failed to authenticate");
                a.setContentText(ex.getMessage());
                a.show();
                password.setText("");
            }
        }
    }

    //Refresh existing user
    public void refresh() {
        User selected = existingUsers.getSelectionModel().getSelectedItem();
        Alert a = new Alert(Alert.AlertType.WARNING);
        Stage s = (Stage) a.getDialogPane().getScene().getWindow();
        s.getIcons().add(new Image("/kml/gui/textures/icon.png"));
        if (selected == null) {
            a.setContentText("Select a user first!");
            a.show();
        } else {
            Authentication auth = kernel.getAuthentication();
            try {
                auth.setSelectedUser(selected.getUserID());
                auth.refresh();
                showLoginPrompt(false);
            } catch (AuthenticationException ex) {
                a.setAlertType(Alert.AlertType.ERROR);
                a.setHeaderText("We could not log you back with that user!");
                a.setContentText(ex.getMessage());
                a.show();
                updateExistingUsers();
            }
        }
    }

    //Logout existing user
    public void logout() {
        User selected = existingUsers.getSelectionModel().getSelectedItem();
        Alert a = new Alert(Alert.AlertType.WARNING);
        Stage s = (Stage) a.getDialogPane().getScene().getWindow();
        s.getIcons().add(new Image("/kml/gui/textures/icon.png"));
        if (selected == null) {
            a.setContentText("Select a user first!");
            a.show();
        } else {
            Authentication auth = kernel.getAuthentication();
            auth.logOut(selected.getUserID());
            updateExistingUsers();
        }
    }

    private void setVisible(boolean b) {
        if (b) {
            stage.show();
        } else {
            stage.close();
        }
    }

    @FXML
    public void register() {
        //Open register page
        kernel.getHostServices().showDocument("https://krothium.com/register");
    }

    @FXML
    public void openHelp() {
        //Open help page
        kernel.getHostServices().showDocument("https://krothium.com/forum/12-soporte/");
    }

    @FXML
    public void triggerAuthenticate(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            authenticate();
        }
    }

    //Handles mouse events on the settings tab and updates launcher settings
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
            s.setEnableSnapshots(!s.getEnableSnapshots());
            toggleLabel(source, s.getEnableSnapshots());
            kernel.getProfiles().updateSessionProfiles();
            loadProfileList();
        } else if (source == historicalVersions) {
            s.setEnableHistorical(!s.getEnableHistorical());
            toggleLabel(source, s.getEnableHistorical());
        } else if (source == advancedSettings) {
            s.setEnableAdvanced(!s.getEnableAdvanced());
            toggleLabel(source, s.getEnableAdvanced());
        }
    }

    //Changes the label icon
    private void toggleLabel(Label label, boolean state) {
        if (state) {
            label.getStyleClass().remove("toggle-disabled");
            label.getStyleClass().add("toggle-enabled");
        } else {
            label.getStyleClass().remove("toggle-enabled");
            label.getStyleClass().add("toggle-disabled");
        }
    }
}
