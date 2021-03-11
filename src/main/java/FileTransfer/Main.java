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

public class Main extends Application{

    public static Stage mainStage, loginStage, processesStage, splashStage, settingsStage;

    public Main() throws IOException
    {
        loginStage = openStageByFileName("FileTransferLogin.fxml", "FileTransfer Login", true);
        mainStage = openStageByFileName("FileTransfer.fxml", "FileTransfer", true);
        processesStage = openStageByFileName("FileTransferProcesses.fxml", "FileTransfer Processes", true);
        splashStage = openStageByFileName("FileTransferSplashScreen.fxml", "FileTransfer Splash Screen", false);
        settingsStage = openStageByFileName("FileTransferSettings.fxml", "FileTransfer Settings", true);

        UIController.addStage(loginStage, mainStage, processesStage, splashStage, settingsStage);

        loginStage.setResizable(false);
        loginStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Login Stage is closing");
                Main.exit();
            }
        });
        mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Main Stage is closing");
                Main.exit();
            }
        });

        splashStage.getIcons().add(new Image("Photos/cloud.png"));
        UIController.setVisible("FileTransfer Splash Screen");
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(new Runnable(){
                                @Override
                                public void run() {
                                    UIController.hideStage("FileTransfer Splash Screen");
                                    try
                                    {
                                        Thread.sleep(200);
                                    }
                                    catch(InterruptedException e)
                                    {
                                        System.out.println(e.getStackTrace());
                                    }
                                    UIController.setVisible("FileTransfer Login");
                                }
                            });
                        }
                    },
                    2500
            );
    }

    //Opens a stage by the name of the file in src/main/resources/FXML (extension of file needed!)
    public static Stage openStageByFileName(String fileName, String title, boolean decorated) throws IOException {
        Parent root;
        Scene scene;
        Stage stage;
        root = FXMLLoader.load(new java.io.File("../FileTransfer/src/main/resources/FXML/" + fileName).toURI().toURL());
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
                    try {
                        Client.close();
                        UIController.closeAllStages();
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
                    UIController.closeAllStages();
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Done disconnecting...");
            }
        }
        UIController.closeAllStages();
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