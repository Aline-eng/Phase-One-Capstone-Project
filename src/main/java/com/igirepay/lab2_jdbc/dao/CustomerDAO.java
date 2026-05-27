package com.igirepay.lab2_jdbc.dao;

import com.igirepay.lab1_oop.model.Customer;
import com.igirepay.lab2_jdbc.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class CustomerDAO implements GenericDAO<Customer> {

    // CREATE - inserts a new customer row into the customers table.
    // Returns the ID that PostgreSQL auto-generated for this customer,
    // so the caller knows what ID was assigned without doing a second query.
    @Override
    public int save(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (full_name, email, phone_number) VALUES (?, ?, ?)";

        // RETURN_GENERATED_KEYS tells JDBC to capture the auto-generated id
        // after the INSERT so we can return it to the caller.
        // try-with-resources ensures the connection and statement are always
        // closed when this block ends, even if an exception is thrown.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // ? placeholders are filled in order - 1 = full_name, 2 = email, 3 = phone_number.
            // PreparedStatement never treats these values as SQL code,
            // which prevents SQL injection attacks.
            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhoneNumber());
            stmt.executeUpdate();

            // getGeneratedKeys() returns a ResultSet containing the auto-generated id.
            // rs.next() moves to the first (and only) row, then getInt(1) reads it.
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Saving customer failed, no ID returned.");
        }
    }

    // READ - finds one customer by their primary key (id).
    // Returns null if no customer with that id exists,
    // so the caller can check and show "not found" to the user.
    @Override
    public Customer findById(int id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            // executeQuery() is used for SELECT statements - it returns a ResultSet.
            // executeUpdate() is used for INSERT, UPDATE, DELETE - it returns row count.
            ResultSet rs = stmt.executeQuery();

            // rs.next() moves to the next row and returns true if a row exists.
            // If no customer was found, rs.next() returns false and we return null.
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        }
    }

    // READ - returns every customer in the table.
    // Used when displaying the full customer list.
    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT * FROM customers ORDER BY id";
        List<Customer> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            // Loop through every row in the result and convert each to a Customer object.
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // UPDATE - overwrites the customer's details in the database.
    // We identify which row to update using the customer's id in the WHERE clause.
    @Override
    public void update(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET full_name = ?, email = ?, phone_number = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhoneNumber());
            // The id goes last because it matches the last ? in the WHERE clause
            stmt.setInt(4, customer.getCustomerId());
            stmt.executeUpdate();
        }
    }

    // DELETE - removes the customer row with the given id.
    // In a real system you would check for linked accounts first,
    // but the database FOREIGN KEY constraint will reject the delete
    // automatically if the customer still has accounts.
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Private helper - converts one ResultSet row into a Customer object.
    // Both findById and findAll use this method so the mapping logic
    // is written once. If the Customer constructor changes, we update here only.
    // Finds a customer by ID and PIN - used for login authentication.
    // Returns the customer if both match, null if either is wrong.
    // We never tell the user which one is wrong - that would help attackers.
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

    // Updates the PIN for a customer - used for PIN change feature.
    public void updatePin(int customerId, String newPin) throws SQLException {
        String sql = "UPDATE customers SET pin = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPin);
            stmt.setInt(2, customerId);
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
}
