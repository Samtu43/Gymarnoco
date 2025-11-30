package com.example.gymarnco;

import javafx.application.Platform;
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
    private VBox basketballCard;

    @FXML
    private VBox volleyballCard;

    @FXML
    private VBox badmintonCard;

    @FXML
    private VBox joggingCard;

    @FXML
    private VBox sepaktakrawCard;

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void handleSportCardClick(MouseEvent event) {

        VBox clickedCard = (VBox) event.getSource();
        BaseSport selectedSport = null;
        String description = "";

        // Determine which card was clicked
        if (clickedCard == basketballCard) {
            selectedSport = BaseSport.BASKETBALL;
            description = "Indoor and Outdoor Courts";
        } else if (clickedCard == volleyballCard) {
            selectedSport = BaseSport.VOLLEYBALL;
            description = "6v6 Format - Beach and Indoor";
        } else if (clickedCard == badmintonCard) {
            selectedSport = BaseSport.BADMINTON;
            description = "Singles and Doubles - AC Courts";
        } else if (clickedCard == joggingCard) {
            selectedSport = BaseSport.JOGGING;
            description = "400m Track - Outdoor Facility";
        } else if (clickedCard == sepaktakrawCard) {
            selectedSport = BaseSport.SEPAK_TAKRAW;
            description = "Traditional Court - Team Format";
        }

        // Save selected BASE sport to session
        saveSelectedSportToSession(selectedSport);

        // Navigate to detail page
        navigateToSportDetail(event, selectedSport.name(), description);
    }

    private void saveSelectedSportToSession(BaseSport sport) {
        SessionData.setSelectedBaseSport(sport);
        System.out.println("Saved base sport to session: " + sport);
    }

    private void navigateToSportDetail(MouseEvent event, String sportName, String description) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gymarnco/SportDetailPage.fxml"));
            Parent parent = loader.load();

            // Pass data to next controller
            SportDetailController controller = loader.getController();
            controller.initData(sportName, description);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(parent));
            stage.setTitle(sportName + " - GYM ARNCO");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading SportDetailPage.fxml");
        }
    }
}
