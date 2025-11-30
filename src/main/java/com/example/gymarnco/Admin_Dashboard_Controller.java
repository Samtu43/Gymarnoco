package com.example.gymarnco;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Admin_Dashboard_Controller {

    @FXML
    private TableView<Transaction> transactionTableView;

    // 🔑 THE FIX: Corrected format to match your database output (yyyy-MM-dd HH:mm)
    private static final DateTimeFormatter DB_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    @SuppressWarnings("unchecked")
    public void initialize() {

        ObservableList<TableColumn<Transaction, ?>> columns = transactionTableView.getColumns();

        // 1. Set up standard cell value factories (Binding Data)
        ((TableColumn<Transaction, String>) columns.get(0)).setCellValueFactory(cellData -> cellData.getValue().idProperty());
        ((TableColumn<Transaction, String>) columns.get(1)).setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        ((TableColumn<Transaction, String>) columns.get(2)).setCellValueFactory(cellData -> cellData.getValue().mobileNumberProperty());
        ((TableColumn<Transaction, String>) columns.get(3)).setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        ((TableColumn<Transaction, String>) columns.get(4)).setCellValueFactory(cellData -> cellData.getValue().facilityProperty());
        ((TableColumn<Transaction, String>) columns.get(5)).setCellValueFactory(cellData -> cellData.getValue().typeProperty());

        // AMOUNT Column (Index 6) - Formatting
        TableColumn<Transaction, Double> amountCol = (TableColumn<Transaction, Double>) columns.get(6);
        amountCol.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        amountCol.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("₱%.2f", item));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });

        ((TableColumn<Transaction, String>) columns.get(7)).setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        // 2. STATUS Column (Index 8) - Custom Cell for Status Badges
        TableColumn<Transaction, String> statusCol = (TableColumn<Transaction, String>) columns.get(8);
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        statusCol.setCellFactory(col -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("status-badge", "completed", "ignored", "active", "past-due-pending");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().add("status-badge");
                    setAlignment(Pos.CENTER);

                    // --- Status Color Logic ---
                    if (item.equalsIgnoreCase("Completed")) {
                        getStyleClass().add("completed");
                    } else if (item.equalsIgnoreCase("Ignored")) {
                        getStyleClass().add("ignored");
                    } else if (item.equalsIgnoreCase("Pending")) {
                        Transaction transaction = getTableRow().getItem();
                        // This uses the isPastDue() method that now works with the corrected formatter
                        if (transaction != null && transaction.isPastDue()) {
                            // Style for pending items that are past due
                            getStyleClass().add("past-due-pending");
                        } else {
                            getStyleClass().add("active");
                        }
                    }
                }
            }
        });

        // 3. ACTION Column (Index 9) - Custom Cell for Buttons
        TableColumn<Transaction, Void> actionCol = (TableColumn<Transaction, Void>) columns.get(9);
        actionCol.setCellFactory(getActionButtonCellFactory());

        // 4. Load LIVE Data from Database
        transactionTableView.setItems(loadTransactionDataFromDB());
    }

    // -------------------------------------------------------------------------
    // --- DATABASE LOADING LOGIC ---
    // -------------------------------------------------------------------------

    private ObservableList<Transaction> loadTransactionDataFromDB() {
        ObservableList<Transaction> transactions = FXCollections.observableArrayList();

        // SQL JOIN query to fetch data from all three tables
        String sql = "SELECT " +
                "b.id AS booking_id, b.booking_date, b.time_slot, b.total_price, b.booking_status, b.payment_type, " +
                "u.name AS user_name, u.phone_number AS user_mobile, u.email_address AS user_email, " +
                "s.court AS court_name " +
                "FROM bookings b " +
                "JOIN users u ON b.user_id = u.id " +
                "JOIN sports s ON b.court_id = s.id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                try {
                    Transaction transaction = new Transaction(rs);

                    String bookingDateStr = rs.getString("booking_date");
                    String timeSlotStr = rs.getString("time_slot");
                    String fullDateTimeStr = bookingDateStr + " " + timeSlotStr;

                    try {
                        // Success relies on the corrected formatter
                        LocalDateTime bookingDateTime = LocalDateTime.parse(fullDateTimeStr, DB_DATETIME_FORMATTER);
                        transaction.setBookingDateTime(bookingDateTime);
                    } catch (DateTimeParseException e) {
                        System.err.println("Failed to parse date/time for transaction " + transaction.getId() + ". Raw string: " + fullDateTimeStr + ". ERROR: " + e.getMessage());
                    }

                    transactions.add(transaction);
                } catch (SQLException e) {
                    System.err.println("Error creating Transaction object from ResultSet row: " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            showAlert("Database Connection Error", "Could not load transaction data. Check your database settings and server status.");
            System.err.println("Database fetch error: " + e.getMessage());
        }

        return transactions;
    }

    // -------------------------------------------------------------------------
    // --- ACTION BUTTON LOGIC ---
    // -------------------------------------------------------------------------

    private Callback<TableColumn<Transaction, Void>, TableCell<Transaction, Void>> getActionButtonCellFactory() {
        return new Callback<TableColumn<Transaction, Void>, TableCell<Transaction, Void>>() {
            @Override
            public TableCell<Transaction, Void> call(final TableColumn<Transaction, Void> param) {
                final TableCell<Transaction, Void> cell = new TableCell<Transaction, Void>() {

                    private final Button completeBtn = new Button("Complete");
                    private final Button ignoreBtn = new Button("Ignored");
                    private final HBox pane = new HBox(5, completeBtn, ignoreBtn);

                    {
                        pane.setAlignment(Pos.CENTER);
                        completeBtn.getStyleClass().addAll("action-button", "complete-action");
                        ignoreBtn.getStyleClass().addAll("action-button", "ignore-action");

                        completeBtn.setOnAction(event -> {
                            Transaction transaction = getTableView().getItems().get(getIndex());
                            String numericId = transaction.idProperty().get().substring(1);
                            updateTransactionStatus(numericId, "Completed");
                            transaction.statusProperty().set("Completed");
                            getTableView().refresh();
                        });

                        ignoreBtn.setOnAction(event -> {
                            Transaction transaction = getTableView().getItems().get(getIndex());
                            String numericId = transaction.idProperty().get().substring(1);
                            updateTransactionStatus(numericId, "Ignored");
                            transaction.statusProperty().set("Ignored");
                            getTableView().refresh();
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || getTableRow().getItem() == null) {
                            setGraphic(null);
                            return;
                        }

                        Transaction transaction = getTableRow().getItem();
                        String status = transaction.getStatus();

                        boolean isPending = status.equalsIgnoreCase("Pending");
                        boolean isPastDue = transaction.isPastDue();

                        // REQUIRED LOGIC: Only display buttons if the status is Pending AND the time has passed
                        if (isPending && isPastDue) {
                            setGraphic(pane);
                            setAlignment(Pos.CENTER);
                            completeBtn.setVisible(true);
                            completeBtn.setManaged(true);
                            ignoreBtn.setVisible(true);
                            ignoreBtn.setManaged(true);
                        } else {
                            setGraphic(null);
                        }
                    }
                };
                return cell;
            }
        };
    }

    /**
     * Updates the booking_status column in the database for a given transaction ID.
     */
    private void updateTransactionStatus(String numericId, String newStatus) {
        boolean completedFlag = newStatus.equalsIgnoreCase("Completed");

        String sql = "UPDATE bookings SET booking_status = ?, is_completed = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setBoolean(2, completedFlag);
            ps.setInt(3, Integer.parseInt(numericId));

            ps.executeUpdate();
            System.out.println("Status for Booking ID " + numericId + " updated to " + newStatus + " in database.");

        } catch (SQLException e) {
            showAlert("Database Update Error", "Could not update status in the database.");
            System.err.println("DB Update Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Invalid Transaction ID format: " + numericId);
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