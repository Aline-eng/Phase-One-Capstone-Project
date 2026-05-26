package com.igirepay.lab2_jdbc.dao;

import com.igirepay.lab2_jdbc.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// ProcessedRequestDAO handles the processed_requests table.
// This table has one job: remember every reference ID that was already processed
// so we can reject duplicate transaction requests.
// This is called idempotency - processing the same request twice must be impossible.
//
// Why does this NOT implement GenericDAO?
// Because its contract is completely different:
// - There is no model class to use as T
// - There is no update() or findById() needed
// - It only needs two operations: check if a ref exists, and save a new ref
// Forcing it to implement GenericDAO would mean writing fake empty methods,
// which is worse than just not using the interface here.
public class ProcessedRequestDAO {

    // Checks whether a reference ID has already been processed.
    // Returns true if found (= duplicate), false if not found (= safe to process).
    // This is called BEFORE every transaction to enforce idempotency.
    public boolean exists(String referenceId) throws SQLException {
        String sql = "SELECT id FROM processed_requests WHERE reference_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, referenceId);
            ResultSet rs = stmt.executeQuery();

            // rs.next() returns true if at least one row was found.
            // We don't need to read any column - just knowing a row exists is enough.
            return rs.next();
        }
    }

    // Saves a reference ID after a transaction is successfully processed.
    // This is called AFTER every successful transaction.
    // The UNIQUE constraint on reference_id in the database is a second layer
    // of protection - even if our Java check somehow fails, the database
    // will reject the duplicate insert with a SQLException.
    public void save(String referenceId) throws SQLException {
        String sql = "INSERT INTO processed_requests (reference_id) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, referenceId);
            stmt.executeUpdate();
        }
    }

    // Returns all processed reference IDs ordered by most recent first.
    // Used by IdempotencyService to display the full processed requests log.
    public List<String> findAll() throws SQLException {
        String sql = "SELECT reference_id, processed_at FROM processed_requests ORDER BY processed_at DESC";
        List<String> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                // Format each row as a readable string for display
                list.add("Ref: " + rs.getString("reference_id")
                        + " | Processed at: " + rs.getTimestamp("processed_at"));
            }
        }
        return list;
    }
}