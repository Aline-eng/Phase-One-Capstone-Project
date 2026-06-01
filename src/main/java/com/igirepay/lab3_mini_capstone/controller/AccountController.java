package com.igirepay.lab3_mini_capstone.controller;

import com.igirepay.lab1_oop.enums.TransactionType;
import com.igirepay.lab1_oop.model.Account;
import com.igirepay.lab1_oop.model.Customer;
import com.igirepay.lab1_oop.model.SavingsAccount;
import com.igirepay.lab1_oop.model.WalletAccount;
import com.igirepay.lab2_jdbc.service.JdbcWalletService;
import com.igirepay.lab3_mini_capstone.util.SceneManager;
import com.igirepay.lab3_mini_capstone.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.UUID;

public class AccountController {

    // Account overview cards
    @FXML private Label walletBalanceLabel;
    @FXML private Label walletIdLabel;
    @FXML private Label savingsBalanceLabel;
    @FXML private Label savingsIdLabel;
    @FXML private Label noAccountsLabel;

    // Internal transfer section
    @FXML private ComboBox<String> fromDropdown;
    @FXML private ComboBox<String> toDropdown;
    @FXML private TextField transferAmountField;
    @FXML private PasswordField transferPinField;
    @FXML private Label transferMessageLabel;

    // Create new account section
    @FXML private ToggleButton walletToggle;
    @FXML private ToggleButton savingsToggle;
    @FXML private TextField balanceField;
    @FXML private Label createMessageLabel;

    private final JdbcWalletService service = new JdbcWalletService();
    private List<Account> myAccounts;
    private boolean isWallet = true;

    @FXML
    public void initialize() {
        walletToggle.setOnAction(e -> selectWallet());
        savingsToggle.setOnAction(e -> selectSavings());
        loadAccounts();
    }

    private void loadAccounts() {
        try {
            Customer customer = SessionManager.getInstance().getCurrentCustomer();
            if (customer == null) return;
            myAccounts = service.findAccountsByCustomer(customer.getCustomerId());

            Account wallet  = myAccounts.stream().filter(a -> "Wallet".equals(a.getAccountType())).findFirst().orElse(null);
            Account savings = myAccounts.stream().filter(a -> "Savings".equals(a.getAccountType())).findFirst().orElse(null);

            if (wallet != null) {
                walletBalanceLabel.setText(String.format("%,.0f RWF", wallet.getBalance()));
                walletIdLabel.setText("Account ID: " + wallet.getAccountId());
            } else {
                walletBalanceLabel.setText("No wallet account");
                walletIdLabel.setText("Create one below");
            }

            if (savings != null) {
                savingsBalanceLabel.setText(String.format("%,.0f RWF", savings.getBalance()));
                savingsIdLabel.setText("Account ID: " + savings.getAccountId());
            } else {
                savingsBalanceLabel.setText("No savings account");
                savingsIdLabel.setText("Create one below");
            }

            // Populate transfer dropdowns
            fromDropdown.getItems().clear();
            toDropdown.getItems().clear();
            for (Account a : myAccounts) {
                String entry = a.getAccountType() + " (ID: " + a.getAccountId() + ")";
                fromDropdown.getItems().add(entry);
                toDropdown.getItems().add(entry);
            }
            if (myAccounts.size() >= 2) {
                fromDropdown.getSelectionModel().selectFirst();
                toDropdown.getSelectionModel().select(1);
            }

            boolean hasAccounts = !myAccounts.isEmpty();
            noAccountsLabel.setVisible(!hasAccounts);
            noAccountsLabel.setManaged(!hasAccounts);

        } catch (Exception e) {
            transferMessageLabel.setText("Error loading accounts: " + e.getMessage());
        }
    }

    // ===== INTERNAL TRANSFER (wallet ↔ savings) =====

    @FXML
    private void handleInternalTransfer() {
        int fromIdx = fromDropdown.getSelectionModel().getSelectedIndex();
        int toIdx   = toDropdown.getSelectionModel().getSelectedIndex();

        if (fromIdx < 0 || toIdx < 0 || myAccounts == null) {
            showTransferMsg("Please select both accounts.", false); return;
        }
        if (fromIdx == toIdx) {
            showTransferMsg("From and To accounts must be different.", false); return;
        }

        String amountText = transferAmountField.getText().trim();
        String pin        = transferPinField.getText().trim();

        if (amountText.isEmpty()) { showTransferMsg("Please enter an amount.", false); return; }
        if (pin.isEmpty())        { showTransferMsg("Please enter your PIN.", false); return; }

        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) { showTransferMsg("Amount must be greater than 0.", false); return; }

            // Verify PIN
            Customer me = SessionManager.getInstance().getCurrentCustomer();
            if (service.login(me.getCustomerId(), pin) == null) {
                showTransferMsg("Incorrect PIN. Transfer cancelled.", false); return;
            }

            int fromId = myAccounts.get(fromIdx).getAccountId();
            int toId   = myAccounts.get(toIdx).getAccountId();
            String ref = "INT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            service.transfer(fromId, toId, ref, amount);

            String fromType = myAccounts.get(fromIdx).getAccountType();
            String toType   = myAccounts.get(toIdx).getAccountType();
            showTransferMsg(String.format("✅ Transferred %,.0f RWF from %s to %s.", amount, fromType, toType), true);
            transferAmountField.clear();
            transferPinField.clear();
            loadAccounts(); // refresh balances

        } catch (NumberFormatException e) {
            showTransferMsg("Amount must be a valid number.", false);
        } catch (Exception e) {
            showTransferMsg(e.getMessage(), false);
        }
    }

    // ===== CREATE NEW ACCOUNT =====

    @FXML
    private void handleCreate() {
        String balText = balanceField.getText().trim();
        if (balText.isEmpty()) { showCreateMsg("Please enter an initial balance.", false); return; }

        Customer customer = SessionManager.getInstance().getCurrentCustomer();
        if (customer == null) { showCreateMsg("Session expired. Please log in again.", false); return; }

        try {
            double balance = Double.parseDouble(balText);
            if (balance < 0) { showCreateMsg("Balance cannot be negative.", false); return; }

            Account account = isWallet ? new WalletAccount(0, balance) : new SavingsAccount(0, balance);
            int newId = service.createAccount(customer.getCustomerId(), account);
            showCreateMsg(account.getAccountType() + " account created! ID: " + newId, true);
            balanceField.clear();
            loadAccounts();
        } catch (NumberFormatException e) {
            showCreateMsg("Balance must be a number.", false);
        } catch (Exception e) {
            showCreateMsg("Error: " + e.getMessage(), false);
        }
    }

    private void selectWallet() {
        isWallet = true;
        walletToggle.setStyle("-fx-background-color: #FFCC00; -fx-text-fill: #1A1A2E; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        savingsToggle.setStyle("-fx-background-color: #F0F2F5; -fx-text-fill: #888888; " +
                "-fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
    }

    private void selectSavings() {
        isWallet = false;
        savingsToggle.setStyle("-fx-background-color: #FFCC00; -fx-text-fill: #1A1A2E; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        walletToggle.setStyle("-fx-background-color: #F0F2F5; -fx-text-fill: #888888; " +
                "-fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
    }

    private void showTransferMsg(String msg, boolean success) {
        transferMessageLabel.setText(msg);
        transferMessageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: "
                + (success ? "#43A047" : "#E53935") + ";");
    }

    private void showCreateMsg(String msg, boolean success) {
        createMessageLabel.setText(msg);
        createMessageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: "
                + (success ? "#43A047" : "#E53935") + ";");
    }

    @FXML private void goBack()         { navigate("dashboard"); }
    @FXML private void goHome()         { navigate("dashboard"); }
    @FXML private void goTransactions() { navigate("transactions"); }
    @FXML private void goHistory()      { navigate("history"); }

    private void navigate(String screen) {
        try { SceneManager.switchTo(screen); } catch (Exception e) { e.printStackTrace(); }
    }
}
