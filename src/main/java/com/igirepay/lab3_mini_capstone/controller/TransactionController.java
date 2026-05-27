package com.igirepay.lab3_mini_capstone.controller;

import com.igirepay.lab1_oop.enums.TransactionType;
import com.igirepay.lab1_oop.model.Account;
import com.igirepay.lab2_jdbc.service.JdbcWalletService;
import com.igirepay.lab1_oop.util.TransactionFee;
import com.igirepay.lab3_mini_capstone.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class TransactionController {

    @FXML private TextField accountIdField;
    @FXML private TextField amountField;
    @FXML private TextField referenceField;
    @FXML private TextField receiverField;
    @FXML private VBox receiverBox;
    @FXML private Label feeInfoLabel;
    @FXML private Label messageLabel;
    @FXML private Button submitButton;
    @FXML private Button tabDeposit;
    @FXML private Button tabWithdraw;
    @FXML private Button tabTransfer;

    private final JdbcWalletService service = new JdbcWalletService();

    // Tracks which tab is currently active
    private TransactionType currentType = TransactionType.DEPOSIT;

    @FXML
    public void initialize() {
        // Show fee info when amount changes so user knows what they'll be charged
        amountField.textProperty().addListener((obs, oldVal, newVal) -> updateFeeInfo(newVal));
        switchToDeposit();
    }

    // ===== TAB SWITCHING =====

    @FXML
    private void switchToDeposit() {
        currentType = TransactionType.DEPOSIT;
        submitButton.setText("DEPOSIT");
        receiverBox.setVisible(false);
        receiverBox.setManaged(false);
        feeInfoLabel.setText("ℹ No fee on deposits.");
        setActiveTab(tabDeposit, tabWithdraw, tabTransfer);
        clearMessage();
    }

    @FXML
    private void switchToWithdraw() {
        currentType = TransactionType.WITHDRAW;
        submitButton.setText("WITHDRAW");
        receiverBox.setVisible(false);
        receiverBox.setManaged(false);
        updateFeeInfo(amountField.getText());
        setActiveTab(tabWithdraw, tabDeposit, tabTransfer);
        clearMessage();
    }

    @FXML
    private void switchToTransfer() {
        currentType = TransactionType.TRANSFER_OUT;
        submitButton.setText("TRANSFER");
        receiverBox.setVisible(true);
        receiverBox.setManaged(true);
        updateFeeInfo(amountField.getText());
        setActiveTab(tabTransfer, tabDeposit, tabWithdraw);
        clearMessage();
    }

    // Updates the fee info label so the user sees the fee before confirming
    private void updateFeeInfo(String amountText) {
        if (currentType == TransactionType.DEPOSIT) {
            feeInfoLabel.setText("ℹ No fee on deposits.");
            return;
        }
        try {
            double amount = Double.parseDouble(amountText);
            double fee;
            String feeType;
            if (currentType == TransactionType.WITHDRAW) {
                // Check if account is wallet or savings to show correct fee
                String accIdText = accountIdField.getText().trim();
                if (!accIdText.isEmpty()) {
                    Account acc = service.findAccount(Integer.parseInt(accIdText));
                    if (acc != null && acc.getAccountType().equals("Savings")) {
                        fee = TransactionFee.getSavingsFee(amount);
                        feeType = "Savings early withdrawal fee";
                    } else {
                        fee = TransactionFee.getWalletFee(amount);
                        feeType = "Wallet withdrawal fee";
                    }
                } else {
                    fee = TransactionFee.getWalletFee(amount);
                    feeType = "Estimated fee";
                }
            } else {
                fee = TransactionFee.getWalletFee(amount);
                feeType = "Transfer fee";
            }
            feeInfoLabel.setText(String.format("ℹ %s: %.0f RWF  |  Total deducted: %.0f RWF",
                    feeType, fee, amount + fee));
        } catch (NumberFormatException e) {
            feeInfoLabel.setText("ℹ Enter an amount to see the fee.");
        } catch (Exception e) {
            feeInfoLabel.setText("ℹ Enter an amount to see the fee.");
        }
    }

    // ===== SUBMIT HANDLER =====

    @FXML
    private void handleSubmit() {
        String accIdText = accountIdField.getText().trim();
        String amountText = amountField.getText().trim();
        String ref = referenceField.getText().trim();

        if (accIdText.isEmpty() || amountText.isEmpty() || ref.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        try {
            int accountId = Integer.parseInt(accIdText);
            double amount = Double.parseDouble(amountText);

            if (amount <= 0) {
                showError("Amount must be greater than 0.");
                return;
            }

            if (currentType == TransactionType.TRANSFER_OUT) {
                // Transfer needs a receiver account
                String receiverText = receiverField.getText().trim();
                if (receiverText.isEmpty()) {
                    showError("Please enter the receiver account ID.");
                    return;
                }
                int receiverId = Integer.parseInt(receiverText);
                service.transfer(accountId, receiverId, ref, amount);
                Account sender = service.findAccount(accountId);
                showSuccess("Transfer successful!\nNew balance: "
                        + String.format("%,.0f RWF", sender.getBalance()));
            } else {
                service.processTransaction(accountId, ref, amount, currentType);
                Account acc = service.findAccount(accountId);
                showSuccess(currentType.name() + " successful!\nNew balance: "
                        + String.format("%,.0f RWF", acc.getBalance()));
            }

            // Clear fields after success
            amountField.clear();
            referenceField.clear();
            receiverField.clear();

        } catch (NumberFormatException e) {
            showError("Account ID and amount must be numbers.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // ===== NAVIGATION =====

    @FXML private void goBack()     { navigate("dashboard"); }
    @FXML private void goHome()     { navigate("dashboard"); }
    @FXML private void goAccounts() { navigate("accounts"); }
    @FXML private void goHistory()  { navigate("history"); }

    private void navigate(String screen) {
        try { SceneManager.switchTo(screen); }
        catch (Exception e) { e.printStackTrace(); }
    }

    // ===== HELPERS =====

    // Highlights the active tab with yellow underline, resets the others
    private void setActiveTab(Button active, Button... inactive) {
        active.setStyle("-fx-background-color: transparent; " +
                "-fx-border-color: transparent transparent #FFCC00 transparent; " +
                "-fx-border-width: 0 0 3 0; -fx-font-weight: bold; -fx-font-size: 13px; " +
                "-fx-text-fill: #FFCC00; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
        for (Button btn : inactive) {
            btn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                    "-fx-font-size: 13px; -fx-text-fill: #888888; " +
                    "-fx-padding: 10 16 10 16; -fx-cursor: hand;");
        }
    }

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: #E53935; -fx-font-weight: bold; -fx-font-size: 13px;");
    }

    private void showSuccess(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: #43A047; -fx-font-weight: bold; -fx-font-size: 13px;");
    }

    private void clearMessage() {
        messageLabel.setText("");
    }
}
