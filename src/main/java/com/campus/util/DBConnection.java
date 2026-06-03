package com.campus.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/campus_db";
    private static final String USER = "root";
    private static final String PASSWORD = "George@0104";

    public static void createTables() {
        String createStudentsTable = "CREATE TABLE IF NOT EXISTS students (" +
                "student_id INT PRIMARY KEY AUTO_INCREMENT," +
                "name VARCHAR(100) NOT NULL," +
                "email VARCHAR(100) UNIQUE," +
                "phone VARCHAR(20) UNIQUE," +
                "WalletId INT UNIQUE," +
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

        try (Connection conn = getConnection()) {
            conn.createStatement().execute(createStudentsTable);
            conn.createStatement().execute(createWalletsTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);

    }
}