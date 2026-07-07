package com.example.pawpal.util;

import com.example.pawpal.HelloApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

/**
 * Small helper around FXMLLoader for screens that need to pass data into
 * the next controller (e.g. opening EditPet with a specific pet already
 * loaded). For simple navigation without passing data,
 * HelloApplication.changeScene(String) is used directly, matching the
 * existing pattern used across the project.
 */
public class SceneManager {

    /**
     * Loads an FXML file and returns its controller so the caller can push
     * data into it before showing the scene.
     */
    public static <T> T loadAndGetController(String fxmlPath) throws IOException {

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlPath));
        loader.load();
        return loader.getController();
    }

    /**
     * Loads an FXML file, returns both the root Parent and its controller.
     */
    public static FXMLLoader load(String fxmlPath) throws IOException {

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlPath));
        Parent root = loader.load();
        loader.setRoot(root);
        return loader;
    }
}
