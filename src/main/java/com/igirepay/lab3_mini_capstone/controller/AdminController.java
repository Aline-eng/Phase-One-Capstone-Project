package com.igirepay.lab3_mini_capstone.controller;

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

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminController {

    // Find customer section
    @FXML private TextField searchIdField;
    @FXML private VBox foundCustomerBox;
    @FXML private Label foundNameLabel;
    @FXML private Label foundEmailLabel;
    @FXML private Label foundPhoneLabel;
    @FXML private Label foundRoleLabel;
    @FXML private Label foundAttemptsLabel;
    @FXML private Label findMessageLabel;

    // Transactions section
    @FXML private TextField txAccountIdField;
    @FXML private Label txSummaryLabel;
    @FXML private VBox txListBox;

    // All customers section
    @FXML private VBox customersListBox;

    // Admin name label
    @FXML private Label adminNameLabel;

    private final JdbcWalletService service = new JdbcWalletService();
    private Customer foundCustomer;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @FXML
    public void initialize() {
        // Redirect non-admins immediately
        if (!SessionManager.getInstance().isAdmin()) {
            navigate("dashboard");
            return;
        }
        Customer admin = SessionManager.getInstance().getCurrentCustomer();
        if (admin != null) {
            adminNameLabel.setText("Welcome, " + admin.getFullName());
        }
    }

    // ===== CUSTOMER MANAGEMENT =====

    @FXML
    private void handleFind() {
        String idText = searchIdField.getText().trim();
        if (idText.isEmpty()) {
            showFindMsg("Please enter a customer ID.", false);
            return;
        }
        try {
            int id = Integer.parseInt(idText);
            Customer c = service.findCustomer(id);
            if (c == null) {
                foundCustomerBox.setVisible(false);
                foundCustomerBox.setManaged(false);
                showFindMsg("No customer found with ID " + id, false);
                return;
            }
            foundCustomer = c;
            foundNameLabel.setText(c.getFullName());
            foundEmailLabel.setText("✉  " + c.getEmail());
            foundPhoneLabel.setText("📱  " + c.getPhoneNumber());

            String role = service.getRoleByPhone(c.getPhoneNumber());
            foundRoleLabel.setText("Role: " + role);
            foundRoleLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: "
                    + ("ADMIN".equals(role) ? "#E53935" : "#43A047") + ";");

            int attempts = service.getFailedAttempts(c.getPhoneNumber());
            boolean locked = service.isAccountLocked(c.getPhoneNumber());
            foundAttemptsLabel.setText("Failed attempts: " + attempts
                    + (locked ? "  🔒 LOCKED" : "  ✓ Active"));
            foundAttemptsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: "
                    + (locked ? "#E53935" : "#43A047") + ";");

            foundCustomerBox.setVisible(true);
            foundCustomerBox.setManaged(true);
            findMessageLabel.setText("");

        } catch (NumberFormatException e) {
            showFindMsg("Customer ID must be a number.", false);
        } catch (Exception e) {
            showFindMsg("Error: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleUnlock() {
        if (foundCustomer == null) return;
        try {
            service.unlockAccount(foundCustomer.getCustomerId());
            showFindMsg("Account unlocked for " + foundCustomer.getFullName(), true);
            handleFind(); // refresh the display
        } catch (Exception e) {
            showFindMsg("Error: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleResetAttempts() {
        if (foundCustomer == null) return;
        try {
            service.unlockAccount(foundCustomer.getCustomerId());
            showFindMsg("Failed attempts reset for " + foundCustomer.getFullName(), true);
            handleFind();
        } catch (Exception e) {
            showFindMsg("Error: " + e.getMessage(), false);
        }
    }

    // ===== TRANSACTION VIEW =====

    // Admin can view transactions for ANY account - not restricted to their own
    @FXML
    private void handleLoadTransactions() {
        String idText = txAccountIdField.getText().trim();
        if (idText.isEmpty()) {
            txSummaryLabel.setText("Please enter an account ID.");
            return;
        }
        try {
            int accountId = Integer.parseInt(idText);
            List<Transaction> transactions = service.getTransactionHistory(accountId);
            txListBox.getChildren().clear();

            if (transactions.isEmpty()) {
                txSummaryLabel.setText("No transactions found for account " + accountId);
                txListBox.getChildren().add(noLabel("No transactions found for this account."));
                return;
            }

            double totalIn = 0, totalOut = 0;
            for (Transaction t : transactions) {
                boolean in = t.getTransactionType().name().contains("DEPOSIT")
                          || t.getTransactionType().name().contains("_IN");
                if (in) totalIn += t.getAmount(); else totalOut += t.getAmount();
            }
            txSummaryLabel.setText(String.format("Account %d  •  %d transactions  •  In: +%,.0f  Out: -%,.0f RWF",
                    accountId, transactions.size(), totalIn, totalOut));

            for (Transaction t : transactions) {
                txListBox.getChildren().add(buildTxRow(t));
            }

        } catch (NumberFormatException e) {
            txSummaryLabel.setText("Account ID must be a number.");
        } catch (Exception e) {
            txSummaryLabel.setText("Error: " + e.getMessage());
        }
    }

    private HBox buildTxRow(Transaction t) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8; -fx-padding: 8 10 8 10;");
        VBox.setMargin(row, new javafx.geometry.Insets(0, 0, 4, 0));

        boolean incoming = t.getTransactionType().name().contains("DEPOSIT")
                        || t.getTransactionType().name().contains("_IN");
        String icon = switch (t.getTransactionType()) {
            case DEPOSIT      -> "⬇";
            case WITHDRAW     -> "⬆";
            case TRANSFER_OUT -> "↗";
            case TRANSFER_IN  -> "↙";
            case TRANSFER     -> "↔";
        };

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 13px;");

        VBox left = new VBox(1);
        Label typeLabel = new Label(t.getTransactionType().name().replace("_", " "));
        typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #1A1A2E;");
        Label refLabel = new Label(t.getReferenceId());
        refLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #AAAAAA;");
        left.getChildren().addAll(typeLabel, refLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox right = new VBox(1);
        right.setAlignment(Pos.CENTER_RIGHT);
        Label amountLabel = new Label((incoming ? "+" : "-") + String.format("%,.0f RWF", t.getAmount()));
        amountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: "
                + (incoming ? "#43A047" : "#E53935") + ";");
        Label dateLabel = new Label(t.getTimestamp().format(FMT));
        dateLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #AAAAAA;");
        right.getChildren().addAll(amountLabel, dateLabel);

        row.getChildren().addAll(iconLabel, left, spacer, right);
        return row;
    }

    // ===== ALL CUSTOMERS =====

    @FXML
    private void loadAllCustomers() {
        try {
            List<Customer> customers = service.findAllCustomers();
            customersListBox.getChildren().clear();

            if (customers.isEmpty()) {
                customersListBox.getChildren().add(noLabel("No customers found."));
                return;
            }
            for (Customer c : customers) {
                customersListBox.getChildren().add(buildCustomerRow(c));
            }
        } catch (Exception e) {
            customersListBox.getChildren().clear();
            customersListBox.getChildren().add(noLabel("Error: " + e.getMessage()));
        }
    }

    private HBox buildCustomerRow(Customer c) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8; " +
                     "-fx-padding: 10 12 10 12; -fx-cursor: hand;");
        VBox.setMargin(row, new javafx.geometry.Insets(0, 0, 4, 0));

        Label icon = new Label("👤");
        icon.setStyle("-fx-font-size: 15px;");

        VBox info = new VBox(2);
        Label name = new Label(c.getFullName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #1A1A2E;");
        Label phone = new Label(c.getPhoneNumber() + "  •  ID: " + c.getCustomerId());
        phone.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");
        info.getChildren().addAll(name, phone);

        // Clicking a row loads that customer into the find section
        row.setOnMouseClicked(e -> {
            searchIdField.setText(String.valueOf(c.getCustomerId()));
            handleFind();
        });

        row.getChildren().addAll(icon, info);
        return row;
    }

    // ===== LOGOUT =====

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clear();
        navigate("login");
    }

    // ===== HELPERS =====

    private Label noLabel(String msg) {
        Label l = new Label(msg);
        l.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 12px;");
        l.setWrapText(true);
        return l;
    }

    private void showFindMsg(String msg, boolean success) {
        findMessageLabel.setText(msg);
        findMessageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: "
                + (success ? "#43A047" : "#E53935") + ";");
    }

    private void navigate(String screen) {
        try { SceneManager.switchTo(screen); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
