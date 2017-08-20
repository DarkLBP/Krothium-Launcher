package kml.gui;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class BrowserFX {
    @FXML
    private WebView webBrowser;

    private Stage stage;
    private String askedURL;

    public void initialize(Stage s) {
        stage = s;
        String userAgent = webBrowser.getEngine().getUserAgent();
        webBrowser.getEngine().setUserAgent(userAgent.substring(0, userAgent.indexOf(")")) + "; rv:55.0) Gecko/20100101 Firefox/55.0");
        webBrowser.getEngine().setJavaScriptEnabled(true);
        webBrowser.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (Worker.State.SUCCEEDED.equals(newValue)) {
                if (!webBrowser.getEngine().getLocation().equalsIgnoreCase(askedURL)) {
                    s.close();
                }
            }
        });
    }

    public void loadWebsite(String url) {
        askedURL = url;
        webBrowser.getEngine().load(url);
    }

    public void show(Stage toHide) {
        toHide.close();
        stage.showAndWait();
        toHide.show();
    }
}
