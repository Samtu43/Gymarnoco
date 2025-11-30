package com.example.gymarnco;

import javafx.beans.property.*;

public class Transaction {
    private final StringProperty id;
    private final StringProperty customerName;
    private final StringProperty mobileNumber; // ADDED
    private final StringProperty email;
    private final StringProperty facility;
    private final StringProperty type;
    private final DoubleProperty amount;
    private final StringProperty date;
    private final StringProperty status;

    public Transaction(java.sql.ResultSet rs) throws java.sql.SQLException {
        // These names MUST match the aliases used in your SQL JOIN query in the controller.
        this.id = new SimpleStringProperty("T" + rs.getInt("booking_id"));
        this.customerName = new SimpleStringProperty(rs.getString("user_name"));
        this.mobileNumber = new SimpleStringProperty(rs.getString("user_mobile"));
        this.email = new SimpleStringProperty(rs.getString("user_email"));
        this.facility = new SimpleStringProperty(rs.getString("court_name")); // From the 'sports' table
        this.type = new SimpleStringProperty("Booking"); // Hardcoded Type
        this.amount = new SimpleDoubleProperty(rs.getDouble("total_price"));

        // Combine date and time slot
        String bookingDateTime = rs.getString("booking_date") + " " + rs.getString("time_slot");
        this.date = new SimpleStringProperty(bookingDateTime);

        this.status = new SimpleStringProperty(rs.getString("booking_status"));
    }

    // Getters for TableView binding
    public StringProperty idProperty() { return id; }
    public StringProperty customerNameProperty() { return customerName; }
    public StringProperty mobileNumberProperty() { return mobileNumber; } // ADDED GETTER
    public StringProperty emailProperty() { return email; }
    public StringProperty facilityProperty() { return facility; }
    public StringProperty typeProperty() { return type; }
    public DoubleProperty amountProperty() { return amount; }
    public StringProperty dateProperty() { return date; }
    public StringProperty statusProperty() { return status; }
}