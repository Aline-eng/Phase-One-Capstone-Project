package com.igirepay.lab2_jdbc.dao;

import com.igirepay.lab1_oop.model.Account;
import com.igirepay.lab1_oop.model.SavingsAccount;
import com.igirepay.lab1_oop.model.WalletAccount;
import com.igirepay.lab2_jdbc.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class AccountDAO implements GenericDAO<Account> {


    public int save(int customerId, Account account) throws SQLException {
        String sql = "INSERT INTO accounts (customer_id, account_type, balance) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, customerId);

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


    @Override
    public int save(Account account) throws SQLException {
        return save(0, account);
    }


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


    public void updateBalance(int accountId, double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newBalance);
            stmt.setInt(2, accountId);
            stmt.executeUpdate();
        }
    }


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


    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM accounts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        double balance = rs.getDouble("balance");
        String type = rs.getString("account_type");

        if (type.equals("Wallet")) {
            return new WalletAccount(id, balance);
        } else {
            return new SavingsAccount(id, balance);
        }
    }
}
