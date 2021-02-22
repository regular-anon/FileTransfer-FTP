package FileTransfer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class FileTransferSettingsController  implements Initializable
{
    public static NodeList languageList;
    public static ArrayList<Language> list = new ArrayList<>();
    public static HashMap<String, Label> labelMap;
    public SplitMenuButton languageSelectButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        getLanguagesFromXmlFile("src/main/resources/Other/Languages.xml");
        listLanguages();
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
        System.out.println("Added languages to button");
    }

    private void changeDisplayLanguage(Language lang) {
        System.out.println("Changing display");
        if(labelMap == null) {
            System.out.println("labelMap is empty, so attempting to create map");
            labelMap = new HashMap<>();
            ArrayList<Stage> stageList = new ArrayList<Stage>(UIController.getStages().values());
            ArrayList<String> idList = new ArrayList<String>(lang.getMap().keySet());//(ArrayList<String>)lang.getMap().keySet();
            System.out.println("Managed to get arraylists from maps");
            System.out.println("StageList size: " + stageList.size());
            for(int i = 0;i < stageList.size();++i) {
                Scene s = stageList.get(i).getScene();
                System.out.println("Created stage with index: " + i);
                for(int j = 0;j < idList.size();++j) {
                    Label l = (Label)s.lookup("#" + idList.get(j));
                    System.out.println("Got result from lookup");
                    if(l != null) {
                        labelMap.put(idList.get(j), l);
                    }
                }
            }
            System.out.println("labelMap has been initialized");
            System.out.println("Found all labels with their respective id's.");
            System.out.println("Printing labels...");
            for(String key : labelMap.keySet()) {
                System.out.printf("Label with id %s has the text set to %s", labelMap.get(key), labelMap.get(key).getText());
            }
        }
        System.out.println("labelMap length: " + labelMap.size());
        for(String key : labelMap.keySet()) {
            labelMap.get(key).setText(lang.getWord(key));
        }
     }

    private static void getLanguagesFromXmlFile(String fileName)
    {
        try {
            File file = new File(fileName);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

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

    private static void listLanguages()
    {
        for(int i = 0;i < list.size();++i)
        {
            System.out.println(list.get(i).getName());
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
