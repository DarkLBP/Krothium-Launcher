package kml.objects;

import javafx.scene.image.Image;

public class Slide {
    private final String action;
    private final String image;
    private Image imageCache;
    private final String title;
    private final String text;

    public Slide(String action, String image, String title, String text) {
        this.action = action;
        this.image = image;
        this.title = title;
        this.text = text;
    }

    public String getAction() {
        return action;
    }

    public Image getImage() {
        if (imageCache == null) {
            imageCache = new Image(image);
        }
        return imageCache;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }
}
