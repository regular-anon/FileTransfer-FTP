package FileTransfer;

import javafx.beans.binding.Binding;
import javafx.beans.binding.FloatBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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

    public int addProcess(FileTransferProcess fp)
    {
        HBox box = new HBox();
        Label idLabel = new Label("" + id + ") ");
        Label fileNameLabel = new Label(fp.getFile().getName());
        fileNameLabel.setMaxWidth(150);
        Label fileSizeLabel = new Label(String.valueOf(FileStructure.longToSizeString(fp.getRemoteFileSize())));
        ProgressBar bar = new ProgressBar();
        bar.widthProperty().add(50);
        fp.setProgressBar(bar);
        MenuItem mi = new MenuItem(fileNameLabel.getText());
        ContextMenu menu = new ContextMenu(mi);
        box.setSpacing(10.0);
        box.getChildren().addAll(idLabel, fileNameLabel, fileSizeLabel, bar);

        fileNameLabel.setOnMouseClicked(event -> {
            System.out.println("Box clicked...");
            menu.show(box.getScene().getWindow(), event.getScreenX(), event.getScreenY());
        });

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
