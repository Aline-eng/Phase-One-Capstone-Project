package com.igirepay.lab2_jdbc.dao;

import com.igirepay.lab2_jdbc.db.DatabaseConnection;
import com.igirepay.lab2_jdbc.model.Loan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    // Saves a new loan request - status defaults to PENDING
    public int save(int customerId, double amount, String reason) throws SQLException {
        String sql = "INSERT INTO loans (customer_id, amount, reason) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, customerId);
            stmt.setDouble(2, amount);
            stmt.setString(3, reason);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            throw new SQLException("Loan request failed, no ID returned.");
        }
    }

    // Returns all loans for one customer
    public List<Loan> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT * FROM loans WHERE customer_id = ? ORDER BY requested_at DESC";
        List<Loan> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // Returns all loans in the system - used by admin
    public List<Loan> findAll() throws SQLException {
        String sql = "SELECT * FROM loans ORDER BY requested_at DESC";
        List<Loan> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // Admin updates loan status to APPROVED or REJECTED
    public void updateStatus(int loanId, String status) throws SQLException {
        String sql = "UPDATE loans SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, loanId);
            stmt.executeUpdate();
        }
    }

    private Loan mapRow(ResultSet rs) throws SQLException {
        return new Loan(
                rs.getInt("id"),
                rs.getInt("customer_id"),
                rs.getDouble("amount"),
                rs.getString("reason"),
                rs.getString("status"),
                rs.getTimestamp("requested_at").toLocalDateTime()
        );
    }
}
