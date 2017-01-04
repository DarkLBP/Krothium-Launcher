package kml.objects;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import kml.Utils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class Browser{

    private final JFXPanel panel = new JFXPanel();
    private final Object lock = new Object();
    private WebView browser;
    private WebEngine webEngine;
    private final Group root = new Group();
    private final Scene scene = new Scene(root);
    public Browser() {
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            panel.setScene(scene);
            synchronized (this.lock) {
                browser = new WebView();
                webEngine = browser.getEngine();
                webEngine.setJavaScriptEnabled(true);
                webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == Worker.State.SUCCEEDED){
                        if (!webEngine.getLocation().contains("tumblr.com") && !webEngine.getLocation().contains("adf.ly") && !webEngine.getLocation().contains("sh.st") && !webEngine.getLocation().contains("adfoc.us") && !webEngine.getLocation().contains("krothium.com")){
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
                            if (webEngine.getLocation().contains("tumblr.com")){
                                if (webEngine.getDocument() != null){
                                    NodeList list = webEngine.getDocument().getElementsByTagName("a");
                                    for (int i = 0; i < list.getLength(); i++){
                                        Node node = list.item(i);
                                        if (node instanceof EventTarget) {
                                            Node a = list.item(i);
                                            if (a instanceof EventTarget) {
                                                ((EventTarget)a).addEventListener("click", listener, false);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }
                });
                webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");
                webEngine.load("http://mc.krothium.com/news/");
            }
            root.getChildren().add(browser);
        });

    }
    public void resizeBrowser(Dimension d){
        synchronized (this.lock){
            browser.setMinSize(d.getWidth(), d.getHeight());
            browser.setMaxSize(d.getWidth(), d.getHeight());
            browser.setPrefSize(d.getWidth(), d.getHeight());
        }
    }
    public JComponent getPanel(){
        return this.panel;
    }
}