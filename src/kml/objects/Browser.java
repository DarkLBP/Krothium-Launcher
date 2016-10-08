package kml.objects;

import javafx.concurrent.Worker;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import kml.Constants;
import kml.Utils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.io.IOException;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class Browser extends Region {

    private final WebView browser = new WebView();
    private final WebEngine webEngine = browser.getEngine();

    public Browser() {
        webEngine.setJavaScriptEnabled(true);
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED){
                if (!webEngine.getLocation().contains("localhost") && !webEngine.getLocation().contains("adf.ly") && !webEngine.getLocation().contains("krothium")){
                    if (Constants.USE_LOCAL){
                        webEngine.load("http://localhost:" + Constants.USED_PORT);
                    } else {
                        webEngine.load("http://mc.krothium.com/launcher/?p=" + Constants.USED_PORT);
                    }
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
        if (Constants.USE_LOCAL){
            webEngine.load("http://localhost:" + Constants.USED_PORT);
        } else {
            webEngine.load("http://mc.krothium.com/launcher/?p=" + Constants.USED_PORT);
        }
        webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:49.0) Gecko/20100101 Firefox/49.0");
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