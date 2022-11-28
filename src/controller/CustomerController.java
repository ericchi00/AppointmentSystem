package controller;

import DBAccess.JDBC;
import helper.Helper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * This class is the controller for the customer menu.
 */
public class CustomerController implements Initializable {
    /**
     * Holds all the countries from the database
     */
    @FXML
    private final ObservableList<String> countriesData = FXCollections.observableArrayList();
    /**
     * Holds all the divisions from the database
     */
    @FXML
    private final ObservableList<String> divisionData = FXCollections.observableArrayList();
    Stage stage;
    Parent scene;
    /**
     * Label that changes depending if user is adding or modifying customer.
     */
    @FXML
    private Label customerLabel;
    /**
     * customer id
     */
    @FXML
    private TextField id;
    /**
     * customer name
     */
    @FXML
    private TextField name;
    /**
     * customer address
     */
    @FXML
    private TextField address;
    /**
     * customer postal code
     */
    @FXML
    private TextField postalCode;
    /**
     * customer phone number
     */
    @FXML
    private TextField phoneNumber;
    /**
     * Combo box for countries
     */
    @FXML
    private ComboBox<String> countryComboBox;
    /**
     * Combo box for division levels
     */
    @FXML
    private ComboBox<String> divisionComboBox;
    /**
     * the current country selected
     */
    private String currentCountry;
    /**
     * Label that displays validation errors
     */
    @FXML
    private Label validation;

    /**
     * Fills out the customer menu when modifying a customer with all the attributes filled out.
     *
     * @param object the object from customer table view that is to be modified
     * @throws SQLException sql error
     */
    public void setCustomer(Object object) throws SQLException {
        customerLabel.setText("Modify Customer");
        String[] values = object.toString().replace("[", "").replace("]", "").split(", ");
        id.setText(values[0]);
        name.setText(values[1]);
        address.setText(values[2]);
        postalCode.setText(values[3]);
        phoneNumber.setText(values[4]);
        setCountryComboBox(values[5]);
        handleCountry();
        divisionComboBox.setValue(values[5]);
    }

    /**
     * Sets the value of the country when the user is modifying a customer
     *
     * @param division the division to be matched to the country ID
     * @throws SQLException
     */
    private void setCountryComboBox(String division) throws SQLException {
        String query = "select countries.Country from first_level_divisions inner join countries on countries.Country_ID = first_level_divisions.Country_ID where Division = \""
                + division + "\"";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            countryComboBox.setValue(rs.getString("Country"));
        }
    }

    /**
     * Saves the new customer or modified customer input and validates input.
     *
     * @param actionEvent user clicking on save
     * @throws SQLException sql error
     */
    @FXML
    private void handleSave(ActionEvent actionEvent) throws SQLException, IOException {
        if (validInput()) {
            String query;
            Statement statement = JDBC.getConnection().createStatement();
            String divisionID = Helper.divisionNameToID(divisionComboBox.getSelectionModel().getSelectedItem());
            if (customerLabel.getText().equals("Add Customer")) {
                query = "insert into customers (customer_name, address, postal_code, phone, division_id) values (\"" + name.getText() + "\", \"" + address.getText() + "\", \"" +
                        postalCode.getText() + "\", \"" + phoneNumber.getText() + "\", \"" + divisionID + "\")";
            } else {
                query = "update customers set Customer_Name = \"" + name.getText() + "\", Address = \"" + address.getText() + "\", Postal_Code = \"" + postalCode.getText()
                        + "\", Phone = \"" + phoneNumber.getText() + "\", " + "Division_ID = \"" + divisionID + "\" where Customer_ID = \"" + id.getText() + "\"";
            }
            statement.executeUpdate(query);
            handleCancel(actionEvent);
        }
    }

    /**
     * Validates user input
     *
     * @return false if not valid and true if all input is valid
     */
    private boolean validInput() {
        validation.setText("");
        if (name.getText().isEmpty()) {
            validation.setText("Name cannot be empty.");
            return false;
        } else if (address.getText().isEmpty()) {
            validation.setText("Address cannot be empty.");
            return false;
        } else if (postalCode.getText().isEmpty()) {
            validation.setText("Postal Code cannot be empty.");
            return false;
        } else if (phoneNumber.getText().isEmpty()) {
            validation.setText("Phone Number cannot be empty.");
            return false;
        } else if (divisionComboBox.getSelectionModel() == null || divisionComboBox.getSelectionModel().isEmpty()) {
            validation.setText("Division needs to be selected.");
            return false;
        }
        return true;
    }

    /**
     * Goes back to the main menu when closing
     *
     * @param actionEvent user clicking on Cancel button
     * @throws IOException input output error
     */
    @FXML
    private void handleCancel(ActionEvent actionEvent) throws IOException {
        stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/mainmenu.fxml")));
        stage.setTitle("Appointment System");
        stage.setScene(new Scene(scene));
        stage.show();
    }

    /**
     * Populates country combo box with countries from observable array list
     *
     * @throws SQLException sql error
     */
    private void populateCountryComboBox() throws SQLException {
        String query = "select Country from countries";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        Helper.fillComboBox(countriesData, rs);
        countryComboBox.getItems().addAll(countriesData);
    }

    /**
     * Changes the first level division combo box list depending on what the country is selected.
     */
    @FXML
    private void handleCountry() throws SQLException {
        divisionData.clear();
        divisionComboBox.getItems().clear();
        currentCountry = countryComboBox.getSelectionModel().getSelectedItem();
        String query;
        if (Objects.equals(currentCountry, "U.S")) {
            query = "SELECT Division FROM first_level_divisions where Country_ID = 1";
        } else if (Objects.equals(currentCountry, "UK")) {
            query = "SELECT Division FROM first_level_divisions where Country_ID = 2";
        } else {
            query = "SELECT Division FROM first_level_divisions where Country_ID = 3";
        }
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        Helper.fillComboBox(divisionData, rs);
        divisionComboBox.getItems().addAll(divisionData);
        divisionComboBox.valueProperty().set(null);
    }

    /**
     * Populates the country combo box list
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            populateCountryComboBox();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
