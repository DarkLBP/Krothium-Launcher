package kml.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by darkl on 18/11/2016.
 */
public class BackgroundPanel extends JPanel {
    private Image image;
    @Override
    public void paintComponent(Graphics g){
        if (image == null) {
            super.paintComponent(g);
        } else {
            int height = image.getHeight(null);
            int width = image.getWidth(null);
            for (int x = 0; x < getWidth(); x += width) {
                for (int y = 0; y < getHeight(); y += height) {
                    g.drawImage( image, x, y, null, null );
                }
            }
        }
    }
    public void setImage(Image img){
        image = img;
    }
}
