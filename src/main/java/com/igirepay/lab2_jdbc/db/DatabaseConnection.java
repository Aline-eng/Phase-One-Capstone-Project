package com.igirepay.lab2_jdbc.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/igirepay_db";
    private static final String USER = "postgres";

    // We read the password from an environment variable instead of hardcoding it.
    // Hardcoding passwords in source code is a serious security risk - anyone who
    // sees your GitHub repository would have your database password.
    // System.getenv("DB_PASSWORD") reads a variable you set on your own machine.
    // The second argument "postgres" is a fallback for local development only.
    private static final String PASSWORD = System.getenv("DB_PASSWORD") != null
            ? System.getenv("DB_PASSWORD")
            : "aline123";

    // Returns a fresh connection to the database.
    // We don't store the connection as a field because a single shared connection
    // causes problems - if one operation fails, it can corrupt the connection for others.
    // Each method that needs the database opens its own connection and closes it when done.
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
