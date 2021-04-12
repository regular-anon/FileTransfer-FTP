package FileTransfer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.io.File;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class FileTransferSettingsController  implements Initializable
{
    public NodeList languageList;
    public ArrayList<Language> list = new ArrayList<>();
    public HashMap<String, Labeled> labelMap;
    public SplitMenuButton languageSelectButton;

    public Language currentLanguage;

    public static FileTransferSettingsController instance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        getLanguagesFromXmlFile("src/main/resources/Other/Languages.xml"); // ../FileTransfer/src/main/
        languageSelectButton.getItems().clear();
        for(int i = 0;i < list.size();++i) {
            Language temp = list.get(i);
            MenuItem newMenuItem = new MenuItem(temp.getName());

            languageSelectButton.getItems().add(newMenuItem);
            newMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    changeDisplayLanguage(temp);
                }
            });
        }
        parsePropFile();
//        ((Labeled)(UIController.getStages().get("FileTransfer").getScene().lookup("#Preferences"))).setText("Preferinte maii");
//        Scene s = UIController.getStages().get("FileTransfer").getScene();
//        HashMap <>s = UIController.getStages();
    }

    public void parsePropFile() {
        Scanner sc = null;
        try {
            File f = new File("src/main/resources/Other/user_settings.txt");
            sc = new Scanner(new java.io.FileInputStream(f));
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] p = line.split("=");
                switch(p[0]) {
                    case "Language":
                        list.forEach(lang -> {
                            if(lang.getName().equals(p[1])) {
                                changeDisplayLanguage(lang);
                            }
                        });
                }
            }
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Properties file not found!");
        }
        finally {
            if(sc != null) {
                sc.close();
            }
        }
    }

    public void saveSettings() {
        System.out.println("Saving prop file...");
        FileWriter fw = null;
        try {
            File f = new File("src/main/resources/Other/user_settings.txt");
            fw = new FileWriter(f);
            fw.write("Language=" + currentLanguage.getName());
        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Failed to close file!");
                }
            }
        }
    }

    private void changeDisplayLanguage(Language lang) {
        if(labelMap == null) {
            labelMap = new HashMap<>();
            ArrayList<Stage> stageList = new ArrayList<Stage>(UIController.getStages().values());
            ArrayList<String> idList = new ArrayList<String>(lang.getMap().keySet());//(ArrayList<String>)lang.getMap().keySet();
            for(int i = 0;i < stageList.size();++i) {
                Scene s = stageList.get(i).getScene();
                for(int j = 0;j < idList.size();++j) {
                    Labeled l = (Labeled)s.lookup("#" + idList.get(j));
                    if(l != null) {
                        l.textProperty().unbind();
                        labelMap.put(idList.get(j), l);
                    }
                }
            }
        }
        for(String key : labelMap.keySet()) {
//            labelMap.get(key).textProperty().unbind(); // < ---------
            labelMap.get(key).setText(lang.getWord(key));
        }
        languageSelectButton.setText(lang.getName());

        currentLanguage = lang;
     }
    //Add other words to wordMap in this method
    private void getLanguagesFromXmlFile(String fileName)
    {
        try {
            File file = new File(fileName);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("Language");
            languageList = nodeList;

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element tElement = (Element)node;
                    Language newLanguage = new Language(tElement.getAttribute("fullName"), tElement.getAttribute("id"));

                    Node n = tElement.getFirstChild();
                    while(n != null)
                    {
                        newLanguage.addWord(n.getNodeName(), n.getTextContent());
                        n = n.getNextSibling();
                    }
                    list.add(newLanguage);
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
}

class Language
{
    private String name, code;
    private HashMap<String, String> wordMap;
    private static HashMap<String, Label> labelMap;

    public Language(String name, String code)
    {
        this.name = name;
        this.code = code;
        wordMap = new HashMap<>();
    }

    public void addWord(String title, String value) {
        wordMap.put(title, value);
    }

    public String getName()
    {
        return name;
    }

    public String getCode()
    {
        return code;
    }

    public String getWord(String title)
    {
        if(wordMap.containsKey(title))
            return wordMap.get(title);
        return null;
    }
    public HashMap<String, String> getMap() {
        return wordMap;
    }
}
