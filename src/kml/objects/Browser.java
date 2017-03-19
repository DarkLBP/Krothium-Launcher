package kml.objects;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import kml.Kernel;
import kml.Utils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class Browser {

    private final JFXPanel panel = new JFXPanel();
    private final Object lock = new Object();
    private final Kernel k;
    private WebView browser;
    private WebEngine webEngine;

    public Browser(final Kernel k) {
        this.k = k;
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            Group root = new Group();
            Scene scene = new Scene(root);
            panel.setScene(scene);
            synchronized (Browser.this.lock) {
                browser = new WebView();
                webEngine = browser.getEngine();
                webEngine.setJavaScriptEnabled(true);
                webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == Worker.State.SUCCEEDED) {
                        if (!webEngine.getLocation().contains("tumblr.com") && !webEngine.getLocation().contains("adf.ly") && !webEngine.getLocation().contains("sh.st") && !webEngine.getLocation().contains("adfoc.us") && !webEngine.getLocation().contains("krothium.com")) {
                            webEngine.load("http://mcupdate.tumblr.com/");
                        }
                        try {
                            EventListener listener = event -> {
                                try {
                                    Utils.openWebsite(event.getTarget().toString());
                                } catch (IOException e) {
                                    System.out.println("Failed to open url. " + event.getTarget());
                                }
                                event.preventDefault();
                                event.stopPropagation();
                            };
                            if (webEngine.getLocation().contains("tumblr.com")) {
                                if (Objects.nonNull(webEngine.getDocument())) {
                                    NodeList list = webEngine.getDocument().getElementsByTagName("a");
                                    for (int i = 0; i < list.getLength(); i++) {
                                        Node node = list.item(i);
                                        if (node instanceof EventTarget) {
                                            Node a = list.item(i);
                                            if (a instanceof EventTarget) {
                                                ((EventTarget) a).addEventListener("click", listener, false);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
            root.getChildren().add(browser);
        });

    }

    public void resizeBrowser(Dimension d) {
        synchronized (this.lock) {
            browser.setMinSize(d.getWidth(), d.getHeight());
            browser.setMaxSize(d.getWidth(), d.getHeight());
            browser.setPrefSize(d.getWidth(), d.getHeight());
        }
    }

    public void home() {
        synchronized (this.lock) {
            Platform.runLater(() -> webEngine.load("http://mc.krothium.com/news/" + k.getAuthentication().getSelectedUser().getProfileID()));
        }
    }

    public JComponent getPanel() {
        return this.panel;
    }
}