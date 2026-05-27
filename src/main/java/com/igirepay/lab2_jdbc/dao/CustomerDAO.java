package com.igirepay.lab2_jdbc.dao;

import com.igirepay.lab1_oop.model.Customer;
import com.igirepay.lab2_jdbc.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO implements GenericDAO<Customer> {

    // CREATE - inserts customer and PIN together in one statement inside a JDBC transaction.
    // Using a transaction means if anything fails, the whole operation is rolled back -
    // including the sequence advance, so IDs don't skip on failed registrations.
    public int save(Customer customer, String pin) throws SQLException {
        String sql = "INSERT INTO customers (full_name, email, phone_number, pin, role) VALUES (?, ?, ?, ?, ?)";

        Connection conn = DatabaseConnection.getConnection();
        try {
            // Disable auto-commit so we control when the transaction is committed
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhoneNumber());
            stmt.setString(4, pin);
            stmt.setString(5, "USER");
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (!rs.next()) {
                conn.rollback();
                throw new SQLException("Registration failed, no ID returned.");
            }
            int newId = rs.getInt(1);
            conn.commit();
            return newId;

        } catch (SQLException e) {
            conn.rollback();
            // Translate database constraint errors into friendly messages
            throw new SQLException(friendlyError(e));
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    // Kept for GenericDAO interface - delegates to the full version with empty pin
    @Override
    public int save(Customer customer) throws SQLException {
        return save(customer, "");
    }

    @Override
    public Customer findById(int id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
            return null;
        }
    }

    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT * FROM customers ORDER BY id";
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public void update(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET full_name = ?, email = ?, phone_number = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhoneNumber());
            stmt.setInt(4, customer.getCustomerId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Login by phone + PIN, also checks if account is locked
    public Customer findByPhoneAndPin(String phone, String pin) throws SQLException {
        String sql = "SELECT * FROM customers WHERE phone_number = ? AND pin = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            stmt.setString(2, pin);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
            return null;
        }
    }

    public Customer findByIdAndPin(int id, String pin) throws SQLException {
        String sql = "SELECT * FROM customers WHERE id = ? AND pin = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, pin);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
            return null;
        }
    }

    // Find by phone only - used to check lock status before verifying PIN
    public Customer findByPhone(String phone) throws SQLException {
        String sql = "SELECT * FROM customers WHERE phone_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
            return null;
        }
    }

    public void updatePin(int customerId, String newPin) throws SQLException {
        String sql = "UPDATE customers SET pin = ?, failed_attempts = 0 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPin);
            stmt.setInt(2, customerId);
            stmt.executeUpdate();
        }
    }

    // Increments failed login attempts - called on wrong PIN
    public void incrementFailedAttempts(int customerId) throws SQLException {
        String sql = "UPDATE customers SET failed_attempts = failed_attempts + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.executeUpdate();
        }
    }

    // Locks the account after too many failed attempts
    public void lockAccount(int customerId) throws SQLException {
        String sql = "UPDATE customers SET is_locked = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.executeUpdate();
        }
    }

    // Resets failed attempts on successful login
    public void resetFailedAttempts(int customerId) throws SQLException {
        String sql = "UPDATE customers SET failed_attempts = 0, is_locked = FALSE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.executeUpdate();
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone_number")
        );
    }

    // Converts raw PostgreSQL error messages into user-friendly ones
    private String friendlyError(SQLException e) {
        String msg = e.getMessage().toLowerCase();
        if (msg.contains("phone_number") && msg.contains("unique")) {
            return "This phone number is already registered. Please use a different number.";
        }
        if (msg.contains("email") && msg.contains("unique")) {
            return "This email address is already registered. Please use a different email.";
        }
        if (msg.contains("pin") && msg.contains("not-null")) {
            return "PIN is required. Please enter a 5-digit PIN.";
        }
        return "Registration failed. Please check your details and try again.";
    }
}
