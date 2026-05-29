package com.igirepay.lab2_jdbc.dao;

import com.igirepay.lab2_jdbc.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProcessedRequestDAO {


    public boolean exists(String referenceId) throws SQLException {
        String sql = "SELECT id FROM processed_requests WHERE reference_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, referenceId);
            ResultSet rs = stmt.executeQuery();

            return rs.next();
        }
    }

    public void save(String referenceId) throws SQLException {
        String sql = "INSERT INTO processed_requests (reference_id) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, referenceId);
            stmt.executeUpdate();
        }
    }


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