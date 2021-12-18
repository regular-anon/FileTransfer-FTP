package fxcontrollers;

import com.filetransfer.Client;
import com.filetransfer.util.FTPTask;
import com.filetransfer.util.files.DirectoryInstance;
import com.filetransfer.util.files.FileInstance;
import com.filetransfer.util.files.FileStructureInstance;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.net.ftp.FTPClient;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainFXMLController implements Initializable {

    public TabPane tabPane;
    public MenuItem viewLogs;
    public MenuItem closeConnection;
    public MenuItem uploadItem;
    public MenuItem viewSettings;
    @FXML
    public Pane transfersMenuContainerPane;
    @FXML
    public Pane helpMenuContainerPane;

    public Stage stage;
    private Button clearButton, connectButton;
    private TextField hostInput, usernameInput, portInput;
    private PasswordField passwordInput;
    public TreeView<FileStructureInstance> treeView;
    public static MainFXMLController instance;
    public static FileTransferLogsController logsController;

    public static Image loadingGif = new Image("Photos/loading.gif");
    public static Image folderImage = new Image("Photos/folder.png");
    public static Image fileImage = new Image("Photos/file.png");
    public static Image arrowImage = new Image("Photos/up_arrow.png");
//    public HBox emptyTreeDir;

    public static Stage logStage;
    public static Stage settingsStage;
    public static Stage transferStage;

    private final List<TabManager> tabManagerList = new ArrayList<>();
    public TabManager currentTabManager;                            ////////////////////////// <====================


    public void removeTab(TabManager tabManager) {
//        for(int i = 0;i < tabManagerList.size(); ++i) {
//            if(tabManagerList.get(i).getTab() == tab) {
//                tabPane.getTabs().remove(tab);
//                tabManagerList.remove(i);
//                return;
//            }
//        }
        tabPane.getTabs().remove(tabManager.getTab());
        tabManagerList.remove(tabManager);
    }

    public MainFXMLController() {
//        emptyTreeDir = new HBox();
//        ImageView imgView = new ImageView(loadingGif);
//        imgView.setPreserveRatio(true);
//        imgView.setFitHeight(16);
//        Label lbl = new Label("...");
//        emptyTreeDir.getChildren().add(imgView);
//        emptyTreeDir.getChildren().add(lbl);
    }

    public static HBox getEmptyTreeDir() {
        HBox emptyTreeDir = new HBox();
        ImageView imgView = new ImageView(loadingGif);
        imgView.setPreserveRatio(true);
        imgView.setFitHeight(16);
        Label lbl = new Label("...");
        emptyTreeDir.getChildren().add(imgView);
        emptyTreeDir.getChildren().add(lbl);

        return emptyTreeDir;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        treeView.setShowRoot(false);

        //Add the login tab
        try {
            Tab newTab = FXMLLoader.load(new File("src/main/resources/FXML/newConnectionTab.fxml").toURI().toURL());
            newTab.setClosable(false);
            tabPane.getTabs().add(newTab);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Getting elements from id's
        connectButton = (Button)tabPane.lookup("#connectButton");
        clearButton = (Button)tabPane.lookup("#clearButton");
        hostInput = (TextField)tabPane.lookup("#hostInput");
        usernameInput = (TextField)tabPane.lookup("#usernameInput");
        portInput = (TextField)tabPane.lookup("#portInput");
        passwordInput = (PasswordField)tabPane.lookup("#passwordInput");

        clearButton.setOnAction(event -> {
            hostInput.clear();
            usernameInput.clear();
            portInput.clear();
            passwordInput.clear();
        });

        connectButton.setOnAction(event -> {
            try {

                String hostname = hostInput.getText();
                String username = usernameInput.getText();
                String password = passwordInput.getText();
                int port = Integer.parseInt(portInput.getText());

                if(hostname == "" || username == "" || password == "" || (port < 1 || port > 65534))
                    return;

                Client client = new Client(hostname, username, password, port);
                client.addTask(new FTPTask() {
                    @Override
                    public void doTask(FTPClient ftpclient) throws IOException {
                        client.connect();
                        client.getFileStructure().init();
                    }

                    @Override
                    public void onError(FTPClient client) {
                        System.out.println("Could not connect to the server!");
                    }

                    @Override
                    public void onFinished(FTPClient client) {
                        System.out.println("Task finished.");
                    }

                    @Override
                    public void onSuccessful(FTPClient ftpclient) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("[+] Connected to server.");
                                try {
                                    TabManager manager = addNewTab(client);
                                    manager.setDirectoryView(client.getFileStructure().getRootDirectory());
//                                    manager.updateTreeView(client.getFileStructure().getRootDirectory(), manager.tabTree);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        treeView.setCellFactory(param -> new TreeCell<FileStructureInstance>() {
            @Override
            protected void updateItem(FileStructureInstance fsi, boolean empty) {
                super.updateItem(fsi, empty);
                if (fsi == null || empty) {
                    setGraphic(new HBox());
                } else {
                    if(fsi.getSize() != -1)
                        setGraphic(createFSIHBox(fsi));
                    else
                        setGraphic(getEmptyTreeDir());
                }
            }
        });

        treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount() > 1) {
                    TreeItem<FileStructureInstance> selectedNode = treeView.getSelectionModel().getSelectedItem();
                    if(selectedNode != null && selectedNode.getValue() instanceof DirectoryInstance) {
                        DirectoryInstance dir = (DirectoryInstance) selectedNode.getValue();
                        if(dir.hasContent())
                            currentTabManager.setDirectoryView(dir);
                    }
                }
            }
        });

        //Load logs window
        try {
            Parent p = (Parent)FXMLLoader.load(new File("src/main/resources/FXML/FileTransferLogs.fxml").toURI().toURL());
            Scene sc = new Scene(p);
            logStage = new Stage();
            logStage.setResizable(false);
            logStage.setScene(sc);
            logStage.getIcons().add(new Image("Photos/cloud.png"));
            logStage.setTitle("Logs");
            FileTransferLogsController logsController = new FileTransferLogsController(logStage);

            settingsStage = new Stage();
            settingsStage.setResizable(false);
            settingsStage.setScene(new Scene(FXMLLoader.load(new File("src/main/resources/FXML/Settings.fxml").toURI().toURL())));
            settingsStage.setTitle("Settings");
            settingsStage.getIcons().add(new Image("Photos/cloud.png"));
            FileTransferSettingsController settingsController = new FileTransferSettingsController(settingsStage);

            transferStage = new Stage();
            transferStage.setScene(new Scene(FXMLLoader.load(new File("src/main/resources/FXML/FileTransferProcesses.fxml").toURI().toURL())));
            transferStage.setTitle("Transfers");
            transferStage.getIcons().add(new Image("Photos/cloud.png"));
            TransferStageController previousTransfers = new TransferStageController(transferStage);

        } catch (IOException e) {
            e.printStackTrace();
        }

        viewLogs.setOnAction(event -> {
            logStage.show();
        });

        viewSettings.setOnAction(event -> {
            settingsStage.show();
        });

        transfersMenuContainerPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                transferStage.show();
            }
        });

        helpMenuContainerPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    Desktop.getDesktop().browse(new URL("https://ftpimpulse.my-free.website/").toURI());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });

        closeConnection.setOnAction(event -> {
            if(currentTabManager != null) {
                currentTabManager.getClient().addTask(new FTPTask() {
                    @Override
                    public void doTask(FTPClient ftpClient) throws IOException {
                        currentTabManager.getClient().close();
                        currentTabManager.getClient().getFileStructure().deleteStructure(currentTabManager.getClient().getFileStructure().getRootDirectory());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                    treeView.setRoot(null);
                            }
                        });
                    }

                    @Override
                    public void onError(FTPClient client) {
                        System.out.println("Error in closing client!?");
                    }

                    @Override
                    public void onFinished(FTPClient client) {
                        System.out.println("Task finished.");
                    }

                    @Override
                    public void onSuccessful(FTPClient client) {
                        System.out.println("[-] Managed to close client!");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                removeTab(currentTabManager);
//                                tabManagerList.remove(currentTabManager);
                            }
                        });
                    }
                });
            }
        });
        uploadItem.setOnAction(event -> {
            if(currentTabManager == null)
                return;
            if(tabManagerList.size() == 0)
                return;
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            List<File> files = fileChooser.showOpenMultipleDialog(MainFXMLController.instance.tabPane.getScene().getWindow());//fileChooser.showOpenMultipleDialog(UIController.getStages().get("FileTransfer"));
            if(files == null)
                return;
            for(int i = 0;i < files.size(); ++i) {
                currentTabManager.getClient().addTask(new FTPTask(files.get(i)) {
                    @Override
                    public void doTask(FTPClient ftpClient) throws IOException {
                        File file = (File)parameter;
                        currentTabManager.getClient().upload(file);
                    }

                    @Override
                    public void onError(FTPClient client) {
                        System.err.println("Could not upload file!");
                    }

                    @Override
                    public void onFinished(FTPClient client) { }

                    @Override
                    public void onSuccessful(FTPClient client) {
                        System.out.println("Managed to upload file!");
                    }
                });
            }
        });
    }

    public static HBox createFSIHBox(FileStructureInstance fsi) {
        HBox box = new HBox();
        ImageView imgView = new ImageView(((fsi instanceof FileInstance) ? MainFXMLController.fileImage : MainFXMLController.folderImage ));
        imgView.setPreserveRatio(true);
        imgView.setFitHeight(16);
        Label lb = new Label(fsi.getName());
        box.getChildren().add(imgView);
        box.getChildren().add(lb);
        return box;
    }

    public static <T> void insert(List<T> ls, int index, T obj) {
        if(index == ls.size())
            ls.add(obj);
        else {
            T temp = ls.get(index);
            ls.set(index, obj);
            insert(ls, ++index, temp);
        }
    }

    public TabManager addNewTab(Client client) throws IOException {
        Tab newTab = FXMLLoader.load(new File("src/main/resources/FXML/tab.fxml").toURI().toURL());
        newTab.setClosable(true);
        insert(tabPane.getTabs(), 0, newTab);
        TabManager manager = new TabManager(newTab, client);
        tabManagerList.add(manager);
        currentTabManager = manager;

        newTab.setOnCloseRequest(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                client.addTask(new FTPTask() {
                    @Override
                    public void doTask(FTPClient ftpClient) throws IOException {
                        client.close();
                        client.getFileStructure().deleteStructure(client.getFileStructure().search("/"));
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if(currentTabManager == manager) {
                                    treeView.setRoot(null);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(FTPClient client) {
                        System.out.println("Error in closing client!?");
                    }

                    @Override
                    public void onFinished(FTPClient client) {
                        System.out.println("Task finished.");
                    }

                    @Override
                    public void onSuccessful(FTPClient client) {
                        System.out.println("[-] Managed to close client!");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                removeTab(manager);
//                                tabManagerList.remove(currentTabManager);
                            }
                        });
                    }
                });
            }
        });
        return manager;
    }

    public void setTreeRoot(TreeItem root) {
        treeView.setRoot(root);
        treeView.refresh();
    }

    public static void printTree(TreeItem<FileStructureInstance> root, int tabs) {
        for(int i = 0;i < root.getChildren().size(); ++ i) {
            FileStructureInstance fsi = root.getChildren().get(i).getValue();
            if(fsi instanceof FileInstance) {
                for(int j = 0;j < tabs; ++j) System.out.print("    ");
                System.out.println(fsi.getName());
            }
            else
                printTree(root.getChildren().get(i), tabs + 1);
        }
    }

    public void setLightMode() {
        treeView.getScene().getStylesheets().clear();
        treeView.getScene().getRoot().setStyle("");
        treeView.getScene().getStylesheets().add("CSS/light.css");
        logStage.getScene().getStylesheets().clear();
        settingsStage.getScene().getStylesheets().clear();
        transferStage.getScene().getStylesheets().clear();
    }

    public void setDarkMode() {
        treeView.getScene().getStylesheets().clear();
        treeView.getScene().getRoot().setStyle("");
        treeView.getScene().getStylesheets().add("CSS/dark.css");
        logStage.getScene().getStylesheets().add("CSS/dark.css");
        settingsStage.getScene().getStylesheets().add("CSS/dark.css");
        transferStage.getScene().getStylesheets().add("CSS/dark.css");
    }

    public void setColor(javafx.scene.paint.Color c) {
        System.out.printf("RGB Values: %s %s %s\n", c.getRed(), c.getGreen(), c.getBlue());

//        treeView.getScene().getStylesheets().add();
//        treeView.getScene().getStylesheets().clear();
//        treeView.getScene().getStylesheets().
        Node root = treeView.getScene().getRoot();
        String rgbCSSString = String.format("rgb(%d, %d, %d)", (int)(c.getRed() * 255), (int)(c.getGreen() * 255), (int)(c.getBlue() * 255));
        System.out.println(rgbCSSString);
        root.setStyle(String.format("; -fx-base: %s;", rgbCSSString) + " -fx-background-color: -fx-outer-border, -fx-inner-border, derive(-fx-color, +20%);");
        logStage.getScene().getRoot().setStyle(String.format("; -fx-base: %s; -fx-color: %s;", rgbCSSString, rgbCSSString));
        settingsStage.getScene().getRoot().setStyle(String.format("; -fx-base: %s; -fx-color: %s;", rgbCSSString, rgbCSSString));
        transferStage.getScene().getRoot().setStyle(String.format("; -fx-base: %s; -fx-color: %s;", rgbCSSString, rgbCSSString));


    }

    public List<TabManager> getTabs() {
        return tabManagerList;
    }
}