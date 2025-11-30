package com.example.gymarnco;

import javafx.beans.property.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Transaction {

    private final StringProperty id;
    private final StringProperty customerName;
    private final StringProperty mobileNumber;
    private final StringProperty email;
    private final StringProperty facility;
    private final StringProperty type; // Now holds the payment_type
    private final DoubleProperty amount;
    private final StringProperty date;
    private final StringProperty status;

    // --- 1. Standard Constructor (For Dummy/Error Data) ---
    // Kept here for compatibility, though loadTransactionDataFromDB() doesn't use it.
    public Transaction(String id, String customerName, String mobileNumber, String email, String facility, String type, double amount, String date, String status) {
        this.id = new SimpleStringProperty(id);
        this.customerName = new SimpleStringProperty(customerName);
        this.mobileNumber = new SimpleStringProperty(mobileNumber);
        this.email = new SimpleStringProperty(email);
        this.facility = new SimpleStringProperty(facility);
        this.type = new SimpleStringProperty(type);
        this.amount = new SimpleDoubleProperty(amount);
        this.date = new SimpleStringProperty(date);
        this.status = new SimpleStringProperty(status);
    }

    // --- 2. Database Constructor (The key one used by the Controller) ---
    public Transaction(ResultSet rs) throws SQLException {
        // NOTE: Column names used here MUST match the AS aliases and table columns in the SQL JOIN query.

        // ID: Format as T001
        this.id = new SimpleStringProperty("T" + rs.getInt("booking_id"));

        // Customer Info from 'users' table
        this.customerName = new SimpleStringProperty(rs.getString("user_name"));
        this.mobileNumber = new SimpleStringProperty(rs.getString("user_mobile"));
        this.email = new SimpleStringProperty(rs.getString("user_email"));

        // Facility (Court Name) from 'sports' table
        this.facility = new SimpleStringProperty(rs.getString("court_name"));

        // Type (Payment Type) from 'bookings' table
        this.type = new SimpleStringProperty(rs.getString("payment_type"));

        // Amount
        this.amount = new SimpleDoubleProperty(rs.getDouble("total_price"));

        // Date: Combine date and time slot
        String bookingDateTime = rs.getString("booking_date") + " " + rs.getString("time_slot");
        this.date = new SimpleStringProperty(bookingDateTime);

        // Status: Translate "Active" DB status to "Pending" for display
        String dbStatus = rs.getString("booking_status");
        String displayStatus = dbStatus;

        if (dbStatus.equalsIgnoreCase("Active")) {
            displayStatus = "Pending";
        }
        this.status = new SimpleStringProperty(displayStatus);
    }

    // --- Property Getters (Essential for TableView) ---
    public StringProperty idProperty() { return id; }
    public StringProperty customerNameProperty() { return customerName; }
    public StringProperty mobileNumberProperty() { return mobileNumber; }
    public StringProperty emailProperty() { return email; }
    public StringProperty facilityProperty() { return facility; }
    public StringProperty typeProperty() { return type; }
    public DoubleProperty amountProperty() { return amount; }
    public StringProperty dateProperty() { return date; }
    public StringProperty statusProperty() { return status; }
}