package FileTransfer;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

public final class UIController
{
    static private Stage splashStage;
//    private static ArrayList<Stage> stageList = new ArrayList<>();
    private static HashMap<String, Stage> stageMap = new HashMap<>();
    //Shows a splash Stage and then shows the stages in the list
//    public static void init()
//    {
//        try
//        {
//            splashStage = Main.openStageByFileName("FileTransferSplashScreen.fxml", "", false);
//            splashStage.getIcons().add(new Image("Photos/cloud.png"));
//            splashStage.setTitle("FileTransfer Splash Screen");
//            splashStage.show();
//            new java.util.Timer().schedule(
//                    new java.util.TimerTask() {
//                        @Override
//                        public void run() {
//                            Platform.runLater(new Runnable(){
//                                @Override
//                                public void run() {
//                                    splashStage.hide();
//                                    removeStage(splashStage);
//                                    try
//                                    {
//                                        Thread.sleep(200);
//                                    }
//                                    catch(InterruptedException e)
//                                    {
//                                        System.out.println(e.getStackTrace());
//                                    }
//                                    showStages();
//                                }
//                            });
//                        }
//                    },
//                    2500
//            );
//
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    //Shows all stages from the list
//    public static void showStages() {
//        Platform.runLater(new Runnable(){
//
//            @Override
//            public void run() {
//                for (int i = 0; i < stageList.size(); ++i) {
//                    stageList.get(i).show();
//                }
//            }
//        });
//    }
    //Adds a stage to the list
    public static void addStage(Stage... sArr) {
//        Platform.runLater(new Runnable(){
//
//            @Override
//            public void run() {
//
////                stageList.add(s);
////                s.setOnCloseRequest(event -> {
////                    removeStage(s);
////                });
////                s.show();
//            }
//        });
        for(Stage s : sArr)
        {
            stageMap.put(s.getTitle(), s);
        }
    }

    public static void removeStage(String s)
    {
        stageMap.remove(s);
    }

    public static void hideStage(String s)
    {
        System.out.println(stageMap.get(s).getTitle() + " set to hidden.");
        stageMap.get(s).hide();
    }

    public static void setVisible(String s)
    {
        System.out.println(stageMap.get(s).getTitle() + " set to visible.");
        stageMap.get(s).show();
    }
    //Removes a stage from the list
    //If there are no stages left, exit
//    public static void removeStage(Stage s) {
//        Platform.runLater(new Runnable(){
//
//            @Override
//            public void run() {
//                //s.close();
//                s.hide();
////                stageList.remove(s);
//
//            }
//        });
////        if(stageList.size() == 0)
////            FileTransfer.Main.exit();
//    }
    //Closes all stages
//    public static void removeAllStages() {
//        for (int i = 0; i < stageList.size(); ++i) {
//            removeStage(stageList.get(i));
//        }
//    }
    public static void closeAllStages()
    {
        for(String s : stageMap.keySet())
        {
            hideStage(s);
        }
    }
}