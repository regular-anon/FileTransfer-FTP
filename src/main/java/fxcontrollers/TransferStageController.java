package fxcontrollers;

import com.filetransfer.util.FileStructure;
import com.filetransfer.util.FileTransferProcess;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;

public class TransferStageController {
    public static Stage stage;
    public static TransferStageController instance;
    public TableView tableView;
    public TableColumn<FileTransferProcess, Label> nameColumn, sizeColumn, remoteHostColumn;
    public MenuBar menuBar;

    public TransferStageController(Stage s) {
        this.stage = s;
        instance = this;
        tableView = (TableView) stage.getScene().lookup("#tableView");
        nameColumn = (TableColumn<FileTransferProcess, Label>) tableView.getColumns().get(0);
        sizeColumn = (TableColumn<FileTransferProcess, Label>) tableView.getColumns().get(1);
        remoteHostColumn = (TableColumn<FileTransferProcess, Label>) tableView.getColumns().get(2);
        menuBar = (MenuBar) stage.getScene().lookup("#menuBar");

        MenuItem clearHistory = menuBar.getMenus().get(0).getItems().get(2);
        clearHistory.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                clearTransferList();
            }
        });

        MenuItem saveItem = menuBar.getMenus().get(0).getItems().get(3);
        saveItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                File f = new File("src/main/resources/Other/TransferHistory/history.txt");
                boolean override = false;
                try {
                    saveTransferInfoToFile(f, override);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        nameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<FileTransferProcess, Label>, ObservableValue<Label>>() {
            @Override
            public ObservableValue<Label> call(TableColumn.CellDataFeatures<FileTransferProcess, Label> param) {
                FileTransferProcess fp = param.getValue();
                //Check for download or upload
                return new ReadOnlyObjectWrapper(new Label(((param.getValue().isDownload()) ? "Download: " : "Upload: ") + fp.getFile().getName()));
            }
        });

        sizeColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<FileTransferProcess, Label>, ObservableValue<Label>>() {
            @Override
            public ObservableValue<Label> call(TableColumn.CellDataFeatures<FileTransferProcess, Label> param) {
                FileTransferProcess fp = param.getValue();
                //Check for download or upload
                return new ReadOnlyObjectWrapper(new Label(FileStructure.longToSizeString(param.getValue().getRemoteFileSize())));
            }
        });

        remoteHostColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<FileTransferProcess, Label>, ObservableValue<Label>>() {
            @Override
            public ObservableValue<Label> call(TableColumn.CellDataFeatures<FileTransferProcess, Label> param) {
                FileTransferProcess fp = param.getValue();
                //Check for download or upload
                return new ReadOnlyObjectWrapper(new Label(String.format("%s@%s", fp.getClient().getCredentials()[0], fp.getClient().getCredentials()[2])));
            }
        });
    }

    public void addFileTransferProcess(FileTransferProcess fp) {
        tableView.getItems().add(fp);
    }

    public void clearTransferList() {
        tableView.getItems().clear();
    }

    public void saveTransferInfoToFile(File f, boolean override) throws IOException {
        if(!f.exists())
            throw new FileNotFoundException();
        if(!f.canWrite())
            throw new FileNotFoundException();
        PrintWriter pw = new PrintWriter(new FileWriter(f, override));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Time", new java.util.Date(System.currentTimeMillis()).toString());
        jsonObject.put("Transfer Count", tableView.getItems().size());
        JSONArray jsonArray = new JSONArray();
        jsonObject.put("Transfers", jsonArray);
        for(int i = 0;i < tableView.getItems().size(); ++i) {
            JSONObject fileJson = new JSONObject();
            FileTransferProcess fp = (FileTransferProcess) tableView.getItems().get(i);
            fileJson.put("File name", fp.getFile().getName());
            fileJson.put("Size", FileStructure.longToSizeString(fp.getRemoteFileSize()));
            fileJson.put("Remote Host", fp.getClient().getServer());
            jsonArray.add(fileJson);
        }
        pw.write(jsonObject.toJSONString());
        pw.write("\n");
        pw.flush();
        pw.close();

        System.out.println("[+] Saved transfer info to history.txt");
    }
}