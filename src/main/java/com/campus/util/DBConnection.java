package com.campus.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/campus_db";
    // public static final String url = "jdbc:mysql://localhost:3306/BankDB";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static void createTables() {
        String createStudentsTable = "CREATE TABLE IF NOT EXISTS students (" +
                "student_id INT PRIMARY KEY AUTO_INCREMENT," +
                "name VARCHAR(100) NOT NULL," +
                "email VARCHAR(100) UNIQUE," +
                "phone VARCHAR(20) UNIQUE," +
                "WalletId INT UNIQUE" +
                ")";

        String createWalletsTable = "CREATE TABLE IF NOT EXISTS wallets (" +
                "wallet_id INT PRIMARY KEY AUTO_INCREMENT," +
                "student_id INT NOT NULL," +
                "balance DOUBLE NOT NULL," +
                "daily_transfer_limit DOUBLE NOT NULL," +
                "balance_cap DOUBLE NOT NULL," +
                "today_transferred DOUBLE NOT NULL," +
                "FOREIGN KEY (student_id) REFERENCES students(student_id)" +
                ")";
        
        String transactionTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                "transaction_id INT PRIMARY KEY AUTO_INCREMENT," +
                "from_wallet_id INT," +
                "to_wallet_id INT," +
                "amount DOUBLE NOT NULL," +
                "type VARCHAR(50) NOT NULL," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "status VARCHAR(20) NOT NULL," +
                "FOREIGN KEY (from_wallet_id) REFERENCES wallets(wallet_id)," +
                "FOREIGN KEY (to_wallet_id) REFERENCES wallets(wallet_id)" +
                ")";
        
        String duesTable = "CREATE TABLE IF NOT EXISTS dues (" +
                "due_id INT PRIMARY KEY AUTO_INCREMENT," +
                "expense_id INT NOT NULL," +
                "payer_id INT NOT NULL," +
                "payee_id INT NOT NULL," +
                "amount DOUBLE NOT NULL," +
                "status VARCHAR(20) NOT NULL DEFAULT 'FALSE'," +
                "FOREIGN KEY (payer_id) REFERENCES students(student_id)" +
                ")";
        String createDB = "CREATE DATABASE IF NOT EXISTS campus_db";

        //INSERT INTO transactions " + "(txn_id, sender_id, receiver_id, amount, type, timestamp, status)

        try (Connection conn = getConnection()) {
            conn.createStatement().execute(createDB);
            conn.createStatement().execute(createStudentsTable);
            conn.createStatement().execute(createWalletsTable);
            conn.createStatement().execute(transactionTable);
            conn.createStatement().execute(duesTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver is not available on the classpath", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);

    }
}