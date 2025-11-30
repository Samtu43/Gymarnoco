package com.example.gymarnco;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3307/court_booking_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection;

    // Singleton: return one shared connection
    public static Connection getConnection() throws SQLException {
        try {
            // Check if connection is null or closed
            if (connection == null || connection.isClosed()) {

                // Load MySQL driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Create connection
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✔ Connected to MySQL successfully!");
            }
            return connection; // Return the active connection
        } catch (SQLException e) {
            System.err.println("❌ MySQL Connection Failed (SQLException): " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw the exception
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL Connection Failed (Driver Not Found): " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
    }
}