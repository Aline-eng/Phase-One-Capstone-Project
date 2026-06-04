package com.igirepay.lab3_mini_capstone.controller;

import com.igirepay.lab1_oop.enums.TransactionType;
import com.igirepay.lab1_oop.model.Account;
import com.igirepay.lab1_oop.model.Customer;
import com.igirepay.lab1_oop.util.TransactionFee;
import com.igirepay.lab2_jdbc.service.JdbcWalletService;
import com.igirepay.lab3_mini_capstone.util.SceneManager;
import com.igirepay.lab3_mini_capstone.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.UUID;

public class TransactionController {

    @FXML private Button tabDeposit;
    @FXML private Button tabWithdraw;
    @FXML private Button tabTransfer;

    @FXML private ComboBox<String> accountDropdown;
    @FXML private TextField amountField;
    @FXML private Label feeInfoLabel;
    @FXML private Label messageLabel;
    @FXML private Button submitButton;

    @FXML private VBox transferSection;
    @FXML private TextField receiverPhoneField;
    @FXML private Button lookupBtn;
    @FXML private VBox confirmCard;
    @FXML private Label confirmNameLabel;
    @FXML private Label confirmPhoneLabel;

    @FXML private VBox pinSection;
    @FXML private PasswordField pinField;

    @FXML private VBox savingsInfoBox;

    private final JdbcWalletService service = new JdbcWalletService();
    private TransactionType currentType = TransactionType.DEPOSIT;
    private List<Account> myAccounts;
    private Customer resolvedReceiver;

    @FXML
    public void initialize() {
        loadMyAccounts();
        amountField.textProperty().addListener((obs, o, n) -> updateFeeInfo(n));
        accountDropdown.valueProperty().addListener((obs, o, n) -> {
            updateFeeInfo(amountField.getText());
            checkSavingsInfo();
        });
        switchToDeposit();
    }

    private void loadMyAccounts() {
        try {
            Customer customer = SessionManager.getInstance().getCurrentCustomer();
            if (customer == null) return;
            myAccounts = service.findAccountsByCustomer(customer.getCustomerId());
            accountDropdown.getItems().clear();
            for (Account a : myAccounts) {
                accountDropdown.getItems().add(a.getAccountType() + " — ID: " + a.getAccountId()
                        + "  (" + String.format("%,.0f RWF", a.getBalance()) + ")");
            }
            if (!accountDropdown.getItems().isEmpty()) accountDropdown.getSelectionModel().selectFirst();
        } catch (Exception e) {
            showError("Could not load your accounts: " + e.getMessage());
        }
    }



    @FXML private void switchToDeposit() {
        currentType = TransactionType.DEPOSIT;
        submitButton.setText("DEPOSIT");
        transferSection.setVisible(false); transferSection.setManaged(false);
        confirmCard.setVisible(false);     confirmCard.setManaged(false);
        pinSection.setVisible(true);       pinSection.setManaged(true);
        resolvedReceiver = null;
        feeInfoLabel.setText("ℹ No fee on deposits.");
        setActiveTab(tabDeposit, tabWithdraw, tabTransfer);
        clearMessage();
    }

    @FXML private void switchToWithdraw() {
        currentType = TransactionType.WITHDRAW;
        submitButton.setText("WITHDRAW");
        transferSection.setVisible(false); transferSection.setManaged(false);
        confirmCard.setVisible(false);     confirmCard.setManaged(false);
        pinSection.setVisible(true);       pinSection.setManaged(true);
        resolvedReceiver = null;
        updateFeeInfo(amountField.getText());
        setActiveTab(tabWithdraw, tabDeposit, tabTransfer);
        clearMessage();
    }

    @FXML private void switchToTransfer() {
        currentType = TransactionType.TRANSFER_OUT;
        submitButton.setText("TRANSFER");
        transferSection.setVisible(true);  transferSection.setManaged(true);
        confirmCard.setVisible(false);     confirmCard.setManaged(false);
        pinSection.setVisible(false);      pinSection.setManaged(false);
        resolvedReceiver = null;
        updateFeeInfo(amountField.getText());
        setActiveTab(tabTransfer, tabDeposit, tabWithdraw);
        clearMessage();
    }


    @FXML
    private void handlePhoneLookup() {
        String phone = receiverPhoneField.getText().trim();
        if (phone.isEmpty()) { showError("Enter the receiver's phone number."); return; }
        if (!phone.matches("^07\\d{8}$")) {
            showError("Invalid phone number. Must be 10 digits starting with 07.");
            return;
        }
        Customer me = SessionManager.getInstance().getCurrentCustomer();
        if (me != null && phone.equals(me.getPhoneNumber())) {
            showError("You cannot transfer money to yourself.");
            return;
        }
        try {
            resolvedReceiver = service.findCustomerByPhone(phone);
            confirmNameLabel.setText("👤 " + resolvedReceiver.getFullName());
            confirmPhoneLabel.setText("📱 " + resolvedReceiver.getPhoneNumber());
            confirmCard.setVisible(true);  confirmCard.setManaged(true);
            pinSection.setVisible(true);   pinSection.setManaged(true);
            clearMessage();
        } catch (Exception e) {
            resolvedReceiver = null;
            confirmCard.setVisible(false); confirmCard.setManaged(false);
            pinSection.setVisible(false);  pinSection.setManaged(false);
            showError(e.getMessage());
        }
    }


    @FXML
    private void handleSubmit() {
        int selectedIndex = accountDropdown.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || myAccounts == null || selectedIndex >= myAccounts.size()) {
            showError("Please select an account."); return;
        }
        String amountText = amountField.getText().trim();
        String pin = pinField.getText().trim();

        if (amountText.isEmpty()) { showError("Please enter an amount."); return; }
        if (pin.isEmpty())        { showError("Please enter your PIN to confirm."); return; }

        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) { showError("Amount must be greater than 0."); return; }

            // Verify PIN
            Customer me = SessionManager.getInstance().getCurrentCustomer();
            Customer verified = service.login(me.getCustomerId(), pin);
            if (verified == null) { showError("Incorrect PIN. Transaction cancelled."); return; }

            int accountId = myAccounts.get(selectedIndex).getAccountId();
            String ref = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            if (currentType == TransactionType.TRANSFER_OUT) {
                if (resolvedReceiver == null) { showError("Please look up the receiver's phone number first."); return; }
                service.transfer(me.getCustomerId(), accountId, resolvedReceiver.getPhoneNumber(), ref, amount);
                showSuccess(String.format("✅ Transfer of %,.0f RWF to %s successful!", amount, resolvedReceiver.getFullName()));
                resolvedReceiver = null;
                confirmCard.setVisible(false); confirmCard.setManaged(false);
                pinSection.setVisible(false);  pinSection.setManaged(false);
                receiverPhoneField.clear();
            } else {
                service.processTransaction(me.getCustomerId(), accountId, ref, amount, currentType);
                Account updated = service.findAccount(accountId);
                showSuccess(String.format("✅ %s of %,.0f RWF successful!%nNew balance: %,.0f RWF",
                        currentType == TransactionType.DEPOSIT ? "Deposit" : "Withdrawal",
                        amount, updated.getBalance()));
            }

            amountField.clear();
            pinField.clear();
            loadMyAccounts();

        } catch (NumberFormatException e) {
            showError("Amount must be a valid number.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }


    private void checkSavingsInfo() {
        int idx = accountDropdown.getSelectionModel().getSelectedIndex();
        if (myAccounts == null || idx < 0 || idx >= myAccounts.size()) {
            savingsInfoBox.setVisible(false); savingsInfoBox.setManaged(false); return;
        }
        boolean isSavings = "Savings".equals(myAccounts.get(idx).getAccountType());
        savingsInfoBox.setVisible(isSavings); savingsInfoBox.setManaged(isSavings);
    }

    private void updateFeeInfo(String amountText) {
        if (currentType == TransactionType.DEPOSIT) { feeInfoLabel.setText("ℹ No fee on deposits."); return; }
        try {
            double amount = Double.parseDouble(amountText);
            int idx = accountDropdown.getSelectionModel().getSelectedIndex();
            boolean isSavings = myAccounts != null && idx >= 0 && idx < myAccounts.size()
                    && "Savings".equals(myAccounts.get(idx).getAccountType());
            double fee = isSavings ? TransactionFee.getSavingsFee(amount) : TransactionFee.getWalletFee(amount);
            String label = isSavings ? "Savings fee" : (currentType == TransactionType.TRANSFER_OUT ? "Transfer fee" : "Withdrawal fee");
            feeInfoLabel.setText(String.format("ℹ %s: %,.0f RWF  |  Total deducted: %,.0f RWF", label, fee, amount + fee));
        } catch (NumberFormatException e) {
            feeInfoLabel.setText("ℹ Enter an amount to see the fee.");
        }
    }

    private void setActiveTab(Button active, Button... inactive) {
        active.setStyle("-fx-background-color: transparent; -fx-border-color: transparent transparent #FFCC00 transparent; " +
                "-fx-border-width: 0 0 3 0; -fx-font-weight: bold; -fx-font-size: 13px; " +
                "-fx-text-fill: #FFCC00; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
        for (Button b : inactive)
            b.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                    "-fx-font-size: 13px; -fx-text-fill: #888888; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
    }

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: #E53935; -fx-font-weight: bold; -fx-font-size: 12px;");
    }

    private void showSuccess(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: #43A047; -fx-font-weight: bold; -fx-font-size: 12px;");
    }

    private void clearMessage() { messageLabel.setText(""); }

    @FXML private void goBack()     { navigate("dashboard"); }
    @FXML private void goHome()     { navigate("dashboard"); }
    @FXML private void goAccounts() { navigate("accounts"); }
    @FXML private void goHistory()  { navigate("history"); }

    private void navigate(String screen) {
        try { SceneManager.switchTo(screen); } catch (Exception e) { e.printStackTrace(); }
    }
}
