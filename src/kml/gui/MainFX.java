package kml.gui;

import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Created by darkl on 18/03/2017.
 */
public class MainFX extends VBox {
    public MainFX() {
        initialize();
    }

    private void initialize() {

        //Import stylesheet
        String style = getClass().getResource("/kml/gui/Main.css").toExternalForm();
        super.getStylesheets().addAll(style);

        //Header
        VBox header = new VBox();
        header.getStyleClass().add("darkBackground");
        header.setMinHeight(180);
        VBox.setVgrow(header, Priority.NEVER);
        super.getChildren().addAll(header);


        //Logo row
        HBox logoRow = new HBox();
        ImageView logo = new ImageView();
        logo.setImage(new Image("/kml/gui/textures/logo.png"));
        logoRow.getChildren().add(logo);
        logoRow.setAlignment(Pos.CENTER);
        header.getChildren().add(logoRow);


        //Content
        ScrollPane content = new ScrollPane();
        content.getStyleClass().add("lightBackground");
        VBox.setVgrow(content, Priority.ALWAYS);
        content.setMinHeight(0);
        super.getChildren().addAll(content);


        //Footer
        VBox footer = new VBox();
        footer.getStyleClass().add("darkBackground");
        footer.setMinHeight(150);
        VBox.setVgrow(footer, Priority.NEVER);
        super.getChildren().addAll(footer);


        super.setPrefSize(950, 700);
    }
}
