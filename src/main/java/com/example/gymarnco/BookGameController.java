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

    // ⚠️ IMPORTANT: These VBox elements must match the fx:id in your FXML
    @FXML private VBox basketballCard;
    @FXML private VBox volleyballCard;
    @FXML private VBox badmintonCard;
    @FXML private VBox joggingCard;
    @FXML private VBox sepaktakrawCard;

    // --- Helper Class for Data Mapping ---
    // 💡 Maps the display sport to its required database ID and display details.
    // Replace the IDs (1, 2, 3, etc.) with the actual IDs from your 'sports' table.
    private static class SportMapping {
        final int id;
        final String name;
        final String description;

        SportMapping(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }

    // -------------------------------------------------------------------------
    // --- Event Handlers and Navigation ---
    // -------------------------------------------------------------------------

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void handleSportCardClick(MouseEvent event) {

        VBox clickedCard = (VBox) event.getSource();
        SportMapping selectedMapping = null;

        // Determine which card was clicked and map it to the correct DB ID and details
        if (clickedCard == basketballCard) {
            // ⚠️ Placeholder ID: Check your 'sports' table for the actual ID of Basketball
            selectedMapping = new SportMapping(1, "BASKETBALL", "Indoor and Outdoor Courts");
        } else if (clickedCard == volleyballCard) {
            // ⚠️ Placeholder ID: Check your 'sports' table for the actual ID of Volleyball
            selectedMapping = new SportMapping(2, "VOLLEYBALL", "6v6 Format - Beach and Indoor");
        } else if (clickedCard == badmintonCard) {
            // ⚠️ Placeholder ID: Check your 'sports' table for the actual ID of Badminton
            selectedMapping = new SportMapping(3, "BADMINTON", "Singles and Doubles - AC Courts");
        } else if (clickedCard == joggingCard) {
            // ⚠️ Placeholder ID: Check your 'sports' table for the actual ID of Jogging Track
            selectedMapping = new SportMapping(4, "JOGGING TRACK", "400m Track - Outdoor Facility");
        } else if (clickedCard == sepaktakrawCard) {
            // ⚠️ Placeholder ID: Check your 'sports' table for the actual ID of Sepak Takraw
            selectedMapping = new SportMapping(5, "SEPAK TAKRAW", "Traditional Court - Team Format");
        } else {
            System.err.println("Error: Unknown sport card clicked.");
            return;
        }

        // Save selected BASE sport to session (Assuming you still need this for 'SessionData')
        // NOTE: Since BaseSport enum is not defined here, I am commenting out the part that requires it.
        // If BaseSport is an enum containing the names, you'll need to update the logic here.
        // saveSelectedSportToSession(selectedSport);

        // Navigate to detail page, passing the critical Sport ID
        navigateToSportDetail(event, selectedMapping);
    }

    // NOTE: This method requires the definition of 'BaseSport' and 'SessionData' classes.
    // private void saveSelectedSportToSession(BaseSport sport) {
    //     SessionData.setSelectedBaseSport(sport);
    //     System.out.println("Saved base sport to session: " + sport);
    // }

    private void navigateToSportDetail(MouseEvent event, SportMapping sportMapping) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gymarnco/SportDetailPage.fxml"));
            Parent parent = loader.load();

            // Pass data to next controller
            SportDetailController controller = loader.getController();

            // 💡 Ensure this line is exactly as written here:
            controller.initData(
                    sportMapping.id,
                    sportMapping.name,
                    sportMapping.description
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(parent));
            stage.setTitle(sportMapping.name + " - GYM ARNCO");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading SportDetailPage.fxml");
        }
    }
}