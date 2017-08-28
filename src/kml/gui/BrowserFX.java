package kml.gui;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class BrowserFX {
    @FXML
    private WebView webBrowser;

    private Stage stage;
    private String askedURL;
    private boolean done;

    public void initialize(Stage s) {
        stage = s;
        String userAgent = webBrowser.getEngine().getUserAgent();
        webBrowser.getEngine().setUserAgent(userAgent.substring(0, userAgent.indexOf(")")) + "; rv:55.0) Gecko/20100101 Firefox/55.0");
        webBrowser.getEngine().setJavaScriptEnabled(true);
        webBrowser.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            String location = webBrowser.getEngine().getLocation();
            if (!location.equalsIgnoreCase(askedURL) && !location.contains("krothium.com") && !done) {
                done = true;
                s.close();
            }
        });
    }

    public void loadWebsite(String url) {
        done = false;
        askedURL = url;
        webBrowser.getEngine().load(url);
    }

    public void show(Stage toHide) {
        toHide.close();
        stage.showAndWait();
        webBrowser.getEngine().loadContent("");
        toHide.show();
    }
}
