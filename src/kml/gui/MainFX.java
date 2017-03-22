package kml.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import kml.Kernel;

/**
 * Created by darkl on 18/03/2017.
 */
public class MainFX
{
	/**
	 * TAB CONTENTS
	 */
	public AnchorPane newsPane;
	public WebView    webView;
	public AnchorPane skinsPane;
	public AnchorPane settingsPane;
	public AnchorPane launcherOptionsPane;

	/**
	 * LOGIN
	 */
	public AnchorPane    loginScreenPane; // By default it's hidden
	public PasswordField passwordInput;
	public TextField     textInput;
	public Button        loginButton;
	public Button        registerButton;

	private Kernel kernel;

	/**
	 * HEADER
	 */
	public Label           languagesButton;
	public ListView<Label> languagesList;

	private Image flag_es, flag_pt, flag_us, flag_val, flag_br, flag_hu;


	public Label switchAccountButton;

	/**
	 * CONTENT
	 */
	// These will trigger the tabchange
	public HBox  navHBox;
	public Label newsTabButton;
	public Label skinsTabButton;
	public Label settingsTabButton;
	public Label launcherOptionsTabButton;

	// Actual tabpane
	public TabPane tabPane;
	public Tab     newsTab;
	public Tab     skinsTab;
	public Tab     settingsTab;
	public Tab     launcherOptionsTab;

	/**
	 * FOOTER
	 */
	public Label            playButton;
	public Label            profilesButton;
	public ListView<String> profilesList;
	public Label            versionLabel;

	public void initialize()
	{
		setupTabs();
		setupButtons();
	}

	private void setupButtons()
	{
		//START THE GAME :D
		playButton.setOnMouseClicked(event -> {

		});


		//SELECTING PROFILES
		profilesButton.setOnMouseClicked(event -> {
			if (profilesList.isVisible()) {
				profilesList.setVisible(false);
			}
			else {
				profilesList.setVisible(true);
			}
		});

/*
		for (Map.Entry<String, Profile> entrySet : kernel.getProfiles().getProfiles().entrySet()) {

		}
*/

		languagesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

		});

		// SWITCH LANGUAGE
		this.flag_es = new Image(LoginTab.class.getResourceAsStream("/kml/gui/textures/flags/flag_es-es.png"));
		this.flag_us = new Image(LoginTab.class.getResourceAsStream("/kml/gui/textures/flags/flag_en-us.png"));
		this.flag_pt = new Image(LoginTab.class.getResourceAsStream("/kml/gui/textures/flags/flag_pt-pt.png"));
		this.flag_val = new Image(LoginTab.class.getResourceAsStream("/kml/gui/textures/flags/flag_val-es.png"));
		this.flag_br = new Image(LoginTab.class.getResourceAsStream("/kml/gui/textures/flags/flag_pt-br.png"));
		this.flag_hu = new Image(LoginTab.class.getResourceAsStream("/kml/gui/textures/flags/flag_hu-hu.png"));

		final Label           en                = new Label("English - United States", new ImageView(flag_us));
		final Label           es                = new Label("Español - España", new ImageView(flag_es));
		final Label           ca                = new Label("Català (Valencià) - País Valencià", new ImageView(flag_val));
		final Label           pt                = new Label("Português - Portugal", new ImageView(flag_pt));
		final Label           br                = new Label("Português - Brasil", new ImageView(flag_br));
		final Label           hu                = new Label("Hungarian - Magyar", new ImageView(flag_hu));
		ObservableList<Label> languageListItems = FXCollections.observableArrayList(en, es, ca, pt, br, hu);
		languagesList.setItems(languageListItems);

		languagesButton.setOnMouseClicked(event -> {
			if (languagesList.isVisible()) {
				languagesList.setVisible(false);
			}
			else {
				languagesList.setVisible(true);
			}
		});

		languagesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
/*
			if (newValue.equals(en)) {
				kernel.getSettings().setLocale("en-us");
			}
			else if (newValue.equals(es)) {
				kernel.getSettings().setLocale("es-es");
			}
			else if (newValue.equals(ca)) {
				kernel.getSettings().setLocale("val-es");
			}
			else if (newValue.equals(pt)) {
				kernel.getSettings().setLocale("pt-pt");
			}
			else if (newValue.equals(br)) {
				kernel.getSettings().setLocale("pt-br");
			}
			else if (newValue.equals(hu)) {
				kernel.getSettings().setLocale("hu-hu");
			}
*/

			languagesList.setVisible(false);
		});

		// SWITCH ACCOUNT
		switchAccountButton.setOnMouseClicked(event -> {

		});
	}

	private void setupTabs()
	{
		SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();

		newsTabButton.setOnMouseClicked(event -> {
			if (selectionModel.getSelectedItem().equals(newsTab)) return;

			selectionModel.select(newsTab);
			newsTabButton.getStyleClass().add("selected");

			skinsTabButton.getStyleClass().remove("selected");
			settingsTabButton.getStyleClass().remove("selected");
			launcherOptionsTabButton.getStyleClass().remove("selected");
		});
		skinsTabButton.setOnMouseClicked(event -> {
			if (selectionModel.getSelectedItem().equals(skinsTab)) return;

			selectionModel.select(skinsTab);
			skinsTabButton.getStyleClass().add("selected");

			newsTabButton.getStyleClass().remove("selected");
			settingsTabButton.getStyleClass().remove("selected");
			launcherOptionsTabButton.getStyleClass().remove("selected");
		});
		settingsTabButton.setOnMouseClicked(event -> {
			if (selectionModel.getSelectedItem().equals(settingsTab)) return;

			selectionModel.select(settingsTab);
			settingsTabButton.getStyleClass().add("selected");

			newsTabButton.getStyleClass().remove("selected");
			skinsTabButton.getStyleClass().remove("selected");
			launcherOptionsTabButton.getStyleClass().remove("selected");
		});
		launcherOptionsTabButton.setOnMouseClicked(event -> {
			if (selectionModel.getSelectedItem().equals(launcherOptionsTab)) return;

			selectionModel.select(launcherOptionsTab);
			launcherOptionsTabButton.getStyleClass().add("selected");

			newsTabButton.getStyleClass().remove("selected");
			skinsTabButton.getStyleClass().remove("selected");
			settingsTabButton.getStyleClass().remove("selected");
		});
	}

	public void setKernel(Kernel kernel)
	{
		this.kernel = kernel;
	}
}
