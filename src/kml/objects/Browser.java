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

import java.awt.*;
import java.io.IOException;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class Browser {

    private final Object lock = new Object();
    private WebView browser;
    private WebEngine webEngine;
    private final JFXPanel panel = new JFXPanel();
    public Browser() {
        synchronized (lock){
            Platform.runLater(() -> {
                browser = new WebView();
                webEngine = browser.getEngine();
                final Group root = new Group();
                final Scene scene = new Scene(root);
                panel.setScene(scene);
                webEngine.setJavaScriptEnabled(true);
                webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == Worker.State.SUCCEEDED){
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
                            if (webEngine.getDocument() != null){
                                NodeList list = webEngine.getDocument().getElementsByTagName("a");
                                for (int i = 0; i < list.getLength(); i++){
                                    Node node = list.item(i);
                                    if (node instanceof EventTarget) {
                                        Node a = node.getAttributes().getNamedItem("target");
                                        if (a != null && a.getNodeValue().equalsIgnoreCase("_blank")){
                                            ((EventTarget)node).addEventListener("click", listener, false);
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
                root.getChildren().add(browser);
            });
        }
    }
    public void resizeBrowser(Dimension d){
        synchronized (this.lock){
            browser.setMinSize(d.getWidth(), d.getHeight());
            browser.setMaxSize(d.getWidth(), d.getHeight());
            browser.setPrefSize(d.getWidth(), d.getHeight());
        }
    }
    public void loadURL(String url){
        synchronized (this.lock){
            Platform.runLater(() -> webEngine.load(url));
        }
    }
    public Component getPanel(){
        return this.panel;
    }
}