package kml.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import kml.Kernel;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class MainFX {

    private Image flag_es, flag_pt, flag_us, flag_val, flag_br, flag_hu;

    @FXML
    private Label languageButton, switchAccountButton, progressText,
            newsLabel, skinsLabel, settingsLabel, launchOptionsLabel;

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
    private ListView<Label> languagesList;

    @FXML
    private VBox progressPane;

    @FXML
    private HBox playPane, tabMenu;

    @FXML
    private AnchorPane root;

    private Kernel kernel;


    public void initialize() {
        webBrowser.getEngine().load("http://mcupdate.tumblr.com");
        //tabMenu.setVisible(false);
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
        //newsLabel.getStyleClass().add("selectedItem");
        contentPane.getSelectionModel().select(loginTab);
    }


    @FXML
    public void launchGame() {
        System.out.println("TO IMPLEMENT LAUNCH GAME");
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
        System.out.println("TO IMPLEMENT SWITCH ACCOUNT");
    }

    @FXML
    public void showProfiles() {
        System.out.println("TO IMPLEMENT SHOW PROFILES");
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
        if (e.getSource() == languagesList && languagesList.isVisible()) {
            languagesList.setVisible(false);
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

    public void setKernel(Kernel k) {
        kernel = k;
    }

    public void setVisible(boolean visible) {
        root.setVisible(visible);
    }

    public void showLoginPrompt(boolean showLoginPrompt) {
        if (showLoginPrompt) {
            contentPane.getSelectionModel().select(loginTab);
        }

    }
}
