package kml.objects;

import javafx.scene.image.Image;
import kml.Console;
import kml.Utils;

import java.io.File;
import java.io.IOException;

public class Slide {
    private final String action;
    private final Image image;
    private final String title;
    private final String text;

    public Slide(String action, String image, String title, String text, Console c) {
        this.action = action;
        File cachedImage = null;
        try {
            cachedImage = Utils.downloadFileCached(Utils.stringToURL(image));
        } catch (IOException e) {
            c.print("Failed to slide image for " + image);
            e.printStackTrace(c.getWriter());
        }
        this.image = new Image(cachedImage.toURI().toString(), true);
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
