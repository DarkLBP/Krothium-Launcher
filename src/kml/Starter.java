package kml;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class Starter extends Application {

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        Utils.testNetwork();
        new Kernel(primaryStage, getHostServices());
    }
}
