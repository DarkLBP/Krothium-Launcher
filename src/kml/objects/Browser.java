package kml.objects;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.Region;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import kml.Constants;
import kml.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.io.IOException;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class Browser extends Region {

    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();

    public Browser() {
        webEngine.setJavaScriptEnabled(true);
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                if (newValue == Worker.State.SUCCEEDED){
                    EventListener listener = new EventListener() {
                        @Override
                        public void handleEvent(final Event event) {
                            try {
                                Utils.openWebsite(event.getTarget().toString());
                            } catch (IOException e) {
                                System.out.println("Failed to open url. " + event.getTarget());
                            }
                            event.preventDefault();
                            event.stopPropagation();
                        }
                    };
                    final Document document = webEngine.getDocument();
                    if (document != null){
                        NodeList list = document.getElementsByTagName("a");
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
                }
            }
        });
        webEngine.load("http://localhost:" + Constants.USED_PORT);
        getChildren().add(browser);
    }

    @Override protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser,0,0,w,h,0, HPos.CENTER, VPos.CENTER);
    }

    @Override protected double computePrefWidth(double height) {
        return 750;
    }

    @Override protected double computePrefHeight(double width) {
        return 500;
    }
}