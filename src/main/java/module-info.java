module com.example.gymarnco {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.gymarnco to javafx.fxml;
    exports com.example.gymarnco;
}