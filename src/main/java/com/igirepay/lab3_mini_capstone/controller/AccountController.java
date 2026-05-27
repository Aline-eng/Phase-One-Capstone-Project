package com.igirepay.lab3_mini_capstone.controller;

import com.igirepay.lab1_oop.model.Account;
import com.igirepay.lab1_oop.model.SavingsAccount;
import com.igirepay.lab1_oop.model.WalletAccount;
import com.igirepay.lab2_jdbc.service.JdbcWalletService;
import com.igirepay.lab3_mini_capstone.util.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class AccountController {

    @FXML private TextField customerIdField;
    @FXML private TextField balanceField;
    @FXML private ToggleButton walletToggle;
    @FXML private ToggleButton savingsToggle;
    @FXML private Label createMessageLabel;

    @FXML private TextField viewCustomerIdField;
    @FXML private VBox accountsListBox;

    @FXML private TextField deleteAccountIdField;
    @FXML private Label deleteMessageLabel;

    private final JdbcWalletService service = new JdbcWalletService();

    // Track which account type is selected
    private boolean isWallet = true;

    @FXML
    public void initialize() {
        // Toggle buttons act as a selector - clicking one deselects the other
        walletToggle.setOnAction(e -> selectWallet());
        savingsToggle.setOnAction(e -> selectSavings());
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

    @FXML
    private void handleCreate() {
        String cidText = customerIdField.getText().trim();
        String balText = balanceField.getText().trim();

        if (cidText.isEmpty() || balText.isEmpty()) {
            showCreateMsg("All fields are required.", false);
            return;
        }

        try {
            int customerId = Integer.parseInt(cidText);
            double balance = Double.parseDouble(balText);

            if (balance < 0) {
                showCreateMsg("Balance cannot be negative.", false);
                return;
            }

            // Create the correct account subclass based on toggle selection
            Account account = isWallet
                    ? new WalletAccount(0, balance)
                    : new SavingsAccount(0, balance);

            int newId = service.createAccount(customerId, account);
            showCreateMsg(account.getAccountType() + " account created! ID: " + newId, true);

            customerIdField.clear();
            balanceField.clear();

        } catch (NumberFormatException e) {
            showCreateMsg("Customer ID and balance must be numbers.", false);
        } catch (Exception e) {
            showCreateMsg("Error: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleLoadAccounts() {
        String cidText = viewCustomerIdField.getText().trim();
        if (cidText.isEmpty()) {
            accountsListBox.getChildren().clear();
            accountsListBox.getChildren().add(
                new Label("Please enter a customer ID.") {{
                    setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 12px;");
                }}
            );
            return;
        }

        try {
            int customerId = Integer.parseInt(cidText);
            List<Account> accounts = service.findAccountsByCustomer(customerId);
            accountsListBox.getChildren().clear();

            if (accounts.isEmpty()) {
                accountsListBox.getChildren().add(
                    new Label("No accounts found for customer " + customerId) {{
                        setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 12px;");
                    }}
                );
                return;
            }

            for (Account acc : accounts) {
                accountsListBox.getChildren().add(buildAccountRow(acc));
            }

        } catch (NumberFormatException e) {
            accountsListBox.getChildren().clear();
            accountsListBox.getChildren().add(
                new Label("Customer ID must be a number.") {{
                    setStyle("-fx-text-fill: #E53935; -fx-font-size: 12px;");
                }}
            );
        } catch (Exception e) {
            accountsListBox.getChildren().clear();
            accountsListBox.getChildren().add(
                new Label("Error: " + e.getMessage()) {{
                    setStyle("-fx-text-fill: #E53935; -fx-font-size: 12px;");
                }}
            );
        }
    }

    @FXML
    private void handleDelete() {
        String idText = deleteAccountIdField.getText().trim();
        if (idText.isEmpty()) {
            showDeleteMsg("Please enter an account ID.", false);
            return;
        }
        try {
            int accountId = Integer.parseInt(idText);
            service.deleteAccount(accountId);
            showDeleteMsg("Account " + accountId + " deleted successfully.", true);
            deleteAccountIdField.clear();
        } catch (NumberFormatException e) {
            showDeleteMsg("Account ID must be a number.", false);
        } catch (Exception e) {
            // Database FK constraint will reject if account has transactions
            showDeleteMsg("Cannot delete: " + e.getMessage(), false);
        }
    }

    // Builds one account row card showing type, ID and balance
    private HBox buildAccountRow(Account acc) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 10; -fx-padding: 10 12 10 12;");

        String icon = acc.getAccountType().equals("Wallet") ? "💳" : "🏦";
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");

        VBox info = new VBox(2);
        Label typeLabel = new Label(acc.getAccountType() + " Account");
        typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #1A1A2E;");
        Label idLabel = new Label("ID: " + acc.getAccountId());
        idLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");
        info.getChildren().addAll(typeLabel, idLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label balLabel = new Label(String.format("%,.0f RWF", acc.getBalance()));
        balLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1A1A2E;");

        row.getChildren().addAll(iconLabel, info, spacer, balLabel);
        return row;
    }

    private void showCreateMsg(String msg, boolean success) {
        createMessageLabel.setText(msg);
        createMessageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: "
                + (success ? "#43A047" : "#E53935") + ";");
    }

    private void showDeleteMsg(String msg, boolean success) {
        deleteMessageLabel.setText(msg);
        deleteMessageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: "
                + (success ? "#43A047" : "#E53935") + ";");
    }

    // ===== NAVIGATION =====
    @FXML private void goBack()         { navigate("dashboard"); }
    @FXML private void goHome()         { navigate("dashboard"); }
    @FXML private void goTransactions() { navigate("transactions"); }
    @FXML private void goHistory()      { navigate("history"); }

    private void navigate(String screen) {
        try { SceneManager.switchTo(screen); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
