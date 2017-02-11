package kml.objects;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import org.w3c.dom.events.Event;
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
    public Browser(final Kernel k) {
        Platform.setImplicitExit(false);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Group root = new Group();
                Scene scene = new Scene(root);
                panel.setScene(scene);
                synchronized (Browser.this.lock) {
                    browser = new WebView();
                    webEngine = browser.getEngine();
                    webEngine.setJavaScriptEnabled(true);
                    webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                        @Override
                        public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                            if (newValue == Worker.State.SUCCEEDED) {
                                if (!webEngine.getLocation().contains("tumblr.com") && !webEngine.getLocation().contains("adf.ly") && !webEngine.getLocation().contains("sh.st") && !webEngine.getLocation().contains("adfoc.us") && !webEngine.getLocation().contains("krothium.com")) {
                                    webEngine.load("http://mcupdate.tumblr.com/");
                                }
                                try {
                                    EventListener listener = new EventListener() {
                                        @Override
                                        public void handleEvent(Event event) {
                                            try {
                                                Utils.openWebsite(event.getTarget().toString());
                                            } catch (IOException e) {
                                                System.out.println("Failed to open url. " + event.getTarget());
                                            }
                                            event.preventDefault();
                                            event.stopPropagation();
                                        }
                                    };
                                    if (webEngine.getLocation().contains("tumblr.com")) {
                                        if (webEngine.getDocument() != null) {
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
                        }
                    });
                    try {
                        String javaVer = System.getProperty("java.version");
                        double ver = Double.parseDouble(javaVer.substring(0,3));
                        if (ver >= 1.8){
                            k.getConsole().printInfo("Browser user agent switched.");
                            webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");
                        }
                    } catch (Exception ex){}
                    webEngine.load("http://mc.krothium.com/news/" + k.getProfiles().getSelectedProfile());
                }
                root.getChildren().add(browser);
            }
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