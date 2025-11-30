package com.example.gymarnco;

import javafx.beans.property.*;

public class Transaction {
    private final StringProperty id;
    private final StringProperty customerName;
    private final StringProperty email;
    private final StringProperty facility;
    private final StringProperty type;
    private final DoubleProperty amount;
    private final StringProperty date;
    private final StringProperty status;

    public Transaction(String id, String customerName, String email, String facility, String type, double amount, String date, String status) {
        this.id = new SimpleStringProperty(id);
        this.customerName = new SimpleStringProperty(customerName);
        this.email = new SimpleStringProperty(email);
        this.facility = new SimpleStringProperty(facility);
        this.type = new SimpleStringProperty(type);
        this.amount = new SimpleDoubleProperty(amount);
        this.date = new SimpleStringProperty(date);
        this.status = new SimpleStringProperty(status);
    }

    // Getters for TableView binding
    public StringProperty idProperty() { return id; }
    public StringProperty customerNameProperty() { return customerName; }
    public StringProperty emailProperty() { return email; }
    public StringProperty facilityProperty() { return facility; }
    public StringProperty typeProperty() { return type; }
    public DoubleProperty amountProperty() { return amount; }
    public StringProperty dateProperty() { return date; }
    public StringProperty statusProperty() { return status; }
}