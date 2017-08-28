package kml.gui;

import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class BrowserFX {
    @FXML
    private WebView webBrowser;

    private Stage stage;
    private String askedURL;
    private Stage toHide;

    public void initialize(Stage s) {
        stage = s;
        WebEngine engine = webBrowser.getEngine();
        String userAgent = engine.getUserAgent();
        engine.setUserAgent(userAgent.substring(0, userAgent.indexOf(")")) + "; rv:55.0) Gecko/20100101 Firefox/55.0");
        engine.setJavaScriptEnabled(true);
        engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            String location = engine.getLocation();
            if (!location.equalsIgnoreCase(askedURL) && !location.contains("krothium.com")) {
                toHide.show();
                s.close();
            }
        });
    }

    public void loadWebsite(String url) {
        askedURL = url;
        webBrowser.getEngine().load(url);
    }

    public void show(Stage toHide) {
        this.toHide = toHide;
        toHide.close();
        stage.show();
    }

    public boolean isVisible() {
        return stage.isShowing();
    }
}
