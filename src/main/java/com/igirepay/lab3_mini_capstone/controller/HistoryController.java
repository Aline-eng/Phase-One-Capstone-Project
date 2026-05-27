package com.igirepay.lab3_mini_capstone.controller;

import com.igirepay.lab1_oop.model.Transaction;
import com.igirepay.lab2_jdbc.service.JdbcWalletService;
import com.igirepay.lab3_mini_capstone.util.SceneManager;
import com.igirepay.lab3_mini_capstone.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoryController {

    @FXML private TextField filterAccountId;
    @FXML private Label summaryLabel;
    @FXML private VBox transactionListBox;

    private final JdbcWalletService service = new JdbcWalletService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm");

    @FXML
    public void initialize() {
        // Auto-load the first account of the logged-in customer on open
        try {
            var customer = SessionManager.getInstance().getCurrentCustomer();
            if (customer == null) return;
            var accounts = service.findAccountsByCustomer(customer.getCustomerId());
            if (!accounts.isEmpty()) {
                filterAccountId.setText(String.valueOf(accounts.get(0).getAccountId()));
                loadHistory();
            }
        } catch (Exception e) {
            summaryLabel.setText("Could not load accounts.");
        }
    }

    @FXML
    private void loadHistory() {
        String idText = filterAccountId.getText().trim();
        if (idText.isEmpty()) {
            summaryLabel.setText("Please enter an account ID.");
            return;
        }

        try {
            int accountId = Integer.parseInt(idText);
            List<Transaction> transactions = service.getTransactionHistory(accountId);

            transactionListBox.getChildren().clear();

            if (transactions.isEmpty()) {
                Label empty = new Label("No transactions found for account " + accountId);
                empty.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 13px; -fx-padding: 20 0 0 0;");
                transactionListBox.getChildren().add(empty);
                summaryLabel.setText("Account " + accountId + " — 0 transactions");
                return;
            }

            // Summary line
            double totalIn = 0, totalOut = 0;
            for (Transaction t : transactions) {
                boolean incoming = t.getTransactionType().name().contains("DEPOSIT")
                                || t.getTransactionType().name().contains("_IN");
                if (incoming) totalIn += t.getAmount();
                else totalOut += t.getAmount();
            }
            summaryLabel.setText(String.format(
                "Account %d  •  %d transactions  •  In: +%,.0f  Out: -%,.0f RWF",
                accountId, transactions.size(), totalIn, totalOut));

            // Build one row per transaction
            for (Transaction t : transactions) {
                transactionListBox.getChildren().add(buildRow(t));
            }

        } catch (NumberFormatException e) {
            summaryLabel.setText("Account ID must be a number.");
        } catch (Exception e) {
            summaryLabel.setText("Error: " + e.getMessage());
        }
    }

    private HBox buildRow(Transaction t) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; " +
                     "-fx-padding: 12 14 12 14; -fx-cursor: default;");
        // Add bottom margin between rows
        VBox.setMargin(row, new javafx.geometry.Insets(0, 0, 6, 0));

        boolean incoming = t.getTransactionType().name().contains("DEPOSIT")
                        || t.getTransactionType().name().contains("_IN");

        // Icon
        String icon = switch (t.getTransactionType()) {
            case DEPOSIT      -> "⬇";
            case WITHDRAW     -> "⬆";
            case TRANSFER_OUT -> "↗";
            case TRANSFER_IN  -> "↙";
        };
        String iconBg = incoming ? "#E8F5E9" : "#FFF3E0";
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-background-color: " + iconBg + "; -fx-background-radius: 20; " +
                           "-fx-padding: 8 10 8 10; -fx-font-size: 14px;");

        // Left: type + reference
        VBox left = new VBox(2);
        Label typeLabel = new Label(t.getTransactionType().name().replace("_", " "));
        typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #1A1A2E;");
        Label refLabel = new Label(t.getReferenceId());
        refLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #AAAAAA;");
        left.getChildren().addAll(typeLabel, refLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Right: amount + date
        VBox right = new VBox(2);
        right.setAlignment(Pos.CENTER_RIGHT);
        String amountColor = incoming ? "#43A047" : "#E53935";
        String amountPrefix = incoming ? "+" : "-";
        Label amountLabel = new Label(amountPrefix + String.format("%,.0f RWF", t.getAmount()));
        amountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + amountColor + ";");
        Label dateLabel = new Label(t.getTimestamp().format(FMT));
        dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #AAAAAA;");
        right.getChildren().addAll(amountLabel, dateLabel);

        row.getChildren().addAll(iconLabel, left, spacer, right);
        return row;
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
