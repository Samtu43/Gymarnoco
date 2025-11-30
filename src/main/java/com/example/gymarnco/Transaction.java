package com.example.gymarnco;

import javafx.beans.property.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class Transaction {

    private final StringProperty id;
    private final StringProperty customerName;
    private final StringProperty mobileNumber;
    private final StringProperty email;
    private final StringProperty facility;
    private final StringProperty type;
    private final DoubleProperty amount;
    private final StringProperty date;
    private final StringProperty status;

    private LocalDateTime bookingDateTime;

    // --- Constructor using ResultSet ---
    public Transaction(ResultSet rs) throws SQLException {
        // ID is prefixed with 'T' for display
        this.id = new SimpleStringProperty("T" + rs.getInt("booking_id"));
        this.customerName = new SimpleStringProperty(rs.getString("user_name"));
        this.mobileNumber = new SimpleStringProperty(rs.getString("user_mobile"));
        this.email = new SimpleStringProperty(rs.getString("user_email"));
        this.facility = new SimpleStringProperty(rs.getString("court_name"));
        this.type = new SimpleStringProperty(rs.getString("payment_type"));
        this.amount = new SimpleDoubleProperty(rs.getDouble("total_price"));

        // Combine date and time for display
        String bookingDateTimeDisplay = rs.getString("booking_date") + " " + rs.getString("time_slot");
        this.date = new SimpleStringProperty(bookingDateTimeDisplay);

        String dbStatus = rs.getString("booking_status");
        String displayStatus = dbStatus;

        // DB Status "Active" is displayed as "Pending" in the UI
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

    // --- Logic Getters and Setters ---

    // FIX: Required by the controller (Admin_Dashboard_Controller.java)
    public String getId() {
        return id.get();
    }

    public String getStatus() {
        return status.get();
    }

    public void setBookingDateTime(LocalDateTime bookingDateTime) {
        this.bookingDateTime = bookingDateTime;
    }

    /** Returns true if the booking time is before the current system time. */
    public boolean isPastDue() {
        if (this.bookingDateTime == null) {
            // Returns false if the date could not be parsed
            return false;
        }
        return this.bookingDateTime.isBefore(LocalDateTime.now());
    }
}