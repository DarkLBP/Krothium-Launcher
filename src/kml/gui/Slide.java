package kml.gui;

import javafx.scene.image.Image;
import kml.Console;

public class Slide {
    private final String action;
    private final Image image;
    private final String title;
    private final String text;

    public Slide(String action, String image, String title, String text, Console c) {
        this.action = action;
        this.image = new Image(image, true);
        this.title = title;
        this.text = text;
    }

    public final String getAction() {
        return this.action;
    }

    public final Image getImage() {
        return this.image;
    }

    public final String getTitle() {
        return this.title;
    }

    public final String getText() {
        return this.text;
    }
}
