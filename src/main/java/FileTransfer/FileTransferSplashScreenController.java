package FileTransfer;

import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.net.URL;
import java.util.ResourceBundle;

public class FileTransferSplashScreenController implements Initializable
{
    //public ImageView imageView;
    public MediaPlayer mediaPlayer;
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        //imageView.setImage(new Image("C:/Users/Cristian/Desktop/NetBeans Projects/FileTransfer/src/main/java/Photos/loadingGif.gif"));
//        String musicFile = "@../Photos/music.mp3";
//        Media sound = new Media(new java.io.File(musicFile).toURI().toString());
//        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
    }
}
