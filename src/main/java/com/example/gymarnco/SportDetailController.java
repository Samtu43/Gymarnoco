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
import java.util.Arrays;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

        public double calculateTotalPrice() {
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
        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
            selectedDate = LocalDate.now();

            datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
                if (newDate != null) {
                    selectedDate = newDate;
                    selectedTimeSlot = null;
                    selectedCourt = null;

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

    public void initData(String sportName, String description) {
        this.currentSport = sportName;
        this.currentDescription = description;
        detailTitleLabel.setText(sportName);
        detailSubtitleLabel.setText(description);

        slotsContainer.getChildren().clear();
        slotsContainer.getChildren().add(new Label("Select a date and time to view available courts..."));

        System.out.println("Initialized with sport: " + sportName);
    }

    @FXML
    private void handleSelectTime(ActionEvent event) { // <-- Added ActionEvent
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

        String[] timeSlots = {
                "08:00 - 09:00", "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00",
                "12:00 - 01:00", "01:00 - 02:00", "02:00 - 03:00", "03:00 - 04:00",
                "04:00 - 05:00", "05:00 - 06:00"
        };

        int row = 0;
        for (String slot : timeSlots) {
            // Get the total number of courts for this sport
            int totalCourts = getTotalCourtsForSport(currentSport);

            // Get how many are already booked
            int bookedCourts = getBookedCourtsCount(currentSport, date, slot);

            // Calculate available slots
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

    private int getBookedCourtsCount(String sportType, LocalDate date, String timeSlot) {
        int bookedCount = 0;
        String sql = "SELECT COUNT(*) AS count FROM bookings b " +
                "JOIN sports s ON b.sport_id = s.id " +
                "WHERE s.type = ? AND b.date_booked = ? AND b.time_slot = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String dbType = mapToDbType(sportType);
            stmt.setString(1, dbType);
            stmt.setString(2, date.toString());
            stmt.setString(3, timeSlot);

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

    private int getTotalCourtsForSport(String sport) {
        switch (sport) {
            case "BASKETBALL":
                return 3; // 3 courts
            case "VOLLEYBALL":
                return 2; // 2 courts
            case "BADMINTON":
                return 2; // 2 courts
            case "JOGGING TRACK":
                return 1; // 1 track
            case "SEPAK TAKRAW":
                return 1; // 1 court
            case "FITNESS GYM":
                return 1; // 1 gym
            default:
                return 0;
        }
    }


    private boolean isTimeSlotAvailable(String sportType, LocalDate date, String timeSlot) {
        boolean available = true;
        String sql = "SELECT COUNT(*) AS count FROM bookings b " +
                "JOIN sports s ON b.sport_id = s.id " +
                "WHERE s.type = ? AND b.date_booked = ? AND b.time_slot = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sportType);
            stmt.setString(2, date.toString());
            stmt.setString(3, timeSlot);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                available = count == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error checking time slot availability: " + e.getMessage());
        }

        return available;
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
        this.selectedCourt = null;

        // Reset styles of all slots
        timeSlotsGrid.getChildren().forEach(node -> {
            if (node instanceof HBox) {
                HBox box = (HBox) node;
                box.setStyle(box.getStyle().replace("-fx-background-color: #3b82f6;", "-fx-background-color: #ecfdf5;"));
            }
        });

        // Highlight selected slot in blue
        slotBox.setStyle("-fx-padding: 12; -fx-background-color: #3b82f6; -fx-background-radius: 8; -fx-border-color: #2563eb; -fx-border-width: 2; -fx-border-radius: 8;");

        if (selectedTimeLabel != null) {
            selectedTimeLabel.setText("Selected: " + timeSlot + " on " + selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            selectedTimeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-color: #3b82f6; -fx-background-radius: 8;");
        }

        System.out.println("Time slot selected: " + timeSlot);
        loadCourtsForTimeSlot(timeSlot);
    }


    private void loadCourtsForTimeSlot(String timeSlot) {
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
        switch (sport) {
            case "BASKETBALL":
                return Arrays.asList(
                        new BookingSlot("Court 1 (Indoor)", selectedTimeSlot != null ? selectedTimeSlot : "10:00 - 12:00", 1, 250.00),
                        new BookingSlot("Court 2 (Indoor)", selectedTimeSlot != null ? selectedTimeSlot : "10:00 - 12:00", 1, 250.00),
                        new BookingSlot("Court 3 (Outdoor)", selectedTimeSlot != null ? selectedTimeSlot : "16:00 - 18:00", 1, 200.00)
                );
            case "VOLLEYBALL":
                return Arrays.asList(
                        new BookingSlot("Beach Court A", selectedTimeSlot != null ? selectedTimeSlot : "14:00 - 16:00", 1, 225.00),
                        new BookingSlot("Indoor Hall", selectedTimeSlot != null ? selectedTimeSlot : "18:00 - 20:00", 1, 250.00)
                );
            case "BADMINTON":
                return Arrays.asList(
                        new BookingSlot("AC Court A", selectedTimeSlot != null ? selectedTimeSlot : "19:00 - 21:00", 1, 150.00),
                        new BookingSlot("AC Court B", selectedTimeSlot != null ? selectedTimeSlot : "19:00 - 21:00", 1, 150.00)
                );
            case "JOGGING TRACK":
                return Arrays.asList(
                        new BookingSlot("Track Lane 1-4", selectedTimeSlot != null ? selectedTimeSlot : "06:00 - 08:00", 1, 50.00)
                );
            case "SEPAK TAKRAW":
                return Arrays.asList(
                        new BookingSlot("Traditional Court", selectedTimeSlot != null ? selectedTimeSlot : "15:00 - 17:00", 1, 200.00)
                );
            case "FITNESS GYM":
                return Arrays.asList(
                        new BookingSlot("Main Gym Area", selectedTimeSlot != null ? selectedTimeSlot : "07:00 - 09:00", 1, 175.00)
                );
            default:
                return Arrays.asList();
        }
    }

    private List<BookingSlot> selectedCourts = new ArrayList<>();

    // Update the addSlotToContainer method
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
            // Toggle selection
            if (selectedCourts.contains(slot)) {
                // Deselect
                selectedCourts.remove(slot);
                System.out.println("Court deselected: " + slot.courtName);
                slotItem.setStyle("-fx-background-color: #f0f4f7; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT;");
                bookButton.setText("SELECT COURT");
                bookButton.setStyle("-fx-background-color: #4f9eff; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
            } else {
                // Select
                selectedCourts.add(slot);
                System.out.println("Court selected: " + slot.courtName);
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



    @FXML
    private void handleNextAction(ActionEvent event) {
        System.out.println("NEXT button clicked!");
        System.out.println("Selected date: " + selectedDate);
        System.out.println("Selected time: " + selectedTimeSlot);
        System.out.println("Selected courts count: " + selectedCourts.size());

        if (selectedTimeSlot == null) {
            showAlert("No Time Selected", "Please select a time slot before proceeding.");
            return;
        }

        if (selectedCourts.isEmpty()) {
            showAlert("No Court Selected", "Please select at least one court before proceeding.");
            return;
        }

        // Validate all selected courts exist in database
        for (BookingSlot court : selectedCourts) {
            int sportId = getSportId(currentSport, court.getCourtName());
            if (sportId == -1) {
                System.err.println("Could not find sport_id for: " + currentSport + " / " + court.getCourtName());
                showAlert("Error", "Court '" + court.getCourtName() + "' not found in database.");
                return;
            }
        }

        try {
            System.out.println("Loading UserDetailsPage.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gymarnco/UserDetailsController.fxml"));
            Parent userDetailsParent = loader.load();

            UserDetailsController controller = loader.getController();
            controller.setBookingData(currentSport, currentDescription, selectedDate, selectedTimeSlot, selectedCourts);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(userDetailsParent);
            stage.setScene(scene);
            stage.setTitle("Enter Your Details - GYM ARNOCO");

            stage.show();
            System.out.println("Successfully navigated to UserDetailsPage");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading UserDetailsPage.fxml");
            showAlert("Error", "Could not load user details page: " + e.getMessage());
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

    private void saveBookingToDB(int userId, int sportId) {
        if (selectedDate == null || selectedTimeSlot == null || selectedCourt == null) return;

        String sql = "INSERT INTO bookings (user_id, sport_id, date_booked, time_slot) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, sportId);
            stmt.setString(3, selectedDate.toString());
            stmt.setString(4, selectedTimeSlot);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Booking saved to database successfully!");
            } else {
                System.err.println("Failed to save booking!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error saving booking: " + e.getMessage());
            showAlert("Database Error", "Could not save booking: " + e.getMessage());
        }
    }

    private int getSportId(String sportName, String courtName) {
        String sql = "SELECT id FROM sports WHERE type = ? AND court = ?";

        String dbType = mapToDbType(sportName);
        String dbCourt = mapToDbCourt(sportName, courtName);

        System.out.println("=== DEBUG getSportId ===");
        System.out.println("Input sportName: " + sportName);
        System.out.println("Input courtName: " + courtName);
        System.out.println("Mapped dbType: " + dbType);
        System.out.println("Mapped dbCourt: " + dbCourt);

        if (dbType == null || dbCourt == null) {
            System.err.println("Mapping returned null!");
            return -1;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dbType);
            stmt.setString(2, dbCourt);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                System.out.println("Found sport_id: " + id);
                return id;
            } else {
                System.err.println("No matching record found in database!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }
    private String mapToDbType(String sportName) {
        switch (sportName) {
            case "BASKETBALL": return "Basketball";
            case "VOLLEYBALL": return "Volleyball";
            case "BADMINTON": return "Badminton";
            case "JOGGING TRACK": return "Jogging Track";
            case "SEPAK TAKRAW": return "Sepak Takraw";
            default: return null;
        }
    }

    private String mapToDbCourt(String sportName, String courtDisplayName) {
        switch (sportName) {
            case "BASKETBALL":
                if (courtDisplayName.contains("Indoor")) return "Indoor";
                if (courtDisplayName.contains("Outdoor")) return "Outdoor";
                break;
            case "VOLLEYBALL":
                if (courtDisplayName.contains("Beach")) return "Beach Court";
                if (courtDisplayName.contains("Indoor")) return "Indoor";
                break;
            case "BADMINTON":
                if (courtDisplayName.contains("Court A")) return "Court A";
                if (courtDisplayName.contains("Court B")) return "Court B";
                break;
            case "JOGGING TRACK":
                return "Track";
            case "SEPAK TAKRAW":
                return "Traditional Court";
        }
        return null;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
