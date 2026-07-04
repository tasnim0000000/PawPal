module com.example.pawpal {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.pawpal to javafx.fxml;
    opens com.example.pawpal.controller to javafx.fxml;

    exports com.example.pawpal;
    exports com.example.pawpal.controller;

}