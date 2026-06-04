package com.igirepay.lab3_mini_capstone.controller;

import com.igirepay.lab1_oop.enums.TransactionType;
import com.igirepay.lab2_jdbc.service.JdbcWalletService;
import com.igirepay.lab3_mini_capstone.util.SceneManager;
import com.igirepay.lab3_mini_capstone.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.UUID;

public class BuyController {

    @FXML private TextField accountIdField;
    @FXML private Label messageLabel;

    private final JdbcWalletService service = new JdbcWalletService();

    @FXML
    public void initialize() {
        try {
            var customer = SessionManager.getInstance().getCurrentCustomer();
            if (customer == null) return;
            var accounts = service.findAccountsByCustomer(customer.getCustomerId());
            if (!accounts.isEmpty()) {
                accountIdField.setText(String.valueOf(accounts.get(0).getAccountId()));
            }
        } catch (Exception e) {
        }
    }

    private void processPurchase(String description, double amount) {
        String idText = accountIdField.getText().trim();
        if (idText.isEmpty()) {
            showError("Please enter your account ID.");
            return;
        }
        try {
            int accountId = Integer.parseInt(idText);
            String ref = "BUY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            service.processTransaction(accountId, ref, amount, TransactionType.WITHDRAW);
            var acc = service.findAccount(accountId);
            showSuccess(description + " purchased!\nRef: " + ref
                    + "\nNew balance: " + String.format("%,.0f RWF", acc.getBalance()));
        } catch (NumberFormatException e) {
            showError("Account ID must be a number.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML private void buyAirtime100()  { processPurchase("Airtime 100 RWF", 100); }
    @FXML private void buyAirtime500()  { processPurchase("Airtime 500 RWF", 500); }
    @FXML private void buyAirtime1000() { processPurchase("Airtime 1000 RWF", 1000); }
    @FXML private void buyBundle200()   { processPurchase("Daily 50MB Bundle", 200); }
    @FXML private void buyBundle1000()  { processPurchase("Weekly 500MB Bundle", 1000); }
    @FXML private void buyBundle3000()  { processPurchase("Monthly 2GB Bundle", 3000); }
    @FXML private void buyBundle5000()  { processPurchase("Monthly 5GB Bundle", 5000); }
    @FXML private void buyNightPack()   { processPurchase("Night Internet Pack", 500); }
    @FXML private void buySocialPack()  { processPurchase("Social Media Pack", 300); }

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: #E53935; -fx-font-weight: bold; -fx-font-size: 12px;");
    }

    private void showSuccess(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: #43A047; -fx-font-weight: bold; -fx-font-size: 12px;");
    }

    @FXML private void goBack()         { navigate("dashboard"); }
    @FXML private void goHome()         { navigate("dashboard"); }
    @FXML private void goTransactions() { navigate("transactions"); }
    @FXML private void goHistory()      { navigate("history"); }

    private void navigate(String screen) {
        try { SceneManager.switchTo(screen); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
