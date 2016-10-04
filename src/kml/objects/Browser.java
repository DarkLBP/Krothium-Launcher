package kml.objects;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import kml.Constants;
import kml.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLIFrameElement;

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
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED){
                if (!webEngine.getLocation().contains("localhost") && !webEngine.getLocation().contains("adf.ly") && !webEngine.getLocation().contains("krothium")){
                    if (Constants.USE_LOCAL){
                        webEngine.load("http://localhost:" + Constants.USED_PORT);
                    } else {
                        webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:49.0) Gecko/20100101 Firefox/49.0");
                        webEngine.load("http://mc.krothium.com/launcher/?p=" + Constants.USED_PORT);
                    }
                }
                EventListener listener = event -> {
                    try {
                        Utils.openWebsite(event.getTarget().toString());
                    } catch (IOException e) {
                        System.out.println("Failed to open url. " + event.getTarget());
                    }
                    event.preventDefault();
                    event.stopPropagation();
                };
                Document document;
                if (Constants.USE_LOCAL){
                    document = webEngine.getDocument();
                } else {
                    System.out.println(webEngine.getLocation());
                    Document doc = webEngine.getDocument();
                    try {
                        HTMLIFrameElement iframeElement = (HTMLIFrameElement) doc.getElementById("launcher");
                        document = iframeElement.getContentDocument();
                    } catch (NullPointerException ex){
                        document = null;
                    }
                }
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
        });
        if (Constants.USE_LOCAL){
            webEngine.load("http://localhost:" + Constants.USED_PORT);
        } else {
            webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:49.0) Gecko/20100101 Firefox/49.0");
            webEngine.load("http://mc.krothium.com/launcher/?p=" + Constants.USED_PORT);
        }
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