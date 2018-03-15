package kml.gui;

import javafx.scene.image.Image;

public class Slide {
    private final String action;
    private final String image;
    private final String title;
    private final String text;

    private Image loadedImage;

    public Slide(String action, String image, String title, String text) {
        this.action = action;
        this.image = image;
        this.title = title;
        this.text = text;
    }

    public final String getAction() {
        return this.action;
    }

    public final Image getImage() {
        if (this.loadedImage == null) {
            this.loadedImage = new Image(this.image, true);
        }
        return this.loadedImage;
    }

    public final String getTitle() {
        return this.title;
    }

    public final String getText() {
        return this.text;
    }
}
