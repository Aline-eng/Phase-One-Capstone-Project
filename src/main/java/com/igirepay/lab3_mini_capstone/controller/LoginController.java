package com.igirepay.lab3_mini_capstone.controller;

import com.igirepay.lab1_oop.model.Customer;
import com.igirepay.lab2_jdbc.service.JdbcWalletService;
import com.igirepay.lab3_mini_capstone.util.SceneManager;
import com.igirepay.lab3_mini_capstone.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField phoneField;
    @FXML private PasswordField pinField;

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField newPinField;
    @FXML private PasswordField confirmPinField;

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
            // Check if account is locked before attempting login
            if (service.isAccountLocked(phone)) {
                showError("Your account is locked after 3 failed attempts.\nPlease contact support to unlock it.");
                return;
            }

            Customer customer = service.loginByPhone(phone, pin);

            if (customer == null) {
                int attempts = service.getFailedAttempts(phone);
                int remaining = 3 - attempts;
                if (remaining <= 0) {
                    showError("Your account has been locked after 3 failed attempts.\nPlease contact support.");
                } else {
                    showError("Invalid phone number or PIN. " + remaining + " attempt(s) remaining.");
                }
                return;
            }

            SessionManager.getInstance().setCurrentCustomer(customer);
            String role = service.getRoleByPhone(phone);
            SessionManager.getInstance().setRole(role);
            // Route to different screens based on role
            if (SessionManager.getInstance().isAdmin()) {
                SceneManager.switchTo("admin");
            } else {
                SceneManager.switchTo("dashboard");
            }

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
        if (!isValidEmail(email)) {
            showError("Invalid email. Must contain '@' and a valid domain (e.g. name@example.com).");
            return;
        }
        if (!isValidPhone(phone)) {
            showError("Invalid phone number. Must be 10 digits starting with 07 (e.g. 0781234567).");
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
            // PIN is included in the same INSERT - no separate update call needed.
            // This also means if anything fails, the ID does not advance.
            Customer customer = new Customer(0, name, email, phone);
            int newId = service.registerCustomer(customer, pin);
            showSuccess("Account created successfully!\nYour Customer ID is: " + newId
                    + "\nYou can now log in with your phone number and PIN.");
        } catch (Exception e) {
            // CustomerDAO.friendlyError() already translated the DB error
            showError(e.getMessage());
        }
    }

    @FXML private void handleGoToSetup() {
        try { SceneManager.switchTo("setup"); }
        catch (Exception e) { showError("Navigation failed."); }
    }

    @FXML private void handleGoToLogin() {
        try { SceneManager.switchTo("login"); }
        catch (Exception e) { showError("Navigation failed."); }
    }

    private boolean isValidPin(String pin)   { return pin.matches("\\d{5}"); }
    private boolean isValidEmail(String email) { return email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"); }
    private boolean isValidPhone(String phone) { return phone.matches("^07\\d{8}$"); }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #E53935; -fx-font-weight: bold; -fx-font-size: 12px;");
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #43A047; -fx-font-weight: bold; -fx-font-size: 12px;");
    }
}
