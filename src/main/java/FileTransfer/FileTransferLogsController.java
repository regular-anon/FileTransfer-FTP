package FileTransfer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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
    public void initialize(URL location, ResourceBundle resources) { }

    public static void addTextToLogs(char c) {
        Platform.runLater(() -> {
            if (instance.textArea.getText().length() > 1_000_000) {
                instance.textArea.setText(instance.textArea.getText().substring(1));
            }
            instance.textArea.appendText(c + "");
        });
    }

    public void search(ActionEvent actionEvent) {
        String query = textField.getText();
        String labelText = textArea.getText();
        int start = 0;
        if(query.equals(textArea.getSelectedText()))
            start = textArea.getSelection().getEnd();
        for(int i = 0;i < labelText.length();++i) {
            int j = 0;
            while(j < query.length() && (labelText.charAt((start + i) % labelText.length()) == query.charAt(j))) {
                i ++;
                j ++;
            }
            int index = (start + i) % labelText.length();
            if(j == query.length()) {
                textArea.selectRange(index - j, index);
                return ;
            }
        }
        System.out.println("Didn't find any matches!");
    }

    public void clear(ActionEvent actionEvent) {
        Platform.runLater(() -> textArea.setText(""));
    }

    public void onTextFieldAction(ActionEvent actionEvent) {
        search(actionEvent);
    }
}
