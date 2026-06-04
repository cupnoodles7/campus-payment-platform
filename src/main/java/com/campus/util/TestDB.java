package com.campus.util;
 
import java.sql.Connection;
 
public class TestDB {
    public static void main(String[] args) {
        // This is a placeholder for testing database connectivity and operations
        System.out.println("Testing database connection...");
        // You can add code here to test your DB connection, queries, etc.
        com.campus.util.FileLogger.logInfo("Testing database connection...");
        try {
            Connection dbConnection = DBConnection.getConnection();
            if (dbConnection != null) {
                System.out.println("Database connection established successfully.");
                com.campus.util.FileLogger.logInfo("Database connection established successfully.");
            } else {
                System.out.println("Failed to establish database connection.");
                com.campus.util.FileLogger.logError("Failed to establish database connection.");
            }
 
            DBConnection.createTables(); // Ensure tables are created
 
        } catch (Exception e) {
            com.campus.util.FileLogger.logError("Failed to connect to database: " + e.getMessage());
        }
 
    }
}