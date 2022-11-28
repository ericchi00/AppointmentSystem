package controller;

import DBAccess.JDBC;
import helper.Helper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * This class is the controller for the login menu.
 */
public class LoginController implements Initializable {
    /**
     * The current stage
     */
    Stage stage;
    /**
     * Set to true if language is english and false if it's not
     */
    private boolean english;
    /**
     * username label
     */
    @FXML
    private Label username;
    /**
     * password label
     */
    @FXML
    private Label password;
    /**
     * Login title label
     */
    @FXML
    private Label login;
    /**
     * Button to  login
     */
    @FXML
    private Button loginButton;
    /**
     * Displays current location
     */
    @FXML
    private Label currentLocation;
    /**
     * Username text field that holds username input
     */
    @FXML
    private TextField usernameField;
    /**
     * password text field that holds password input
     */
    @FXML
    private TextField passwordField;

    /**
     * Connects to database to check if entered username and password are valid and switches scene if successful.
     * Writes to a .txt file if  login is successfully or not
     * Alert pops up if user gave incorrect information.
     *
     * @param actionEvent user clicking on Login button
     */
    @FXML
    private void handleLogin(ActionEvent actionEvent) throws IOException, SQLException {
        File loginActivity = new File("login_activity.txt");
        loginActivity.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(loginActivity, true));
        writer.newLine();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        if (loginValidation(usernameField.getText(), passwordField.getText())) {
            writer.write("User " + usernameField.getText() + " successfully logged in at " + formatter.format(date));
            switchToMainMenuScene(actionEvent);
        } else {
            writer.write("User " + usernameField.getText() + " gave invalid log-in at " + formatter.format(date));
            Alert alert;
            if (english) {
                alert = Helper.stringToAlert(Alert.AlertType.WARNING, "Incorrect Login Info", "Wrong username or password. Please try again.");
            } else {
                alert = Helper.stringToAlert(Alert.AlertType.WARNING, "Informations de connexion incorrectes", "Nom d'utilisateur ou mot de passe erroné. Veuillez réessayer.");
            }
            Optional<ButtonType> result = alert.showAndWait();
        }
        writer.close();
    }

    /**
     * Validates user login
     *
     * @param username user's username
     * @param password user's password
     * @return true if login info valid, false if not
     */
    private boolean loginValidation(String username, String password) {
        try {
            String query = "select password from users where User_Name = \"" + username + "\"";
            PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1).equals(password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Switches scene to main menu
     *
     * @param actionEvent user clicking on login with correct login information
     */
    @FXML
    private void switchToMainMenuScene(ActionEvent actionEvent) throws IOException, SQLException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/mainmenu.fxml"));
        loader.load();

        MainMenuController controller = loader.getController();
        controller.nearbyAppointments(usernameField.getText());


        stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        Parent scene = loader.getRoot();
        stage.setTitle("Appointment System");
        stage.setScene(new Scene(scene));
        stage.show();
    }

    /**
     * Sets current text and labels to French.
     */
    private void setAllToFrench() {
        username.setText("Nom d'utilisateur");
        password.setText("Le mot de passe");
        login.setText("Connexion");
        loginButton.setText("nous faire parvenir");
        currentLocation.setText("Localisation Actuelle: ");
    }

    /**
     * Sets entire login form to French if user locale is set to French.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (Locale.getDefault().equals(new Locale("fr"))) {
            english = false;
            setAllToFrench();
        } else {
            english = true;
        }
        currentLocation.setText(currentLocation.getText() + " " + ZoneId.systemDefault());
    }
}
