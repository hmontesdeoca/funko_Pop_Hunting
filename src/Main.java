import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.util.Date;
import java.io.*;
import java.net.URL;
public class Main extends Application
{
    //Browser stuff
    WebView browser = new WebView();
    WebEngine webEngine = browser.getEngine();

    //left side
    Label myLabel = new Label("Enter the Pops you would like to actively search for!");
    TextField addPopField = new TextField();
    Button addItems = new Button("Add Pop");
    VBox left;

    //center side
    Label listLabel = new Label("Funko Pop's Entered");
    Label warningLabel = new Label("");
    ListView<String> funkoNames = new ListView<>();
    VBox center;
    HBox listOptions;
    Button remove = new Button("Remove");
    Button next = new Button("Next");
    Button prev = new Button("Previous");
    Button check = new Button("Check Pops");


    //Alternate Center
    VBox altVbox;
    Hyperlink hyperlink = new Hyperlink();
    Button goBack = new Button("Back");
    VBox buttonHolder = new VBox(goBack);

    //Menu Stuff
    MenuItem menuItems = new MenuItem();
    Menu menu = new Menu();
    BorderPane borderPane = new BorderPane();


    @Override
    public void start(Stage stage) throws IOException
    {
        //Html document to crawl through
        Date date = new Date();
        File htmlFile = new File("./test.html");
        final long hourConvert = 60*60*1000;
        long fileTime = htmlFile.lastModified()/hourConvert;
        long currentTime = date.getTime()/hourConvert;

        //if file doesn't exist and we are accessing a new timestamp
        if(!htmlFile.exists() || fileTime < currentTime - 1)
        {
            loadingDocument("https://twitter.com/FunkoPopHunters");
        }

        Document doc = Jsoup.parse(htmlFile, "UTF-8");
        //adding the pops
        addItems.setOnAction(event -> {

            funkoNames.getItems().add(addPopField.getText());
            funkoNames.getSelectionModel().select(0);
        });

        //checking for pops
        check.setOnAction(Event->{
            altVbox = new VBox();
            for(int i =0; i < funkoNames.getItems().size(); i++)
            {

                //regular expression logic
                String cssQuery = "[^data-aria-label-part]:contains" + "(" + funkoNames.getItems().get(i) + ")";
                Elements element = doc.select(cssQuery);
                try {


                    String temp = element.get(0).toString();
                    String temp2 = temp.substring(temp.indexOf('>'));
                    String[] tokens = temp2.split("\"");

                    String title = temp2.substring(1, temp2.indexOf('<'));

                    //logging information
                    System.out.println(1 +" iterations");


                    borderPane.setLeft(null);
                    //alt vbox set up
                    Hyperlink hyp = new Hyperlink(tokens[1]);
                    hyp.setOnMouseClicked(Event1-> {
                        getHostServices().showDocument(tokens[1]);
                    });
                    altVbox.getChildren().addAll(new Label(title), hyp);
                    altVbox.setAlignment(Pos.CENTER);
                    altVbox.setPadding(new Insets(10));
                    borderPane.setCenter(altVbox);
                    buttonHolder.setPadding(new Insets(10));
                    buttonHolder.setAlignment(Pos.CENTER);
                    borderPane.setBottom(buttonHolder);
                    borderPane.setPadding(new Insets(10));

                }
                catch(IndexOutOfBoundsException e)
                {
                    warningLabel.setText("No pops Available check later!");
                    System.out.println(funkoNames.getItems().get(i));

                }
            }
        });

        //naviagting and deleting from list
        prev.setOnAction(Event-> funkoNames.getSelectionModel().selectPrevious());

        next.setOnAction(Event-> funkoNames.getSelectionModel().selectNext());

        remove.setOnAction(Event-> funkoNames.getItems().remove(funkoNames.getSelectionModel().getSelectedIndex()));


        //setting size for list vie
        funkoNames.setMaxHeight(200);
        funkoNames.setMaxWidth(200);

        //setting size for text field
        addPopField.setMaxWidth(100);

        //setting left side up
        left = new VBox(10,  myLabel,addPopField, addItems );
        left.setPadding(new Insets(10));
        left.setAlignment(Pos.CENTER);
        borderPane.setLeft(left);

        //setting center up
        listOptions = new HBox(10, check, next, prev, remove);
        listOptions.setPadding(new Insets(10));
        listOptions.setAlignment(Pos.CENTER);
        center = new VBox(10,listLabel, warningLabel, funkoNames, listOptions);
        center.setPadding(new Insets(10));
        center.setAlignment(Pos.CENTER);
        borderPane.setCenter(center);

        //going back to center
        goBack.setOnAction(Event->{
            borderPane.setCenter(center);
            borderPane.setLeft(left);
            borderPane.setBottom(null);
        });
        //displaying the scene
        stage.setScene(new Scene(borderPane, 800,500));
        stage.show();
    }

    //Used to download the html file
    private void loadingDocument(String urlString) throws IOException
    {
        URL url = new URL(urlString);
        try(
                BufferedReader reader =  new BufferedReader(new InputStreamReader(url.openStream()));
                BufferedWriter writer = new BufferedWriter(new FileWriter("test.html"));
        )  {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
            }
            System.out.println("Page downloaded.");
        }
    }
}