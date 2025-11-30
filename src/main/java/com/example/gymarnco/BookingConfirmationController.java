package com.example.gymarnco;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BookingConfirmationController {

    @FXML
    private Label sportNameLabel;

    @FXML
    private Label courtNameLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label pricePerHourLabel;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private Label bookingIdLabel;

    @FXML
    private Label customerNameLabel;

    @FXML
    private Label customerPhoneLabel;

    @FXML
    private Label customerEmailLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private Button cancelButton;

    private String sportName;
    private String courtName;
    private LocalDate bookingDate;
    private String timeSlot;
    private double pricePerHour;
    private double totalPrice;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String bookingId;

    public void setBookingData(String sport, String description, LocalDate date, String time,
                               SportDetailController.BookingSlot court, String name, String phone, String email) {
        this.sportName = sport;
        this.courtName = court.getCourtName();
        this.bookingDate = date;
        this.timeSlot = time;
        this.pricePerHour = court.getPricePerHour();
        this.totalPrice = court.calculateTotalPrice();
        this.customerName = name;
        this.customerPhone = phone;
        this.customerEmail = email;

        // Generate booking ID
        this.bookingId = generateBookingId();

        // Update UI labels
        sportNameLabel.setText(sport);
        courtNameLabel.setText(court.getCourtName());
        dateLabel.setText(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        timeLabel.setText(time);
        pricePerHourLabel.setText("₱ " + String.format("%.2f", pricePerHour) + "/hour");
        totalPriceLabel.setText("₱ " + String.format("%.2f", totalPrice));
        bookingIdLabel.setText(bookingId);

        customerNameLabel.setText(name);
        customerPhoneLabel.setText(phone);
        customerEmailLabel.setText(email);
    }

    private String generateBookingId() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return "GYM-" + timestamp + "-" + random;
    }

    @FXML
    private void handleConfirmBooking(ActionEvent event) {
        // Here you would save to database

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Booking Confirmed!");
        alert.setHeaderText("Your booking has been successfully confirmed");
        alert.setContentText(
                "Booking ID: " + bookingId + "\n\n" +
                        "A confirmation email has been sent to:\n" + customerEmail
        );
        alert.showAndWait();

        // Navigate to receipt page
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gymarnco/BookingReceipt.fxml"));
            Parent receiptParent = loader.load();

            // Pass booking data to receipt controller
            BookingReceiptController controller = loader.getController();
            controller.setReceiptData(bookingId, sportName, courtName, bookingDate, timeSlot,
                    pricePerHour, totalPrice, customerName, customerPhone, customerEmail);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(receiptParent);
            stage.setScene(scene);
            stage.setTitle("Booking Receipt - GYM ARNOCO");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading BookingReceipt.fxml");
        }
    }

    @FXML
    private void handleCancelBooking(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Cancel Booking");
        alert.setHeaderText("Are you sure you want to cancel?");
        alert.setContentText("This will discard your booking.");

        alert.showAndWait().ifPresent(response -> {
            if (response.getText().equals("OK")) {
                navigateToHome(event);
            }
        });
    }

    @FXML
    private void handleBackToDetails(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gymarnco/UserDetailsPage.fxml"));
            Parent userDetailsParent = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(userDetailsParent);
            stage.setScene(scene);
            stage.setTitle("Enter Your Details - GYM ARNOCO");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading UserDetailsPage.fxml");
        }
    }

    private void navigateToHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gymarnco/Bookgame.fxml"));
            Parent mainPageParent = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(mainPageParent);
            stage.setScene(scene);
            stage.setTitle("BOOK YOUR GAME - GYM ARNOCO");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Bookgame.fxml");
        }
    }
}