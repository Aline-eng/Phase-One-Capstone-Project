package com.igirepay.lab2_jdbc.dao;

import com.igirepay.lab1_oop.enums.TransactionType;
import com.igirepay.lab1_oop.model.Transaction;
import com.igirepay.lab2_jdbc.db.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class TransactionDAO implements GenericDAO<Transaction> {

    
    public int save(int accountId, Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, reference_id, transaction_type, amount) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, accountId);
            stmt.setString(2, transaction.getReferenceId());
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

    
    @Override
    public int save(Transaction transaction) throws SQLException {
        return save(0, transaction);
    }

    
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

    /**
     * Returns a daily summary map: date string -> [totalIn, totalOut]
     * for a given account, grouped by calendar day.
     */
    public Map<String, double[]> getDailySummary(int accountId) throws SQLException {
        String sql = "SELECT DATE(created_at) AS day, transaction_type, SUM(amount) AS total "
                   + "FROM transactions WHERE account_id = ? "
                   + "GROUP BY day, transaction_type ORDER BY day DESC";
        Map<String, double[]> summary = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String day = rs.getString("day");
                String type = rs.getString("transaction_type");
                double total = rs.getDouble("total");
                summary.putIfAbsent(day, new double[]{0, 0});
                boolean incoming = type.contains("DEPOSIT") || type.contains("_IN");
                if (incoming) summary.get(day)[0] += total;
                else          summary.get(day)[1] += total;
            }
        }
        return summary;
    }

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

    
    @Override
    public void update(Transaction transaction) {
        throw new UnsupportedOperationException("Transactions cannot be modified.");
    }

    
    @Override
    public void delete(int id) {
        throw new UnsupportedOperationException("Transactions cannot be deleted.");
    }

    
    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("id"),
                rs.getString("reference_id"),
                rs.getDouble("amount"),
                TransactionType.valueOf(rs.getString("transaction_type")),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
