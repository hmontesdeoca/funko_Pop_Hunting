import javafx.application.Application;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.util.Date;
import java.sql.Timestamp;
import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application
{

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
    Button check = new Button("Check Pops");

    //Alternate Center
    Label popName =  new Label("");
    Label popLink = new Label("");
    VBox altVbox;
    Hyperlink hyperlink;

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
        addItems.setOnAction(event -> funkoNames.getItems().add(addPopField.getText()));

        //editing the pops
        funkoNames.getSelectionModel().selectedItemProperty().addListener((source, o, n)->{
            String selected = funkoNames.getSelectionModel().getSelectedItem();
            int index = funkoNames.getSelectionModel().getSelectedIndex();
            if(selected != null)
            {
                editFunkos(selected, index);
            }
        });

        //checking for pops
        check.setOnAction(Event->{
            for(int i =0; i < funkoNames.getItems().size(); i++)
            {
                //regular expression logic
                String cssQuery = "[^data-aria-label-part]:contains" + "(" + funkoNames.getItems().get(i) + ")";
                Elements element = doc.select(cssQuery);
                System.out.println("Pop Name: " + funkoNames.getItems().get(i));
                System.out.println("Results: " + element.size());
                System.out.println();

                try {

                    String temp = element.get(0).toString();
                    String temp2 = temp.substring(temp.indexOf('>'));
                    String[] tokens = temp2.split("\"");

                    String title = temp2.substring(1, temp2.indexOf('<'));

                    //logging information
                    System.out.println(temp);
                    System.out.println("Source");
                    System.out.println(temp2);
                    System.out.println("Title");
                    System.out.println(title);
                    System.out.println("Link");
                    System.out.println(tokens[1]);


                    borderPane.setLeft(null);
                    //alt vbox set up
                    popName.setText(title);
                    hyperlink = new Hyperlink(tokens[1]);
                    altVbox = new VBox(popName, hyperlink);
                    altVbox.setAlignment(Pos.CENTER);
                    altVbox.setPadding(new Insets(10));
                    borderPane.setCenter(altVbox);

                }
                catch(IndexOutOfBoundsException e)
                {
                    warningLabel.setText("No pops Available check later!");
                }
            }
        });
        /*hyperlink.setOnAction(Event->{
            getHostServices().showDocument(hyperlink.getText());
        });*/
        //setting size for list vie
        funkoNames.setMaxHeight(200);
        funkoNames.setMaxWidth(200);

        //setting size for text field
        addPopField.setMaxWidth(100);

        left = new VBox(10,  myLabel,addPopField, addItems );
        left.setPadding(new Insets(10));
        left.setAlignment(Pos.CENTER);
        borderPane.setLeft(left);

        center = new VBox(10,listLabel, warningLabel, funkoNames, check);
        center.setPadding(new Insets(10));
        center.setAlignment(Pos.CENTER);
        borderPane.setCenter(center);

        stage.setScene(new Scene(borderPane, 800,500));
        stage.show();


    }
    private void editFunkos(String funkoPop, int index)
    {
        Label editLabel = new Label("Edit Name");
        TextField funkoName = new TextField();
        funkoName.setMaxWidth(100);
        Button change = new Button("Change");
        Button exit = new Button("Exit");

        HBox buttons = new HBox(10, change, exit);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10));

        VBox v = new VBox(10, editLabel, funkoName, buttons);
        v.setAlignment(Pos.CENTER);
        v.setPadding(new Insets(10));
        borderPane.setCenter(v);

        change.setOnAction(Event->{

            funkoNames.getItems().set(index, funkoName.getText());
            borderPane.setCenter(center);
        });

        exit.setOnAction(Event->{
            borderPane.setCenter(center);
        });

    }


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
