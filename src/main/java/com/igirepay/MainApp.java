package com.igirepay;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

// JavaFX requires a class that extends Application as the entry point for the GUI.
// This class is responsible for loading the FXML layout and showing the window.
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("IgirePay - Payment Management System");
        stage.setScene(scene);
        stage.show();
    }
}
