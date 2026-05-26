package com.igirepay.lab2_jdbc.dao;

import com.igirepay.lab1_oop.model.Account;
import com.igirepay.lab1_oop.model.SavingsAccount;
import com.igirepay.lab1_oop.model.WalletAccount;
import com.igirepay.lab2_jdbc.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// AccountDAO handles all database operations for the accounts table.
// Notice it implements GenericDAO<Account> but also adds extra methods
// that are specific to accounts (updateBalance, findByCustomerId).
// The GenericDAO interface only covers the 4 standard CRUD operations.
public class AccountDAO implements GenericDAO<Account> {

    // CREATE - inserts a new account linked to a customer.
    // We need customerId as a separate parameter because Account itself
    // does not store which customer it belongs to - that relationship
    // lives in the database as a foreign key.
    public int save(int customerId, Account account) throws SQLException {
        String sql = "INSERT INTO accounts (customer_id, account_type, balance) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, customerId);
            // getAccountType() returns "Wallet" or "Savings" - we store this string
            // in the database so we know which subclass to recreate when reading back
            stmt.setString(2, account.getAccountType());
            stmt.setDouble(3, account.getBalance());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Saving account failed, no ID returned.");
        }
    }

    // This satisfies the GenericDAO interface contract.
    // It delegates to the two-parameter version with customerId = 0
    // because the interface does not know about customerId.
    // In practice, always use save(customerId, account) directly.
    @Override
    public int save(Account account) throws SQLException {
        return save(0, account);
    }

    // READ - finds one account by its id.
    // Returns the correct subclass (WalletAccount or SavingsAccount)
    // so that the fee logic in those classes is preserved.
    @Override
    public Account findById(int id) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        }
    }

    // READ - returns all accounts that belong to one customer.
    // Used when displaying a customer's portfolio of accounts.
    public List<Account> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE customer_id = ? ORDER BY id";
        List<Account> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // UPDATE - saves the new balance after a deposit, withdrawal, or transfer.
    // We only update balance here - account_type and customer_id never change.
    public void updateBalance(int accountId, double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newBalance);
            stmt.setInt(2, accountId);
            stmt.executeUpdate();
        }
    }

    // UPDATE - satisfies the GenericDAO interface.
    // Updates all fields of an account row.
    @Override
    public void update(Account account) throws SQLException {
        String sql = "UPDATE accounts SET balance = ?, account_type = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, account.getBalance());
            stmt.setString(2, account.getAccountType());
            stmt.setInt(3, account.getAccountId());
            stmt.executeUpdate();
        }
    }

    // DELETE - removes an account row by id.
    // Used for deleting inactive accounts (Exercise 2.2 requirement).
    // The database will reject this if the account still has transactions
    // linked to it via the FOREIGN KEY on the transactions table.
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM accounts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Private helper - reads one row from the ResultSet and returns
    // the correct Account subclass based on the account_type column.
    // This is polymorphism at the data layer - we recreate the right
    // object type so WalletAccount and SavingsAccount fee logic still works
    // after loading from the database.
    private Account mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        double balance = rs.getDouble("balance");
        String type = rs.getString("account_type");

        // We check the stored string to decide which subclass to instantiate.
        // "Wallet" → WalletAccount (instant transfers, tiered fees)
        // anything else → SavingsAccount (date-based fee, minimum balance)
        if (type.equals("Wallet")) {
            return new WalletAccount(id, balance);
        } else {
            return new SavingsAccount(id, balance);
        }
    }
}
