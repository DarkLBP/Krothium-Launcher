package kml;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;


/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class TexturePreview {

    /**
     * Generates the front preview of the skin
     * @param skin The skin image
     * @param cape The cape image
     * @param slim If the format is slim
     * @return The generated preview image
     */
    public static Image generateFront(Image skin, Image cape, boolean slim) {
        double h = skin.getHeight();
        PixelReader pr = skin.getPixelReader();
        WritableImage wi = new WritableImage(slim && h == 64 ? 14 : 16, 32);
        PixelWriter pw = wi.getPixelWriter();
        if (h == 64) { // New format
            //MAIN ZONES
            //Head
            pw.setPixels(slim ? 3 : 4, 0, 8, 8, pr, 8, 8);
            //Right Arm
            pw.setPixels(0, 8, slim ? 3 : 4, 12, pr, 44, 20);
            //Left Arm
            pw.setPixels(slim ? 11 : 12, 8, slim ? 3 : 4, 12, pr, 36, 52);
            //Body
            pw.setPixels(slim ? 3 : 4, 8, 8, 12, pr, 20, 20);
            //Right Leg
            pw.setPixels(slim ? 3 : 4, 20, 4, 12, pr, 4, 20);
            //Left Leg
            pw.setPixels(slim ? 7 : 8, 20, 4, 12, pr, 20, 52);

            //EXTRA ZONES
            //Head
            renderLayer(slim ? 3 : 4, 0, 8, 8, pr, pw, 40, 8);
            //Right Arm
            renderLayer(0, 8, slim ? 3 : 4, 12, pr, pw, 44, 36);
            //Left Arm
            renderLayer(slim ? 11 : 12, 8, slim ? 3 : 4, 12, pr, pw, 52, 52);
            //Body
            renderLayer(slim ? 3 : 4, 8, 8, 12, pr, pw, 20, 36);
            //Right Leg
            renderLayer(slim ? 3 : 4, 20, 4, 12, pr, pw, 4, 36);
            //Left Leg
            renderLayer(slim ? 7 : 8, 20, 4, 12, pr, pw, 4, 52);
        } else if (h == 32) {
            //Head
            pw.setPixels(4, 0, 8, 8, pr, 8, 8);
            //Hat
            renderLayer(4, 0, 8, 8, pr, pw, 40, 8);
            //Right Arm
            pw.setPixels(0, 8, 4, 12, pr, 44, 20);
            //Left Arm
            renderLayerInverse(12, 8, 4, 12, pr, pw,44, 20);
            //Body
            pw.setPixels(4, 8, 8, 12, pr, 20, 20);
            //Right Leg
            pw.setPixels(4, 20, 4, 12, pr, 4, 20);
            //Left Leg
            renderLayerInverse(8, 20, 4, 12, pr, pw, 4, 20);
        }

        if (cape != null) {
            PixelReader pr2 = cape.getPixelReader();
            pw.setPixels(slim && h == 64 ? 2 : 3, 20, 1, 4, pr2, 12, 1);
            pw.setPixels(slim && h == 64 ? 11 : 12, 20, 1, 4, pr2, 21, 1);
        }
        return wi;
    }

    /**
     * Generates the back preview of the skin
     * @param skin The skin image
     * @param cape The cape image
     * @param slim If the format is slim
     * @return The generated preview image
     */
    public static Image generateBack(Image skin, Image cape, boolean slim) {
        double h = skin.getHeight();
        PixelReader pr = skin.getPixelReader();
        WritableImage wi = new WritableImage(slim && h == 64 ? 14 : 16, 32);
        PixelWriter pw = wi.getPixelWriter();
        if (h == 64) { // New format
            //MAIN ZONES
            //Head
            pw.setPixels(slim ? 3 : 4, 0, 8, 8, pr, 24, 8);
            //Right Arm
            pw.setPixels(slim ? 11 : 12, 8, slim ? 3 : 4, 12, pr, slim ? 51 : 52, 20);
            //Left Arm
            pw.setPixels(0, 8, slim ? 3 : 4, 12, pr, slim ? 43 : 44, 52);
            //Body
            pw.setPixels(slim ? 3 : 4, 8, 8, 12, pr, 32, 20);
            //Right Leg
            pw.setPixels(slim ? 7 : 8, 20, 4, 12, pr, 12, 20);
            //Left Leg
            pw.setPixels(slim ? 3 : 4, 20, 4, 12, pr, 28, 52);

            //EXTRA ZONES
            //Head
            renderLayer(slim ? 3 : 4, 0, 8, 8, pr, pw, 56, 8);
            //Right Arm
            renderLayer(slim ? 11 : 12, 8, slim ? 3 : 4, 12, pr, pw, slim ? 51 : 52, 36);
            //Left Arm
            renderLayer(0, 8, slim ? 3 : 4, 12, pr, pw, slim ? 59 : 60, 52);
            //Body
            renderLayer(slim ? 3 : 4, 8, 8, 12, pr, pw, 32, 36);
            //Right Leg
            renderLayer(slim ? 7 : 8, 20, 4, 12, pr, pw, 12, 36);
            //Left Leg
            renderLayer(slim ? 3 : 4, 20, 4, 12, pr, pw, 12, 52);
        } else if (h == 32) { //Legacy format
            //Head
            pw.setPixels(4, 0, 8, 8, pr, 24, 8);
            //Hat
            renderLayer(4, 0, 8, 8, pr, pw, 56, 8);
            //Right Arm
            pw.setPixels(12, 8, 4, 12, pr, 52, 20);
            //Left Arm
            renderLayerInverse(0, 8, 4, 12, pr, pw,52, 20);
            //Body
            pw.setPixels(4, 8, 8, 12, pr, 32, 20);
            //Right Leg
            pw.setPixels(8, 20, 4, 12, pr, 12, 20);
            //Left Leg
            renderLayerInverse(4, 20, 4, 12, pr, pw, 12, 20);
        }

        if (cape != null) {
            PixelReader pr2 = cape.getPixelReader();
            pw.setPixels(slim && h == 64 ? 2 : 3, 8, 10, 16, pr2, 1, 1);
        }

        return wi;
    }

    /**
     * Generates the left preview of the skin
     * @param skin The skin image
     * @param cape The cape image
     * @return The generated preview image
     */
    public static Image generateLeft(Image skin, Image cape) {
        double h = skin.getHeight();
        PixelReader pr = skin.getPixelReader();
        WritableImage wi = new WritableImage(8, 32);
        PixelWriter pw = wi.getPixelWriter();
        if (h == 64) { // New format
            //MAIN ZONES
            //Head
            pw.setPixels(0, 0, 8, 8, pr, 16, 8);
            //Left Arm
            pw.setPixels(2, 8, 4, 12, pr, 40, 52);
            //Left Leg
            pw.setPixels(2, 20, 4, 12, pr, 24, 52);

            //EXTRA ZONES
            //Head
            renderLayer(0, 0, 8, 8, pr, pw, 48, 8);
            //Left Arm
            renderLayer(2, 8, 4, 12, pr, pw, 56, 52);
            //Left Leg
            renderLayer(2, 20, 4, 12, pr, pw, 8, 52);
        } else if (h == 32) { //Legacy format
            //Head
            pw.setPixels(0, 0, 8, 8, pr, 16, 8);
            //Hat
            renderLayer(0, 0, 8, 8, pr, pw, 48, 8);
            //Left Arm
            renderLayerInverse(2, 8, 4, 12, pr, pw, 40, 20);
            //Left Leg
            renderLayerInverse(2, 20, 4, 12, pr, pw, 0, 20);
        }

        if (cape != null) {
            PixelReader pr2 = cape.getPixelReader();
            pw.setPixels(6, 8, 1, 16, pr2, 0, 1);
        }

        return wi;
    }

    /**
     * Generates the right preview of the skin
     * @param skin The skin image
     * @param cape The cape image
     * @return The generated preview image
     */
    public static Image generateRight(Image skin, Image cape) {
        double h = skin.getHeight();
        PixelReader pr = skin.getPixelReader();
        WritableImage wi = new WritableImage(8, 32);
        PixelWriter pw = wi.getPixelWriter();
        if (h == 64) { // New format
            //MAIN ZONES
            //Head
            pw.setPixels(0, 0, 8, 8, pr, 0, 8);
            //Right Arm
            pw.setPixels(2, 8, 4, 12, pr, 40, 20);
            //Right Leg
            pw.setPixels(2, 20, 4, 12, pr, 0, 20);

            //EXTRA ZONES
            //Head
            renderLayer(0, 0, 8, 8, pr, pw, 32, 8);
            //Right Arm
            renderLayer(2, 8, 4, 12, pr, pw, 40, 36);
            //Right Leg
            renderLayer(2, 20, 4, 12, pr, pw, 0, 36);
        } else if (h == 32) { //Legacy format
            //Head
            pw.setPixels(0, 0, 8, 8, pr, 0, 8);
            //Hat
            renderLayer(0, 0, 8, 8, pr, pw, 32, 8);
            //Right Arm
            pw.setPixels(2, 8, 4, 12, pr,40, 20);
            //Right Leg
            pw.setPixels(2, 20, 4, 12, pr, 0, 20);
        }

        if (cape != null) {
            PixelReader pr2 = cape.getPixelReader();
            pw.setPixels(1, 8, 1, 16, pr2, 11, 1);
        }

        return wi;
    }

    /**
     * Renders a layer. Transparent colors and background color is ignored.
     * @param dstx Destination X
     * @param dsty Destination Y
     * @param w Width of the chunk
     * @param h Height of the chunk
     * @param pr The image pixel reader
     * @param pw The output pixel writer
     * @param srcx The X coordinate from the source skin
     * @param srcy The Y coordinate from the source skin
     */
    private static void renderLayer(int dstx, int dsty, int w, int h, PixelReader pr, PixelWriter pw, int srcx, int srcy) {
        Color background = pr.getColor(0, 0);
        int x_origin = dstx;
        for (int y = srcy; y < srcy + h; y++){
            for (int x = srcx; x < srcx + w; x++) {
                Color c = pr.getColor(x, y);
                if (c.getOpacity() == 1 && !c.equals(background)) {
                    pw.setColor(dstx, dsty, c);
                }
                dstx++;
            }
            dsty++;
            dstx = x_origin;
        }
    }

    /**
     * Renders a layer inversed
     * @param dstx Destination X
     * @param dsty Destination Y
     * @param w Width of the chunk
     * @param h Height of the chunk
     * @param pr The image pixel reader
     * @param pw The output pixel writer
     * @param srcx The X coordinate from the source skin
     * @param srcy The Y coordinate from the source skin
     */
    private static void renderLayerInverse(int dstx, int dsty, int w, int h, PixelReader pr, PixelWriter pw, int srcx, int srcy) {
        int x_origin = dstx;
        for (int y = srcy; y < srcy + h; y++){
            for (int x = srcx + w - 1; x >= srcx; x--) {
                Color c = pr.getColor(x, y);
                pw.setColor(dstx, dsty, c);
                dstx++;
            }
            dsty++;
            dstx = x_origin;
        }
    }
}
