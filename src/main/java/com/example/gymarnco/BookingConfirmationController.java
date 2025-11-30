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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class BookingConfirmationController {

    // FXML Elements (Labels and Buttons)
    @FXML private Label sportNameLabel;
    @FXML private Label courtNameLabel;
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private Label pricePerHourLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Label bookingIdLabel;
    @FXML private Label customerNameLabel;
    @FXML private Label customerPhoneLabel;
    @FXML private Label customerEmailLabel;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;

    // Instance Variables to hold data
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
    private List<SportDetailController.BookingSlot> courts; // Holds selected courts

    // -------------------------------------------------------------------------
    // --- 1. DATA INITIALIZATION & UI UPDATE ---
    // -------------------------------------------------------------------------

    /**
     * Receives all booking and user data from UserDetailsController and updates the confirmation UI.
     */
    public void setBookingData(String sport, String description, LocalDate date, String time,
                               List<SportDetailController.BookingSlot> selectedCourts, String name, String phone, String email) {

        // Store received data
        this.sportName = sport;
        this.courts = selectedCourts;
        this.bookingDate = date;
        this.timeSlot = time;
        this.customerName = name;
        this.customerPhone = phone;
        this.customerEmail = email;

        // Calculate and format derived data
        this.totalPrice = selectedCourts.stream()
                .mapToDouble(SportDetailController.BookingSlot::calculateTotalPrice)
                .sum();

        this.pricePerHour = selectedCourts.stream()
                .mapToDouble(SportDetailController.BookingSlot::getPricePerHour)
                .average()
                .orElse(0.0);

        this.courtName = selectedCourts.stream()
                .map(SportDetailController.BookingSlot::getCourtName)
                .collect(Collectors.joining(", "));

        // Generate ID now (Used for display/receipt only, DB generates primary key ID)
        this.bookingId = generateBookingId();

        // Update UI labels
        sportNameLabel.setText(sport);
        courtNameLabel.setText(courtName);
        dateLabel.setText(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        timeLabel.setText(time);

        // Show price breakdown for multiple courts
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

    /**
     * Generates a temporary client-side booking ID for display purposes.
     */
    private String generateBookingId() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return "GYM-" + timestamp + "-" + random;
    }

    // -------------------------------------------------------------------------
    // --- 2. ACTION HANDLERS ---
    // -------------------------------------------------------------------------

    @FXML
    private void handleConfirmBooking(ActionEvent event) {

        String paymentType = getPaymentType(); // Retrieves payment type (currently hardcoded placeholder)

        if (paymentType == null || paymentType.isEmpty()) {
            showAlert("Payment Missing", "Please select a payment method before confirming.", AlertType.WARNING);
            return;
        }

        // --- DATABASE INSERTION ---
        if (insertBooking(paymentType)) {
            // SUCCESS - Show alert and navigate
            showAlert("Booking Confirmed!",
                    "Your booking has been successfully confirmed.\n\n" +
                            "Booking ID: " + bookingId + "\n" +
                            "A confirmation email has been sent to:\n" + customerEmail,
                    AlertType.INFORMATION);

            navigateToReceipt(event);

        } else {
            // FAILURE - Alert is handled inside insertBooking methods
            // Fallback error alert if nothing else was thrown
            showAlert("Database Error", "Failed to finalize your booking due to an unknown database issue.", AlertType.ERROR);
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

    // -------------------------------------------------------------------------
    // --- 3. DATABASE LOGIC ---
    // -------------------------------------------------------------------------

    /**
     * Placeholder method to get the selected payment type from the UI.
     * Replace the 'GCASH' return with logic to read from a ComboBox, Radio Buttons, etc.
     */
    private String getPaymentType() {
        // !!! IMPORTANT: THIS IS A HARDCODED PLACEHOLDER !!!
        // Replace this with UI logic (e.g., return paymentComboBox.getValue();)
        return "GCASH";
    }

    /**
     * Handles the complex task of finding the user and inserting all court bookings.
     */
    private boolean insertBooking(String paymentType) {
        // --- 1. Get/Insert User and get user_id ---
        int userId = findOrInsertUser();
        if (userId == -1) {
            // Error alert is handled inside findOrInsertUser or its caller method
            return false;
        }

        // --- 2. Insert all selected court bookings ---
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction for safety

            String bookingSql = "INSERT INTO bookings (user_id, court_id, booking_date, time_slot, total_price, payment_type, booking_status, is_completed) " +
                    "VALUES (?, ?, ?, ?, ?, ?, 'Active', 0)";

            try (PreparedStatement bookingPs = conn.prepareStatement(bookingSql)) {
                for (SportDetailController.BookingSlot courtSlot : courts) {

                    bookingPs.setInt(1, userId);
                    bookingPs.setInt(2, courtSlot.getCourtId());
                    bookingPs.setString(3, bookingDate.toString());
                    bookingPs.setString(4, timeSlot);
                    bookingPs.setDouble(5, courtSlot.calculateTotalPrice());
                    bookingPs.setString(6, paymentType);

                    bookingPs.addBatch(); // Batch insertion for multiple courts
                }

                bookingPs.executeBatch();
                conn.commit(); // Commit all bookings
                return true;

            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                System.err.println("Booking insert failed: " + e.getMessage());
                showAlert("Booking Failure", "A database error occurred while saving the booking. Please check logs.", AlertType.ERROR);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Database connection error during booking: " + e.getMessage());
            showAlert("Connection Error", "Cannot connect to the database. Please verify your connection settings.", AlertType.ERROR);
            return false;
        }
    }

    /**
     * Checks if user exists by email. If yes, returns ID. If no, inserts new user and returns new ID.
     */
    private int findOrInsertUser() {
        int userId = -1;
        String findUserSql = "SELECT id FROM users WHERE email_address = ?";
        String insertUserSql = "INSERT INTO users (name, phone_number, email_address) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // --- A. Find existing user ---
            try (PreparedStatement findPs = conn.prepareStatement(findUserSql)) {
                findPs.setString(1, customerEmail);
                ResultSet rs = findPs.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }

            // --- B. Insert new user (if not found) ---
            try (PreparedStatement insertPs = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                insertPs.setString(1, customerName);
                insertPs.setString(2, customerPhone);
                insertPs.setString(3, customerEmail);
                insertPs.executeUpdate();

                ResultSet rs = insertPs.getGeneratedKeys();
                if (rs.next()) {
                    userId = rs.getInt(1);
                } else {
                    System.err.println("Failed to get generated user ID.");
                }
            }
        } catch (SQLException e) {
            System.err.println("User database operation failed: " + e.getMessage());
        }
        return userId;
    }


    // -------------------------------------------------------------------------
    // --- 4. NAVIGATION & HELPER METHODS ---
    // -------------------------------------------------------------------------

    private void navigateToReceipt(ActionEvent event) {
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
            showAlert("Navigation Error", "Could not load the receipt page.", AlertType.ERROR);
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

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}