package com.igirepay.lab3_mini_capstone.controller;

import com.igirepay.lab1_oop.model.Customer;
import com.igirepay.lab2_jdbc.service.JdbcWalletService;
import com.igirepay.lab3_mini_capstone.util.SceneManager;
import com.igirepay.lab3_mini_capstone.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    // Login screen fields
    @FXML private TextField phoneField;
    @FXML private PasswordField pinField;

    // Setup screen fields
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField newPinField;
    @FXML private PasswordField confirmPinField;

    // Shared between both screens
    @FXML private Label messageLabel;

    private final JdbcWalletService service = new JdbcWalletService();

    @FXML
    private void handleLogin() {
        String phone = phoneField.getText().trim();
        String pin = pinField.getText().trim();

        if (phone.isEmpty() || pin.isEmpty()) {
            showError("Please enter your phone number and PIN.");
            return;
        }

        if (!isValidPin(pin)) {
            showError("PIN must be exactly 5 digits.");
            return;
        }

        try {
            Customer customer = service.loginByPhone(phone, pin);

            if (customer == null) {
                showError("Invalid phone number or PIN.");
                return;
            }

            SessionManager.getInstance().setCurrentCustomer(customer);
            SceneManager.switchTo("dashboard");

        } catch (Exception e) {
            showError("Login failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        String name = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String pin = newPinField.getText().trim();
        String confirmPin = confirmPinField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()
                || pin.isEmpty() || confirmPin.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        if (!isValidPin(pin)) {
            showError("PIN must be exactly 5 digits (numbers only).");
            return;
        }

        if (!pin.equals(confirmPin)) {
            showError("PINs do not match.");
            return;
        }

        try {
            Customer customer = new Customer(0, name, email, phone);
            int newId = service.registerCustomer(customer);
            service.updatePin(newId, pin);
            showSuccess("Account created! Your Customer ID is: " + newId
                    + "\nYou can now log in with your phone number and PIN.");
        } catch (Exception e) {
            showError("Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToSetup() {
        try { SceneManager.switchTo("setup"); }
        catch (Exception e) { showError("Navigation failed."); }
    }

    @FXML
    private void handleGoToLogin() {
        try { SceneManager.switchTo("login"); }
        catch (Exception e) { showError("Navigation failed."); }
    }

    // PIN must be exactly 5 digits - matches MTN MoMo standard
    // \\d{5} means: exactly 5 characters, all digits 0-9
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
