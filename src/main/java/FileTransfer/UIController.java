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
    private static HashMap<String, Stage> stageMap = new HashMap<>();
    //Adds a stage to the list
    public static void addStage(Stage... sArr) {
        for(Stage s : sArr)
        {
            stageMap.put(s.getTitle(), s);
        }
    }

    public static HashMap<String, Stage> getStages() {
        return stageMap;
    }

    public static void hideStage(String s)
    {
        Platform.runLater(new Runnable(){

            @Override
            public void run() {
                stageMap.get(s).hide();
            }
        });
    }

    public static void setVisible(String s)
    {
        Platform.runLater(new Runnable(){

            @Override
            public void run() {
                stageMap.get(s).show();
            }
        });
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