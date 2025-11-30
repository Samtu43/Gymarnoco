package com.example.gymarnco;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class Admin_Dashboard_Controller {

    @FXML
    private TableView<Transaction> transactionTableView;

    @FXML
    public void initialize() {
        // NEW COLUMN ORDER:
        // ID: Index 0, CUSTOMER: Index 1, Mobile: Index 2, Email: Index 3, FACILITY: Index 4
        // Type: Index 5, AMOUNT: Index 6, DATE: Index 7, STATUS: Index 8, ACTION: Index 9

        ObservableList<TableColumn<Transaction, ?>> columns = transactionTableView.getColumns();

        // 1. Set up standard cell value factories (Binding Data) - Updated Indices
        ((TableColumn<Transaction, String>) columns.get(0)).setCellValueFactory(cellData -> cellData.getValue().idProperty());
        ((TableColumn<Transaction, String>) columns.get(1)).setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        ((TableColumn<Transaction, String>) columns.get(2)).setCellValueFactory(cellData -> cellData.getValue().mobileNumberProperty()); // BINDING FOR MOBILE NUMBER
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
                getStyleClass().removeAll("status-badge", "completed", "ignored", "active");
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    getStyleClass().add("status-badge");

                    // --- Status Color Logic ---
                    if (item.equalsIgnoreCase("Completed")) {
                        getStyleClass().add("completed"); // Maps to Green
                    } else if (item.equalsIgnoreCase("Ignored")) {
                        getStyleClass().add("ignored"); // Maps to Red
                    } else if (item.equalsIgnoreCase("Active")) {
                        getStyleClass().add("active"); // Maps to Blue
                    }
                }
            }
        });

        // 3. ACTION Column (Index 9) - Custom Cell for Buttons
        TableColumn<Transaction, Void> actionCol = (TableColumn<Transaction, Void>) columns.get(9);
        actionCol.setCellFactory(getActionButtonCellFactory());

        // 4. Load Dummy Data (Default status is "Active")
        transactionTableView.setItems(getDummyTransactionData());
    }

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

                        completeBtn.getStyleClass().addAll("action-button", "complete-action"); // Green button
                        ignoreBtn.getStyleClass().addAll("action-button", "ignore-action"); // Red button

                        // Action for the COMPLETE button
                        completeBtn.setOnAction(event -> {
                            Transaction transaction = getTableView().getItems().get(getIndex());
                            transaction.statusProperty().set("Completed");
                            getTableView().refresh();
                        });

                        // Action for the IGNORED button
                        ignoreBtn.setOnAction(event -> {
                            Transaction transaction = getTableView().getItems().get(getIndex());
                            transaction.statusProperty().set("Ignored");
                            getTableView().refresh();
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                        setAlignment(Pos.CENTER);
                    }
                };
                return cell;
            }
        };
    }

    private ObservableList<Transaction> getDummyTransactionData() {
        // ADDED MOBILE NUMBER to the data
        return FXCollections.observableArrayList(
                new Transaction("T001", "George Lindelof", "0917-123-4567", "george@mail.com", "Main Branch", "gCASH", 1500.00, "2025-11-28 10:30", "Active"),
                new Transaction("T002", "Haitam Alassami", "0917-789-0123", "haitam@gmail.com", "North Office", "Bank Transfer", 85.50, "2025-11-28 11:45", "Active"),
                new Transaction("T003", "Vanessa Paradi", "0917-345-6789", "vanessa@google.com", "South Kiosk", "Deposit", 3200.00, "2025-11-29 09:10", "Active"),
                new Transaction("T004", "Christy Newborn", "0917-000-1111", "christy@amazon.com", "Main Branch", "gCASH", 450.00, "2025-11-29 14:00", "Active"),
                new Transaction("T005", "Tora Laundren", "0917-222-3333", "tora@lan.com", "HQ", "Withdrawal", 99.99, "2025-11-30 08:00", "Active")
        );
    }
}