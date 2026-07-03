module com.example.pawpal {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.pawpal to javafx.fxml;
    exports com.example.pawpal;
}