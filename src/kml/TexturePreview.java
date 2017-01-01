package kml;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class TexturePreview {
    private static Image generateComboSkin(Image i, int s, int sep) {
        BufferedImage bImg = new BufferedImage((16*2+sep)*s, 32*s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bImg.createGraphics();
        int sep_index = (sep+16)*s;
        int h = i.getHeight(null);
        if (h == 32) { // 32*64 format
            //FRONT
            //Head
            g.drawImage(i, 4*s, 0, 12*s, 8*s, 8, 8, 16, 16, null);
            //Left Arm
            g.drawImage(i, 0, 8*s, 4*s, 20*s, 44, 20, 48, 32, null);
            //Right Arm
            g.drawImage(i, 12*s, 8*s, 16*s, 20*s, 48, 20, 44, 32, null);
            //Body
            g.drawImage(i, 4*s, 8*s, 12*s, 20*s, 20, 20, 28, 32, null);
            //Left Leg
            g.drawImage(i, 4*s, 20*s, 8*s, 32*s, 4, 20, 8, 32, null);
            //Right Leg
            g.drawImage(i, 8*s, 20*s, 12*s, 32*s, 8, 20, 4, 32, null);
            //Hat
            g.drawImage(i, 4*s, 0, 12*s, 8*s, 40, 8, 48, 16, null);
            //Back
            //Head
            g.drawImage(i, (sep_index+4*s), 0, (sep_index+12*s), 8*s, 24, 8, 32, 16, null);
            //Left Arm
            g.drawImage(i, (sep_index), 8*s, (sep_index+4*s), 20*s, 44, 20, 48, 32, null);
            //Right Arm
            g.drawImage(i, (sep_index+12*s), 8*s, (sep_index+16*s), 20*s, 48, 20, 44, 32, null);
            //Body
            g.drawImage(i, (sep_index+4*s), 8*s, (sep_index+12*s), 20*s, 32, 20, 40, 32, null);
            //Left Leg
            g.drawImage(i, (sep_index+4*s), 20*s, (sep_index+8*s), 32*s, 16, 20, 12, 32, null);
            //Right Leg
            g.drawImage(i, (sep_index+8*s), 20*s, (sep_index+12*s), 32*s, 12, 20, 16, 32, null);
            //Hat
            g.drawImage(i, (sep_index+4*s), 0, (sep_index+12*s), 8*s, 56, 8, 64, 16, null);
        } else if (h == 64) { // 64*64 format
            //FRONT
            //Head
            g.drawImage(i, 4*s, 0, 12*s, 8*s, 8, 8, 16, 16, null);
            //Left Arm
            g.drawImage(i, 0, 8*s, 4*s, 20*s, 44, 20, 48, 32, null);
            //Right Arm
            g.drawImage(i, 12*s, 8*s, 16*s, 20*s, 36, 52, 40, 64, null);
            //Body
            g.drawImage(i, 4*s, 8*s, 12*s, 20*s, 20, 20, 28, 32, null);
            //Left Leg
            g.drawImage(i, 4*s, 20*s, 8*s, 32*s, 4, 20, 8, 32, null);
            //Right Leg
            g.drawImage(i, 8*s, 20*s, 12*s, 32*s, 20, 52, 24, 64, null);
            //Hat
            g.drawImage(i, 4*s, 0, 12*s, 8*s, 40, 8, 48, 16, null);
            //BACK
            //Head
            g.drawImage(i, (sep_index+4*s), 0, (sep_index+12*s), 8*s, 24, 8, 32, 16, null);
            //Left Arm
            g.drawImage(i, (sep_index), 8*s, (sep_index+4*s), 20*s, 44, 52, 48, 64, null);
            //Right Arm
            g.drawImage(i, (sep_index+12*s), 8*s, (sep_index+16*s), 20*s, 52, 20, 56, 32, null);
            //Body
            g.drawImage(i, (sep_index+4*s), 8*s, (sep_index+12*s), 20*s, 32, 20, 40, 32, null);
            //Left Leg
            g.drawImage(i, (sep_index+4*s), 20*s, (sep_index+8*s), 32*s, 28, 52, 32, 64, null);
            //Right Leg
            g.drawImage(i, (sep_index+8*s), 20*s, (sep_index+12*s), 32*s, 12, 20, 16, 32, null);
            //Hat
            g.drawImage(i, (sep_index+4*s), 0, (sep_index+12*s), 8*s, 56, 8, 64, 16, null);
        }
        return bImg;
    }
    public static Image generateComboSkin(URL u, int s, int sep) throws IOException {
        return generateComboSkin(ImageIO.read(u), s, sep);
    }
    private static Image generateComboCape(Image i, int s, int sep) {
        BufferedImage bImg = new BufferedImage((10*2+sep)*s, 16*s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bImg.createGraphics();
        int sep_index = (sep+10)*s;
        g.drawImage(i, 0, 0, 10*s, 16*s, 1, 1, 11, 17, null);
        g.drawImage(i, (sep_index), 0, (sep_index+10*s), 16*s, 12, 1, 22, 17, null);
        return bImg;
    }
    public static Image generateComboCape(URL u, int s, int sep) throws IOException {
        return generateComboCape(ImageIO.read(u), s, sep);
    }
}
