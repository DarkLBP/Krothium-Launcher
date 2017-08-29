package kml.gui;

import javafx.concurrent.Worker.State;
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

    public final void initialize(Stage s) {
        this.stage = s;
        WebEngine engine = this.webBrowser.getEngine();
        String userAgent = engine.getUserAgent();
        engine.setUserAgent(userAgent.substring(0, userAgent.indexOf(')')) + "; rv:55.0) Gecko/20100101 Firefox/55.0");
        engine.setJavaScriptEnabled(true);
        engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(State.SUCCEEDED)) {
                String location = engine.getLocation();
                if (!location.equalsIgnoreCase(this.askedURL) && !location.contains("krothium.com") && !location.contains("about:blank")) {
                    this.webBrowser.getEngine().load("about:blank");
                }
                if (location.contains("about:blank") && !this.toHide.isShowing()) {
                    this.toHide.show();
                    s.close();
                }
            }
        });
    }

    public final void loadWebsite(String url) {
        this.askedURL = url;
        this.webBrowser.getEngine().load(url);
    }

    public final void show(Stage toHide) {
        this.toHide = toHide;
        toHide.close();
        this.stage.show();
    }

    public final boolean isVisible() {
        return this.stage.isShowing();
    }
}
