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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * This is the controller for the main menu.
 */
public class MainMenuController implements Initializable {
    /**
     * Observable array list for appointment data
     */
    @FXML
    private final ObservableList<ObservableList> appointmentData = FXCollections.observableArrayList();
    /**
     * Observable array list for customer data
     */
    @FXML
    private final ObservableList<ObservableList> customerData = FXCollections.observableArrayList();
    /**
     * Observable array list for current month appointments
     */
    @FXML
    private final ObservableList<ObservableList> currentMonthData = FXCollections.observableArrayList();
    /**
     * Observable array list for current week appointments
     */
    @FXML
    private final ObservableList<ObservableList> currentWeekData = FXCollections.observableArrayList();
    /**
     * Observable array list for selected contact appointment details
     */
    @FXML
    private final ObservableList<ObservableList> selectedContactData = FXCollections.observableArrayList();
    /**
     * Array list that holds all contact data
     */
    @FXML
    private final ObservableList<String> contactData = FXCollections.observableArrayList();
    Stage stage;
    Parent scene;
    /**
     * Appointment table view
     */
    @FXML
    private TableView appointmentTableView;
    /**
     * Customer table view
     */
    @FXML
    private TableView<Object> customerTableView;
    /**
     * Label to display that customer is deleted
     */
    @FXML
    private Label customerDelete;
    /**
     * Label to display appointment ID and what type of appointment was deleted.
     */
    @FXML
    private Label appointmentDelete;
    /**
     * Selects all appointments
     */
    @FXML
    private RadioButton allAppointments;
    /**
     * Informs user if there is an appointment within 15 minutes of login or not
     */
    @FXML
    private Label appointmentLabel;
    /**
     * Displays appointments by type
     */
    @FXML
    private Label appointmentsByType;
    /**
     * Displays appointments by month
     */
    @FXML
    private Label appointmentsByMonth;
    /**
     * Displays appointments by location
     */
    @FXML
    private Label appointmentsByLocation;
    /**
     * Combo box that holds all contacts to filter for the table view
     */
    @FXML
    private ComboBox<String> contactComboBox;
    /**
     * Radio button for current month selection
     */
    @FXML
    private RadioButton currentMonth;
    /**
     * Radio button for current week selection
     */
    @FXML
    private RadioButton currentWeek;

    /**
     * Shows all appointments using a lambda function
     * The lambda function sets the table view to all appointments when clicked using setOnAction and if it fails, it will print the stack trace
     */
    private void showAllAppointments() {
        allAppointments.setOnAction(e -> {
            try {
                contactComboBox.setValue(null);
                fillAppointmentTable();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    /**
     * Shows the current month appointments
     */
    @FXML
    private void showCurrentMonth() throws SQLException {
        contactComboBox.setValue(null);
        String query = "select Appointment_ID, Title, Description, Location, Type, Start, End, customers.Customer_Name as Customer, " +
                "users.User_Name as User, contacts.Contact_Name as Contact from appointments inner join customers on appointments.Customer_ID = customers.Customer_ID " +
                "inner join users on appointments.User_ID = users.User_ID inner join contacts on appointments.Contact_ID = contacts.Contact_ID " +
                "where month(Start) = month(current_date()) and month(End) = month(current_date())";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        Helper.fillTableView(appointmentTableView, currentMonthData, rs);
    }

    /**
     * Shows the current week appointments
     *
     * @throws SQLException sql error
     */
    @FXML
    private void showCurrentWeek() throws SQLException {
        contactComboBox.setValue(null);
        String query = "select Appointment_ID, Title, Description, Location, Type, Start, End, customers.Customer_Name as Customer, " +
                "users.User_Name as User, contacts.Contact_Name as Contact from appointments inner join customers on appointments.Customer_ID = customers.Customer_ID " +
                "inner join users on appointments.User_ID = users.User_ID inner join contacts on appointments.Contact_ID = contacts.Contact_ID " +
                "where week(Start) = week(current_date()) and week(End) = week(current_date())";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        Helper.fillTableView(appointmentTableView, currentWeekData, rs);
    }

    /**
     * Opens up add appointment menu
     *
     * @param actionEvent user clicking on add
     */
    @FXML
    private void openAddAppointmentMenu(ActionEvent actionEvent) throws IOException {
        stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/appointment.fxml")));
        stage.setTitle("Add Appointment");
        stage.setScene(new Scene(scene));
        stage.show();
    }

    /**
     * Opens up modify appointment menu and sends the selected item over to the appointment controller.
     *
     * @param actionEvent user clicking on modify
     */
    @FXML
    private void openModifyAppointmentMenu(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/appointment.fxml"));
        loader.load();

        AppointmentController controller = loader.getController();
        Object appointment = appointmentTableView.getSelectionModel().getSelectedItem();
        if (appointment == null) return;

        controller.setAppointment(appointment);

        stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        Parent scene = loader.getRoot();
        stage.setTitle("Modify Appointment");
        stage.setScene(new Scene(scene));
        stage.show();
    }

    /**
     * Deletes appointment if user clicks OK on the alert and cancels if user presses cancel.
     * Repopulates appointment tableview.
     *
     * @throws SQLException sql error
     */
    @FXML
    private void deleteAppointment() throws SQLException {
        appointmentDelete.setText("");
        customerDelete.setText("");
        Object appointment = appointmentTableView.getSelectionModel().getSelectedItem();
        String[] values = appointment.toString().replaceFirst("\\[", "").split(", ");
        Alert alert = Helper.stringToAlert(Alert.AlertType.CONFIRMATION, "Delete Appointment " + values[0], "Are you sure you want to delete appointment id " + values[0] + "?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String query = "delete from appointments where appointment_id = " + values[0];
            Statement statement = JDBC.getConnection().createStatement();
            statement.executeUpdate(query);
            appointmentDelete.setText("Appointment ID " + values[0] + " with appointment type " + values[4] + " was deleted.");
            fillAppointmentTable();
        } else {
            appointmentDelete.setText("Did not delete appointment.");
        }
    }

    /**
     * Opens up add customer menu.
     *
     * @param actionEvent user clicking on add
     */
    @FXML
    private void openAddCustomerMenu(ActionEvent actionEvent) throws IOException {
        stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/customer.fxml")));
        stage.setTitle("Add Appointment");
        stage.setScene(new Scene(scene));
        stage.show();
    }

    /**
     * Opens up modify customer menu.
     *
     * @param actionEvent user clicking on modify
     * @throws IOException input output error
     */
    @FXML
    private void openModifyCustomerMenu(ActionEvent actionEvent) throws IOException, SQLException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/customer.fxml"));
        loader.load();

        CustomerController controller = loader.getController();
        Object customer = customerTableView.getSelectionModel().getSelectedItem();
        if (customer == null) {
            return;
        }
        controller.setCustomer(customer);

        stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        Parent scene = loader.getRoot();
        stage.setTitle("Modify Customer");
        stage.setScene(new Scene(scene));
        stage.show();
    }

    /**
     * Deletes customer and related appointments.
     * Confirmation alert pops up asking user to confirm and if user confirms, database entry is deleted and tableviews are repopulated.
     * UI label displays if user deleted customer or did not.
     *
     * @throws SQLException sql error
     */
    @FXML
    private void deleteCustomer() throws SQLException {
        appointmentDelete.setText("");
        customerDelete.setText("");
        Object customer = customerTableView.getSelectionModel().getSelectedItem();
        String[] values = customer.toString().replaceFirst("\\[", "").split(", ");
        Alert alert = Helper.stringToAlert(Alert.AlertType.CONFIRMATION, "Delete Customer ID " + values[0], "Are you sure you want to delete? Deleting this customer will also delete every single " + values[1] + " appointment.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String query = "delete from appointments where Customer_ID = " + values[0];
            Statement statement = JDBC.getConnection().createStatement();
            statement.executeUpdate(query);
            query = "delete from customers where Customer_ID = " + values[0];
            statement.executeUpdate(query);
            fillCustomerTable();
            fillAppointmentTable();
            customerDelete.setText("Customer ID " + values[0] + " deleted.");
        } else {
            customerDelete.setText("Did not delete customer.");
        }
    }

    /**
     * Fills appointment table view with data from appointment data and sorts by the first column by default
     */
    private void fillAppointmentTable() throws SQLException {
        String query = "select Appointment_ID, Title, Description, Location, Type, Start, End, customers.Customer_Name as Customer, users.User_Name as User, contacts.Contact_Name as Contact from appointments inner join customers on appointments.Customer_ID = customers.Customer_ID inner join users on appointments.User_ID = users.User_ID inner join contacts on appointments.Contact_ID = contacts.Contact_ID";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        Helper.fillTableView(appointmentTableView, appointmentData, rs);
        appointmentTableView.getSortOrder().add(appointmentTableView.getColumns().get(0));
    }

    /**
     * Fills customer table view with data from customer data
     *
     * @throws SQLException sql error
     */
    private void fillCustomerTable() throws SQLException {
        String query = "select Customer_ID, Customer_Name, Address, Postal_Code, Phone, first_level_divisions.Division from customers inner join first_level_divisions on first_level_divisions.Division_ID = customers.Division_ID";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        Helper.fillTableView(customerTableView, customerData, rs);
    }

    /**
     * Sets appointment label if there is a nearby appointment within 15 minutes or not.
     *
     * @param username username to check if there is a nearby appointment
     */
    public void nearbyAppointments(String username) throws SQLException {
        String query = "select appointment_id, start, end from appointments where user_id = \"" + Helper.usernameToID(username) + "\"";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        while (rs.next()) {
            LocalDateTime appointment = LocalDateTime.parse(Helper.UTCtoLocal(rs.getString(2)), dateTimeFormatter);
            if (Helper.appointmentWithin15Minutes(appointment)) {
                appointmentLabel.setText("Appointment ID: " + rs.getString(1) + " at " + rs.getString(2) + " is within 15 minutes. ");
                return;
            }
        }
        appointmentLabel.setText("No appointments within 15 minutes.");
    }

    /**
     * Sets appointment report and filters it by month and type
     *
     * @throws SQLException sql error
     */
    private void setAppointmentsByMonthAndType() throws SQLException {
        String query = "select date_format(start, \"%M\") as 'month', type, count(*) as 'count' from appointments group by date_format(start, '%M'), type;";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        StringBuilder appointments = new StringBuilder();
        while (rs.next()) {
            appointments.append(" ").append(rs.getString(1)).append(": ").append(rs.getString(2)).append(" - ").append(rs.getString(3)).append("\n");
        }
        appointmentsByMonth.setText(appointments.toString());
    }

    /**
     * Sets appointment report by location
     */
    private void setAppointmentsByLocation() throws SQLException {
        String query = "select location, count(*) as 'count' from appointments group by location";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        StringBuilder location = new StringBuilder();
        while (rs.next()) {
            location.append(" ").append(rs.getString(1)).append(": ").append(rs.getString(2)).append("\n");
        }
        appointmentsByLocation.setText(location.toString());
    }

    /**
     * Populates the contact combo box on the top to filer the table view by contacts
     *
     * @throws SQLException sql error
     */
    private void populateContactComboBox() throws SQLException {
        String query = "select contact_name from contacts";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        Helper.fillComboBox(contactData, rs);
        contactComboBox.getItems().addAll(contactData);
    }

    /**
     * Filters the table view by contact
     *
     * @throws SQLException sql error
     */
    @FXML
    private void handleAppointmentsByContact() throws SQLException {
        allAppointments.setSelected(false);
        currentMonth.setSelected(false);
        currentWeek.setSelected(false);
        Integer contact = Helper.contactNameToID(contactComboBox.getSelectionModel().getSelectedItem());
        String query = "select Appointment_ID, Title, Type, Description, Start, End, customers.Customer_Name from appointments " +
                "inner join customers on customers.Customer_ID = appointments.Customer_ID where contact_id = " + contact;
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        Helper.fillTableView(appointmentTableView, selectedContactData, rs);
    }

    /**
     * Fills appointment table and customer table and displays appointments reports (month/type, location)
     * Populates the contact combo box
     **/
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            fillAppointmentTable();
            fillCustomerTable();
            showAllAppointments();
            setAppointmentsByMonthAndType();
            setAppointmentsByLocation();
            populateContactComboBox();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
