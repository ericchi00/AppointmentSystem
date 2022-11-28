package helper;

import DBAccess.JDBC;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * This class provides helper methods to the rest of the program
 */
public abstract class Helper {
    /**
     * Returns an alert when given the title and text
     *
     * @param alertType the alert type to set
     * @param title     title of the alert
     * @param text      text to be put into the alert
     * @return an alert with
     */
    public static Alert stringToAlert(Alert.AlertType alertType, String title, String text) {
        Alert alert = new Alert(alertType, text);
        alert.setTitle(title);
        return alert;
    }

    /**
     * Converts contact name to ID
     *
     * @param contactName name to be converted to id
     * @return name to id
     * @throws SQLException sql error
     */
    public static Integer contactNameToID(String contactName) throws SQLException {
        String query = "select Contact_ID from contacts where Contact_Name = \"" + contactName + "\"";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return Integer.parseInt(rs.getString(1));
        }
        return null;
    }

    /**
     * Converts username to id
     *
     * @param username username to be converted to id
     * @return username to id
     * @throws SQLException sql error
     */
    public static Integer usernameToID(String username) throws SQLException {
        String query = "select User_ID from users where User_Name = \"" + username + "\"";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return Integer.parseInt(rs.getString(1));
        }
        return null;
    }

    /**
     * Converts customer name to id
     *
     * @param name name to be converted
     * @return customer name to id
     * @throws SQLException sql error
     */
    public static Integer customerNameToID(String name) throws SQLException {
        String query = "select Customer_ID from customers where Customer_Name = \"" + name + "\"";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return Integer.parseInt(rs.getString(1));
        }
        return null;
    }

    /**
     * Returns true if there is an appointment within 15 mintues
     *
     * @param appointment appointment time to check
     * @return true if appointment is within 15 minutes or not
     */
    public static boolean appointmentWithin15Minutes(LocalDateTime appointment) {
        LocalDateTime current = LocalDateTime.now();
        Duration duration = Duration.between(current, appointment);
        return appointment.isAfter(current) && duration.toMinutes() <= 15;
    }

    /**
     * Dynamically fills table view using a lambda expression with for loops and while loops
     * The lambda function sets the table column's name when grabbed from the database using a simple string property
     * Converts date and time from UTC to the user's local timezone
     *
     * @param data      Observable list of observable list of data to be put into the tableview that is grabbed from the database
     * @param rs        result set from running the JDBC MYSQL query
     * @param tableView table view that will be populated with the result set data from the observable list
     */
    public static void fillTableView(TableView tableView, ObservableList<ObservableList> data, ResultSet rs) throws SQLException {
        tableView.getItems().clear();
        tableView.getColumns().clear();
        for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
            final int j = i;
            TableColumn tableColumn = new TableColumn(rs.getMetaData().getColumnLabel(i + 1));
            tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param -> new SimpleStringProperty(param.getValue().get(j).toString()));
            tableView.getColumns().addAll(tableColumn);
        }

        while (rs.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                if (rs.getString(i).matches("\\d{4}-[01]\\d-[0-3]\\d\\s[0-2]\\d((:[0-5]\\d)?){2}")) {
                    row.add(UTCtoLocal(rs.getString(i)));
                } else {
                    row.add(rs.getString(i));
                }
            }
            data.add(row);
        }
        tableView.setItems(data);
    }

    /**
     * Dynamically fills combo box using a while loop and for loop and puts the database data into an observable array list
     *
     * @param data observable list to fill up with the data base query
     * @param rs   result set from the query
     * @throws SQLException sql error
     */
    public static void fillComboBox(ObservableList<String> data, ResultSet rs) throws SQLException {
        while (rs.next()) {
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                data.add(rs.getString(i).trim());
            }
        }
    }

    /**
     * Converts division name to division id
     *
     * @param division name of division to convert to id
     * @return division ID of the division name
     * @throws SQLException sql error
     */
    public static String divisionNameToID(String division) throws SQLException {
        String query = "SELECT Division_ID FROM first_level_divisions where Division = \"" + division + "\"";
        PreparedStatement ps = JDBC.getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getString(1);
        }
        return null;
    }

    /**
     * Converts UTC time to local time
     *
     * @param timeInString time to be converted in string format
     * @return time in local timezone
     */
    public static String UTCtoLocal(String timeInString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime oldDateTime = LocalDateTime.parse(timeInString, dateTimeFormatter);
        ZoneId utc = ZoneOffset.UTC;
        ZoneId local = ZoneId.systemDefault();
        LocalDateTime newDateTime = oldDateTime.atZone(utc).withZoneSameInstant(local).toLocalDateTime();
        return newDateTime.format(dateTimeFormatter);
    }

    /**
     * Converts UTC time to Eastern time
     *
     * @param timeInString time to be converted
     * @return time in eastern time
     */
    public static String UTCtoEastern(String timeInString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime oldDateTime = LocalDateTime.parse(timeInString, dateTimeFormatter);
        ZoneId utc = ZoneOffset.UTC;
        ZoneId eastern = ZoneId.of("America/New_York");
        LocalDateTime newDateTime = oldDateTime.atZone(utc).withZoneSameInstant(eastern).toLocalDateTime();
        return newDateTime.format(dateTimeFormatter);
    }


    /**
     * Converts local computer time to eastern
     *
     * @param timeInString time to be converted
     * @return local time in eastern time
     */
    public static String localToEastern(String timeInString) {
        LocalTime oldTime = LocalTime.parse(timeInString);
        ZoneId local = ZoneId.systemDefault();
        ZoneId eastern = ZoneId.of("America/New_York");
        LocalTime newDateTime = LocalDateTime.of(LocalDate.now(), oldTime).atZone(local).withZoneSameInstant(eastern).toLocalTime();
        return newDateTime.toString();
    }

    /**
     * Converts local time to UTC time
     *
     * @param timeInString time to be converted
     * @return local time in UTC time
     */
    public static String localToUTC(String timeInString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime oldDateTime = LocalDateTime.parse(timeInString, dateTimeFormatter);
        ZoneId local = ZoneId.systemDefault();
        ZoneId utc = ZoneOffset.UTC;
        LocalDateTime newDateTime = oldDateTime.atZone(local).withZoneSameInstant(utc).toLocalDateTime();
        return newDateTime.format(dateTimeFormatter);
    }
}
