/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileTransfer;

import javafx.application.Application;
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

    public static Stage mainStage;
    private static Stage loginStage;

    public Main() throws IOException
    {
        loginStage = openStageByFileName("FileTransferLogin.fxml", "FileTransfer", true);
        mainStage = openStageByFileName("FileTransfer.fxml", "FileTransfer", true);
        loginStage.setResizable(false);
        loginStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Stage is closing");
                Main.exit();
            }
        });
        loginStage.setOnHidden(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Stage is closing");
                Main.exit();
            }
        });
        mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Stage is closing");
                Main.exit();
            }
        });
        mainStage.setOnHidden(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Stage is closing");
                Main.exit();
            }
        });
        FileTransferLoginController.instance.setStage(loginStage);
        UIController.addStage(loginStage);
        UIController.init();
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
        if(Client.isConnected())
        {
            if(FileTransferManager.instance.isRunning())
            {
                //Alert user
                Alert alert = new Alert(Alert.AlertType.WARNING, "Files are still being downloaded!", ButtonType.CANCEL, ButtonType.FINISH);
                alert.setTitle("Warning!");
                alert.setHeaderText("File are still being transfered!");
                alert.setContentText("Are you sure you want to exit now? Every transfer will be aborted!");
                Optional<ButtonType> result = alert.showAndWait();
                if(result.get() == ButtonType.FINISH)
                {

                }
                else
                {
                    return;
                }
            }
            try {
                Client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        UIController.removeAllStages();
        System.exit(0);
        //throw new RuntimeException("Close program");
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
//        Alert alert = new Alert(Alert.AlertType.WARNING, "Files are still being downloaded!", ButtonType.CANCEL, ButtonType.FINISH);
//        alert.setTitle("Warning!");
//        alert.setHeaderText("File are still being transfered!");
//        alert.setContentText("Are you sure you want to exit now? Every transfer will be aborted!");
//        alert.showAndWait();
        launch(args);
    }
}

//Console for testing (console I/O)
class DeveloperConsole implements Runnable
{
    Scanner sc;
    //DirectoryInstance currentDirectory = FileStructure.getRootDirectory();
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