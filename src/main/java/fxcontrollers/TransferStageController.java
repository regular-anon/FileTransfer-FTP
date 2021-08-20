package fxcontrollers;

import com.filetransfer.util.FileStructure;
import com.filetransfer.util.FileTransferProcess;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;

public class TransferStageController {
    public static Stage stage;
    public static TransferStageController instance;
    public TableView tableView;
    public TableColumn<FileTransferProcess, Label> nameColumn, sizeColumn, remoteHostColumn;

    public TransferStageController(Stage s) {
        this.stage = s;
        instance = this;
        tableView = (TableView) stage.getScene().lookup("#tableView");
        nameColumn = (TableColumn<FileTransferProcess, Label>) tableView.getColumns().get(0);
        sizeColumn = (TableColumn<FileTransferProcess, Label>) tableView.getColumns().get(1);
        remoteHostColumn = (TableColumn<FileTransferProcess, Label>) tableView.getColumns().get(2);

        nameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<FileTransferProcess, Label>, ObservableValue<Label>>() {
            @Override
            public ObservableValue<Label> call(TableColumn.CellDataFeatures<FileTransferProcess, Label> param) {
                FileTransferProcess fp = param.getValue();
                //Check for download or upload
                return new ReadOnlyObjectWrapper(new Label(fp.getFile().getName()));
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
}
