package com.example.gymarnco;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class SportDetailController {

    @FXML
    private Label detailTitleLabel;

    @FXML
    private Label detailSubtitleLabel;

    @FXML
    private VBox slotsContainer;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Button selectTimeButton;

    @FXML
    private Label selectedTimeLabel;

    @FXML
    private VBox timeSlotsSection;

    @FXML
    private Label earliestDateLabel;

    @FXML
    private GridPane timeSlotsGrid;

    @FXML
    private Button nextButton;

    @FXML
    private Button backButton;

    private String currentSport;
    private String currentDescription;
    private String selectedTimeSlot;
    private LocalDate selectedDate;
    private BookingSlot selectedCourt;

    // Enhanced BookingSlot class with pricing
    public static class BookingSlot {
        String courtName;
        String time;
        int available;
        double pricePerHour;

        public BookingSlot(String courtName, String time, int available, double pricePerHour) {
            this.courtName = courtName;
            this.time = time;
            this.available = available;
            this.pricePerHour = pricePerHour;
        }

        public String getCourtName() { return courtName; }
        public String getTime() { return time; }
        public int getAvailable() { return available; }
        public double getPricePerHour() { return pricePerHour; }

        // Calculate total price based on time slot duration
        public double calculateTotalPrice() {
            // Parse time slot to calculate hours (e.g., "10:00 - 12:00" = 2 hours)
            String[] times = time.split(" - ");
            if (times.length == 2) {
                try {
                    int startHour = Integer.parseInt(times[0].split(":")[0]);
                    int endHour = Integer.parseInt(times[1].split(":")[0]);
                    int hours = endHour - startHour;
                    return pricePerHour * hours;
                } catch (Exception e) {
                    return pricePerHour;
                }
            }
            return pricePerHour;
        }
    }

    @FXML
    public void initialize() {
        // Set minimum date to today
        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
            selectedDate = LocalDate.now();

            // Add listener to date picker
            datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
                if (newDate != null) {
                    selectedDate = newDate;
                    // Reset selections when date changes
                    selectedTimeSlot = null;
                    selectedCourt = null;

                    if (selectedTimeLabel != null) {
                        selectedTimeLabel.setText("No time selected");
                        selectedTimeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280; -fx-padding: 10; -fx-background-color: #f3f4f6; -fx-background-radius: 8;");
                    }

                    // Clear court container
                    if (slotsContainer != null) {
                        slotsContainer.getChildren().clear();
                        slotsContainer.getChildren().add(new Label("Select a time slot to view available courts..."));
                    }

                    // Auto-show time slots when date is selected
                    if (timeSlotsSection != null) {
                        timeSlotsSection.setVisible(true);
                        timeSlotsSection.setManaged(true);
                        loadTimeSlotsForDate(newDate);
                    }
                }
            });
        }

        // Set earliest date label
        if (earliestDateLabel != null) {
            earliestDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        }

        System.out.println("SportDetailController initialized");
    }

    public void initData(String sportName, String description) {
        this.currentSport = sportName;
        this.currentDescription = description;
        detailTitleLabel.setText(sportName);
        detailSubtitleLabel.setText(description);

        // Clear any placeholder/loading message
        slotsContainer.getChildren().clear();
        slotsContainer.getChildren().add(new Label("Select a date and time to view available courts..."));

        System.out.println("Initialized with sport: " + sportName);
    }

    @FXML
    private void handleSelectTime() {
        if (selectedDate == null) {
            showAlert("No Date Selected", "Please select a date first before choosing a time slot.");
            return;
        }

        // Show the time slots section
        if (timeSlotsSection != null) {
            timeSlotsSection.setVisible(true);
            timeSlotsSection.setManaged(true);
            loadTimeSlotsForDate(selectedDate);
        }
    }

    private void loadTimeSlotsForDate(LocalDate date) {
        if (timeSlotsGrid == null) return;

        // Clear existing time slots
        timeSlotsGrid.getChildren().clear();

        // Sample time slots
        String[] timeSlots = {
                "08:00 - 09:00", "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00",
                "12:00 - 13:00", "13:00 - 14:00", "14:00 - 15:00", "15:00 - 16:00",
                "16:00 - 17:00", "17:00 - 18:00"
        };

        int row = 0;
        for (String slot : timeSlots) {
            // Randomly determine if slot is available
            boolean isAvailable = Math.random() > 0.5;
            int availableSlots = isAvailable ? (int)(Math.random() * 3) + 1 : 0;

            // Create time slot row
            HBox timeSlotBox = createTimeSlotBox(slot, isAvailable);
            Label statusLabel = createStatusLabel(isAvailable, availableSlots);

            // Add to grid
            timeSlotsGrid.add(timeSlotBox, 0, row);
            timeSlotsGrid.add(statusLabel, 1, row);

            // Make clickable if available
            if (isAvailable) {
                String timeSlotFinal = slot;
                timeSlotBox.setOnMouseClicked(e -> selectTimeSlot(timeSlotFinal));
                timeSlotBox.setStyle(timeSlotBox.getStyle() + " -fx-cursor: hand;");
            }

            row++;
        }
    }

    private HBox createTimeSlotBox(String timeSlot, boolean isAvailable) {
        HBox box = new HBox(10);
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        box.setPadding(new Insets(12));

        Label bullet = new Label("⚪");
        bullet.setStyle("-fx-font-size: 12px;");

        Label timeLabel = new Label(timeSlot);
        timeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        box.getChildren().addAll(bullet, timeLabel);

        if (isAvailable) {
            box.setStyle("-fx-padding: 12; -fx-background-color: #ecfdf5; -fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; -fx-border-radius: 8;");
        } else {
            box.setStyle("-fx-padding: 12; -fx-background-color: #f9fafb; -fx-background-radius: 8;");
        }

        return box;
    }

    private Label createStatusLabel(boolean isAvailable, int slots) {
        Label label = new Label();

        if (isAvailable) {
            label.setText("Available Slots: " + slots);
            label.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 13px;");
        } else {
            label.setText("Fully Booked");
            label.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 13px;");
        }

        return label;
    }

    private void selectTimeSlot(String timeSlot) {
        this.selectedTimeSlot = timeSlot;
        this.selectedCourt = null; // Reset court selection

        if (selectedTimeLabel != null) {
            selectedTimeLabel.setText("Selected: " + timeSlot + " on " + selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            selectedTimeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #10b981; -fx-font-weight: bold; -fx-padding: 10; -fx-background-color: #ecfdf5; -fx-background-radius: 8;");
        }

        System.out.println("Time slot selected: " + timeSlot);

        // Load courts for selected time
        loadCourtsForTimeSlot(timeSlot);
    }

    private void loadCourtsForTimeSlot(String timeSlot) {
        // Clear and load courts based on selected date and time
        slotsContainer.getChildren().clear();

        List<BookingSlot> slots = fetchSlotsForSport(currentSport);

        if (slots.isEmpty()) {
            slotsContainer.getChildren().add(new Label("No courts available for this time slot."));
        } else {
            System.out.println("Loading " + slots.size() + " courts for " + currentSport);
            slots.forEach(this::addSlotToContainer);
        }
    }

    private List<BookingSlot> fetchSlotsForSport(String sport) {
        // Dummy data with pricing per hour
        switch (sport) {
            case "BASKETBALL":
                return Arrays.asList(
                        new BookingSlot("Court 1 (Indoor)", selectedTimeSlot != null ? selectedTimeSlot : "10:00 - 12:00", 1, 250.00),
                        new BookingSlot("Court 2 (Indoor)", selectedTimeSlot != null ? selectedTimeSlot : "10:00 - 12:00", 2, 250.00),
                        new BookingSlot("Court 3 (Outdoor)", selectedTimeSlot != null ? selectedTimeSlot : "16:00 - 18:00", 3, 200.00)
                );
            case "VOLLEYBALL":
                return Arrays.asList(
                        new BookingSlot("Beach Court A", selectedTimeSlot != null ? selectedTimeSlot : "14:00 - 16:00", 1, 225.00),
                        new BookingSlot("Indoor Hall", selectedTimeSlot != null ? selectedTimeSlot : "18:00 - 20:00", 2, 250.00)
                );
            case "BADMINTON":
                return Arrays.asList(
                        new BookingSlot("AC Court A", selectedTimeSlot != null ? selectedTimeSlot : "19:00 - 21:00", 4, 150.00),
                        new BookingSlot("AC Court B", selectedTimeSlot != null ? selectedTimeSlot : "19:00 - 21:00", 3, 150.00)
                );
            case "JOGGING TRACK":
                return Arrays.asList(
                        new BookingSlot("Track Lane 1-4", selectedTimeSlot != null ? selectedTimeSlot : "06:00 - 08:00", 10, 50.00)
                );
            case "SEPAK TAKRAW":
                return Arrays.asList(
                        new BookingSlot("Traditional Court", selectedTimeSlot != null ? selectedTimeSlot : "15:00 - 17:00", 2, 200.00)
                );
            case "FITNESS GYM":
                return Arrays.asList(
                        new BookingSlot("Main Gym Area", selectedTimeSlot != null ? selectedTimeSlot : "07:00 - 09:00", 5, 175.00)
                );
            default:
                return Arrays.asList();
        }
    }

    private void addSlotToContainer(BookingSlot slot) {
        HBox slotItem = new HBox(20);
        slotItem.setPadding(new Insets(15));
        slotItem.setStyle("-fx-background-color: #f0f4f7; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT;");

        Label courtLabel = new Label(slot.courtName);
        courtLabel.setPrefWidth(180);
        courtLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label timeLabel = new Label("Time: " + slot.time);
        timeLabel.setPrefWidth(130);
        timeLabel.setStyle("-fx-font-size: 13px;");

        Label priceLabel = new Label("₱" + String.format("%.2f", slot.getPricePerHour()) + "/hr");
        priceLabel.setPrefWidth(100);
        priceLabel.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label totalLabel = new Label("Total: ₱" + String.format("%.2f", slot.calculateTotalPrice()));
        totalLabel.setPrefWidth(120);
        totalLabel.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label availableLabel = new Label("Available: " + slot.available);
        availableLabel.setPrefWidth(100);
        availableLabel.setStyle("-fx-text-fill: #1abc9c; -fx-font-weight: 600; -fx-font-size: 13px;");

        Button bookButton = new Button("SELECT COURT");
        bookButton.setStyle("-fx-background-color: #4f9eff; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
        bookButton.setOnAction(e -> {
            selectedCourt = slot;
            System.out.println("Court selected: " + slot.courtName);

            // Highlight selected
            slotsContainer.getChildren().forEach(node -> {
                if (node instanceof HBox) {
                    node.setStyle("-fx-background-color: #f0f4f7; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT;");
                }
            });
            slotItem.setStyle("-fx-background-color: #e0f2fe; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");

            // Update button text to show selection
            bookButton.setText("✓ SELECTED");
            bookButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
        });

        slotItem.getChildren().addAll(courtLabel, timeLabel, priceLabel, totalLabel, availableLabel, bookButton);
        slotsContainer.getChildren().add(slotItem);
    }

    @FXML
    private void handleNextAction(ActionEvent event) {
        System.out.println("NEXT button clicked!");
        System.out.println("Selected date: " + selectedDate);
        System.out.println("Selected time: " + selectedTimeSlot);
        System.out.println("Selected court: " + (selectedCourt != null ? selectedCourt.getCourtName() : "null"));

        if (selectedTimeSlot == null) {
            showAlert("No Time Selected", "Please select a time slot before proceeding.");
            return;
        }

        if (selectedCourt == null) {
            showAlert("No Court Selected", "Please select a court before proceeding.");
            return;
        }

        // Navigate to user details page
        try {
            System.out.println("Loading UserDetailsPage.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gymarnco/UserDetailsController.fxml"));
            Parent userDetailsParent = loader.load();

            // Pass booking data to user details controller
            UserDetailsController controller = loader.getController();
            controller.setBookingData(currentSport, currentDescription, selectedDate, selectedTimeSlot, selectedCourt);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(userDetailsParent);
            stage.setScene(scene);
            stage.setTitle("Enter Your Details - GYM ARNOCO");
            stage.show();

            System.out.println("Successfully navigated to UserDetailsPage");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading UserDetailsPage.fxml");
            System.err.println("Error message: " + e.getMessage());
            showAlert("Error", "Could not load user details page: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unexpected error: " + e.getMessage());
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackAction(ActionEvent event) {
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}