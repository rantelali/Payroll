package com.example.payroll;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/payroll_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "901017181";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found!", e);
        }
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            if (connection != null && !connection.isClosed()) {
                System.out.println("✅ Database connection successful!");
            } else {
                System.out.println("❌ Failed to connect to the database.");
            }
        } catch (SQLException e) {
            System.out.println("⚠️ Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
