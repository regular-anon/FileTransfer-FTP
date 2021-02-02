package FileTransfer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FileTransferLoginController  implements Initializable {
    public TextField hostField;
    public TextField usernameField;
    public PasswordField passwordField;
    public Button connectButton;
    public Button clearButton;

    public static Stage loginStage;
    public static FileTransferLoginController instance;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        instance = this;
    }

    public void setStage(Stage s)
    {
        loginStage = s;
    }

    public void tryConnecting(ActionEvent actionEvent)
    {
        String host = hostField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        Client.setCredentials(host, 21, username, password);
        //Client.open();

        try {
            Client.connect();
        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not connect to server!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();

            return;
        }
        try {
            Client.login();
        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("Credentials are probably not valid!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();

            return;
        }

        try {
            FileStructure.init2();
            FileTransferController.instance.showDirectory(FileStructure.rootDirectory);

            FileTransferManager.init();
        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("Could not init the file structure.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();

            return;
        }
        loginStage.setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {

            }
        });
        loginStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {

            }
        });
        UIController.removeAllStages();
        UIController.addStage(Main.mainStage);
        UIController.init();
    }

    public void clear(ActionEvent actionEvent)
    {
        hostField.setText("");
        usernameField.setText("");
        passwordField.setText("");
    }
}
