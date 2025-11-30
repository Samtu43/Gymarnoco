package com.example.gymarnco;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class BookGameController {

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private VBox basketballCard;

    @FXML
    private VBox volleyballCard;

    @FXML
    private VBox badmintonCard;

    @FXML
    private VBox joggingCard;

    @FXML
    private VBox sepaktakrawCard;


    /**
     * Handles click on any sport card
     */
    @FXML
    private void handleSportCardClick(MouseEvent event) {
        VBox clickedCard = (VBox) event.getSource();
        String sportName = "";
        String description = "";

        // Determine which card was clicked
        if (clickedCard == basketballCard) {
            sportName = "BASKETBALL";
            description = "Indoor and Outdoor Courts";
        } else if (clickedCard == volleyballCard) {
            sportName = "VOLLEYBALL";
            description = "6v6 Format - Beach and Indoor";
        } else if (clickedCard == badmintonCard) {
            sportName = "BADMINTON";
            description = "Singles and Doubles - AC Courts";
        } else if (clickedCard == joggingCard) {
            sportName = "JOGGING TRACK";
            description = "400m Track - Outdoor Facility";
        } else if (clickedCard == sepaktakrawCard) {
            sportName = "SEPAK TAKRAW";
            description = "Traditional Court - Team Format";
        }

        // Navigate to sport detail page
        navigateToSportDetail(event, sportName, description);
    }

    /**
     * Navigate to SportDetailPage and pass sport data
     */
    private void navigateToSportDetail(MouseEvent event, String sportName, String description) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gymarnco/SportDetailPage.fxml"));
            Parent sportDetailParent = loader.load();

            // Get the controller and pass data
            SportDetailController controller = loader.getController();
            controller.initData(sportName, description);

            // Get current stage and switch scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(sportDetailParent);
            stage.setScene(scene);
            stage.setTitle(sportName + " - GYM ARNOCO");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading SportDetailPage.fxml");
        }
    }
}