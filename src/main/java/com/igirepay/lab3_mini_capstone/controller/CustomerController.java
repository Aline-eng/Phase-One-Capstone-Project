package com.igirepay.lab3_mini_capstone.controller;

import com.igirepay.lab1_oop.model.Customer;
import com.igirepay.lab2_jdbc.service.JdbcWalletService;
import com.igirepay.lab3_mini_capstone.util.SceneManager;
import com.igirepay.lab3_mini_capstone.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class CustomerController {

    // Profile card
    @FXML private Label profileNameLabel;
    @FXML private Label profileEmailLabel;
    @FXML private Label profilePhoneLabel;
    @FXML private Label profileIdLabel;

    // Find customer
    @FXML private TextField searchIdField;
    @FXML private VBox foundCustomerBox;
    @FXML private Label foundNameLabel;
    @FXML private Label foundEmailLabel;
    @FXML private Label foundPhoneLabel;
    @FXML private Label findMessageLabel;

    // Update customer
    @FXML private TextField updateIdField;
    @FXML private TextField updateNameField;
    @FXML private TextField updateEmailField;
    @FXML private TextField updatePhoneField;
    @FXML private Label updateMessageLabel;

    // Change PIN
    @FXML private PasswordField currentPinField;
    @FXML private PasswordField newPinField;
    @FXML private PasswordField confirmNewPinField;
    @FXML private Label pinMessageLabel;

    private final JdbcWalletService service = new JdbcWalletService();

    @FXML
    public void initialize() {
        // Load the logged-in customer's profile on screen open
        Customer customer = SessionManager.getInstance().getCurrentCustomer();
        if (customer == null) return;

        profileNameLabel.setText(customer.getFullName());
        profileEmailLabel.setText(customer.getEmail());
        profilePhoneLabel.setText(customer.getPhoneNumber());
        profileIdLabel.setText("Customer ID: " + customer.getCustomerId());

        // Pre-fill update form with current values for convenience
        updateIdField.setText(String.valueOf(customer.getCustomerId()));
        updateNameField.setText(customer.getFullName());
        updateEmailField.setText(customer.getEmail());
        updatePhoneField.setText(customer.getPhoneNumber());
    }

    @FXML
    private void handleFind() {
        String idText = searchIdField.getText().trim();
        if (idText.isEmpty()) {
            findMessageLabel.setText("Please enter a customer ID.");
            findMessageLabel.setStyle("-fx-text-fill: #E53935;");
            return;
        }
        try {
            int id = Integer.parseInt(idText);
            Customer c = service.findCustomer(id);
            if (c == null) {
                foundCustomerBox.setVisible(false);
                foundCustomerBox.setManaged(false);
                findMessageLabel.setText("No customer found with ID " + id);
                findMessageLabel.setStyle("-fx-text-fill: #E53935;");
                return;
            }
            foundNameLabel.setText(c.getFullName());
            foundEmailLabel.setText("✉  " + c.getEmail());
            foundPhoneLabel.setText("📱  " + c.getPhoneNumber());
            foundCustomerBox.setVisible(true);
            foundCustomerBox.setManaged(true);
            findMessageLabel.setText("");
        } catch (NumberFormatException e) {
            findMessageLabel.setText("Customer ID must be a number.");
            findMessageLabel.setStyle("-fx-text-fill: #E53935;");
        } catch (Exception e) {
            findMessageLabel.setText("Error: " + e.getMessage());
            findMessageLabel.setStyle("-fx-text-fill: #E53935;");
        }
    }

    @FXML
    private void handleUpdate() {
        String idText   = updateIdField.getText().trim();
        String name     = updateNameField.getText().trim();
        String email    = updateEmailField.getText().trim();
        String phone    = updatePhoneField.getText().trim();

        if (idText.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showUpdateMsg("All fields are required.", false);
            return;
        }
        try {
            int id = Integer.parseInt(idText);
            service.updateCustomer(new Customer(id, name, email, phone));

            // If the logged-in customer updated their own profile, refresh the session
            Customer current = SessionManager.getInstance().getCurrentCustomer();
            if (current != null && current.getCustomerId() == id) {
                SessionManager.getInstance().setCurrentCustomer(new Customer(id, name, email, phone));
                profileNameLabel.setText(name);
                profileEmailLabel.setText(email);
                profilePhoneLabel.setText(phone);
            }
            showUpdateMsg("Customer updated successfully.", true);
        } catch (NumberFormatException e) {
            showUpdateMsg("Customer ID must be a number.", false);
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

    // ===== NAVIGATION =====
    @FXML private void goBack()         { navigate("dashboard"); }
    @FXML private void goHome()         { navigate("dashboard"); }
    @FXML private void goTransactions() { navigate("transactions"); }
    @FXML private void goAccounts()     { navigate("accounts"); }

    private void navigate(String screen) {
        try { SceneManager.switchTo(screen); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
