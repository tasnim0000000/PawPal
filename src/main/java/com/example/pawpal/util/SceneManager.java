package com.example.pawpal.util;

import com.example.pawpal.HelloApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;


public class SceneManager {


    public static <T> T loadAndGetController(String fxmlPath) throws IOException {

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlPath));
        loader.load();
        return loader.getController();
    }

    public static FXMLLoader load(String fxmlPath) throws IOException {

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlPath));
        Parent root = loader.load();
        loader.setRoot(root);
        return loader;
    }
}
