package com.example.gymarnco;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class BookGameController {

    /**
     * Handles the click event for any of the sport cards, extracts the sport name,
     * loads the detail page, passes the data, and switches the scene.
     */
    @FXML
    private void handleSportCardClick(MouseEvent event) {
        try {
            // 1. Get the VBox (the card) that was clicked
            VBox clickedCard = (VBox) event.getSource();

            // 2. Extract the sport name from the card's title label
            // We use .lookup(".card-title") to find the label by its CSS class within the card VBox.
            Label titleLabel = (Label) clickedCard.lookup(".card-title");
            String sportName = titleLabel.getText();

            // 3. Load the detail page FXML using the correct package path
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gymarnco/SportDetailPage.fxml"));
            Parent detailPageParent = loader.load();

            // 4. Get the detail controller and initialize it with the sport name
            SportDetailController detailController = loader.getController();
            detailController.initData(sportName);

            // 5. Get the current Scene and Stage
            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            // 6. Switch the scene to the detail view
            currentScene.setRoot(detailPageParent);
            stage.setTitle(sportName + " Details - GYM ARNOCO");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading the detail page FXML. Check that SportDetailPage.fxml is in the correct resources path.");
        }
    }
}