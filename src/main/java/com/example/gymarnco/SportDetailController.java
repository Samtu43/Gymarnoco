package com.example.gymarnco;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SportDetailController {

    // FXML IDs from SportDetailPage.fxml
    @FXML
    private Label detailTitleLabel;

    @FXML
    private Label detailSubtitleLabel;

    @FXML
    private VBox slotsContainer; // Container for dynamically added booking slots

    // Simple class to model a bookable slot
    private static class BookingSlot {
        String courtName;
        String time;
        int available;
        public BookingSlot(String courtName, String time, int available) {
            this.courtName = courtName;
            this.time = time;
            this.available = available;
        }
    }

    /**
     * Initializes the view with data passed from the previous scene.
     */
    public void initData(String sportName) {
        detailTitleLabel.setText(sportName);
        detailSubtitleLabel.setText("View courts, schedules, and pricing for " + sportName.toLowerCase() + ".");

        // Clear any placeholder/loading message and load dynamic slots
        slotsContainer.getChildren().clear();

        List<BookingSlot> slots = fetchSlotsForSport(sportName);

        if (slots.isEmpty()) {
            slotsContainer.getChildren().add(new Label("No courts available for booking today."));
        } else {
            slots.forEach(this::addSlotToContainer);
        }
    }

    // --- Dynamic Slot Logic ---

    private List<BookingSlot> fetchSlotsForSport(String sport) {
        // Dummy data for demonstration. In a real app, this connects to a database.
        switch (sport) {
            case "BASKETBALL":
                return Arrays.asList(
                        new BookingSlot("Court 1 (Indoor)", "10:00 - 12:00", 1),
                        new BookingSlot("Court 3 (Outdoor)", "16:00 - 18:00", 3)
                );
            case "VOLLEYBALL":
                return Arrays.asList(
                        new BookingSlot("Beach Court A", "14:00 - 16:00", 1),
                        new BookingSlot("Indoor Hall", "18:00 - 20:00", 2)
                );
            case "BADMINTON":
                return Arrays.asList(
                        new BookingSlot("AC Court A", "19:00 - 21:00", 4)
                );
            default:
                return Arrays.asList();
        }
    }

    private void addSlotToContainer(BookingSlot slot) {
        HBox slotItem = new HBox(30); // Use a slightly larger spacing
        slotItem.setPadding(new Insets(15));
        slotItem.setStyle("-fx-background-color: #f0f4f7; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT;");

        Label courtLabel = new Label(slot.courtName);
        courtLabel.setPrefWidth(200);
        courtLabel.setStyle("-fx-font-weight: bold;");

        Label timeLabel = new Label("Time: " + slot.time);
        timeLabel.setPrefWidth(150);

        Label availableLabel = new Label("Available: " + slot.available);
        availableLabel.setStyle("-fx-text-fill: #1abc9c; -fx-font-weight: 600;");

        // Simple button with a placeholder action
        Button bookButton = new Button("BOOK NOW");
        bookButton.setStyle("-fx-background-color: #4f9eff; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        bookButton.setOnAction(e -> {
            // **Placeholder for actual booking logic**
            System.out.println("Initiating booking for " + slot.courtName + " at " + slot.time);
            bookButton.setText("Booked!");
            bookButton.setDisable(true);
        });

        slotItem.getChildren().addAll(courtLabel, timeLabel, availableLabel, bookButton);
        slotsContainer.getChildren().add(slotItem);
    }

    // --- Back Navigation Handler ---

    /**
     * Handles the Back button click, loading the main sports page and switching the scene back.
     */
    @FXML
    private void handleBackAction(ActionEvent event) {
        try {
            // Load the main sport selection FXML (BookGame.fxml) using the correct package path
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gymbooking/BookGame.fxml"));
            Parent mainPageParent = loader.load();

            // Get the Stage (window) from the clicked node
            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            // Switch the scene back
            currentScene.setRoot(mainPageParent);
            stage.setTitle("BOOK YOUR GAME - GYM ARNOCO");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading the main page FXML.");
        }
    }
}