package visualiser;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Visualiser extends Application {


    // TODO: rename fxml file
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("visualiser.fxml"));
        primaryStage.setTitle("Scheduling Problem Visualiser");
        primaryStage.setScene(new Scene(root));
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
