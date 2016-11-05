package kml;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import kml.objects.Browser;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public class WebBrowser extends Application {
    private Scene scene;
    @Override
    public void start(Stage stage) {
        stage.setTitle("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME);
        String width = WebLauncher.kernel.getProperty("width");
        String height = WebLauncher.kernel.getProperty("height");
        double w = 1050;
        double h = 600;
        if (width != null && height != null){
            try {
                w = Double.parseDouble(width);
                h = Double.parseDouble(height);
                if (w <= 0 || h <= 0){
                    w = 1050;
                    h = 600;
                }
            } catch (Exception ex){
                w = 1050;
                h = 600;
            }
        }
        scene = new Scene(new Browser(), w, h, Color.web("#666970"));
        stage.getIcons().add(new Image(WebBrowser.class.getResourceAsStream("/kml/gui/textures/icon.png")));
        stage.setScene(scene);
        stage.show();
    }
    @Override
    public void stop(){
        WebLauncher.kernel.setProperty("width", String.valueOf(scene.getWidth()));
        WebLauncher.kernel.setProperty("height", String.valueOf(scene.getHeight()));
        WebLauncher.kernel.exitSafely();
    }
}
