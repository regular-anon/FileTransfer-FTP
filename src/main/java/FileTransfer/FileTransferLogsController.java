package FileTransfer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

import java.net.URL;
import java.util.ResourceBundle;

public class FileTransferLogsController implements Initializable {
    @FXML
    public TextField textField;
    @FXML
    private TextArea textArea;

    private static FileTransferLogsController instance;

    public FileTransferLogsController() {
        instance = this;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        String loremSampleText = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions oLorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.f Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.LoremLorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum. Ipsum.";
//        textArea.setMaxHeight(Region.USE_PREF_SIZE);
//        anchorPane.prefHeightProperty().bind(textArea.heightProperty());
//        scrollPane.vvalueProperty().bind(anchorPane.heightProperty());
    }

    public static void addTextToLogs(char c) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (instance.textArea.getText().length() > 1_000_000) {
                    instance.textArea.setText(instance.textArea.getText().substring(1));
                    instance.textArea.appendText(c + "");
                }
                else
                    instance.textArea.appendText(c + "");
//                instance.textArea.setPrefHeight(Region.USE_COMPUTED_SIZE);
//                instance.scrollPane.setVvalue(instance.scrollPane.getVmax());
//                instance.textArea.setScrollTop(Double.MAX_VALUE);
            }
        });
    }

    public void search(ActionEvent actionEvent) {
        boolean selected = false;
        String query = textField.getText();
        String labelText = textArea.getText();
        for(int i = 0;i < labelText.length();++i) {
            int j = 0;
            while(j < query.length() && (labelText.charAt(i) == query.charAt(j))) {
                i ++;
                j ++;
            }
            if(j == query.length()) {
                textArea.selectRange(i - j, i);
            }
        }
    }

    public void clear(ActionEvent actionEvent) {
        //Clear the logs from the window
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
//                textArea.setScrollTop(0);
                textArea.setText("");
//                textArea.setPrefHeight(Region.USE_COMPUTED_SIZE);
            }
        });
    }
}
