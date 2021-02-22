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

    public UITask task;
    public boolean onStandby;

    public void enterStandbyMode(UITask task)
    {
        onStandby = true;
        this.task = task;
        new Thread(task).start();
    }

    public void cancelUITask()
    {
        this.task = null;
        onStandby = false;
    }

    public static FileTransferLoginController instance;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        instance = this;

        //Temporary code
        hostField.setText("192.168.100.254");
        usernameField.setText("admin");
        passwordField.setText("admin");

    }

    public void tryConnecting(ActionEvent actionEvent)
    {
        String host = hostField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        Client.setCredentials(host, 21, username, password);

        UITask connectTask = new UITask()
        {
            public void exec() throws Exception {
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
                System.out.println("Controller: Connected to client");
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
                System.out.println("Controller: Logged in");
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
                System.out.println("Controller: Initiated FileStructure");
            }

            public void execFinal() throws Exception {
                UIController.hideStage("FileTransfer Login");
                UIController.setVisible("FileTransfer");

                //Exit standby mode
                cancelUITask();


            }

            @Override public void run()
            {
                try {
                    exec();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                if(onStandby && task == this)
                {
                    try {
                        execFinal();
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    cancelUITask();
                }
                else
                    System.out.println("Cancelled task");
            }
        };
//        try {
//            Client.connect();
//        } catch (IOException e) {
//            e.printStackTrace();
//
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("Error");
//            alert.setHeaderText("Could not connect to server!");
//            alert.setContentText(e.getMessage());
//            alert.showAndWait();
//
//            return;
//        }
//        try {
//            Client.login();
//        } catch (IOException e) {
//            e.printStackTrace();
//
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("Error Dialog");
//            alert.setHeaderText("Credentials are probably not valid!");
//            alert.setContentText(e.getMessage());
//            alert.showAndWait();
//
//            return;
//        }
//        try {
//            FileStructure.init2();
//            FileTransferController.instance.showDirectory(FileStructure.rootDirectory);
//
//            FileTransferManager.init();
//        } catch (IOException e) {
//            e.printStackTrace();
//
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("Error Dialog");
//            alert.setHeaderText("Could not init the file structure.");
//            alert.setContentText(e.getMessage());
//            alert.showAndWait();
//
//            return;
//        }

//        UIController.hideStage("FileTransfer Login");
//        UIController.setVisible("FileTransfer");

        enterStandbyMode(connectTask);
    }

    public void clear(ActionEvent actionEvent)
    {
        hostField.setText("");
        usernameField.setText("");
        passwordField.setText("");
    }
}