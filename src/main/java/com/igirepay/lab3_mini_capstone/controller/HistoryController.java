package com.igirepay.lab3_mini_capstone.controller;

import com.igirepay.lab1_oop.model.Account;
import com.igirepay.lab1_oop.model.Customer;
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

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoryController {

    @FXML private TextField filterAccountId;
    @FXML private Label summaryLabel;
    @FXML private VBox transactionListBox;

    private final JdbcWalletService service = new JdbcWalletService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm");

    private List<Transaction> allTransactions;

    @FXML
    public void initialize() {
        try {
            Customer customer = SessionManager.getInstance().getCurrentCustomer();
            if (customer == null) return;
            List<Account> accounts = service.findAccountsByCustomer(customer.getCustomerId());
            if (!accounts.isEmpty()) {
                filterAccountId.setText(String.valueOf(accounts.get(0).getAccountId()));
                loadHistory();
            }
        } catch (Exception e) {
            summaryLabel.setText("Could not load your accounts.");
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

            Customer customer = SessionManager.getInstance().getCurrentCustomer();
            List<Account> myAccounts = service.findAccountsByCustomer(customer.getCustomerId());
            boolean ownsAccount = myAccounts.stream()
                    .anyMatch(a -> a.getAccountId() == accountId);

            if (!ownsAccount) {
                summaryLabel.setText("Account " + accountId + " does not belong to your profile.");
                transactionListBox.getChildren().clear();
                transactionListBox.getChildren().add(noTransactionsLabel(
                    "You can only view transactions for your own accounts."));
                return;
            }

            Account account = service.findAccount(accountId);
            String accountType = account != null ? account.getAccountType() : "Unknown";

            allTransactions = service.getTransactionHistory(accountId);
            transactionListBox.getChildren().clear();

            if (allTransactions.isEmpty()) {
                summaryLabel.setText("Account " + accountId + " (" + accountType + ") — No transactions yet");
                transactionListBox.getChildren().add(noTransactionsLabel(
                    "No transactions found for your " + accountType + " account (ID: " + accountId + ").\n"
                    + "Make a deposit or transfer to see your history here."));
                return;
            }

            renderTransactions(allTransactions, accountId, accountType);

        } catch (NumberFormatException e) {
            summaryLabel.setText("Account ID must be a number.");
        } catch (Exception e) {
            summaryLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportCsv() {
        if (allTransactions == null || allTransactions.isEmpty()) {
            summaryLabel.setText("No transactions to export.");
            return;
        }
        String filename = "transactions_account_" + filterAccountId.getText().trim()
                + "_" + LocalDate.now() + ".csv";
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("ID,Reference,Type,Amount,Date\n");
            for (Transaction t : allTransactions) {
                writer.write(t.getTransactionId() + ","
                        + t.getReferenceId() + ","
                        + t.getTransactionType().name() + ","
                        + t.getAmount() + ","
                        + t.getTimestamp().format(FMT) + "\n");
            }
            summaryLabel.setText("Exported to " + filename);
        } catch (IOException e) {
            summaryLabel.setText("Export failed: " + e.getMessage());
        }
    }

    private void renderTransactions(List<Transaction> transactions, int accountId, String accountType) {
        double totalIn = 0, totalOut = 0;
        for (Transaction t : transactions) {
            boolean in = isIncoming(t);
            if (in) totalIn += t.getAmount(); else totalOut += t.getAmount();
        }
        summaryLabel.setText(String.format(
            "Account %d (%s)  •  %d transactions  •  In: +%,.0f  Out: -%,.0f RWF",
            accountId, accountType, transactions.size(), totalIn, totalOut));

        transactionListBox.getChildren().clear();
        transactions.forEach(t -> transactionListBox.getChildren().add(buildRow(t)));
    }

    private HBox buildRow(Transaction t) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-padding: 12 14 12 14;");
        VBox.setMargin(row, new javafx.geometry.Insets(0, 0, 6, 0));

        boolean incoming = isIncoming(t);

        // Icon - handle TRANSFER legacy type gracefully
        String icon = switch (t.getTransactionType()) {
            case DEPOSIT      -> "⬇";
            case WITHDRAW     -> "⬆";
            case TRANSFER_OUT -> "↗";
            case TRANSFER_IN  -> "↙";
            case TRANSFER     -> "↔"; // legacy records
        };

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-background-color: " + (incoming ? "#E8F5E9" : "#FFF3E0")
                + "; -fx-background-radius: 20; -fx-padding: 8 10 8 10; -fx-font-size: 14px;");

        // Left: type label + reference + from/to info
        VBox left = new VBox(2);
        Label typeLabel = new Label(t.getTransactionType().name().replace("_", " "));
        typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #1A1A2E;");
        Label refLabel = new Label(t.getReferenceId());
        refLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #AAAAAA;");

        // From/To line - shows which customer phone is involved
        String fromTo = getFromToLabel(t);
        Label fromToLabel = new Label(fromTo);
        fromToLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");

        left.getChildren().addAll(typeLabel, refLabel, fromToLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Right: amount + date
        VBox right = new VBox(2);
        right.setAlignment(Pos.CENTER_RIGHT);
        Label amountLabel = new Label((incoming ? "+" : "-") + String.format("%,.0f RWF", t.getAmount()));
        amountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: "
                + (incoming ? "#43A047" : "#E53935") + ";");
        Label dateLabel = new Label(t.getTimestamp().format(FMT));
        dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #AAAAAA;");
        right.getChildren().addAll(amountLabel, dateLabel);

        row.getChildren().addAll(iconLabel, left, spacer, right);
        return row;
    }

    // Builds the From/To line shown under the reference ID in each transaction row
    private String getFromToLabel(Transaction t) {
        Customer me = SessionManager.getInstance().getCurrentCustomer();
        String myPhone = me != null ? me.getPhoneNumber() : "Me";

        return switch (t.getTransactionType()) {
            case DEPOSIT      -> "To: " + myPhone;
            case WITHDRAW     -> "From: " + myPhone;
            case TRANSFER_OUT -> "From: " + myPhone;
            case TRANSFER_IN  -> "To: " + myPhone;
            case TRANSFER     -> "Transfer";
        };
    }

    private boolean isIncoming(Transaction t) {
        String name = t.getTransactionType().name();
        return name.contains("DEPOSIT") || name.contains("_IN");
    }

    private Label noTransactionsLabel(String message) {
        Label label = new Label(message);
        label.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 12px; -fx-padding: 16 0 0 0;");
        label.setWrapText(true);
        return label;
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
