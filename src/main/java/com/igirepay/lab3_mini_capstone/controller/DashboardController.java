package com.igirepay.lab3_mini_capstone.controller;

import com.igirepay.lab1_oop.model.Account;
import com.igirepay.lab1_oop.model.Customer;
import com.igirepay.lab1_oop.model.Transaction;
import com.igirepay.lab2_jdbc.service.JdbcWalletService;
import com.igirepay.lab3_mini_capstone.util.SceneManager;
import com.igirepay.lab3_mini_capstone.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML private Label greetingLabel;
    @FXML private Label dateLabel;
    @FXML private Label sidebarNameLabel;
    @FXML private Label totalBalanceLabel;
    @FXML private Label walletBalanceLabel;
    @FXML private Label walletIdLabel;
    @FXML private Label savingsBalanceLabel;
    @FXML private Label savingsIdLabel;

    @FXML private TableView<Transaction> recentTable;
    @FXML private TableColumn<Transaction, String> colRef;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, String> colAmount;
    @FXML private TableColumn<Transaction, String> colDate;

    private final JdbcWalletService service = new JdbcWalletService();

    // initialize() is called automatically by JavaFX after the FXML is loaded.
    // This is where we populate the screen with data.
    @FXML
    public void initialize() {
        Customer customer = SessionManager.getInstance().getCurrentCustomer();
        if (customer == null) return;

        // Set greeting based on time of day
        int hour = LocalDateTime.now().getHour();
        String greeting = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";
        greetingLabel.setText(greeting + ", " + customer.getFullName().split(" ")[0] + "!");

        // Show today's date
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));

        // Show name in sidebar
        sidebarNameLabel.setText(customer.getFullName());

        loadAccountBalances(customer);
        loadRecentTransactions(customer);
    }

    private void loadAccountBalances(Customer customer) {
        try {
            List<Account> accounts = service.findAccountsByCustomer(customer.getCustomerId());

            double total = 0;
            for (Account acc : accounts) {
                total += acc.getBalance();
                if (acc.getAccountType().equals("Wallet")) {
                    walletBalanceLabel.setText(String.format("%.0f RWF", acc.getBalance()));
                    walletIdLabel.setText("Account ID: " + acc.getAccountId());
                } else {
                    savingsBalanceLabel.setText(String.format("%.0f RWF", acc.getBalance()));
                    savingsIdLabel.setText("Account ID: " + acc.getAccountId());
                }
            }
            totalBalanceLabel.setText(String.format("%.0f RWF", total));

        } catch (Exception e) {
            totalBalanceLabel.setText("Error loading");
        }
    }

    private void loadRecentTransactions(Customer customer) {
        // Set up table columns - each column reads one property from Transaction
        // SimpleStringProperty wraps a String so JavaFX can observe it for changes
        colRef.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getReferenceId()));
        colType.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTransactionType().name()));
        colAmount.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.0f RWF", data.getValue().getAmount())));
        colDate.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTimestamp()
                        .format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))));

        try {
            List<Account> accounts = service.findAccountsByCustomer(customer.getCustomerId());
            // Load transactions from the first account found - just for the preview
            if (!accounts.isEmpty()) {
                List<Transaction> recent = service.getTransactionHistory(accounts.get(0).getAccountId());
                // Show only the 5 most recent
                int limit = Math.min(recent.size(), 5);
                recentTable.setItems(FXCollections.observableArrayList(recent.subList(0, limit)));
            }
        } catch (Exception e) {
            // Table stays empty - not a critical failure
        }
    }

    // ===== SIDEBAR NAVIGATION =====
    // Each method switches to the corresponding screen

    @FXML private void showDashboard() {
        try { SceneManager.switchTo("dashboard"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void showCustomers() {
        try { SceneManager.switchTo("customers"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void showAccounts() {
        try { SceneManager.switchTo("accounts"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void showTransactions() {
        try { SceneManager.switchTo("transactions"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void showHistory() {
        try { SceneManager.switchTo("history"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleLogout() {
        // Clear the session so no customer data remains
        SessionManager.getInstance().clear();
        try { SceneManager.switchTo("login"); } catch (Exception e) { e.printStackTrace(); }
    }
}
