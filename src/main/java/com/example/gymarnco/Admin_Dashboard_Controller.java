package com.example.gymarnco;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.geometry.Pos; // Required for centering the action buttons

public class Admin_Dashboard_Controller {

    @FXML
    private TableView<Transaction> transactionTableView;

    @FXML
    public void initialize() {
        ObservableList<TableColumn<Transaction, ?>> columns = transactionTableView.getColumns();

        // 1. Standard Bindings
        ((TableColumn<Transaction, String>) columns.get(0)).setCellValueFactory(cellData -> cellData.getValue().idProperty());
        ((TableColumn<Transaction, String>) columns.get(1)).setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        ((TableColumn<Transaction, String>) columns.get(2)).setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        ((TableColumn<Transaction, String>) columns.get(3)).setCellValueFactory(cellData -> cellData.getValue().facilityProperty());
        ((TableColumn<Transaction, String>) columns.get(4)).setCellValueFactory(cellData -> cellData.getValue().typeProperty());

        // AMOUNT Column (Index 5) - Formatting
        TableColumn<Transaction, Double> amountCol = (TableColumn<Transaction, Double>) columns.get(5);
        amountCol.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        amountCol.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("₱%.2f", item));
                setAlignment(Pos.CENTER_RIGHT); // Align amount text to the right
            }
        });

        ((TableColumn<Transaction, String>) columns.get(6)).setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        // 2. STATUS Column (Index 7) - Custom Cell for Status Badges
        TableColumn<Transaction, String> statusCol = (TableColumn<Transaction, String>) columns.get(7);
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        statusCol.setCellFactory(col -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("status-badge", "completed", "ignored", "active");
                if (empty || item == null) {
                    setText(null);
                    setAlignment(Pos.CENTER_LEFT);
                } else {
                    setText(item);
                    getStyleClass().add("status-badge");
                    setAlignment(Pos.CENTER); // Center the badge content

                    // --- Status Color Logic ---
                    if (item.equalsIgnoreCase("Completed")) {
                        getStyleClass().add("completed");
                    } else if (item.equalsIgnoreCase("Ignored")) {
                        getStyleClass().add("ignored");
                    } else if (item.equalsIgnoreCase("Active")) {
                        getStyleClass().add("active");
                    }
                }
            }
        });

        // 3. ACTION Column (Index 8) - Custom Cell for Buttons
        TableColumn<Transaction, Void> actionCol = (TableColumn<Transaction, Void>) columns.get(8);
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
                    // HBox to hold the buttons
                    private final HBox pane = new HBox(5, completeBtn, ignoreBtn);

                    {
                        completeBtn.getStyleClass().addAll("action-button", "complete-action");
                        ignoreBtn.getStyleClass().addAll("action-button", "ignore-action");

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
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                            // --- SET ALIGNMENT TO CENTER THE BUTTONS IN THE CELL ---
                            setAlignment(Pos.CENTER);
                        }
                    }
                };
                return cell;
            }
        };
    }

    private ObservableList<Transaction> getDummyTransactionData() {
        return FXCollections.observableArrayList(
                new Transaction("T001", "George Lindelof", "george@mail.com", "Main Branch", "gCASH", 1500.00, "2025-11-28 10:30", "Active"),
                new Transaction("T002", "Haitam Alassami", "haitam@gmail.com", "North Office", "Bank Transfer", 85.50, "2025-11-28 11:45", "Active"),
                new Transaction("T003", "Vanessa Paradi", "vanessa@google.com", "South Kiosk", "Deposit", 3200.00, "2025-11-29 09:10", "Active"),
                new Transaction("T004", "Christy Newborn", "christy@amazon.com", "Main Branch", "gCASH", 450.00, "2025-11-29 14:00", "Active"),
                new Transaction("T005", "Tora Laundren", "tora@lan.com", "HQ", "Withdrawal", 99.99, "2025-11-30 08:00", "Active")
        );
    }
}