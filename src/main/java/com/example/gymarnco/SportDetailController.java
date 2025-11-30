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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SportDetailController {

    // --- FXML Elements ---
    @FXML private Label detailTitleLabel;
    @FXML private Label detailSubtitleLabel;
    @FXML private VBox slotsContainer;
    @FXML private DatePicker datePicker;
    @FXML private Button selectTimeButton;
    @FXML private Label selectedTimeLabel;
    @FXML private VBox timeSlotsSection;
    @FXML private Label earliestDateLabel;
    @FXML private GridPane timeSlotsGrid;
    @FXML private Button nextButton;
    @FXML private Button backButton;

    // --- Instance Variables ---
    private int currentSportId;
    private String currentSportName;
    private String currentDescription;
    private String selectedTimeSlot;
    private LocalDate selectedDate;
    private List<BookingSlot> selectedCourts = new ArrayList<>();

    // -------------------------------------------------------------------------
    // --- BookingSlot Class (Data Model) ---
    // -------------------------------------------------------------------------
    public static class BookingSlot {
        private int courtId;
        private String courtName;
        private String time;
        private int available;
        private double pricePerHour;

        public BookingSlot(int courtId, String courtName, String time, int available, double pricePerHour) {
            this.courtId = courtId;
            this.courtName = courtName;
            this.time = time;
            this.available = available;
            this.pricePerHour = pricePerHour;
        }

        // --- Getters ---
        public int getCourtId() { return courtId; }
        public String getCourtName() { return courtName; }
        public String getTime() { return time; }
        public int getAvailable() { return available; }
        public double getPricePerHour() { return pricePerHour; }

        public double calculateTotalPrice() {
            String[] times = time.split(" - ");
            if (times.length == 2) {
                try {
                    // Simple calculation assuming hour-based slots (e.g., 10:00 - 12:00 = 2 hours)
                    int startHour = Integer.parseInt(times[0].split(":")[0]);
                    int endHour = Integer.parseInt(times[1].split(":")[0]);
                    int hours = endHour - startHour;
                    // Handle wrap-around time slots if necessary, but assumes same day here
                    return pricePerHour * hours;
                } catch (Exception e) {
                    return pricePerHour; // Default to 1 hour price on error
                }
            }
            return pricePerHour; // Default to 1 hour price if not a range
        }

        // Custom equals/hashCode for List<BookingSlot>.contains() to work correctly
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BookingSlot that = (BookingSlot) o;
            // Courts are considered equal if they have the same ID (the unique court/sport ID)
            return courtId == that.courtId;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(courtId);
        }
    }

    // -------------------------------------------------------------------------
    // --- FXML Initialization and Data Setup ---
    // -------------------------------------------------------------------------
    @FXML
    public void initialize() {
        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
            selectedDate = LocalDate.now();

            datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
                if (newDate != null) {
                    selectedDate = newDate;
                    selectedTimeSlot = null;
                    selectedCourts.clear(); // Clear selected courts on date change

                    if (selectedTimeLabel != null) {
                        selectedTimeLabel.setText("No time selected");
                        selectedTimeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280; -fx-padding: 10; -fx-background-color: #f3f4f6; -fx-background-radius: 8;");
                    }

                    if (slotsContainer != null) {
                        slotsContainer.getChildren().clear();
                        slotsContainer.getChildren().add(new Label("Select a time slot to view available courts..."));
                    }

                    if (timeSlotsSection != null) {
                        timeSlotsSection.setVisible(true);
                        timeSlotsSection.setManaged(true);
                        loadTimeSlotsForDate(newDate);
                    }
                }
            });
        }

        if (earliestDateLabel != null) {
            earliestDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        }

        System.out.println("SportDetailController initialized");
    }

    /**
     * Initializes the controller with the selected sport data.
     */
    public void initData(int sportId, String sportName, String description) {
        this.currentSportId = sportId;
        this.currentSportName = sportName;
        this.currentDescription = description;
        detailTitleLabel.setText(sportName);
        detailSubtitleLabel.setText(description);

        slotsContainer.getChildren().clear();
        slotsContainer.getChildren().add(new Label("Select a date and time to view available courts..."));

        System.out.println("Initialized with Sport ID: " + sportId + ", Name: " + sportName);

        // Load time slots immediately on load, using today's date
        if (selectedDate != null) {
            loadTimeSlotsForDate(selectedDate);
        }
    }

    // -------------------------------------------------------------------------
    // --- Time Slot Loading and Selection ---
    // -------------------------------------------------------------------------

    @FXML
    private void handleSelectTime(ActionEvent event) {
        if (selectedDate == null) {
            showAlert("No Date Selected", "Please select a date first before choosing a time slot.");
            return;
        }

        if (timeSlotsSection != null) {
            timeSlotsSection.setVisible(true);
            timeSlotsSection.setManaged(true);
            loadTimeSlotsForDate(selectedDate);
        }
    }

    private void loadTimeSlotsForDate(LocalDate date) {
        if (timeSlotsGrid == null) return;

        timeSlotsGrid.getChildren().clear();

        // The time slots used for booking
        String[] timeSlots = {
                "08:00 - 09:00", "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00",
                "12:00 - 13:00", "13:00 - 14:00", "14:00 - 15:00", "15:00 - 16:00",
                "16:00 - 17:00", "17:00 - 18:00"
        };

        // 1. Get ALL possible courts for the current sport type using the ID
        List<BookingSlot> allCourts = fetchAvailableCourts(currentSportId);
        int totalCourts = allCourts.size();

        if (totalCourts == 0) {
            showAlert("Configuration Error", "No courts found for " + currentSportName + ". Check the 'sports' table and ensure courts exist with the Sport ID: " + currentSportId);

            // Clear the slots container and grid on error
            if (slotsContainer != null) slotsContainer.getChildren().clear();
            if (timeSlotsGrid != null) timeSlotsGrid.getChildren().clear();
            return;
        }

        int row = 0;
        for (String slot : timeSlots) {

            // Get how many courts are booked for this specific slot
            int bookedCourts = getBookedCourtsCount(allCourts, date, slot);

            int availableSlots = totalCourts - bookedCourts;
            boolean isAvailable = availableSlots > 0;

            HBox timeSlotBox = createTimeSlotBox(slot, isAvailable);
            Label statusLabel = createStatusLabel(isAvailable, availableSlots);

            timeSlotsGrid.add(timeSlotBox, 0, row);
            timeSlotsGrid.add(statusLabel, 1, row);

            if (isAvailable) {
                String timeSlotFinal = slot;
                timeSlotBox.setOnMouseClicked(e -> selectTimeSlot(timeSlotFinal, timeSlotBox));
                timeSlotBox.setStyle(timeSlotBox.getStyle() + " -fx-cursor: hand;");
            }

            row++;
        }
    }

    // -------------------------------------------------------------------------
    // --- Court Loading and Selection ---
    // -------------------------------------------------------------------------

    private void loadCourtsForTimeSlot(String timeSlot) {
        slotsContainer.getChildren().clear();
        selectedCourts.clear(); // Ensure selected courts list is cleared when changing time slots

        // 1. Get ALL courts associated with the current sport type from DB using the ID
        List<BookingSlot> allCourts = fetchAvailableCourts(currentSportId);

        // If fetchAvailableCourts failed (and showed an alert), stop execution here
        if (allCourts.isEmpty()) {
            slotsContainer.getChildren().add(new Label("No court data available. Please check system configuration."));
            return;
        }

        // 2. Fetch the IDs of courts already booked for the selected date/time
        List<Integer> bookedCourtIds = getBookedCourtIds(selectedDate, timeSlot);

        // 3. Filter the list to show only available courts
        List<BookingSlot> availableSlots = allCourts.stream()
                .filter(slot -> !bookedCourtIds.contains(slot.getCourtId())) // Filter out booked courts
                .peek(slot -> slot.time = timeSlot) // Set the selected time slot on the object
                .collect(Collectors.toList());

        if (availableSlots.isEmpty()) {
            slotsContainer.getChildren().add(new Label("No courts available for this time slot on " + selectedDate.format(DateTimeFormatter.ofPattern("MMM dd")) + "."));
        } else {
            System.out.println("Displaying " + availableSlots.size() + " available courts for " + currentSportName);
            availableSlots.forEach(this::addSlotToContainer);
        }

        // Reset time label display
        updateSelectedCourtsDisplay();
    }

    /**
     * DATABASE METHOD: Fetches all court details (ID, Name, Price) using the Sport ID.
     */
    private List<BookingSlot> fetchAvailableCourts(int sportId) {
        List<BookingSlot> allCourts = new ArrayList<>();

        // 1. Retrieve the correct, case-sensitive 'type' name from the DB using the ID
        String dbType = getSportTypeById(sportId);

        if (dbType == null) {
            showAlert("Configuration Error", "Could not find a sport type name for ID: " + sportId + ". Check the ID passed from the previous screen.");
            return allCourts;
        }

        // 2. Use the retrieved type name to find the specific court entries
        // 🟢 FIX: Removed 'AND is_active = 1' which caused the SQLSyntaxErrorException
        String sql = "SELECT id, court, amount FROM sports WHERE type = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dbType); // Use the correctly retrieved DB type

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                double pricePerHour = rs.getDouble("amount");

                // Construct a BookingSlot object with the data retrieved from the DB
                allCourts.add(new BookingSlot(
                        rs.getInt("id"),
                        rs.getString("court"),
                        "N/A", // Time is filled in loadCourtsForTimeSlot
                        1, // Available count placeholder
                        pricePerHour
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not load court details using type '" + dbType + "'. Check the database connection and the 'sports' table structure.");
        }
        return allCourts;
    }

    /**
     * Retrieves the case-sensitive 'type' name (e.g., "Basketball")
     * from the database using the unique ID from the 'sports' table.
     */
    private String getSportTypeById(int sportId) {
        String dbType = null;
        String sql = "SELECT type FROM sports WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sportId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                dbType = rs.getString("type");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error retrieving sport type by ID: " + e.getMessage());
        }
        return dbType;
    }


    /**
     * DATABASE METHOD: Finds the IDs of courts already booked for a specific date and time slot.
     */
    private List<Integer> getBookedCourtIds(LocalDate date, String timeSlot) {
        List<Integer> bookedIds = new ArrayList<>();
        // Query targets the bookings table
        String sql = "SELECT court_id FROM bookings WHERE booking_date = ? AND time_slot = ? AND booking_status = 'Active'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, date.toString());
            stmt.setString(2, timeSlot);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bookedIds.add(rs.getInt("court_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error checking booked court IDs: " + e.getMessage());
        }
        return bookedIds;
    }

    /**
     * Gets the count of booked courts for a time slot for the current sport.
     */
    private int getBookedCourtsCount(List<BookingSlot> allCourts, LocalDate date, String timeSlot) {
        // Collect all possible court IDs for the current sport
        String courtIdList = allCourts.stream()
                .map(slot -> String.valueOf(slot.getCourtId()))
                .collect(Collectors.joining(","));

        if (courtIdList.isEmpty()) return 0;

        int bookedCount = 0;
        // NOTE: The booking_status check is crucial to only count active bookings
        String sql = "SELECT COUNT(court_id) AS count FROM bookings " +
                "WHERE court_id IN (" + courtIdList + ") AND booking_date = ? AND time_slot = ? AND booking_status = 'Active'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, date.toString());
            stmt.setString(2, timeSlot);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                bookedCount = rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error checking booked courts count: " + e.getMessage());
        }

        return bookedCount;
    }


    // -------------------------------------------------------------------------
    // --- UI/Helper Methods ---
    // -------------------------------------------------------------------------

    @SuppressWarnings("unused")
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
            box.setOnMouseClicked(e -> selectTimeSlot(timeSlot, box));
            box.setStyle(box.getStyle() + " -fx-cursor: hand;");
        } else {
            box.setStyle("-fx-padding: 12; -fx-background-color: #f9fafb; -fx-background-radius: 8; -fx-opacity: 0.5;");
            Label fullLabel = new Label("Fully Booked");
            fullLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 12px;");
            box.getChildren().add(fullLabel);
        }

        return box;
    }

    @SuppressWarnings("unused")
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

    private void selectTimeSlot(String timeSlot, HBox slotBox) {
        this.selectedTimeSlot = timeSlot;
        this.selectedCourts.clear(); // Clear previously selected courts when time changes

        // Reset styles of all slots
        timeSlotsGrid.getChildren().forEach(node -> {
            if (node instanceof HBox) {
                HBox box = (HBox) node;
                // Safely remove the selection style (blue)
                String currentStyle = box.getStyle();
                String newStyle = currentStyle.contains("-fx-background-color: #3b82f6;") ?
                        currentStyle.replace("-fx-background-color: #3b82f6;", "-fx-background-color: #ecfdf5;") : currentStyle;
                // Remove blue border if it exists
                newStyle = newStyle.replace("-fx-border-color: #2563eb;", "-fx-border-color: #10b981;");
                box.setStyle(newStyle);
            }
        });

        // Highlight selected slot in blue
        slotBox.setStyle(slotBox.getStyle().replace("-fx-background-color: #ecfdf5;", "-fx-background-color: #3b82f6;")
                .replace("-fx-border-color: #10b981;", "-fx-border-color: #2563eb;")
                + " -fx-cursor: hand;");

        if (selectedTimeLabel != null) {
            selectedTimeLabel.setText("Selected: " + timeSlot + " on " + selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            selectedTimeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-color: #3b82f6; -fx-background-radius: 8;");
        }

        System.out.println("Time slot selected: " + timeSlot);
        loadCourtsForTimeSlot(timeSlot);
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

        Label availableLabel = new Label("Available");
        availableLabel.setPrefWidth(100);
        availableLabel.setStyle("-fx-text-fill: #1abc9c; -fx-font-weight: 600; -fx-font-size: 13px;");

        Button bookButton = new Button("SELECT COURT");
        bookButton.setStyle("-fx-background-color: #4f9eff; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");

        // Check if this court is already selected (for visual continuity)
        if (selectedCourts.contains(slot)) {
            slotItem.setStyle("-fx-background-color: #e0f2fe; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
            bookButton.setText("✓ SELECTED");
            bookButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
        }

        bookButton.setOnAction(e -> {
            // Toggle selection
            if (selectedCourts.contains(slot)) {
                // Deselect
                selectedCourts.remove(slot);
                System.out.println("Court deselected: " + slot.courtName + " (ID: " + slot.getCourtId() + ")");
                slotItem.setStyle("-fx-background-color: #f0f4f7; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT;");
                bookButton.setText("SELECT COURT");
                bookButton.setStyle("-fx-background-color: #4f9eff; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
            } else {
                // Select
                selectedCourts.add(slot);
                System.out.println("Court selected: " + slot.courtName + " (ID: " + slot.getCourtId() + ")");
                slotItem.setStyle("-fx-background-color: #e0f2fe; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
                bookButton.setText("✓ SELECTED");
                bookButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
            }

            // Update total display
            updateSelectedCourtsDisplay();
        });

        slotItem.getChildren().addAll(courtLabel, timeLabel, priceLabel, totalLabel, availableLabel, bookButton);
        slotsContainer.getChildren().add(slotItem);
    }

    private void updateSelectedCourtsDisplay() {
        if (selectedTimeLabel != null) {
            if (selectedCourts.isEmpty()) {
                selectedTimeLabel.setText("Selected: " + selectedTimeSlot + " on " + selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                selectedTimeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-color: #3b82f6; -fx-background-radius: 8;");
            } else {
                double totalAmount = selectedCourts.stream()
                        .mapToDouble(BookingSlot::calculateTotalPrice)
                        .sum();

                String courtsText = selectedCourts.size() + " court(s) selected";
                selectedTimeLabel.setText(courtsText + " • " + selectedTimeSlot + " • Total: ₱" + String.format("%.2f", totalAmount));
                selectedTimeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-color: #10b981; -fx-background-radius: 8;");
            }
        }
    }

    // -------------------------------------------------------------------------
    // --- Navigation & Cleanup ---
    // -------------------------------------------------------------------------

    @FXML
    private void handleNextAction(ActionEvent event) {
        if (selectedTimeSlot == null) {
            showAlert("No Time Selected", "Please select a time slot before proceeding.");
            return;
        }

        if (selectedCourts.isEmpty()) {
            showAlert("No Court Selected", "Please select at least one court before proceeding.");
            return;
        }

        // Validate that all selected courts have a valid ID (should be > 0 if loaded from DB)
        for (BookingSlot court : selectedCourts) {
            if (court.getCourtId() <= 0) {
                System.err.println("Court ID is invalid for: " + court.getCourtName());
                showAlert("System Error", "Court data is invalid (ID missing). Cannot proceed with booking.");
                return;
            }
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gymarnco/UserDetailsPage.fxml"));
            Parent userDetailsParent = loader.load();

            UserDetailsController controller = loader.getController();

            // Pass the selected booking data to the next controller
            controller.setBookingData(
                    currentSportId,
                    currentSportName,
                    currentDescription,
                    selectedDate,
                    selectedTimeSlot,
                    selectedCourts
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(userDetailsParent);
            stage.setScene(scene);
            stage.setTitle("Enter Your Details - GYM ARNOCO");

            stage.show();
            System.out.println("Successfully navigated to UserDetailsPage with " + selectedCourts.size() + " courts.");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading UserDetailsPage.fxml");
            showAlert("Error", "Could not load user details page: " + e.getMessage());
        }
    }

    /**
     * 🟢 FIX: Added `handleBackAction` to resolve the original LoadException.
     * This method navigates the user back to the main sport selection screen (Bookgame.fxml).
     */
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
            System.err.println("Error loading Bookgame.fxml from SportDetailController");
            showAlert("Navigation Error", "Could not load the previous page: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}