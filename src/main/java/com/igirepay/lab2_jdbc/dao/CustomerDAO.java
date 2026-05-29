package com.igirepay.lab2_jdbc.dao;

import com.igirepay.lab1_oop.model.Customer;
import com.igirepay.lab2_jdbc.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO implements GenericDAO<Customer> {


    public int save(Customer customer, String pin) throws SQLException {
        String sql = "INSERT INTO customers (full_name, email, phone_number, pin, role) VALUES (?, ?, ?, ?, ?)";

        Connection conn = DatabaseConnection.getConnection();
        try {
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
            throw new SQLException(friendlyError(e));
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

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
        try {
            String sql = "UPDATE customers SET pin = ?, failed_attempts = 0 WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newPin);
                stmt.setInt(2, customerId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String sql = "UPDATE customers SET pin = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newPin);
                stmt.setInt(2, customerId);
                stmt.executeUpdate();
            }
        }
    }

    public void incrementFailedAttempts(int customerId) throws SQLException {
        String sql = "UPDATE customers SET failed_attempts = failed_attempts + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.executeUpdate();
        }
    }

    public void lockAccount(int customerId) throws SQLException {
        String sql = "UPDATE customers SET is_locked = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.executeUpdate();
        }
    }

    public void resetFailedAttempts(int customerId) throws SQLException {
        String sql = "UPDATE customers SET failed_attempts = 0, is_locked = FALSE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.executeUpdate();
        }
    }

    public String findRoleByPhone(String phone) throws SQLException {
        String sql = "SELECT role FROM customers WHERE phone_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("role");
            return "USER";
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

    private String friendlyError(SQLException e) {
        String msg = e.getMessage().toLowerCase();
        if (msg.contains("phone_number") && msg.contains("unique"))
            return "This phone number is already registered. Please use a different number.";
        if (msg.contains("email") && msg.contains("unique"))
            return "This email address is already registered. Please use a different email.";
        if (msg.contains("pin") && msg.contains("not-null"))
            return "PIN is required. Please enter a 5-digit PIN.";
        return "Registration failed. Please check your details and try again.";
    }
}
