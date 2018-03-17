package kml.gui;

import javafx.scene.image.Image;
import kml.utils.Utils;
import java.io.IOException;
import java.io.InputStream;

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
            InputStream stream = Utils.readCachedStream(this.image);
            this.loadedImage = new Image(stream);
            try {
                stream.close();
            } catch (IOException ignored) {}
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
