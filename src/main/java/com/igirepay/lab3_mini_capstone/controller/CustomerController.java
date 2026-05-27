package com.igirepay.lab3_mini_capstone.controller;

import com.igirepay.lab1_oop.model.Customer;
import com.igirepay.lab2_jdbc.service.JdbcWalletService;
import com.igirepay.lab3_mini_capstone.util.SceneManager;
import com.igirepay.lab3_mini_capstone.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class CustomerController {

    @FXML private Label profileNameLabel;
    @FXML private Label profileEmailLabel;
    @FXML private Label profilePhoneLabel;
    @FXML private Label profileIdLabel;

    @FXML private TextField updateIdField;
    @FXML private TextField updateNameField;
    @FXML private TextField updateEmailField;
    @FXML private TextField updatePhoneField;
    @FXML private Label updateMessageLabel;

    @FXML private PasswordField currentPinField;
    @FXML private PasswordField newPinField;
    @FXML private PasswordField confirmNewPinField;
    @FXML private Label pinMessageLabel;

    private final JdbcWalletService service = new JdbcWalletService();

    @FXML
    public void initialize() {
        Customer customer = SessionManager.getInstance().getCurrentCustomer();
        if (customer == null) return;

        profileNameLabel.setText(customer.getFullName());
        profileEmailLabel.setText(customer.getEmail());
        profilePhoneLabel.setText(customer.getPhoneNumber());
        profileIdLabel.setText("Customer ID: " + customer.getCustomerId());

        // Pre-fill update form - ID is read-only, user only edits name/email/phone
        updateIdField.setText(String.valueOf(customer.getCustomerId()));
        updateNameField.setText(customer.getFullName());
        updateEmailField.setText(customer.getEmail());
        updatePhoneField.setText(customer.getPhoneNumber());
    }

    @FXML
    private void handleUpdate() {
        String name  = updateNameField.getText().trim();
        String email = updateEmailField.getText().trim();
        String phone = updatePhoneField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showUpdateMsg("All fields are required.", false);
            return;
        }

        try {
            // Always use the session customer's ID - the field is read-only but we
            // get it from the session as the authoritative source
            Customer current = SessionManager.getInstance().getCurrentCustomer();
            if (current == null) return;

            service.updateCustomer(new Customer(current.getCustomerId(), name, email, phone));

            // Refresh the session with updated details
            Customer updated = new Customer(current.getCustomerId(), name, email, phone);
            SessionManager.getInstance().setCurrentCustomer(updated);

            profileNameLabel.setText(name);
            profileEmailLabel.setText(email);
            profilePhoneLabel.setText(phone);

            showUpdateMsg("Profile updated successfully.", true);
        } catch (Exception e) {
            showUpdateMsg("Error: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleChangePin() {
        String currentPin    = currentPinField.getText().trim();
        String newPin        = newPinField.getText().trim();
        String confirmNewPin = confirmNewPinField.getText().trim();

        if (currentPin.isEmpty() || newPin.isEmpty() || confirmNewPin.isEmpty()) {
            showPinMsg("All PIN fields are required.", false);
            return;
        }
        if (!newPin.matches("\\d{5}")) {
            showPinMsg("New PIN must be exactly 5 digits.", false);
            return;
        }
        if (!newPin.equals(confirmNewPin)) {
            showPinMsg("New PINs do not match.", false);
            return;
        }

        try {
            Customer customer = SessionManager.getInstance().getCurrentCustomer();
            if (customer == null) return;

            // Verify current PIN before allowing change
            Customer verified = service.login(customer.getCustomerId(), currentPin);
            if (verified == null) {
                showPinMsg("Current PIN is incorrect.", false);
                return;
            }

            service.updatePin(customer.getCustomerId(), newPin);
            showPinMsg("PIN changed successfully.", true);
            currentPinField.clear();
            newPinField.clear();
            confirmNewPinField.clear();

        } catch (Exception e) {
            showPinMsg("Error: " + e.getMessage(), false);
        }
    }

    private void showUpdateMsg(String msg, boolean success) {
        updateMessageLabel.setText(msg);
        updateMessageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: "
                + (success ? "#43A047" : "#E53935") + ";");
    }

    private void showPinMsg(String msg, boolean success) {
        pinMessageLabel.setText(msg);
        pinMessageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: "
                + (success ? "#43A047" : "#E53935") + ";");
    }

    @FXML private void goBack()         { navigate("dashboard"); }
    @FXML private void goHome()         { navigate("dashboard"); }
    @FXML private void goTransactions() { navigate("transactions"); }
    @FXML private void goAccounts()     { navigate("accounts"); }

    private void navigate(String screen) {
        try { SceneManager.switchTo(screen); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
