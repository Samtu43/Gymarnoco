package com.example.gymarnco;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3307/court_booking_db";
    private static final String USER = "root";        // default XAMPP user
    private static final String PASSWORD = "";        // default is empty in XAMPP

    private static Connection connection;

    // Singleton: return one shared connection
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {

                // Load MySQL driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Create connection
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✔ Connected to MySQL successfully!");
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("❌ MySQL Connection Failed: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }
}

