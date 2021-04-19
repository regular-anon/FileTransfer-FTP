package FileTransfer;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import java.net.URL;
import java.util.ResourceBundle;

public class FileTransferProcessesController implements Initializable
{
    public ListView transferList;
    public Button closeButton;
    private static int id = 1;

    public static FileTransferProcessesController instance;

    public void closeWindow(ActionEvent actionEvent)
    {
        UIController.hideStage("FileTransfer Processes");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        FileTransferProcessesController.instance = this;
//        UIController.addStage(processesStage);
//        processesStage.show();
    }

    public int addProcess(String fileName, float size)
    {
        HBox box = new HBox();
        Label idLabel = new Label("" + id + ") ");
        Label fileNameLabel = new Label(fileName);
        String[] s = {"B", "KB", "MB", "GB"};
        String sizeStr;
        int i = 0;
        while(size >= 1024 && i < s.length){
            i++;
            size /= 1024;
        }
        sizeStr = String.format("%.2f %s", size, s[i]);//size + " " + s[i];

        Label fileSizeLabel = new Label(" - " + sizeStr);

        //Platform.runLater
        box.getChildren().addAll(idLabel, fileNameLabel, fileSizeLabel);
        transferList.getItems().add(box);
        return id++;
    }

//    public void removeProcess(int id)
//    {
//        for(int i = 0;i < transferList.getItems().size();++i)
//        {
//            HBox box = ((HBox)(transferList.getItems().get(i)));
//            if(box.getChildren().get(0).equals(id + ") "))
//            {
//                //Platform.runLater
//                transferList.getItems().remove(box);
//                return;
//            }
//        }
//        return;
//    }
}
