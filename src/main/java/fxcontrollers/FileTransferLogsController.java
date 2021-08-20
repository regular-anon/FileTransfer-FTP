package fxcontrollers;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FileTransferLogsController {
    
    public TextField textField;
    public Button searchButton;
    public Button clearButton;
    public TextArea textArea;
    public static Stage stage;
    public static FileTransferLogsController instance;

//    public static void setStage(Stage stage) {
//        FileTransferLogsController.stage = stage;
//    }

    public FileTransferLogsController(Stage s) {
        instance = this;
        stage = s;

        textField = (TextField) stage.getScene().lookup("#textField");
        searchButton = (Button) stage.getScene().lookup("#searchButton");
        clearButton = (Button) stage.getScene().lookup("#clearButton");
        textArea = (TextArea) stage.getScene().lookup("#textArea");

        searchButton.setOnAction(event -> {
            search();
        });

        clearButton.setOnAction(event -> {
            textArea.setText("");
        });
    }

    public void addChar(char b) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                textArea.setText(textArea.getText() + b);
            }
        });
    }

    public void search() {
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
    }
}
