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
    private Browser browser;
    @Override
    public void start(Stage stage) {
        stage.setTitle("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME);
        browser = new Browser();
        scene = new Scene(browser, 1100, 850, Color.web("#666970"));
        stage.getIcons().add(new Image(WebBrowser.class.getResourceAsStream("/icon.png")));
        stage.setScene(scene);
        stage.show();
    }
}
