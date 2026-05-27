package com.igirepay.lab3_mini_capstone.controller;

import com.igirepay.lab1_oop.model.Customer;
import com.igirepay.lab2_jdbc.service.JdbcWalletService;
import com.igirepay.lab3_mini_capstone.util.SceneManager;
import com.igirepay.lab3_mini_capstone.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

// LoginController handles two screens: login.fxml and setup.fxml.
// Both screens are simple enough to share one controller.
// @FXML fields that don't exist on the current screen will just be null - that's fine.
public class LoginController {

    // --- Login screen fields ---
    @FXML private TextField customerIdField;
    @FXML private PasswordField pinField;

    // --- Setup screen fields ---
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField newPinField;
    @FXML private PasswordField confirmPinField;

    // Shared - exists on both screens
    @FXML private Label messageLabel;

    private final JdbcWalletService service = new JdbcWalletService();

    // Called when the LOGIN button is clicked
    @FXML
    private void handleLogin() {
        String idText = customerIdField.getText().trim();
        String pin = pinField.getText().trim();

        // Validate inputs are not empty
        if (idText.isEmpty() || pin.isEmpty()) {
            showError("Please enter your Customer ID and PIN.");
            return;
        }

        // Validate PIN is exactly 5 digits
        if (!isValidPin(pin)) {
            showError("PIN must be exactly 5 digits.");
            return;
        }

        try {
            int customerId = Integer.parseInt(idText);
            Customer customer = service.login(customerId, pin);

            if (customer == null) {
                // We don't say which field is wrong - security best practice
                showError("Invalid Customer ID or PIN. Please try again.");
                return;
            }

            // Store the logged-in customer in SessionManager so all other
            // screens can access it without passing it around
            SessionManager.getInstance().setCurrentCustomer(customer);
            SceneManager.switchTo("dashboard");

        } catch (NumberFormatException e) {
            showError("Customer ID must be a number.");
        } catch (Exception e) {
            showError("Login failed: " + e.getMessage());
        }
    }

    // Called when CREATE ACCOUNT button is clicked on setup.fxml
    @FXML
    private void handleRegister() {
        String name = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String pin = newPinField.getText().trim();
        String confirmPin = confirmPinField.getText().trim();

        // All fields required
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()
                || pin.isEmpty() || confirmPin.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        // PIN must be exactly 5 digits
        if (!isValidPin(pin)) {
            showError("PIN must be exactly 5 digits (numbers only).");
            return;
        }

        // Both PIN entries must match
        if (!pin.equals(confirmPin)) {
            showError("PINs do not match. Please try again.");
            return;
        }

        try {
            // Step 1: Register the customer (without PIN first)
            Customer customer = new Customer(0, name, email, phone);
            int newId = service.registerCustomer(customer);

            // Step 2: Set their PIN
            service.updatePin(newId, pin);

            // Show success with their assigned ID so they can log in
            showSuccess("Account created! Your Customer ID is: " + newId
                    + "\nPlease save this ID - you need it to log in.");

        } catch (Exception e) {
            showError("Registration failed: " + e.getMessage());
        }
    }

    // Navigates to the setup/registration screen
    @FXML
    private void handleGoToSetup() {
        try {
            SceneManager.switchTo("setup");
        } catch (Exception e) {
            showError("Navigation failed: " + e.getMessage());
        }
    }

    // Navigates back to the login screen
    @FXML
    private void handleGoToLogin() {
        try {
            SceneManager.switchTo("login");
        } catch (Exception e) {
            showError("Navigation failed: " + e.getMessage());
        }
    }

    // Validates that the PIN is exactly 5 digits and contains only numbers.
    // matches() with regex "\\d{5}" means: exactly 5 characters, all digits.
    // \\d means any digit (0-9), {5} means exactly 5 of them.
    private boolean isValidPin(String pin) {
        return pin.matches("\\d{5}");
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #E53935; -fx-font-weight: bold;");
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #43A047; -fx-font-weight: bold;");
    }
}
