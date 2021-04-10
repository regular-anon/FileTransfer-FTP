/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileTransfer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;


import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application {

    public static Stage mainStage, loginStage, processesStage, splashStage, settingsStage;
    private static boolean hasIconTray = false;

    public Main() throws IOException {
        Platform.setImplicitExit(false);
        loginStage = openStageByFileName("FileTransferLogin.fxml", "FileTransfer Login", true);
        mainStage = openStageByFileName("FileTransfer.fxml", "FileTransfer", true);
        processesStage = openStageByFileName("FileTransferProcesses.fxml", "FileTransfer Processes", true);
        splashStage = openStageByFileName("FileTransferSplashScreen.fxml", "FileTransfer Splash Screen", false);
        UIController.addStage(loginStage, mainStage, processesStage, splashStage);
        settingsStage = openStageByFileName("FileTransferSettings.fxml", "FileTransfer Settings", true);
        UIController.addStage(settingsStage);


        loginStage.setResizable(false);
        loginStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Login Stage is closing");
                Main.exit();
            }
        });
        mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                we.consume();
                if(hasIconTray)
                    UIController.hideAllStages();
                else
                    exit();
            }
        });

        splashStage.getIcons().add(new Image("Photos/cloud.png"));
        UIController.setVisible("FileTransfer Splash Screen");
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                UIController.hideStage("FileTransfer Splash Screen");
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    System.out.println(e.getStackTrace());
                                }
                                UIController.setVisible("FileTransfer Login");
                            }
                        });
                    }
                },
                2500
        );

//        java.awt.TrayIcon trayIcon = null;
//            java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
//            System.out.println("Looking for tray icon photo from " + System.getProperty("user.dir"));
//            java.awt.Image image = java.awt.Toolkit.getDefaultToolkit().getImage("src/main/resources/Photos/cloud_white.png");
//            java.awt.event.ActionListener listener = new java.awt.event.ActionListener() {
//                public void actionPerformed(java.awt.event.ActionEvent e) {
//                    System.out.println("Closed program!");
//                    Platform.runLater(() -> {
//                        Main.exit();
//                    });
//                }
//            };
//            java.awt.PopupMenu popup = new java.awt.PopupMenu();
//            java.awt.MenuItem defaultItem = new java.awt.MenuItem("Close");
//            defaultItem.addActionListener(listener);
//            popup.add(defaultItem);
//            trayIcon = new java.awt.TrayIcon(image, "FileTransfer", popup);
//            trayIcon.addActionListener(e -> {
//                if(Client.isConnected()) {
//                    UIController.setVisible("FileTransfer");
//                }
//            });
//            try {
//                tray.add(trayIcon);
//            } catch (java.awt.AWTException e) {
//                System.err.println(e);
//            }
            java.awt.TrayIcon trayIcon = null;
            if (java.awt.SystemTray.isSupported()) {
                hasIconTray = true;
                // get the SystemTray instance
                java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
                // load an image
                java.awt.Image image = java.awt.Toolkit.getDefaultToolkit().getImage("src/main/resources/Photos/cloud_white.gif");
                // create a action listener to listen for default action executed on the tray icon
                java.awt.event.ActionListener listener = new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        System.out.println("Closed program!");
                        Platform.runLater(() -> {
                            Main.exit();
                        });
                    }
                };
                // create a popup menu
                java.awt.PopupMenu popup = new java.awt.PopupMenu();
                // create menu item for the default action
                java.awt.MenuItem defaultItem = new java.awt.MenuItem("Close");
                defaultItem.addActionListener(listener);
                popup.add(defaultItem);
                /// ... add other items
                // construct a TrayIcon
                trayIcon = new java.awt.TrayIcon(image, "FileTransfer", popup);
                // set the TrayIcon properties
                trayIcon.addActionListener(e -> {
                    if(Client.isConnected()) {
                    UIController.setVisible("FileTransfer");
                }
                });
                // ...
                // add the tray image
                try {
                    tray.add(trayIcon);
                } catch (java.awt.AWTException e) {
                    System.err.println(e);
                }
                // ...
            } else {
                // disable tray option in your application or
            }
    }

    //Opens a stage by the name of the file in src/main/resources/FXML (extension of file needed!)
    public static Stage openStageByFileName(String fileName, String title, boolean decorated) throws IOException {
        Parent root;
        Scene scene;
        Stage stage;
        root = FXMLLoader.load(new java.io.File("src/main/resources/FXML/" + fileName).toURI().toURL());
        scene = new Scene(root);
        stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        if (!decorated)
            stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().add(new Image("Photos/cloud.png"));
        return stage;
    }

    public static void exit() {
        System.out.println("Trying to exit application");
        FileTransferSettingsController.instance.saveSettings();
        FileStructure.currentDirectory = FileStructure.rootDirectory;
        if(Client.isConnected())
        {
            System.out.println("But client is connected!!");
            if(FileTransferManager.instance != null && FileTransferManager.instance.isRunning())
            {
                //Alert user
                Alert alert = new Alert(Alert.AlertType.WARNING, "Files are still being downloaded!", ButtonType.CANCEL, ButtonType.FINISH);
                alert.setTitle("Warning!");
                alert.setHeaderText("File are still being transfered!");
                alert.setContentText("Are you sure you want to exit now? Every transfer will be aborted!");
                Optional<ButtonType> result = alert.showAndWait();
                if(result.get() == ButtonType.FINISH)
                {
                    System.out.println("Pressed Finish button...");
//                    System.exit(0);
                    try {
                        Client.close();
                        UIController.hideAllStages();
                        Platform.exit();
                        System.exit(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    return;
                }
            }
            else
            {
                System.out.println("Transfer manager is not running, but trying to disconnect...");
                try {
                    Client.close();
                    UIController.hideAllStages();
                    Platform.exit();
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Done disconnecting...");
            }
        }
        UIController.hideAllStages();
        System.exit(0);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        String home = System.getProperty("user.home") + "/Downloads";
        String path = "";
        for(int i = 0;i < home.length();++i)
        {
            if(home.charAt(i) == '\\')
                path += '/';
            else
                path += home.charAt(i);
        }
        Client.setDownloadsPath(path);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
//        new Thread(new DeveloperConsole()).start();
        launch(args);
    }
}

//Console for testing (console I/O)
class DeveloperConsole implements Runnable
{
    Scanner sc;
    public void run()
    {
        System.out.println("Developer console started.");
        sc = new Scanner(System.in);
        while(true)
        {
            System.out.print(">");
            String input = sc.nextLine();
            switch(input)
            {
                case "pwd":
                    System.out.println(FileStructure.currentDirectory.getPath());
                    break;
                case "dir":
                case "ls":
                    for(FileStructureInstance fsi : FileStructure.currentDirectory.getContents())
                    {
                        System.out.println("-" + fsi.getName());
                    }
                    System.out.println("Done printing contents.");
                    break;
                case "pdir":
                    System.out.println("Parent directory: " + FileStructure.currentDirectory.getName());
                    break;
                case "exit":
                    Main.exit();
                    break;
                case "test upload":
                    try {
                        Client.uploadTest(new File("C:/Users/Cristian/Desktop/menu.jpg"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    if(input.startsWith("cd "))
                    {
                        try
                        {
                            String newDirectory = input.substring(3);
                            System.out.println(newDirectory);
                            try {
                                FileStructure.cd(newDirectory);
                                FileTransferController.instance.showDirectory(FileStructure.currentDirectory);
                            }
                            catch(Exception e)
                            {
                                System.out.println(e.getMessage());
                            }
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
            }
        }
    }
}