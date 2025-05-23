package edu.icet.Db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static DBConnection instance;
    private Connection connection;
    
    private String url = "jdbc:mysql://localhost:3306/hotel_reservation_sys_db";
    private String username = "root";
    private String password = "root";
    

    private DBConnection() {
        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connected successfully");
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }
    
    // Get the instance (singleton pattern)
    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }
    
    // Get a connection that works
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Create a new connection
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("Database connection renewed");
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
        return connection;
    }
}
