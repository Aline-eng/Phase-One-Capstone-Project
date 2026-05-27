package com.igirepay.lab3_mini_capstone.controller;

import com.igirepay.lab1_oop.model.Account;
import com.igirepay.lab1_oop.model.Customer;
import com.igirepay.lab1_oop.model.Transaction;
import com.igirepay.lab2_jdbc.service.JdbcWalletService;
import com.igirepay.lab3_mini_capstone.util.SceneManager;
import com.igirepay.lab3_mini_capstone.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML private Label phoneLabel;
    @FXML private Label balanceLabel;
    @FXML private Label accountTypeLabel;
    @FXML private VBox recentTransactionsBox;

    private final JdbcWalletService service = new JdbcWalletService();

    @FXML
    public void initialize() {
        Customer customer = SessionManager.getInstance().getCurrentCustomer();
        if (customer == null) return;

        // Show phone number on the balance card
        phoneLabel.setText(customer.getPhoneNumber());

        loadBalances(customer);
        loadRecentTransactions(customer);
    }

    private void loadBalances(Customer customer) {
        try {
            List<Account> accounts = service.findAccountsByCustomer(customer.getCustomerId());
            if (accounts.isEmpty()) {
                balanceLabel.setText("0 RWF");
                accountTypeLabel.setText("No accounts yet");
                return;
            }
            // Show the first account's balance on the main card
            Account primary = accounts.get(0);
            balanceLabel.setText(String.format("%,.0f RWF", primary.getBalance()));
            accountTypeLabel.setText(primary.getAccountType() + " Account  •  ID: " + primary.getAccountId());
        } catch (Exception e) {
            balanceLabel.setText("— RWF");
        }
    }

    private void loadRecentTransactions(Customer customer) {
        try {
            List<Account> accounts = service.findAccountsByCustomer(customer.getCustomerId());
            if (accounts.isEmpty()) return;

            List<Transaction> transactions = service.getTransactionHistory(accounts.get(0).getAccountId());
            recentTransactionsBox.getChildren().clear();

            if (transactions.isEmpty()) {
                recentTransactionsBox.getChildren().add(
                    new Label("No recent transactions") {{
                        setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 12px;");
                    }}
                );
                return;
            }

            // Show up to 4 most recent transactions as rows
            int limit = Math.min(transactions.size(), 4);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM HH:mm");

            for (int i = 0; i < limit; i++) {
                Transaction t = transactions.get(i);
                recentTransactionsBox.getChildren().add(buildTransactionRow(t, fmt));
            }

        } catch (Exception e) {
            // Leave the default label
        }
    }

    // Builds one transaction row: icon + type/ref on left, amount + date on right
    private HBox buildTransactionRow(Transaction t, DateTimeFormatter fmt) {
        HBox row = new HBox();
        row.setStyle("-fx-padding: 8 0 8 0; -fx-border-color: transparent transparent #F5F5F5 transparent;");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setSpacing(10);

        // Icon based on transaction type
        String icon = switch (t.getTransactionType()) {
            case DEPOSIT -> "⬇";
            case WITHDRAW -> "⬆";
            case TRANSFER_OUT -> "↗";
            case TRANSFER_IN -> "↙";
        };

        // Icon circle
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 20; " +
                           "-fx-padding: 8; -fx-font-size: 14px;");

        // Left side: type and reference
        VBox leftBox = new VBox(2);
        Label typeLabel = new Label(t.getTransactionType().name().replace("_", " "));
        typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #333333;");
        Label refLabel = new Label(t.getReferenceId());
        refLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #AAAAAA;");
        leftBox.getChildren().addAll(typeLabel, refLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Right side: amount and date
        VBox rightBox = new VBox(2);
        rightBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        // Color amount based on type - green for incoming, red for outgoing
        boolean isIncoming = t.getTransactionType().name().contains("DEPOSIT")
                          || t.getTransactionType().name().contains("IN");
        Label amountLabel = new Label((isIncoming ? "+" : "-") +
                String.format("%,.0f RWF", t.getAmount()));
        amountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: "
                + (isIncoming ? "#43A047" : "#E53935") + ";");
        Label dateLabel = new Label(t.getTimestamp().format(fmt));
        dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #AAAAAA;");
        rightBox.getChildren().addAll(amountLabel, dateLabel);

        row.getChildren().addAll(iconLabel, leftBox, spacer, rightBox);
        return row;
    }

    // ===== NAVIGATION =====

    @FXML private void showDashboard() {
        navigate("dashboard");
    }
    @FXML private void showHistory() {
        navigate("history");
    }
    @FXML private void showTransactions() {
        navigate("transactions");
    }
    @FXML private void showSendMoney() {
        navigate("transactions");
    }
    @FXML private void showDeposit() {
        navigate("transactions");
    }
    @FXML private void showWithdraw() {
        navigate("transactions");
    }
    @FXML private void showAccounts() {
        navigate("accounts");
    }
    @FXML private void showCustomers() {
        navigate("customers");
    }
    @FXML private void showSavings() {
        navigate("accounts");
    }
    @FXML private void showMore() {
        navigate("customers");
    }
    @FXML private void showProfile() {
        navigate("customers");
    }
    @FXML private void showNotifications() {
        // Placeholder - shows history as notifications for now
        navigate("history");
    }

    private void navigate(String screen) {
        try { SceneManager.switchTo(screen); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
