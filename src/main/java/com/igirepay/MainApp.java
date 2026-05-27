package com.igirepay;

import com.igirepay.lab3_mini_capstone.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

// MainApp is the JavaFX entry point.
// It hands control to SceneManager which handles all screen switching from here on.
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SceneManager.setPrimaryStage(stage);
        stage.setTitle("IgirePay - Mobile Money");
        stage.setWidth(1000);
        stage.setHeight(660);
        stage.setResizable(false);
        // First screen the user sees is the login screen
        SceneManager.switchTo("login");
    }
}
