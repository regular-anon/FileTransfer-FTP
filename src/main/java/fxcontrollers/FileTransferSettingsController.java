package fxcontrollers;

import com.filetransfer.util.FileTransferManager;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FileTransferSettingsController {

    public static Stage stage;
    public static FileTransferSettingsController instance;

    public Slider slider;
    public RadioButton lightButton, darkButton;
    public Button closeButton, colorPickerButton;
    public Label bytesString;

    public ToggleGroup group;

    public FileTransferSettingsController(Stage s) {
        this.stage = s;
        instance = this;

        //Initialize controls
        slider = (Slider) stage.getScene().lookup("#slider");
        lightButton = (RadioButton) stage.getScene().lookup("#lightButton");
        darkButton = (RadioButton) stage.getScene().lookup("#darkButton");
        closeButton = (Button) stage.getScene().lookup("#closeButton");
        bytesString = (Label) stage.getScene().lookup("#bytesString");
        colorPickerButton = (Button) stage.getScene().lookup("#colorPickerButton");

        //Max: 32_767
        //Default: 16_384
        //Min: 512

        slider.setMin(512);
        slider.setMax(32_767);
        slider.setValue(16_384);
        bytesString.setText("16384");

        slider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            bytesString.setText(newValue.intValue() + "");
            FileTransferManager.setMaxBufferSize(newValue.intValue());
        }));

        closeButton.setOnAction(event -> {
            stage.close();
        });

        group = new ToggleGroup();
        lightButton.setSelected(true);
        lightButton.setToggleGroup(group);

        darkButton.setSelected(false);
        darkButton.setToggleGroup(group);

        lightButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Light button selected!");
                MainFXMLController.instance.setLightMode();
            }
        });

        darkButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Dark button selected!");
                MainFXMLController.instance.setDarkMode();
            }
        });

        colorPickerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
//                ColorPicker colorPicker = new ColorPicker();
//                colorPicker.setValue(Color.BLUE);
//
//                colorPicker.show();
//
//                colorPicker.setOnAction(new EventHandler() {
//                    public void handle(Event t) {
//                        System.out.println("Color picker handle().");
//                    }
//                });

                ColorPicker colorPicker = new ColorPicker();

                Scene scene = new Scene(colorPicker);
                Stage stage = new Stage();
                stage.initStyle(StageStyle.UNDECORATED);
                stage.setScene(scene);

                colorPicker.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        //Do something with color

                        MainFXMLController.instance.setColor(colorPicker.getValue());

                        stage.close();
                    }
                });

                stage.showAndWait();

            }
        });
    }
}