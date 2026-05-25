package com.igirepay;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

// Controller class linked to main-view.fxml.
// @FXML annotation tells JavaFX to inject the UI components defined in the FXML file.
public class MainController {

    @FXML
    private Label welcomeLabel;

    // This method is called when the button in main-view.fxml is clicked
    @FXML
    protected void onGetStartedClick() {
        welcomeLabel.setText("Welcome to IgirePay Payment Management System!");
    }
}
