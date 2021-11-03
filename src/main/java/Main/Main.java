package Main;

import fxcontrollers.MainFXMLController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

public class Main extends Application { 

    public static boolean backgroundTask = false;
    private static TrayIcon trayIcon;

    public static void main(String[] args) {
        Platform.setImplicitExit(false); // Prevents the app from exiting when all stages are hidden
        launch(args);
        System.out.println("App closed.");
        if(backgroundTask) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene mainScene = loadScene("src/main/resources/FXML/Main.fxml");
        primaryStage.setScene(mainScene);

        //Title and image
        primaryStage.setTitle("FTPImpulse");

        primaryStage.minHeightProperty().set(680);
        primaryStage.minWidthProperty().set(1100);
        primaryStage.getIcons().add(new Image("Photos/impulse_cloud.png"));
        primaryStage.show();

        MainFXMLController.instance.stage = primaryStage;

        if(SystemTray.isSupported()) {
            backgroundTask = true;
            SystemTray tray = SystemTray.getSystemTray();
            java.awt.Image image = java.awt.Toolkit.getDefaultToolkit().getImage("src/main/resources/Photos/cloud_white.gif");

            java.awt.event.ActionListener listener = new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Platform.runLater(() -> {
                        if(MainFXMLController.instance.getTabs().size() > 0) {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setContentText("Make sure that all connections are closed before exiting the app!");
                            alert.setTitle("Warning!");
                            alert.showAndWait();
                        }
                        else {
                            Platform.exit();
                        }
                    });
                }
            };

            PopupMenu popup = new PopupMenu();
            MenuItem menuItem = new MenuItem("Exit");
            menuItem.addActionListener(listener);
            popup.add(menuItem);

            trayIcon = new TrayIcon(image, "FTPImpulse");
            trayIcon.setPopupMenu(popup);
            trayIcon.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if(e.getClickCount() > 1)
                                primaryStage.show();
                        }
                    });
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });
            tray.add(trayIcon);
        }
    }

    public static Scene loadScene(String path) throws IOException {
        Parent root;
        Scene scene;
        root = FXMLLoader.load(new java.io.File(path).toURI().toURL());
        scene = new Scene(root);
        scene.getStylesheets().add("CSS/light.css");
        return scene;
    }
}