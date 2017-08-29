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

    public final void initialize() {
        this.outputList.setItems(FXCollections.observableArrayList());
        this.outputList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public final void pushString(String str) {
        this.outputList.getItems().add(str);
        this.outputList.scrollTo(this.outputList.getItems().size() - 1);
    }

    @FXML
    public final void copyClipboard(KeyEvent e) {
        //Copy to clipboard selected rows
        if (e.getCode() == KeyCode.C && e.isControlDown()) {
            this.copySelected();
        }
    }

    @FXML
    public final void copySelected() {
        if (!this.outputList.getSelectionModel().getSelectedItems().isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            StringBuilder b = new StringBuilder();
            for (String s : this.outputList.getSelectionModel().getSelectedItems()) {
                b.append(s).append('\n');
            }
            content.putString(b.toString());
            clipboard.setContent(content);
        }
    }

    @FXML
    private void clearAll() {
        this.outputList.getItems().clear();
    }
}
