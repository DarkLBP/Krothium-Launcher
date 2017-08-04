package kml.gui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import kml.*;
import kml.enums.ProfileIcon;
import kml.enums.ProfileType;
import kml.exceptions.AuthenticationException;
import kml.exceptions.DownloaderException;
import kml.exceptions.GameLauncherException;
import kml.objects.Profile;
import kml.objects.User;

import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class MainFX {

    private Image flag_es, flag_pt, flag_us, flag_val, flag_br, flag_hu;

    @FXML
    private Label languageButton, switchAccountButton, progressText,
            newsLabel, skinsLabel, settingsLabel, launchOptionsLabel,
            keepLauncherOpen, outputLog, enableSnapshots, historicalVersions,
            advancedSettings;

    @FXML
    private Button playButton, profilesButton;

    @FXML
    private Tab loginTab, newsTab, skinsTab,
            settingsTab, launchOptionsTab, profileEditorTab;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private TabPane contentPane;

    @FXML
    private WebView webBrowser;

    @FXML
    private ListView<Label> languagesList, profileList, profilePopupList;

    @FXML
    private VBox progressPane, existingPanel;

    @FXML
    private HBox playPane, tabMenu, profilePopup;

    @FXML
    private AnchorPane root;

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

    private Kernel kernel;
    private Stage stage;

    public void initialize(Kernel k, Stage s) {
        //Require to exit using Platform.exit()
        Platform.setImplicitExit(false);

        //Set kernel and stage
        kernel = k;
        stage = s;

        //Load news tab website
        webBrowser.getEngine().load("http://mcupdate.tumblr.com");

        //Prepare language list
        flag_es = new Image("/kml/gui/textures/flags/flag_es-es.png");
        flag_us = new Image("/kml/gui/textures/flags/flag_en-us.png");
        flag_pt = new Image("/kml/gui/textures/flags/flag_pt-pt.png");
        flag_val = new Image("/kml/gui/textures/flags/flag_val-es.png");
        flag_br = new Image("/kml/gui/textures/flags/flag_pt-br.png");
        flag_hu = new Image("/kml/gui/textures/flags/flag_hu-hu.png");
        final Label en = new Label("English - United States", new ImageView(flag_us));
        en.setId("en-us");
        final Label es = new Label("Español - España", new ImageView(flag_es));
        es.setId("es-es");
        final Label ca = new Label("Valencià - C. Valenciana", new ImageView(flag_val));
        ca.setId("val-es");
        final Label pt = new Label("Português - Portugal", new ImageView(flag_pt));
        pt.setId("pt-pt");
        final Label br = new Label("Português - Brasil", new ImageView(flag_br));
        br.setId("pt-br");
        final Label hu = new Label("Hungarian - Magyar", new ImageView(flag_hu));
        hu.setId("hu-hu");
        ObservableList<Label> languageListItems = FXCollections.observableArrayList(en, es, ca, pt, br, hu);
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

    }

    @FXML
    public void loadProfileList() {
        Profiles ps = kernel.getProfiles();
        Set<String> profiles = ps.getProfiles().keySet();
        //For some reason using the same label for both lists one list appear the items blank
        ObservableList<Label> profileListItems = FXCollections.observableArrayList();
        ObservableList<Label> profileListItems2 = FXCollections.observableArrayList();
        Label l;
        Label l2;
        for (String id : profiles) {
            Profile p = ps.getProfile(id);
            if (p.getType() == ProfileType.RELEASE) {
                l = new Label("Latest Release", new ImageView(Utils.getProfileIcon(ProfileIcon.GRASS)));
                l2 = new Label("Latest Release", new ImageView(Utils.getProfileIcon(ProfileIcon.GRASS)));
            } else if (p.getType() == ProfileType.SNAPSHOT) {
                l = new Label("Latest Snapshot", new ImageView(Utils.getProfileIcon(ProfileIcon.CRAFTING_TABLE)));
                l2 = new Label("Latest Snapshot", new ImageView(Utils.getProfileIcon(ProfileIcon.CRAFTING_TABLE)));
            } else {
                String name = p.hasName() ? p.getName() : "Unnamed Profile";
                ProfileIcon pi = p.hasIcon() ? p.getIcon() : ProfileIcon.FURNACE;
                l = new Label(name, new ImageView(Utils.getProfileIcon(pi)));
                l2 = new Label(name, new ImageView(Utils.getProfileIcon(pi)));
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
            l.getStyleClass().add("text-4");
            l.setId(p.getID());
            l2.getStyleClass().add("text-4");
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

    @FXML
    private void selectProfile(MouseEvent e) {
        //Select profile and refresh list
        ListView<Label> profiles = (ListView<Label>)e.getSource();
        kernel.getProfiles().setSelectedProfile(profiles.getSelectionModel().getSelectedItem().getId());
        loadProfileList();
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
        }
    }

    @FXML
    public void switchTab(Event e) {
        SingleSelectionModel<Tab> selection = contentPane.getSelectionModel();
        Tab oldTab = selection.getSelectedItem();
        if (oldTab == newsTab) {
            newsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == skinsTab) {
            skinsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == settingsTab) {
            settingsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == launchOptionsTab) {
            launchOptionsLabel.getStyleClass().remove("selectedItem");
        }
        if (e.getSource() == newsLabel) {
            newsLabel.getStyleClass().add("selectedItem");
            selection.select(newsTab);
        } else if (e.getSource() == skinsLabel) {
            skinsLabel.getStyleClass().add("selectedItem");
            selection.select(skinsTab);
        } else if (e.getSource() == settingsLabel) {
            settingsLabel.getStyleClass().add("selectedItem");
            selection.select(settingsTab);
        } else if (e.getSource() == launchOptionsLabel) {
            launchOptionsLabel.getStyleClass().add("selectedItem");
            selection.select(launchOptionsTab);
        }
    }

    @FXML
    public void hidePopup(Event e) {
        System.out.println("PASA");
        ListView ls = (ListView)e.getSource();
        if (ls.isVisible()) {
            ls.setVisible(false);
        }
    }

    @FXML
    public void updateLanguage() {
        Label selected = languagesList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            kernel.getSettings().setLocale(selected.getId());
        }
        languagesList.setVisible(false);
    }

    //Load profile editor for clicked profile
    @FXML
    public void loadEditor() {
        Label selectedElement = profileList.getSelectionModel().getSelectedItem();
        if (selectedElement != null) {
            this.contentPane.getSelectionModel().select(profileEditorTab);
            Profile p = kernel.getProfiles().getProfile(selectedElement.getId());

            if (p.hasName()){
                profileName.setText(p.getName());
            }
            if (p.hasResolution()) {
                resH.getValueFactory().setValue(p.getResolutionHeight());
                resW.getValueFactory().setValue(p.getResolutionWidth());
            }
            if (p.hasGameDir()) {
                gameDir.setText(p.getGameDir().getAbsolutePath());
            }
            if (p.hasJavaDir()){
                javaExec.setText(p.getJavaDir().getAbsolutePath());
            }
            if (p.hasJavaArgs()) {
                javaArgs.setText(p.getJavaArgs());
            }
        }
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
            switchAccountButton.setVisible(false);
            playPane.setVisible(false);
            Authentication a = kernel.getAuthentication();
            updateExistingUsers();
        } else {
            contentPane.getSelectionModel().select(newsTab);
            tabMenu.setVisible(true);
            tabMenu.setManaged(true);
            switchAccountButton.setVisible(true);
            playPane.setVisible(true);
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
