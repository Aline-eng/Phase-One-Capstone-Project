package com.igirepay.lab3_mini_capstone.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneManager {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchTo(String fxmlName) throws IOException {
        String path = "/com/igirepay/lab3_mini_capstone/" + fxmlName + ".fxml";
        URL fxmlUrl = SceneManager.class.getResource(path);

        if (fxmlUrl == null) {
            throw new IOException("Screen not found: " + path
                + ". Make sure the FXML file exists in resources.");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
