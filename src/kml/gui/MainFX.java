package kml.gui;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

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
        header.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(header, Priority.NEVER);
        super.getChildren().addAll(header);

        //Quick action row
        StackPane actionRow = new StackPane();
        Label languages = new Label("Languages");
        languages.setCursor(Cursor.HAND);
        languages.setId("languages");
        languages.setMinHeight(35);
        languages.setMaxHeight(35);
        StackPane.setAlignment(languages, Pos.TOP_LEFT);
        Label switchAccount = new Label("Switch Account");
        switchAccount.setCursor(Cursor.HAND);
        switchAccount.setId("switchAccount");
        switchAccount.setMinHeight(35);
        switchAccount.setMaxHeight(35);
        StackPane.setAlignment(switchAccount, Pos.TOP_RIGHT);
        actionRow.getChildren().addAll(languages, switchAccount);


        //Logo row
        ImageView logoRow = new ImageView();
        logoRow.setImage(new Image("/kml/gui/textures/logo.png"));


        header.getChildren().addAll(actionRow, logoRow); //Add everything to header


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
