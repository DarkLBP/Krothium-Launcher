package kml.gui;

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

    public void initialize() {
        outputList.setItems(FXCollections.observableArrayList());
        outputList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void pushString(String str) {
        outputList.getItems().add(str);
        outputList.scrollTo(outputList.getItems().size() - 1);
    }

    @FXML
    public void copyClipboard(KeyEvent e) {
        //Copy to clipboard selected rows
        if (e.getCode() == KeyCode.C && e.isControlDown()) {
            copySelected();
        }
    }

    @FXML
    public void copySelected() {
        if (outputList.getSelectionModel().getSelectedItems().size() > 0) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            StringBuilder b = new StringBuilder();
            for (String s : outputList.getSelectionModel().getSelectedItems()) {
                b.append(s).append("\n");
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
