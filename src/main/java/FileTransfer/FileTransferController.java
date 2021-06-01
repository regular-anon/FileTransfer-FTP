package FileTransfer;


import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
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
    private static boolean standby;
    private static UITask currentTask;
    public static FileTransferController instance;

    public TableView<FileStructureInstance> tableView;
    public TableColumn<FileStructureInstance, HBox> fileNameColumn;
    public TableColumn<FileStructureInstance, String> sizeColumn;
    public TableColumn<FileStructureInstance, String> lastModifiedColumn;

    private ContextMenu contextMenu = new ContextMenu();

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
        UIController.getStages().get("FileTransfer").getScene().setCursor(Cursor.WAIT);
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
        UIController.getStages().get("FileTransfer").getScene().setCursor(Cursor.DEFAULT);
    }


    //@Override
    public void initialize(URL location, ResourceBundle resourceBundle) {

        fileNameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<FileStructureInstance, HBox>, ObservableValue<HBox>>() {
            @Override
            public ObservableValue<HBox> call(TableColumn.CellDataFeatures<FileStructureInstance, HBox> param) {
                if(param.getValue().getSize() == -1)
                    return new ReadOnlyObjectWrapper(createParentDirectoryHBox());
                return new ReadOnlyObjectWrapper(createHBox(param.getValue()));
            }
        });

        sizeColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<FileStructureInstance, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<FileStructureInstance, String> param) {
                if(param.getValue().getSize() == -1)
                    return new ReadOnlyStringWrapper("---");
                return new ReadOnlyStringWrapper(FileStructure.longToSizeString(param.getValue().getSize()));
            }
        });

            lastModifiedColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<FileStructureInstance, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<FileStructureInstance, String> param) {
                if(param.getValue().getSize() == -1)
                    return new ReadOnlyStringWrapper("---");
                return new ReadOnlyStringWrapper(new Date(param.getValue().lastModified).toString());
            }
        });
        tableView.setOnMouseClicked(event -> {
                contextMenu.hide();
                contextMenu.getItems().clear();
            if(event.getClickCount() > 1) {
                //start task on changing directory here
                String path = FileStructure.currentDirectory.getPath() + "/" + tableView.getSelectionModel().getSelectedItem().getName();
                if(path == null || path == "")
                    return;
                try {
                    UITask newTask = new UITask() {
                        FileStructureInstance fsi;
                        @Override public void exec() throws Exception
                        {
                            fsi = FileStructure.search(path);
                            if(fsi instanceof DirectoryInstance) {
                                DirectoryInstance dir = (DirectoryInstance) fsi;
                                if(!dir.hasContent)
                                    FileStructure.assignContents(dir, Client.getFTPClient());
                            }
                        }

                        @Override public void execFinal() throws Exception
                        {
                            if(fsi instanceof FileInstance)
                                return;
                            DirectoryInstance dir = (DirectoryInstance) fsi;
                            FileStructure.cd(dir);
                            showDirectory(dir);
                        }
                    };

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
            else if(event.getButton() == MouseButton.SECONDARY) {
                MenuItem deleteItem = new MenuItem("Delete");
                MenuItem mkdirItem = new MenuItem("Create new directory");

                deleteItem.setOnAction(event2 -> {
                    for(int i = 0;i < tableView.getSelectionModel().getSelectedItems().size();++i) {
                        try {
                            FileStructureInstance fsi = tableView.getSelectionModel().getSelectedItems().get(i);
                            Client.deleteFile(fsi);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                mkdirItem.setOnAction(event2 -> {
                    TextField nameInput = new TextField();
                    Button create = new Button("Create Folder");
                    HBox box = new HBox();
                    box.getChildren().addAll(nameInput, create);
                    Scene scene = new Scene(box);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    create.setOnAction(event3 -> {
                        String name = nameInput.getText();
                        if(name.contains("/"))
                            return;
                        try {
                            Client.createFolder(name);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        stage.close();
                    });
                    stage.showAndWait();
                });
                    contextMenu.getItems().addAll(deleteItem, mkdirItem);
                    contextMenu.show(tableView, event.getScreenX() ,event.getScreenY());
            }
        });
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        contextMenu.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Detected mouse exit");
                contextMenu.hide();
            }
        });
        dropImage.setOpacity(0);
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
        tableView.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (event.getGestureSource() != tableView && event.getDragboard().hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                    FadeTransition ft = new FadeTransition();
                    ft.setNode(dropImage);
                    ft.setDuration(Duration.millis(100));
                    ft.setFromValue(dropImage.getOpacity());
                    ft.setToValue(1.0);
                    ft.setAutoReverse(false);

                    ft.play();
                }
                //Modify ui
                event.consume();
            }
        });

        tableView.setOnDragExited(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                FadeTransition ft = new FadeTransition();
                ft.setNode(dropImage);
                ft.setDuration(Duration.millis(100));
                ft.setFromValue(dropImage.getOpacity());
                ft.setToValue(0.0);
                ft.setAutoReverse(false);

                ft.play();
            }
        });
        tableView.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (event.getGestureSource() != tableView && event.getDragboard().hasFiles()) {
                    FadeTransition ft = new FadeTransition();
                    ft.setNode(dropImage);
                    ft.setDuration(Duration.millis(100));
                    ft.setFromValue(dropImage.getOpacity());
                    ft.setToValue(0.0);
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
    }

    public void showDirectory(DirectoryInstance dir)
    {
        Platform.runLater(() -> {
            tableView.getItems().clear();
            FileStructureInstance fsi = new FileStructureInstance("..");
            fsi.setSize(-1);
            tableView.getItems().add(fsi);
            for(int i = 0;i < dir.getContents().size();++i) {
                tableView.getItems().add(dir.getContents().get(i));
            }
            pwdLabel.setText("Current Path: " + FileStructure.currentDirectory.getPath());
        });
    }

    private Image folderImage = new Image("Photos/folder.png");
    private Image fileImage = new Image("Photos/file.png");
    private Image arrowImage = new Image("Photos/up_arrow.png");

    private HBox createHBox(FileStructureInstance fsi)
    {
        HBox box = new HBox();
        Label l = new Label(fsi.getName());
        ImageView imgView;
        if(fsi instanceof DirectoryInstance)
        {
            imgView = new ImageView(folderImage);
        }
        else
        {
            imgView = new ImageView(fileImage);
        }
        imgView.setPreserveRatio(true);
        imgView.setFitHeight(15);
        box.getChildren().add(imgView);
        box.getChildren().add(new Label(" "));
        box.getChildren().add(l);
        return box;
    }

    private HBox createParentDirectoryHBox() {
        HBox box = new HBox();
        Label l = new Label("..");
        ImageView imgView = new ImageView(arrowImage);
        imgView.setPreserveRatio(true);
        imgView.setFitHeight(15);
        box.getChildren().add(imgView);
        box.getChildren().add(new Label(" "));
        box.getChildren().add(l);
        return box;
    }

    public void uploadPress(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        List<File> files = fileChooser.showOpenMultipleDialog(UIController.getStages().get("FileTransfer"));
        for(int i = 0;i < files.size(); ++i) {
            try {
                Client.upload(files.get(i));
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
        //Try to get the file instance without errors
        if(tableView.getSelectionModel().getSelectedItems().size() == 0)
            return;
        FileStructureInstance[] fileArr = new FileStructureInstance[tableView.getSelectionModel().getSelectedItems().size()];
        tableView.getSelectionModel().getSelectedItems().toArray(fileArr);//(FileStructureInstance[]) tableView.getSelectionModel().getSelectedItems().toArray();

        for(int i = 0;i < fileArr.length;++i) {
            if(fileArr[i] instanceof FileInstance) {
                try {
                Client.download((FileInstance)fileArr[i], fileArr[i].getName());
                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Dialog");
                    alert.setHeaderText("Could not download file");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
                catch(Exception e) { e.printStackTrace(); }
            }
            else
                downloadDirectory((DirectoryInstance)fileArr[i], fileArr[i].getName());
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
            } catch (IOException e) { e.printStackTrace();}
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
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot find directory!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public void modifyDownloadsDirectory(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Downloads Directory");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Directory", "."));
        File f = fileChooser.showOpenDialog(UIController.getStages().get("FileTransfer"));
        if(f.isDirectory()) {
            Client.setDownloadsPath(f.getPath());
            System.out.println("Changed download folder");
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Could not set downloads directory");
            alert.setHeaderText("Make sure you have selected a valid folder.");
            alert.showAndWait();
        }
    }

    public void showProcesses(ActionEvent actionEvent) {
        System.out.println("Showing FileTransfer Processes");
        UIController.setVisible("FileTransfer Processes");
    }

    public void languageButtonClicked(MouseEvent mouseEvent) {

    }

    public void disconnect(ActionEvent actionEvent) {
        Main.exit();
    }

    public void viewLogs(ActionEvent actionEvent) {
        UIController.setVisible("FileTransfer Logs");
    }
}