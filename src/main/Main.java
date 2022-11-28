package main;

import DBAccess.JDBC;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

/**
 * This class starts the application along with the connection to the database.
 */
public class Main extends Application {
    /**
     * Sets the stage and scene for the application.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        if (Locale.getDefault().equals(new Locale("fr"))) {
            stage.setTitle("Connexion");
        } else {
            stage.setTitle("Login");
        }
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Main method that launches application and connects to the database.
     */
    public static void main(String[] args) {
        JDBC.openConnection();
        launch();
        JDBC.closeConnection();
    }
}