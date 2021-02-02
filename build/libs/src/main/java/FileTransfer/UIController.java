package FileTransfer;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

public final class UIController
{
    static private Stage splashStage;
    private static ArrayList<Stage> stageList = new ArrayList<>();
    //Shows a splash Stage and then shows the stages in the list
    public static void init()
    {
        try
        {
            splashStage = Main.openStageByFileName("FileTransferSplashScreen.fxml", "", false);
            splashStage.getIcons().add(new Image("Photos/cloud.png"));
            splashStage.setTitle("FileTransfer Splash Screen");
            splashStage.show();
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(new Runnable(){
                                @Override
                                public void run() {
                                    splashStage.hide();
                                    removeStage(splashStage);
                                    try
                                    {
                                        Thread.sleep(200);
                                    }
                                    catch(InterruptedException e)
                                    {
                                        System.out.println(e.getStackTrace());
                                    }
                                    showStages();
                                }
                            });
                        }
                    },
                    2500
            );

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Shows all stages from the list
    public static void showStages() {
        Platform.runLater(new Runnable(){

            @Override
            public void run() {
                for (int i = 0; i < stageList.size(); ++i) {
                    stageList.get(i).show();
                }
            }
        });
    }

    //Adds a stage to the list
    public static void addStage(Stage s) {
        Platform.runLater(new Runnable(){

            @Override
            public void run() {
                stageList.add(s);
                s.setOnCloseRequest(event -> {
                    removeStage(s);
                });
            }
        });
    }
    //Removes a stage from the list
    //If there are no stages left, exit
    public static void removeStage(Stage s) {
        Platform.runLater(new Runnable(){

            @Override
            public void run() {
                s.close();
                stageList.remove(s);
            }
        });
//        if(stageList.size() == 0)
//            FileTransfer.Main.exit();
    }
    //Closes all stages
    public static void removeAllStages() {
        for (int i = 0; i < stageList.size(); ++i) {
            removeStage(stageList.get(i));
        }
    }
}