package com.igirepay.lab3_mini_capstone.controller;

import com.igirepay.lab2_jdbc.model.Loan;
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

public class LoanController {

    @FXML private TextField amountField;
    @FXML private TextField reasonField;
    @FXML private Label requestMessageLabel;
    @FXML private VBox loansListBox;

    private final JdbcWalletService service = new JdbcWalletService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @FXML
    public void initialize() {
        loadMyLoans();
    }

    @FXML
    private void handleRequest() {
        String amountText = amountField.getText().trim();
        String reason = reasonField.getText().trim();

        if (amountText.isEmpty() || reason.isEmpty()) {
            showMsg("Please fill in all fields.", false);
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showMsg("Loan amount must be greater than 0.", false);
                return;
            }

            var customer = SessionManager.getInstance().getCurrentCustomer();
            if (customer == null) return;

            int loanId = service.requestLoan(customer.getCustomerId(), amount, reason);
            showMsg("Loan request submitted! ID: " + loanId + "\nStatus: PENDING — awaiting admin review.", true);
            amountField.clear();
            reasonField.clear();
            loadMyLoans();

        } catch (NumberFormatException e) {
            showMsg("Amount must be a number.", false);
        } catch (Exception e) {
            showMsg("Error: " + e.getMessage(), false);
        }
    }

    @FXML
    private void loadMyLoans() {
        try {
            var customer = SessionManager.getInstance().getCurrentCustomer();
            if (customer == null) return;

            List<Loan> loans = service.getLoansByCustomer(customer.getCustomerId());
            loansListBox.getChildren().clear();

            if (loans.isEmpty()) {
                loansListBox.getChildren().add(noLabel("No loan requests yet."));
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
        row.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 10; -fx-padding: 12 14 12 14;");
        VBox.setMargin(row, new javafx.geometry.Insets(0, 0, 6, 0));

        String icon = switch (loan.getStatus()) {
            case "APPROVED" -> "✅";
            case "REJECTED" -> "❌";
            default         -> "⏳";
        };
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18px;");

        VBox left = new VBox(2);
        Label amountLabel = new Label(String.format("%,.0f RWF", loan.getAmount()));
        amountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1A1A2E;");
        Label reasonLabel = new Label(loan.getReason());
        reasonLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");
        Label dateLabel = new Label(loan.getRequestedAt().format(FMT));
        dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #AAAAAA;");
        left.getChildren().addAll(amountLabel, reasonLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String statusColor = switch (loan.getStatus()) {
            case "APPROVED" -> "#43A047";
            case "REJECTED" -> "#E53935";
            default         -> "#FF9800";
        };
        Label statusLabel = new Label(loan.getStatus());
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: " + statusColor + ";");

        row.getChildren().addAll(iconLabel, left, spacer, statusLabel);
        return row;
    }

    private void showMsg(String msg, boolean success) {
        requestMessageLabel.setText(msg);
        requestMessageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: "
                + (success ? "#43A047" : "#E53935") + ";");
    }

    private Label noLabel(String msg) {
        Label l = new Label(msg);
        l.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 12px;");
        return l;
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
