package com.example.pawpal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {

        stage = primaryStage;

        Parent root = FXMLLoader.load(
                HelloApplication.class.getResource("/fxml/Login.fxml"));

        Scene scene = new Scene(root);

        stage.setTitle("PawPal - Pet Marketplace");

        stage.setScene(scene);

        stage.setResizable(false);

        stage.show();

    }
    public static Stage getStage() {
        return stage;
    }

    public static void changeScene(String fxml) throws Exception {

        Parent pane = FXMLLoader.load(
                HelloApplication.class.getResource(fxml));

        stage.getScene().setRoot(pane);

    }

    public static void main(String[] args) {

        launch(args);

    }

}