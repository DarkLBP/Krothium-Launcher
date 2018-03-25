package kml.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


public class OutputFX {
    @FXML
    private ListView<String> outputList;

    public final void initialize() {
        outputList.setItems(FXCollections.observableArrayList());
        outputList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public final void pushString(String str) {
        Platform.runLater(() -> {
            outputList.getItems().add(str);
            outputList.scrollTo(outputList.getItems().size() - 1);
        });
    }

    @FXML
    public final void copyClipboard(KeyEvent e) {
        //Copy to clipboard selected rows
        if (e.getCode() == KeyCode.C && e.isControlDown()) {
            copySelected();
        }
    }

    @FXML
    public final void copySelected() {
        if (!outputList.getSelectionModel().getSelectedItems().isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            StringBuilder b = new StringBuilder();
            for (String s : outputList.getSelectionModel().getSelectedItems()) {
                b.append(s).append('\n');
            }
            content.putString(b.toString());
            clipboard.setContent(content);
        }
    }

    @FXML
    private void clearAll() {
        outputList.getItems().clear();
    }
}
