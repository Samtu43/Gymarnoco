package com.example.gymarnco;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BookingReceiptController {

    @FXML
    private VBox receiptContainer;

    @FXML
    private Label receiptDateLabel;

    @FXML
    private Label bookingIdLabel;

    @FXML
    private Label customerNameLabel;

    @FXML
    private Label customerPhoneLabel;

    @FXML
    private Label customerEmailLabel;

    @FXML
    private Label sportNameLabel;

    @FXML
    private Label courtNameLabel;

    @FXML
    private Label bookingDateLabel;

    @FXML
    private Label timeSlotLabel;

    @FXML
    private Label pricePerHourLabel;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button printButton;

    @FXML
    private Button doneButton;

    private String bookingId;

    public void setReceiptData(String bookingId, String sportName, String courtName,
                               LocalDate bookingDate, String timeSlot, double pricePerHour,
                               double totalPrice, String customerName, String customerPhone, String customerEmail) {
        this.bookingId = bookingId;

        // Current date and time for receipt
        String receiptDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss"));
        receiptDateLabel.setText(receiptDateTime);

        // Booking information
        bookingIdLabel.setText(bookingId);
        customerNameLabel.setText(customerName);
        customerPhoneLabel.setText(customerPhone);
        customerEmailLabel.setText(customerEmail);

        sportNameLabel.setText(sportName);
        courtNameLabel.setText(courtName);
        bookingDateLabel.setText(bookingDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        timeSlotLabel.setText(timeSlot);

        pricePerHourLabel.setText("₱ " + String.format("%.2f", pricePerHour));
        totalPriceLabel.setText("₱ " + String.format("%.2f", totalPrice));

        statusLabel.setText("CONFIRMED");
    }

    @FXML
    private void handlePrintReceipt() {
        // Create a printer job
        PrinterJob printerJob = PrinterJob.createPrinterJob();

        if (printerJob != null) {
            boolean proceed = printerJob.showPrintDialog(receiptContainer.getScene().getWindow());

            if (proceed) {
                boolean success = printerJob.printPage(receiptContainer);

                if (success) {
                    printerJob.endJob();

                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Print Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Receipt has been sent to printer successfully!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Print Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to print receipt. Please try again.");
                    alert.showAndWait();
                }
            }
        } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Printer Not Found");
            alert.setHeaderText(null);
            alert.setContentText("No printer available. Please check your printer connection.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDone(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Thank You!");
        alert.setHeaderText("Booking Completed Successfully");
        alert.setContentText(
                "Thank you for booking with GYM ARNOCO!\n\n" +
                        "Booking ID: " + bookingId + "\n\n" +
                        "See you at the gym!"
        );
        alert.showAndWait();

        // Navigate back to home
        navigateToHome(event);
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