module com.example.gymarnco {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.gymarnco to javafx.fxml;
    exports com.example.gymarnco;
}