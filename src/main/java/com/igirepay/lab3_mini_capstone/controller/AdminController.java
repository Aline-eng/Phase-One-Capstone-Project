package com.igirepay.lab3_mini_capstone.controller;

import com.igirepay.lab1_oop.model.Customer;
import com.igirepay.lab1_oop.model.Transaction;
import com.igirepay.lab2_jdbc.model.Loan;
import com.igirepay.lab2_jdbc.service.JdbcWalletService;
import com.igirepay.lab3_mini_capstone.util.SceneManager;
import com.igirepay.lab3_mini_capstone.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AdminController {


    @FXML private TextField searchIdField;
    @FXML private VBox foundCustomerBox;
    @FXML private Label foundNameLabel;
    @FXML private Label foundEmailLabel;
    @FXML private Label foundPhoneLabel;
    @FXML private Label foundRoleLabel;
    @FXML private Label foundAttemptsLabel;
    @FXML private Label findMessageLabel;


    @FXML private TextField txAccountIdField;
    @FXML private Label txSummaryLabel;
    @FXML private VBox txListBox;

    @FXML private VBox customersListBox;

    @FXML private VBox loansListBox;

    @FXML private TextField summaryAccountIdField;
    @FXML private VBox dailySummaryListBox;

    @FXML private Label adminNameLabel;

    private final JdbcWalletService service = new JdbcWalletService();
    private Customer foundCustomer;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @FXML
    public void initialize() {
        if (!SessionManager.getInstance().isAdmin()) {
            navigate("dashboard");
            return;
        }
        Customer admin = SessionManager.getInstance().getCurrentCustomer();
        if (admin != null) {
            adminNameLabel.setText("Welcome, " + admin.getFullName());
        }
    }


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

        row.setOnMouseClicked(e -> {
            searchIdField.setText(String.valueOf(c.getCustomerId()));
            handleFind();
        });

        row.getChildren().addAll(icon, info);
        return row;
    }


    @FXML
    private void handleLoadDailySummary() {
        String idText = summaryAccountIdField.getText().trim();
        if (idText.isEmpty()) {
            dailySummaryListBox.getChildren().setAll(noLabel("Please enter an account ID."));
            return;
        }
        try {
            int accountId = Integer.parseInt(idText);
            Map<String, double[]> summary = service.getDailySummary(accountId);
            dailySummaryListBox.getChildren().clear();
            if (summary.isEmpty()) {
                dailySummaryListBox.getChildren().add(noLabel("No transactions found for account " + accountId));
                return;
            }
            for (Map.Entry<String, double[]> entry : summary.entrySet()) {
                double in  = entry.getValue()[0];
                double out = entry.getValue()[1];
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8; -fx-padding: 10 12 10 12;");
                VBox.setMargin(row, new javafx.geometry.Insets(0, 0, 4, 0));

                Label dateLabel = new Label(entry.getKey());
                dateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #1A1A2E; -fx-min-width: 90;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label inLabel = new Label(String.format("+%,.0f", in));
                inLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #43A047;");
                Label outLabel = new Label(String.format("-%,.0f", out));
                outLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #E53935;");
                Label netLabel = new Label(String.format("Net: %,.0f RWF", in - out));
                netLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555;");

                row.getChildren().addAll(dateLabel, spacer, inLabel, new Label(" / "), outLabel, new Label("  "), netLabel);
                dailySummaryListBox.getChildren().add(row);
            }
        } catch (NumberFormatException e) {
            dailySummaryListBox.getChildren().setAll(noLabel("Account ID must be a number."));
        } catch (Exception e) {
            dailySummaryListBox.getChildren().setAll(noLabel("Error: " + e.getMessage()));
        }
    }


    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clear();
        navigate("login");
    }


    @FXML
    private void loadAllLoans() {
        try {
            List<Loan> loans = service.getAllLoans();
            loansListBox.getChildren().clear();

            if (loans.isEmpty()) {
                loansListBox.getChildren().add(noLabel("No loan requests found."));
                return;
            }
            for (Loan loan : loans) {
                loansListBox.getChildren().add(buildLoanRow(loan));
            }
        } catch (Exception e) {
            loansListBox.getChildren().clear();
            loansListBox.getChildren().add(noLabel("Error: " + e.getMessage()));
        }
    }

    private HBox buildLoanRow(Loan loan) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8; -fx-padding: 10 12 10 12;");
        VBox.setMargin(row, new javafx.geometry.Insets(0, 0, 4, 0));

        String icon = switch (loan.getStatus()) {
            case "APPROVED" -> "✅";
            case "REJECTED" -> "❌";
            default         -> "⏳";
        };
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");

        VBox info = new VBox(2);
        Label amtLabel = new Label(String.format("%,.0f RWF  •  Customer ID: %d", loan.getAmount(), loan.getCustomerId()));
        amtLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #1A1A2E;");
        Label reasonLabel = new Label(loan.getReason());
        reasonLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");
        info.getChildren().addAll(amtLabel, reasonLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox actions = new VBox(4);
        actions.setAlignment(Pos.CENTER_RIGHT);
        if ("PENDING".equals(loan.getStatus())) {
            Button approveBtn = new Button("Approve");
            approveBtn.setStyle("-fx-background-color: #43A047; -fx-text-fill: #FFFFFF; " +
                    "-fx-font-size: 10px; -fx-background-radius: 6; -fx-padding: 4 8 4 8; -fx-cursor: hand;");
            approveBtn.setOnAction(e -> updateLoanStatus(loan.getId(), "APPROVED"));

            Button rejectBtn = new Button("Reject");
            rejectBtn.setStyle("-fx-background-color: #E53935; -fx-text-fill: #FFFFFF; " +
                    "-fx-font-size: 10px; -fx-background-radius: 6; -fx-padding: 4 8 4 8; -fx-cursor: hand;");
            rejectBtn.setOnAction(e -> updateLoanStatus(loan.getId(), "REJECTED"));

            actions.getChildren().addAll(approveBtn, rejectBtn);
        } else {
            String statusColor = "APPROVED".equals(loan.getStatus()) ? "#43A047" : "#E53935";
            Label statusLabel = new Label(loan.getStatus());
            statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: " + statusColor + ";");
            actions.getChildren().add(statusLabel);
        }

        row.getChildren().addAll(iconLabel, info, spacer, actions);
        return row;
    }

    private void updateLoanStatus(int loanId, String status) {
        try {
            service.updateLoanStatus(loanId, status);
            loadAllLoans();
        } catch (Exception e) {
        }
    }


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
