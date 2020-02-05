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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.util.Date;
import java.io.*;
import java.net.URL;
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
    HBox listOptions;
    Button remove = new Button("Remove");
    Button next = new Button("Next");
    Button prev = new Button("Previous");
    Button check = new Button("Check Pops");


    //Alternate Center
    VBox altVbox;
    Button goBack = new Button("Back");
    VBox buttonHolder = new VBox(goBack);

    //Menu Stuff
    MenuItem openFile = new MenuItem("Open");
    MenuItem saveFile = new MenuItem("Save");
    MenuItem exitProgram = new MenuItem("Exit");
    FileWriter fileWriter;
    PrintWriter printWriter;
    BorderPane borderPane = new BorderPane();


    @Override
    public void start(Stage stage) throws IOException
    {
        //FILE DOWNLOADING/CACHE MANAGEMENT

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

        //EVENT HANDLING FUNCTIONS

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

                //avoid repetition
                if(i>=1 && funkoNames.getItems().get(i).equals(funkoNames.getItems().get(i-1)))
                    continue;

                //The query to search for
                String cssQuery = "[^data-aria-label-part]:contains" + "(" + funkoNames.getItems().get(i) + ")";
                Elements element = doc.select(cssQuery);

                //If the exception is handled then that implies no pops are present!
                try {

                    //regular expression handling
                    String temp = element.get(0).toString();
                    String temp2 = temp.substring(temp.indexOf('>'));
                    String[] tokens = temp2.split("\"");
                    String title = temp2.substring(1, temp2.indexOf('<'));

                    borderPane.setLeft(null);

                    //new instantiations of hyperlinks
                    Hyperlink hyp = new Hyperlink(tokens[1]);
                    hyp.setOnMouseClicked(Event1-> {
                        getHostServices().showDocument(tokens[1]);
                    });
                    Label lbl = new Label(title);
                    lbl.setId(funkoNames.getItems().get(i));

                    //alt vbox set up
                    altVbox.getChildren().addAll(lbl, hyp);
                    altVbox.setAlignment(Pos.CENTER);
                    altVbox.setPadding(new Insets(10));
                    borderPane.setCenter(altVbox);
                    buttonHolder.setPadding(new Insets(10));
                    buttonHolder.setAlignment(Pos.CENTER);
                    borderPane.setBottom(buttonHolder);
                }

                //No pops are present
                catch(IndexOutOfBoundsException e)
                {
                    warningLabel.setText("No pops Available check later!");
                    System.out.println(funkoNames.getItems().get(i));
                }
            }
        });

            //navigating and deleting from list
        prev.setOnAction(Event-> funkoNames.getSelectionModel().selectPrevious());

        next.setOnAction(Event-> funkoNames.getSelectionModel().selectNext());

        remove.setOnAction(Event-> {
            if(funkoNames.getItems().size()!=0)
                funkoNames.getItems().remove(funkoNames.getSelectionModel().getSelectedIndex());
        });

            //setting menu Events
        openFile.setOnAction(Event->{});
        saveFile.setOnAction(Event->{
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File newFile = fileChooser.showSaveDialog(stage);
            try{

                fileWriter = new FileWriter(newFile);
                printWriter = new PrintWriter(fileWriter);

                for(int i =0; i < altVbox.getChildren().size();i++)
                {
                    Label tempLabel;
                    Hyperlink tempHyperlink;
                    String[] tokens;
                    if(i%2==0){
                        tempLabel = (Label)altVbox.getChildren().get(i);
                        tokens = tempLabel.getText().split("'");
                        printWriter.println("Name:" + tempLabel.getId());
                    }
                    else{
                        tempHyperlink = (Hyperlink)altVbox.getChildren().get(i);
                         tokens = tempHyperlink.getText().split("'");
                    }
                    printWriter.println(tokens[0]);
                    if((i+1)%2==0)
                        printWriter.println();
                }
                fileWriter.close();
                printWriter.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Error Saving File!");
                alert.show();
            }
        });
        exitProgram.setOnAction(Event-> System.exit(0));

        //GUI SET UP

            //setting size for list view
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

            //setting the top/menu up
        Menu menu = new Menu("File");
        menu.getItems().addAll(openFile, new SeparatorMenuItem(), saveFile, new SeparatorMenuItem(), exitProgram);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(menu);
        borderPane.setTop(menuBar);

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