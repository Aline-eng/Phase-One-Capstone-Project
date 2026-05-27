package com.igirepay.lab3_mini_capstone.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

// SceneManager handles all screen navigation in one place.
// Any controller calls SceneManager.switchTo("dashboard") to navigate.
// This means controllers never need to know where FXML files are stored.
public class SceneManager {

    private static Stage primaryStage;

    // Called once at app startup to give SceneManager access to the main window
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    // Loads an FXML file and replaces the current scene
    public static void switchTo(String fxmlName) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource(
                        "/com/igirepay/lab3_mini_capstone/" + fxmlName + ".fxml"
                )
        );
        Scene scene = new Scene(loader.load());
        // Load our CSS stylesheet into every scene
        scene.getStylesheets().add(
                SceneManager.class.getResource(
                        "/com/igirepay/lab3_mini_capstone/css/style.css"
                ).toExternalForm()
        );
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
