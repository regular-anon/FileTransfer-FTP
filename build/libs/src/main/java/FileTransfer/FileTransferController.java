package FileTransfer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FileTransferController implements Initializable {
    public Label pwdLabel;
    public Button uploadButton;
    public Button downloadButton;
    @FXML
    private ListView list;

    public static FileTransferController instance;

    //@Override
    public void initialize(URL location, ResourceBundle resourceBundle)
    {
        System.out.println("FileTransferController class has been initialized!!!");
        instance = this;
        list.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent click) {

                if (click.getClickCount() == 2) {
                    //Use ListView's getSelected Item
                    //list.getSelectionModel().getSelectedItem();
                    //String str = (String)list.getSelectionModel().getSelectedItem();
                    String str;
                    Object obj = list.getSelectionModel().getSelectedItem();
//                    if(obj instanceof String)
//                        str = (String)obj;
//                    else
                    //{
                        HBox box = (HBox)obj;
                        str = ((Label)box.getChildren().get(2)).getText();
                    //}
                    if(str == null || str == "")
                        return;
                    try {
                        if(str.equals(".."))
                            FileStructure.cd("..");
//                        else if(str.contains("."))
//                            throw new Exception("Cannot change directory to file.");
                        else
                            FileStructure.cd(str);
                    } catch (Exception e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Warning");
                        alert.setHeaderText("Cannot change the directory at the moment.");
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                        return;
                    }
                    showDirectory(FileStructure.currentDirectory);
                }
            }
        });
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//
//        }
    }

    public void showDirectory(DirectoryInstance dir)
    {
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                System.out.println("Showing different directory in the FileTransfer main window");
                list.getItems().clear();

                HBox box = new HBox();
                ImageView imgView = new ImageView(new Image("Photos/folder.png"));
                Label l = new Label("..");
                imgView.setPreserveRatio(true);
                imgView.setFitHeight(15);
                box.getChildren().add(imgView);
                box.getChildren().add(new Label(" "));
                box.getChildren().add(l);
                list.getItems().add(box);

                //HBox[] content = new HBox[dir.getContents().size()];

                for(int i = 0;i < dir.getContents().size();++i)
                {
                    //content[i] = createHBox(dir.getContents().get(i));
                    if(dir.getContents().get(i).getName().equals("..") || dir.getContents().get(i).getName().equals("."))
                        continue;
                    list.getItems().add(createHBox(dir.getContents().get(i)));
                }
                pwdLabel.setText("Current Path: " + FileStructure.currentDirectory.getPath());
            }
        });
    }
    private HBox createHBox(FileStructureInstance fsi)
    {
        HBox box = new HBox();
        Label l = new Label(fsi.getName());
        ImageView imgView;
        if(fsi instanceof DirectoryInstance)
        {
            imgView = new ImageView(new Image("Photos/folder.png"));
        }
        else
        {
            imgView = new ImageView(new Image("Photos/file.png"));
        }
        imgView.setPreserveRatio(true);
        imgView.setFitHeight(15);
        box.getChildren().add(imgView);
        box.getChildren().add(new Label(" "));
        box.getChildren().add(l);

        return box;
    }

    public void upload(ActionEvent actionEvent) {
        //Under construction
    }

    public void download(ActionEvent actionEvent)
    {
        HBox box = (HBox) list.getSelectionModel().getSelectedItem();
        String name = ((Label)box.getChildren().get(2)).getText();

        FileStructureInstance fsi = FileStructure.searchInContentsOfDirectory(FileStructure.currentDirectory, name);
        if(fsi == null)
        {
            System.out.println("Unexpected errpr!");
            return;
        }

        if(fsi instanceof FileInstance)
        {
            try {
                Client.download((FileInstance)fsi, fsi.getName());
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setHeaderText("Could not download file");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    public void modifyDownloadDirectory(ActionEvent actionEvent)
    {
        FileDialog fd = new FileDialog(new JFrame(), "Choose a directory", FileDialog.LOAD);
        fd.setDirectory("C:\\");
        //fd.setFile("");
        fd.setVisible(true);
        File[] files = fd.getFiles();
        if(files[0].isDirectory())
        {
            Client.setDownloadsPath(files[0].getPath());
        }
    }
}