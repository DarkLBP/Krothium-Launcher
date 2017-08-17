package kml;

import javafx.application.Application;
import javafx.stage.Stage;
import kml.handlers.BrowserHandler;

import java.net.URL;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class Starter extends Application {

    public static void main(String[] args) {
        URL.setURLStreamHandlerFactory(new BrowserHandler());
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        Utils.testNetwork();
        new Kernel(primaryStage, getHostServices());
    }
}
