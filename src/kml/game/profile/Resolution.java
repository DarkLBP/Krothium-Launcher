package kml.game.profile;

import com.google.gson.annotations.Expose;

public class Resolution {
    @Expose
    private int width;
    @Expose
    private int height;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
