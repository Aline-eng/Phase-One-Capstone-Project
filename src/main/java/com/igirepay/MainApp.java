package com.igirepay;

import com.igirepay.lab3_mini_capstone.util.SceneManager;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        SceneManager.setPrimaryStage(stage);
        stage.setTitle("IgirePay - Mobile Money");
        stage.setWidth(1000);
        stage.setHeight(660);
        stage.setResizable(false);

        try {
            SceneManager.switchTo("login");
        } catch (Exception e) {
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Startup Error");
            alert.setHeaderText("IgirePay could not start");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
