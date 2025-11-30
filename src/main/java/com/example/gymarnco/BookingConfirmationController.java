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
import java.util.List;
import java.util.stream.Collectors;

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
    private List<SportDetailController.BookingSlot> courts; // Added to store multiple courts

    // Updated method signature to accept List
    public void setBookingData(String sport, String description, LocalDate date, String time,
                               List<SportDetailController.BookingSlot> selectedCourts, String name, String phone, String email) {
        this.sportName = sport;
        this.courts = selectedCourts;
        this.bookingDate = date;
        this.timeSlot = time;
        this.customerName = name;
        this.customerPhone = phone;
        this.customerEmail = email;

        // Calculate total price for all courts
        this.totalPrice = selectedCourts.stream()
                .mapToDouble(SportDetailController.BookingSlot::calculateTotalPrice)
                .sum();

        // Calculate average price per hour (or you can show differently)
        this.pricePerHour = selectedCourts.stream()
                .mapToDouble(SportDetailController.BookingSlot::getPricePerHour)
                .average()
                .orElse(0.0);

        // Combine all court names
        this.courtName = selectedCourts.stream()
                .map(SportDetailController.BookingSlot::getCourtName)
                .collect(Collectors.joining(", "));

        // Generate booking ID
        this.bookingId = generateBookingId();

        // Update UI labels
        sportNameLabel.setText(sport);
        courtNameLabel.setText(courtName);
        dateLabel.setText(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        timeLabel.setText(time);

        // Show breakdown if multiple courts
        if (selectedCourts.size() > 1) {
            String breakdown = selectedCourts.stream()
                    .map(c -> c.getCourtName() + " (₱" + String.format("%.2f", c.getPricePerHour()) + "/hr)")
                    .collect(Collectors.joining("\n"));
            pricePerHourLabel.setText(breakdown);
        } else {
            pricePerHourLabel.setText("₱ " + String.format("%.2f", pricePerHour) + "/hour");
        }

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gymarnco/BookingReceiptController.fxml"));
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