package kml;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import kml.gui.MainFX;

/**
 * Created by darkl on 17/03/2017.
 */
public class JavaFXStart extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainFX main = new MainFX();
        Scene scene = new Scene(main, 950, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME);
        primaryStage.getIcons().add(new Image("/kml/gui/textures/icon.png"));

        primaryStage.show();

    }
}
