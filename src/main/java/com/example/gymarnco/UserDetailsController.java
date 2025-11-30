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
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserDetailsController {

    @FXML
    private Label sportNameLabel;

    @FXML
    private Label courtNameLabel;

    @FXML
    private Label dateTimeLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private TextField emailField;

    @FXML
    private Button proceedButton;

    @FXML
    private Button backButton;

    private String sportName;
    private String description;
    private LocalDate bookingDate;
    private String timeSlot;
    private SportDetailController.BookingSlot court;

    public void setBookingData(String sport, String desc, LocalDate date, String time, SportDetailController.BookingSlot selectedCourt) {
        this.sportName = sport;
        this.description = desc;
        this.bookingDate = date;
        this.timeSlot = time;
        this.court = selectedCourt;

        // Update UI labels
        sportNameLabel.setText(sport);
        courtNameLabel.setText(selectedCourt.getCourtName());
        dateTimeLabel.setText(date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + " • " + time);
        priceLabel.setText("₱ " + String.format("%.2f", selectedCourt.calculateTotalPrice()));
    }

    @FXML
    private void handleProceedToConfirmation(ActionEvent event) {
        // Validate inputs
        String fullName = fullNameField.getText().trim();
        String phoneNumber = phoneNumberField.getText().trim();
        String email = emailField.getText().trim();

        if (fullName.isEmpty()) {
            showAlert("Validation Error", "Please enter your full name.");
            return;
        }

        if (phoneNumber.isEmpty()) {
            showAlert("Validation Error", "Please enter your phone number.");
            return;
        }

        if (!phoneNumber.matches("\\d{11}")) {
            showAlert("Validation Error", "Please enter a valid 11-digit phone number.");
            return;
        }

        if (email.isEmpty()) {
            showAlert("Validation Error", "Please enter your email address.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Validation Error", "Please enter a valid email address.");
            return;
        }

        // Navigate to confirmation page
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gymarnco/BookingConfirmation.fxml"));
            Parent confirmationParent = loader.load();

            // Pass all data to confirmation controller
            BookingConfirmationController controller = loader.getController();
            controller.setBookingData(sportName, description, bookingDate, timeSlot, court, fullName, phoneNumber, email);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(confirmationParent);
            stage.setScene(scene);
            stage.setTitle("Confirm Your Booking - GYM ARNOCO");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load confirmation page.");
        }
    }

    @FXML
    private void handleBackAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gymarnco/SportDetailPage.fxml"));
            Parent sportDetailParent = loader.load();

            // Pass data back
            SportDetailController controller = loader.getController();
            controller.initData(sportName, description);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(sportDetailParent);
            stage.setScene(scene);
            stage.setTitle(sportName + " - GYM ARNOCO");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading SportDetailPage.fxml");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}