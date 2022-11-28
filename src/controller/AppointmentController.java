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
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * This is the controller for appointment menu.
 */
public class AppointmentController implements Initializable {
    /**
     * Observable array list for all customer data
     */
    @FXML
    private final ObservableList<String> customerData = FXCollections.observableArrayList();
    /**
     * Observable array list for all contact data
     */
    @FXML
    private final ObservableList<String> contactData = FXCollections.observableArrayList();
    /**
     * Observable array list for all user data
     */
    @FXML
    private final ObservableList<String> userData = FXCollections.observableArrayList();
    Stage stage;
    Parent scene;
    /**
     * Label for appointment that changes depending if user is adding or modifying.
     */
    @FXML
    private Label appointmentLabel;
    /**
     * appointment ID
     */
    @FXML
    private TextField appointmentID;
    /**
     * appointment title
     */
    @FXML
    private TextField title;
    /**
     * appointment description
     */
    @FXML
    private TextField description;
    /**
     * appointment location
     */
    @FXML
    private TextField location;
    /**
     * appointment type
     */
    @FXML
    private TextField type;
    /**
     * appointment date picker
     */
    @FXML
    private DatePicker date;
    /**
     * appointment end time
     */
    @FXML
    private TextField endTime;
    /**
     * Appointment start time
     */
    @FXML
    private TextField startTime;
    /**
     * combo box that lists all users
     */
    @FXML
    private ComboBox<String> userComboBox;
    /**
     * Combo box that lists all customers
     */
    @FXML
    private ComboBox<String> customerComboBox;
    /**
     * combo box that lists all contacts
     */
    @FXML
    private ComboBox<String> contactComboBox;
    /**
     * Validation label for incorrect input
     */
    @FXML
    private Label validation;

    /**
     * Fills out the appointment form when user is modifying an existing appointment.
     *
     * @param object selected item from the appointment table view
     */
    public void setAppointment(Object object) {
        appointmentLabel.setText("Modify Appointment");
        String[] values = object.toString().replace("[", "").replace("]", "").split(", ");
        String[] appointmentDate = values[5].split(" ");
        appointmentID.setText(values[0]);
        title.setText(values[1]);
        description.setText(values[2]);
        location.setText(values[3]);
        type.setText(values[4]);
        date.setValue(LocalDate.parse(appointmentDate[0]));
        startTime.setText(appointmentDate[1]);
        endTime.setText(values[6].substring(11));
        customerComboBox.setValue(values[7]);
        userComboBox.setValue(values[8]);
        contactComboBox.setValue(values[9]);
    }

    /**
     * Goes back to the main menu
     *
     * @param actionEvent user clicking on cancel
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
     * Saves appointment depending if user is creating a new appointment or modifying an existing appointment.
     *
     * @param actionEvent user clicking on save
     * @throws SQLException sql error
     * @throws IOException  input output error
     */
    @FXML
    private void saveAppointment(ActionEvent actionEvent) throws SQLException, IOException {
        if (validInput()) {
            String query;
            Statement statement = JDBC.getConnection().createStatement();
            String start = Helper.localToUTC(date.getValue() + " " + startTime.getText());
            String end = Helper.localToUTC(date.getValue() + " " + endTime.getText());
            Integer customerID = Helper.customerNameToID(customerComboBox.getSelectionModel().getSelectedItem());
            Integer userID = Helper.usernameToID(userComboBox.getSelectionModel().getSelectedItem());
            Integer contactID = Helper.contactNameToID(contactComboBox.getSelectionModel().getSelectedItem());
            if (appointmentLabel.getText().equals("Add Appointment")) {
                query = "insert into appointments (title, description, location, type, start, end, customer_id, user_id, contact_id) values (\"" + title.getText()
                        + "\", \"" + description.getText() + "\", \"" + location.getText() + "\", \"" + type.getText() + "\", \"" + start + "\", \"" + end + "\", " + customerID
                        + ", " + userID + ", " + contactID + ");";
            } else {
                query = "update appointments set Title = \"" + title.getText() + "\", Description = \"" + description.getText() + "\", Location = \""
                        + location.getText() + "\", Type = \"" + type.getText() + "\", Start = \"" + start + "\", End = \"" + end + "\", Customer_ID = \""
                        + customerID + "\", User_ID = \"" + userID + "\", Contact_ID = \"" + contactID + "\" where Appointment_ID = \"" + appointmentID.getText() + "\"";
            }
            statement.executeUpdate(query);
            handleCancel(actionEvent);
        }
    }

    /**
     * Validates user input
     *
     * @return true if input is valid, false if not
     */
    private boolean validInput() throws SQLException {
        validation.setText("");
        if (title.getText().isEmpty()) {
            validation.setText("Title cannot be empty.");
            return false;
        }
        if (description.getText().isEmpty()) {
            validation.setText("Description cannot be empty.");
            return false;
        }
        if (location.getText().isEmpty()) {
            validation.setText("Location cannot be empty.");
            return false;
        }
        if (type.getText().isEmpty()) {
            validation.setText("Type cannot be empty.");
            return false;
        }
        if (date.getValue() == null) {
            validation.setText("Date is not set correctly.");
            return false;
        }
        if (!startTime.getText().matches("(?:[01]\\d|2[0123]):(?:[012345]\\d):(?:[012345]\\d)")) {
            validation.setText("Start time is not in a valid format. (HH:mm:ss)");
            return false;
        }
        if (!endTime.getText().matches("(?:[01]\\d|2[0123]):(?:[012345]\\d):(?:[012345]\\d)")) {
            validation.setText("End time is not in a valid format. (HH:mm:ss)");
            return false;
        }
        Date start = Time.valueOf(startTime.getText());
        Date end = Time.valueOf(endTime.getText());
        if (customerComboBox.getSelectionModel() == null || customerComboBox.getSelectionModel().isEmpty()) {
            validation.setText("Customer cannot be empty.");
            return false;
        }
        if (contactComboBox.getSelectionModel() == null || contactComboBox.getSelectionModel().isEmpty()) {
            validation.setText("Contact cannot be empty.");
            return false;
        }
        if (userComboBox.getSelectionModel() == null || userComboBox.getSelectionModel().isEmpty()) {
            validation.setText("User cannot be empty.");
            return false;
        }
        if (start.after(end)) {
            validation.setText("Start time must be before end time.");
            return false;
        }
        if (start.equals(end)) {
            validation.setText("Time must be different.");
            return false;
        }
        if (checkOutsideBusinessHours()) {
            validation.setText("Time must be between 08:00 and 22:00 EST.");
            return false;
        }
        if (overLappingAppointmentHours(customerComboBox.getValue())) {
            validation.setText("Customer has overlapping appointments.");
            return false;
        }
        return true;
    }

    /**
     * Checks if hours are outside 8:00 and 22:00 EST time.
     *
     * @return true if hours are outside 8:00 and 22:00 EST.
     */
    private boolean checkOutsideBusinessHours() {
        String startString = Helper.localToEastern(startTime.getText());
        String endString = Helper.localToEastern(endTime.getText());
        LocalTime start = LocalTime.parse(startString);
        LocalTime end = LocalTime.parse(endString);
        LocalTime businessStart = LocalTime.of(8, 00);
        LocalTime businessEnd = LocalTime.of(22, 00);
        return start.isBefore(businessStart) || end.isAfter(businessEnd);
    }

    /**
     * Checks if the customer has overlapping appointment hours
     *
     * @param customer customer's appointments to be checked
     * @return true if there are overlapping appointment hours
     * @throws SQLException sql error
     */
    private boolean overLappingAppointmentHours(String customer) throws SQLException {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentTimeString = date.getValue() + " " + startTime.getText();
        String currentEndString = date.getValue() + " " + endTime.getText();
        LocalDateTime currentStart = LocalDateTime.parse(currentTimeString, dateTimeFormatter);
        LocalDateTime currentEnd = LocalDateTime.parse(currentEndString, dateTimeFormatter);
        String query = "select Start, End from appointments where customer_id = \"" + Helper.customerNameToID(customer) + "\"";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String startTimeString = Helper.UTCtoLocal(rs.getString(1));
            String endTimeString = Helper.UTCtoLocal(rs.getString(2));
            LocalDateTime start = LocalDateTime.parse(startTimeString, dateTimeFormatter);
            LocalDateTime end = LocalDateTime.parse(endTimeString, dateTimeFormatter);
            if (currentStart.equals(start) && currentEnd.equals(end)) {
                return true;
            } else if (currentStart.equals(start)) {
                return true;
            } else if (currentEnd.equals(end)) {
                return true;
            } else if (currentStart.isBefore(start) && currentEnd.isAfter(end)) {
                return true;
            } else if (currentStart.isAfter(start) && currentStart.isBefore(end) && currentEnd.isAfter(end)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Populates all combo boxes (customers, contacts, users)
     *
     * @throws SQLException sql error
     */
    private void populateComboBoxes() throws SQLException {
        String query = "select Customer_Name from customers";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        Helper.fillComboBox(customerData, rs);
        customerComboBox.getItems().addAll(customerData);

        query = "select Contact_Name from contacts";
        ps = JDBC.getConnection().prepareStatement(query);
        rs = ps.executeQuery();
        Helper.fillComboBox(contactData, rs);
        contactComboBox.getItems().addAll(contactData);

        query = "select User_Name from users";
        ps = JDBC.getConnection().prepareStatement(query);
        rs = ps.executeQuery();
        Helper.fillComboBox(userData, rs);
        userComboBox.getItems().addAll(userData);
    }

    /**
     * Populates all combo boxes with data from the database
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            populateComboBoxes();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
