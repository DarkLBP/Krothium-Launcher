package kml.gui;

import javafx.scene.image.Image;
import kml.utils.Utils;
import java.io.IOException;
import java.io.InputStream;

class Slide {
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
        return action;
    }

    public final Image getImage() {
        if (loadedImage == null) {
            InputStream stream = Utils.readCachedStream(image);
            if (stream == null) {
                return null;
            }
            loadedImage = new Image(stream);
            try {
                stream.close();
            } catch (IOException ignored) {}
        }
        return loadedImage;
    }

    public final String getTitle() {
        return title;
    }

    public final String getText() {
        return text;
    }
}
