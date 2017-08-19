package kml.gui;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;

public class BrowserFX {
    @FXML
    private WebView webBrowser;

    public void initialize() {
        webBrowser.getEngine().load("http://html5test.com");
        System.out.println(webBrowser.getEngine().isJavaScriptEnabled());
    }
}
