package FileTransfer;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.Duration;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

//TODO: UI Themes, Log window


public class FileTransferController implements Initializable {
    public Label pwdLabel;
    public Button uploadButton;
    public Button downloadButton;
    public Button searchButton;
    public TextField searchField;
    public Label languageButtonLabel;
    public Menu menuPreferences;
    public ImageView dropImage;
    //    public Menu runningProcessesButton;
    @FXML
    private ListView list;
    private static boolean standby;
    private static UITask currentTask;
    public static FileTransferController instance;

    public static boolean onStandby()
    {
        return standby;
    }

    public static void enterStandbyMode(UITask task)
    {
        //Modify UI
        standby = true;
        currentTask = task;
        new Thread(task).start();
    }

    public static boolean equalsTask(UITask task)
    {
        return currentTask == task;
    }

    public static void exitStandbyMode()
    {
        //Modify UI
        standby = false;
        currentTask = null;
    }


    //@Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        dropImage.setOpacity(0);
//        imageViewProfile.imageProperty().unbind();
        menuPreferences.textProperty().unbind();
        instance = this;
        Image img = new Image("Photos/language.png");
        languageButtonLabel.setText("");
        ImageView imgView = new ImageView(img);
        imgView.setPreserveRatio(true);
        imgView.setFitHeight(20);
        languageButtonLabel.setGraphic(imgView);
        languageButtonLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                UIController.setVisible("FileTransfer Settings");
            }
        });
        list.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                if (click.getClickCount() == 2) {
                    String str;
                    Object obj = list.getSelectionModel().getSelectedItem();
                    HBox box = (HBox) obj;
                    str = ((Label) box.getChildren().get(2)).getText();
                    if (str == null || str == "")
                        return;
                    try {
                        UITask newTask;
                        if (str.equals("..")) {
                            newTask = new UITask() {
                                public void exec() throws Exception {
                                    System.out.println("Exec method of .. directory change.");
                                }

                                public void execFinal() throws Exception {
                                    System.out.println("Started execFinal on ..");
                                    FileStructure.cd("..");
                                    showDirectory(FileStructure.currentDirectory);
                                    System.out.println("Finished execFinal on ..");
                                }
                            };
                        } else {
                            newTask = new UITask() {
                                public void exec() throws Exception {
                                    System.out.println("Started exec on changing directory");
                                    DirectoryInstance nextDir = (DirectoryInstance) FileStructure.searchInContentsOfDirectory(FileStructure.currentDirectory, str);
                                    if (!nextDir.hasContent)
                                        FileStructure.assignContents(nextDir, Client.getFTPClient());
                                    System.out.println("Finished exec on changing directory");
                                }

                                public void execFinal() throws Exception {
                                    System.out.println("Started execFinal on changing directory");
                                    FileStructure.cd(FileStructure.currentDirectory.getPath() + "/" + str);
                                    showDirectory(FileStructure.currentDirectory);
                                    System.out.println("Finished execFinal on changing directory");
                                }
                            };
                        }
                        enterStandbyMode(newTask);
                    } catch (Exception e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Warning");
                        alert.setHeaderText("Cannot change the directory at the moment.");
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                        return;
                    }
                }
            }
        });
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        list.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (event.getGestureSource() != list && event.getDragboard().hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);

//                    dropImage.setOpacity(1);
                    FadeTransition ft = new FadeTransition();
                    ft.setNode(dropImage);
                    ft.setDuration(Duration.millis(100));
                    ft.setFromValue(dropImage.getOpacity());
                    ft.setToValue(1.0);
//                    ft.setCycleCount(6);
                    ft.setAutoReverse(false);

                    ft.play();
                }
                //Modify ui
                event.consume();
            }
        });

        list.setOnDragExited(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
//                dropImage.setOpacity(0);
                FadeTransition ft = new FadeTransition();
                ft.setNode(dropImage);
                ft.setDuration(Duration.millis(100));
                ft.setFromValue(dropImage.getOpacity());
                ft.setToValue(0.0);
//                ft.setCycleCount(6);
                ft.setAutoReverse(false);

                ft.play();
            }
        });

        list.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (event.getGestureSource() != list && event.getDragboard().hasFiles()) {
//                    dropImage.setOpacity(0);
                    FadeTransition ft = new FadeTransition();
                    ft.setNode(dropImage);
                    ft.setDuration(Duration.millis(100));
                    ft.setFromValue(dropImage.getOpacity());
                    ft.setToValue(0.0);
//                    ft.setCycleCount(6);
                    ft.setAutoReverse(false);

                    ft.play();
                    for (File f : event.getDragboard().getFiles()) {
                        try {
                            Client.upload(f);
                        } catch (IOException e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error Dialog");
                            alert.setHeaderText("Could not download file");
                            alert.setContentText(e.getMessage());
                            alert.showAndWait();
                            e.printStackTrace();
                        }
                    }
                }
            }
        });


        //Set context menus for file deletion
//        list.setCellFactory(new Callback<ListView, ListCell>() {
//            @Override
//            public ListCell call(ListView param) {
//                return null;
//            }
//        });
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

    public void uploadPress(ActionEvent actionEvent) {
        FileDialog d = new FileDialog((java.awt.Frame) null);
        d.setVisible(true);
        File[] files;// = {new File("")};
        files = d.getFiles();
        for (int i = 0; i < files.length; ++i) {
            try {
                Client.upload(files[i]);
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setHeaderText("Could not download file");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }

    public void downloadPress(ActionEvent actionEvent)
    {
        HBox box = (HBox) list.getSelectionModel().getSelectedItem();
        String name;
        try {
            name = ((Label)box.getChildren().get(2)).getText();
        }
        catch(Exception e)
        {
            return;
        }

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
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            downloadDirectory((DirectoryInstance)fsi, fsi.getName());
        }
    }

    private void downloadDirectory(DirectoryInstance dir, String directory)
    {
        File d = new File(Client.getDownloadsPath() + "/" + directory);
        if(!d.exists())
            d.mkdir();
        if(!dir.hasContent) {
            try {
                FileStructure.assignContents(dir, Client.getFTPClient());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for(int i = 0;i < dir.getContents().size();++i)
        {
            FileStructureInstance fsi = dir.getContents().get(i);
            if(fsi instanceof DirectoryInstance)
                downloadDirectory((DirectoryInstance)fsi, directory + "/" + fsi.getName());
            else
            {
                try {
                    Client.download((FileInstance)fsi, directory + "/" + fsi.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void searchButtonClicked(ActionEvent actionEvent)
    {
        try {
            FileStructure.cd(searchField.getText());
            showDirectory(FileStructure.currentDirectory);

//            searchField.setText("");
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot find directory!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public void modifyDownloadsDirectory(ActionEvent actionEvent) {
        System.out.println("Pressed directory button!");
    }

    public void showProcesses(ActionEvent actionEvent) {
        System.out.println("Showing FileTransfer Processes");
        UIController.setVisible("FileTransfer Processes");
    }

    public void languageButtonClicked(MouseEvent mouseEvent) {
        System.out.println("Language button clicked from label");
    }
}