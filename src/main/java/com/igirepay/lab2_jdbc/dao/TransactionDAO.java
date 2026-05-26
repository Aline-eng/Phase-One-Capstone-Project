package com.igirepay.lab2_jdbc.dao;

import com.igirepay.lab1_oop.enums.TransactionType;
import com.igirepay.lab1_oop.model.Transaction;
import com.igirepay.lab2_jdbc.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// TransactionDAO handles all database operations for the transactions table.
// Transactions are never updated or deleted in a real financial system -
// they are permanent records. So update() and delete() are intentionally
// left as unsupported operations.
public class TransactionDAO implements GenericDAO<Transaction> {

    // CREATE - saves a completed transaction linked to an account.
    // accountId is passed separately for the same reason as AccountDAO:
    // the Transaction object itself does not store which account it belongs to.
    public int save(int accountId, Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, reference_id, transaction_type, amount) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, accountId);
            stmt.setString(2, transaction.getReferenceId());
            // .name() converts the enum constant to its String name.
            // TransactionType.DEPOSIT.name() → "DEPOSIT"
            // We store the string in the database because SQL has no enum type
            // that maps directly to Java enums.
            stmt.setString(3, transaction.getTransactionType().name());
            stmt.setDouble(4, transaction.getAmount());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Saving transaction failed, no ID returned.");
        }
    }

    // Satisfies the GenericDAO interface - delegates to the two-parameter version.
    // Always use save(accountId, transaction) directly in the service layer.
    @Override
    public int save(Transaction transaction) throws SQLException {
        return save(0, transaction);
    }

    // READ - finds one transaction by its id.
    // Useful when you need to look up a specific transaction record.
    @Override
    public Transaction findById(int id) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE id = ?";

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

    // READ - returns all transactions for one specific account.
    // ORDER BY created_at DESC means newest transactions appear first,
    // which is the standard way bank statements are displayed.
    public List<Transaction> findByAccountId(int accountId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY created_at DESC";
        List<Transaction> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // READ - returns every transaction in the system.
    // Used for admin-level reporting or daily summaries.
    public List<Transaction> findAll() throws SQLException {
        String sql = "SELECT * FROM transactions ORDER BY created_at DESC";
        List<Transaction> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // UPDATE - transactions must never be modified in a financial system.
    // Once a transaction is recorded it is permanent - this is an audit requirement.
    // We throw UnsupportedOperationException to make this rule explicit and visible.
    @Override
    public void update(Transaction transaction) {
        throw new UnsupportedOperationException("Transactions cannot be modified.");
    }

    // DELETE - same reasoning as update - transactions are permanent records.
    @Override
    public void delete(int id) {
        throw new UnsupportedOperationException("Transactions cannot be deleted.");
    }

    // Private helper - converts one ResultSet row into a Transaction object.
    // TransactionType.valueOf() converts the stored String back to the enum constant.
    // "DEPOSIT" → TransactionType.DEPOSIT
    // This is the reverse of what .name() did when we saved it.
    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("id"),
                rs.getString("reference_id"),
                rs.getDouble("amount"),
                TransactionType.valueOf(rs.getString("transaction_type"))
        );
    }
}
