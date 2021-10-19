package fxcontrollers;

import com.filetransfer.Client;
import com.filetransfer.util.FTPTask;
import com.filetransfer.util.FileStructure;
import com.filetransfer.util.FileTransferManager;
import com.filetransfer.util.files.DirectoryInstance;
import com.filetransfer.util.files.FileInstance;
import com.filetransfer.util.files.FileStructureInstance;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class TabManager {

    private Tab tab;
    private Client client;
    private TableView<FileStructureInstance> tableView;
    public TableColumn<FileStructureInstance, HBox> fileNameColumn;
    public TableColumn<FileStructureInstance, Label> sizeColumn;
    public TableColumn<FileStructureInstance, Label> lastModifiedColumn;
    public TreeItem<FileStructureInstance> tabTree;

    private Button downloadButton;
    private Button uploadButton;
    private Button searchButton;
    private TextField searchQuery;
    private Button disconnectButton;
    private boolean searchMode;

    public synchronized boolean searchMode() {
        return searchMode;
    }

    public synchronized void setSearchMode(boolean b) {
        searchMode = b;
    }

    public TabManager(Tab tab, Client client) {
        this.tab = tab;
        this.client = client;
        tableView = (TableView<FileStructureInstance>)tab.getContent().lookup("#tableView");
        fileNameColumn = (TableColumn<FileStructureInstance, HBox>)tableView.getColumns().get(0);
        sizeColumn = (TableColumn<FileStructureInstance, Label>)tableView.getColumns().get(1);
        lastModifiedColumn = (TableColumn<FileStructureInstance, Label>)tableView.getColumns().get(2);
        downloadButton = (Button) tab.getContent().lookup("#downloadButton");
        uploadButton = (Button) tab.getContent().lookup("#uploadButton");
        searchButton = (Button) tab.getContent().lookup("#searchButton");
        searchQuery = (TextField) tab.getContent().lookup("#searchQuery");
        disconnectButton = (Button) tab.getContent().lookup("#disconnectButton");

        tabTree = new TreeItem();

        tab.setOnSelectionChanged(event -> {
            if(MainFXMLController.instance.tabPane.getTabs().size() == 1) {
                MainFXMLController.instance.treeView.setRoot(null);
                MainFXMLController.instance.treeView.refresh();
                return;
            }
            if(MainFXMLController.instance.tabPane.getSelectionModel().getSelectedItem() == tab) {
                MainFXMLController.instance.currentTabManager = this;
                MainFXMLController.instance.treeView.setRoot(tabTree);
                MainFXMLController.instance.treeView.refresh();
            }
        });

        initialize();
    }

    private ContextMenu contextMenu = new ContextMenu();

    //Initialize buttons and UI onAction() methods
    public void initialize() {

        tab.setText(String.format("%s@%s", client.getCredentials()[0], client.getCredentials()[2]));
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        fileNameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<FileStructureInstance, HBox>, ObservableValue<HBox>>() {
            @Override
            public ObservableValue<HBox> call(TableColumn.CellDataFeatures<FileStructureInstance, HBox> param) {
                if(param.getValue().getSize() == -1)
                    return new ReadOnlyObjectWrapper(createParentDirectoryHBox(param.getValue().getName()));
                return new ReadOnlyObjectWrapper(createHBox(param.getValue()));
            }
        });
        sizeColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<FileStructureInstance, Label>, ObservableValue<Label>>() {
            @Override
            public ObservableValue<Label> call(TableColumn.CellDataFeatures<FileStructureInstance, Label> param) {
                if(param.getValue().getSize() == -1)
                    return new ReadOnlyObjectWrapper(new Label("---"));
                return new ReadOnlyObjectWrapper(new Label(FileStructure.longToSizeString(param.getValue().getSize())));
            }
        });
        lastModifiedColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<FileStructureInstance, Label>, ObservableValue<Label>>() {
            @Override
            public ObservableValue<Label> call(TableColumn.CellDataFeatures<FileStructureInstance, Label> param) {
                if(param.getValue().getSize() == -1)
                    return new ReadOnlyObjectWrapper(new Label("---"));
                return new ReadOnlyObjectWrapper(new Label(new Date(param.getValue().lastModified).toString()));
            }
        });

        tableView.setOnMouseClicked(event -> {
            contextMenu.hide();
            contextMenu.getItems().clear();
            if(event.getClickCount() > 1) {
                FileStructureInstance fsi = tableView.getSelectionModel().getSelectedItem();
                if(fsi.getName().equals("Return")) {
                    setDirectoryView(client.getFileStructure().getCurrentDirectory());
                    return;
                }
                if(fsi == null)
                    return;
                if(fsi instanceof FileInstance)
                    return;
                DirectoryInstance dir = (DirectoryInstance) fsi;
//                System.out.println("Directory path: " + dir.getPath());
                if(!dir.hasContent()) {
                    client.addTask(new FTPTask() {
                        @Override
                        public void doTask(FTPClient FTPClient) throws IOException {
                            if(dir.getSize() == -1) {
                                if(!client.getFileStructure().getCurrentDirectory().parentDirectory.hasContent()) {
                                    client.getFileStructure().assignContents(client.getFileStructure().getCurrentDirectory().parentDirectory);
                                }
//                                System.out.println("Clicked arrow folder");
                            }
                            else {
                                MainFXMLController.instance.stage.getScene().setCursor(Cursor.WAIT);
                                client.getFileStructure().assignContents(dir);
                            }
                        }

                        @Override
                        public void onError(FTPClient client) {
                            System.out.println("Could not change directory. Could not get directory listing.");
                        }

                        @Override
                        public void onFinished(FTPClient client) {
                            System.out.println("Task finished.");
                            MainFXMLController.instance.stage.getScene().setCursor(Cursor.DEFAULT);
                        }

                        @Override
                        public void onSuccessful(FTPClient ftpclient) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    if(dir.getSize() == -1)
                                        setDirectoryView(client.getFileStructure().getCurrentDirectory().parentDirectory);
                                    else {
                                        setDirectoryView(dir);
                                        if(!updateTreeView(dir, tabTree))
                                            System.err.println("Tree could not be updated!");
                                        MainFXMLController.instance.setTreeRoot(tabTree);
                                        MainFXMLController.instance.treeView.refresh();
//                                        printDirectory((DirectoryInstance) tabTree.getValue(), 0);
//                                        printTree(tabTree, 0);
                                    }
                                }
                            });
                        }
                    });
                }
                else {
                    setDirectoryView(dir);
                }
            }
            else if(event.getButton() == MouseButton.SECONDARY) {
                if(tableView.getSelectionModel().getSelectedItems().size() > 0) {
                    MenuItem downloadMenuItem = new MenuItem("Download");
                    downloadMenuItem.setOnAction(event1 -> {
                        client.addTask(new FTPTask() {
                            @Override
                            public void doTask(FTPClient ftpclient) throws IOException {
                                for(FileStructureInstance fsi : tableView.getSelectionModel().getSelectedItems()) {
                                    if(fsi instanceof FileInstance) {
                                        client.download((FileInstance)fsi);
                                    }
                                }
                            }

                            @Override
                            public void onError(FTPClient client) {
                                System.out.println("Couldn't download file(s).");
                            }

                            @Override
                            public void onFinished(FTPClient client) { }

                            @Override
                            public void onSuccessful(FTPClient client) {
                                System.out.println("Files put in download queue.");
                            }
                        });
                    });
                    contextMenu.getItems().add(downloadMenuItem);
                    contextMenu.show(tableView, event.getScreenX(), event.getScreenY());
                }
            }
        });

        downloadButton.setOnAction(event -> {
            client.addTask(new FTPTask() {
                @Override
                public void doTask(FTPClient ftpclient) throws IOException {
                    for(FileStructureInstance fsi : tableView.getSelectionModel().getSelectedItems()) {
                        if(fsi instanceof FileInstance) {
                            client.download((FileInstance)fsi);
                        }
                    }
                }

                @Override
                public void onError(FTPClient client) {
                    System.out.println("Couldn't download file(s).");
                }

                @Override
                public void onFinished(FTPClient client) { }

                @Override
                public void onSuccessful(FTPClient client) {
                    System.out.println("Files put in download queue.");
                }
            });
        });

        uploadButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            List<File> files = fileChooser.showOpenMultipleDialog(MainFXMLController.instance.tabPane.getScene().getWindow());//fileChooser.showOpenMultipleDialog(UIController.getStages().get("FileTransfer"));
            if(files == null) return;
            for(int i = 0;i < files.size(); ++i) {
                    client.addTask(new FTPTask(files.get(i)) {
                        @Override
                        public void doTask(FTPClient ftpClient) throws IOException {
                            File file = (File)parameter;
                            client.upload(file);
                        }

                        @Override
                        public void onError(FTPClient client) {
                            System.err.println("Could not upload file!");
                        }

                        @Override
                        public void onFinished(FTPClient client) {

                        }

                        @Override
                        public void onSuccessful(FTPClient client) {
                            System.out.println("Managed to upload file!");
                        }
                    });
            }
        });

        tab.getContent().lookup("#imgView").setVisible(false);

        tabTree = new TreeItem<>(client.getFileStructure().getRootDirectory());
//        printDirectory((DirectoryInstance) tabTree.getValue(), 0);
//        printTree(tabTree, 0);
        MainFXMLController.instance.setTreeRoot(tabTree);
        updateTreeView(client.getFileStructure().getRootDirectory(), tabTree);
//        System.out.println("=============================================");
//        MainFXMLController.printTree(tabTree, 0);
//        System.out.println("=============================================");
//        if(!updateTreeView(client.getFileStructure().getRootDirectory(), tabTree))
//            System.err.println("Tree could not be updated!");

        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String query = searchQuery.getText();
                if(query.equals("") || query == null)
                    return;
                client.addTask(new FTPTask() {
                    @Override
                    public void doTask(FTPClient ftpClient) throws IOException {
                        List<FileStructureInstance> files = client.getFileStructure().searchForFiles(query, client.getFileStructure().getCurrentDirectory(), 3);
                        setSearchMode(true);
                        //Show search results in tableView
//                        for(FileStructureInstance fsi : files) {
//                            System.out.println(fsi.getName());
//                        }
                        showSearchResults(files);
                    }

                    @Override
                    public void onError(FTPClient client) {

                    }

                    @Override
                    public void onFinished(FTPClient client) {
                        System.out.println("Done task!");
                        setSearchMode(false);
                    }

                    @Override
                    public void onSuccessful(FTPClient client) {

                    }
                });
            }
        });

        disconnectButton.setOnAction(event -> {
            if(MainFXMLController.instance.currentTabManager != null) {
                MainFXMLController.instance.currentTabManager.getClient().addTask(new FTPTask() {
                    @Override
                    public void doTask(FTPClient ftpClient) throws IOException {
                        MainFXMLController.instance.currentTabManager.getClient().close();
                        MainFXMLController.instance.currentTabManager.getClient().getFileStructure().deleteStructure(MainFXMLController.instance.currentTabManager.getClient().getFileStructure().getRootDirectory());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                MainFXMLController.instance.treeView.setRoot(null);
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
                                MainFXMLController.instance.removeTab(MainFXMLController.instance.currentTabManager);
//                                MainFXMLController.instance.getTabs().remove(this);
                                System.out.println("Pressed Disconnect Button!");
                            }
                        });
                    }
                });
            }
        });
    }

    static {
        KeyCodeCombination kc = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);
        Runnable rn = () -> System.out.println(FileTransferManager.getProcessCount());
        MainFXMLController.instance.tabPane.getScene().getAccelerators().put(kc, rn);

        KeyCodeCombination kc2 = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);
        Runnable rn2 = () -> MainFXMLController.logStage.show();
        MainFXMLController.instance.tabPane.getScene().getAccelerators().put(kc2, rn2);

        KeyCodeCombination kc3= new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN);
        Runnable rn3 = () -> {
            MainFXMLController.instance.currentTabManager.setDirectoryView(MainFXMLController.instance.currentTabManager.getClient().getFileStructure().getCurrentDirectory().parentDirectory);
        };
        MainFXMLController.instance.tabPane.getScene().getAccelerators().put(kc3, rn3);

        KeyCodeCombination kc4 = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
        Runnable rn4 = () -> { MainFXMLController.instance.currentTabManager.getClient().setTransferState(true);
            System.out.println("Cancel shortcut detected!"); };
        MainFXMLController.instance.tabPane.getScene().getAccelerators().put(kc4, rn4);

        KeyCodeCombination kc5 = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
        Runnable rn5 = () -> {
            if(MainFXMLController.instance.currentTabManager != null) {
                MainFXMLController.instance.currentTabManager.getClient().addTask(new FTPTask() {
                    @Override
                    public void doTask(FTPClient ftpClient) throws IOException {
                        MainFXMLController.instance.currentTabManager.getClient().close();
                        MainFXMLController.instance.currentTabManager.getClient().getFileStructure().deleteStructure(MainFXMLController.instance.currentTabManager.getClient().getFileStructure().getRootDirectory());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                MainFXMLController.instance.treeView.setRoot(null);
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
                                MainFXMLController.instance.removeTab(MainFXMLController.instance.currentTabManager);
//                                MainFXMLController.instance.getTabs().remove(this);
                                System.out.println("Pressed Disconnect Button!");
                            }
                        });
                    }
                });
            }
        };
        MainFXMLController.instance.tabPane.getScene().getAccelerators().put(kc5, rn5);
    }

    public void printDirectory(DirectoryInstance dir, int tabs) { //tabs default value: 0
        for(int i = 0;i < tabs; ++i)
            System.out.print("--");
        System.out.println(dir.getName());
        for(int i = 0;i < dir.getContents().size(); ++i) {
            for(int j = 0;j < tabs; ++j) System.out.print("--");
            if(dir.getContents().get(i) instanceof DirectoryInstance)
                printDirectory((DirectoryInstance) dir.getContents().get(i), tabs + 1);
            else
                System.out.println(dir.getContents().get(i).getName());
        }
    }

    public void printTree(TreeItem<FileStructureInstance> root, int tabs) {
        for(int i = 0;i < root.getChildren().size(); ++i) {
            for(int j = 0;j < tabs; ++j) System.out.print("--");
            FileStructureInstance fsi = root.getChildren().get(i).getValue();
            System.out.println(fsi.getName());
            if(fsi instanceof DirectoryInstance)
                printTree(root.getChildren().get(i), tabs + 1);
        }
    }

    public Tab getTab() {
        return tab;
    }

    public Client getClient() {
        return client;
    }

    public void setDirectoryView(DirectoryInstance dir) {
        if(!dir.hasContent())
            return;
        tableView.getItems().clear();
        DirectoryInstance d = new DirectoryInstance("...");
        d.setSize(-1);
        tableView.getItems().add(d);
        for(int i = 0;i < dir.getContents().size(); ++i) {
            tableView.getItems().add(dir.getContents().get(i));
        }
        try {
            client.getFileStructure().cd(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(client.getFileStructure().getCurrentDirectory().getPath());
    }

    public void showSearchResults(List<FileStructureInstance> files) {
        tableView.getItems().clear();
        FileStructureInstance returnInstance = new FileStructureInstance("<NOPATH>");
        returnInstance.setSize(-1);
        returnInstance.setName("Return");
        tableView.getItems().add(returnInstance);
        for(int i = 0;i < files.size(); ++i) {
            tableView.getItems().add(files.get(i));
        }
    }

    private HBox createParentDirectoryHBox(String name) {
        HBox box = new HBox();
        Label l = new Label(name);
        ImageView imgView = new ImageView(MainFXMLController.arrowImage);
        imgView.setPreserveRatio(true);
        imgView.setFitHeight(15);
        box.getChildren().add(imgView);
        box.getChildren().add(new Label(" "));
        box.getChildren().add(l);
        return box;
    }

    private HBox createHBox(FileStructureInstance fsi) {
        HBox box = new HBox();
        Label l = new Label(fsi.getName());
        ImageView imgView;
        if(fsi instanceof DirectoryInstance)
        {
            imgView = new ImageView(MainFXMLController.folderImage);
        }
        else
        {
            imgView = new ImageView(MainFXMLController.fileImage);
        }
        imgView.setPreserveRatio(true);
        imgView.setFitHeight(15);
        box.getChildren().add(imgView);
        box.getChildren().add(new Label(" "));
        box.getChildren().add(l);
        return box;
    }

    public boolean updateTreeView(DirectoryInstance dir, TreeItem<FileStructureInstance> rootTreeItem) {
        //rootTreeItem: search root
        //dir.hasContent() always true
        //When expanded, check if value dir has content, if yes, update.

        if(rootTreeItem.getValue().getName().equals(dir.getName())) {
            rootTreeItem.getChildren().clear();
            for(int i = 0;i < dir.getContents().size(); ++i) {
                TreeItem<FileStructureInstance> temp = new TreeItem<>(dir.getContents().get(i));
                rootTreeItem.getChildren().add(temp);
                if(temp.getValue() instanceof DirectoryInstance) {
                    FileStructureInstance emptyFSI = new FileStructureInstance("<NOPATH>");
                    emptyFSI.setSize(-1);
                    TreeItem<FileStructureInstance> emptyItem = new TreeItem<>(emptyFSI);
                    temp.getChildren().add(emptyItem);

                    temp.addEventHandler(TreeItem.branchExpandedEvent(), new EventHandler<TreeItem.TreeModificationEvent<Object>>() {
                        @Override
                        public void handle(TreeItem.TreeModificationEvent<Object> event) {
                            DirectoryInstance valueDir = (DirectoryInstance) temp.getValue();
                            if(valueDir.hasContent() && temp.getChildren().get(0).getValue().getSize() == -1) {
                                updateTreeView((DirectoryInstance) temp.getValue(), temp);
                            }
                            else if(!valueDir.hasContent()) {
                                client.addTask(new FTPTask() {
                            @Override
                            public void doTask(FTPClient FTPClient) throws IOException {
                                MainFXMLController.instance.stage.getScene().setCursor(Cursor.WAIT);
                                client.getFileStructure().assignContents((DirectoryInstance) temp.getValue());
                            }

                            @Override
                            public void onError(FTPClient client) {
                                System.out.println("Could not change directory. Could not get directory listing.");
                            }

                            @Override
                            public void onFinished(FTPClient client) {
                                System.out.println("Task finished.");
                                MainFXMLController.instance.stage.getScene().setCursor(Cursor.DEFAULT);
                            }

                            @Override
                            public void onSuccessful(FTPClient ftpclient) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(temp.getValue().getSize() != -1) {
//                                            setDirectoryView(client.getFileStructure().getCurrentDirectory().parentDirectory);
//                                            setDirectoryView(dir);
                                            updateTreeView((DirectoryInstance) temp.getValue(), temp);
                                            temp.setExpanded(true);
//                                            MainFXMLController.instance.setTreeRoot(tabTree);
//                                            MainFXMLController.instance.treeView.refresh();
//                                        printDirectory((DirectoryInstance) tabTree.getValue(), 0);
//                                            printTree(tabTree, 0);
                                        }
                                    }
                                });
                            }
                        });
                            }
                        }
                    });
                }
            }

            return true;
        }
        else
            return false;
    }
}
