package kml;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import kml.gui.MainFX;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class JavaFXStart extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/kml/gui/fxml/Main.fxml"));
        Parent root = loader.load();

        primaryStage.getIcons().add(new Image("/kml/gui/textures/icon.png"));

        primaryStage.setTitle("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME);
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(true);
        primaryStage.setMaximized(false);
        primaryStage.show();

        MainFX mainFX = loader.getController();
        //mainFX.setKernel(new Kernel());

        /*Scene scene = new Scene(main, 950, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME);
        primaryStage.getIcons().add(new Image("/kml/gui/textures/icon.png"));

        primaryStage.show();*/
    }
}
