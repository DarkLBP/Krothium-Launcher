package kml;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by darkl on 01/01/2017.
 */
public class CapePreview {
    public static Image generateFront(Image i, int s) {
        BufferedImage bImg = new BufferedImage(10*s,16*s,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bImg.createGraphics();
        g.drawImage(i, 0*s, 0*s, 10*s, 16*s, 1, 1, 11, 17, null);
        return bImg;
    }
    public static Image generateFront(File f, int s) throws IOException {
        return generateFront(ImageIO.read(f), s);
    }
    public static Image generateFront(URL u, int s) throws IOException {
        return generateFront(ImageIO.read(u), s);
    }
    public static Image generateBack(Image i, int s) {
        BufferedImage bImg = new BufferedImage(10*s,16*s,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bImg.createGraphics();
        g.drawImage(i, 0*s, 0*s, 10*s, 16*s, 12, 1, 22, 17, null);
        return bImg;
    }
    public static Image generateBack(File f, int s) throws IOException
    {
        return generateBack(ImageIO.read(f), s);
    }
    public static Image generateBack(URL u, int s) throws IOException {
        return generateBack(ImageIO.read(u), s);
    }
    public static Image generateCombo(Image i, int s, int sep) {
        BufferedImage bImg = new BufferedImage((10*2+sep)*s, 16*s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bImg.createGraphics();
        int sep_index = (sep+10)*s;
        g.drawImage(i, 0*s, 0*s, 10*s, 16*s, 1, 1, 11, 17, null);
        g.drawImage(i, (sep_index+0*s), 0*s, (sep_index+10*s), 16*s, 12, 1, 22, 17, null);
        return bImg;
    }
    public static Image generateCombo(File f, int s, int sep) throws IOException {
        return generateCombo(ImageIO.read(f), s, sep);
    }
    public static Image generateCombo(URL u, int s, int sep) throws IOException {
        return generateCombo(ImageIO.read(u), s, sep);
    }
    public static void optimizeCape(File f) throws IOException {
        Image i = ImageIO.read(f);
        BufferedImage bImg = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bImg.createGraphics();
        g.drawImage(i, 0, 0, 22, 17, 0, 0, 22, 17, null);
        ImageIO.write(bImg, "png", f);
    }
}
