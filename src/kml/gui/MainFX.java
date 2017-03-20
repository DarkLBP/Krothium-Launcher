package kml.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 * Created by darkl on 18/03/2017.
 */
public class MainFX {

    /**
     * HEADER
     */
    @FXML
    public Label languages;
    @FXML
    public Label switchAccount;

    /**
     * FOOTER
     */
    @FXML
    public Label playButton;
    @FXML
    public Label profileButton;
    @FXML
    public ProgressBar progress;

    public void initialize()
    {
        /*
          Set progressbar width.
         */
        progress.setMaxWidth(Double.MAX_VALUE);
    }
}
