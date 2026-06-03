package com.campus.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;



public class DBConnection {
    // Connection settings are loaded from the .env file (real environment
    // variables take precedence over .env values).
    private static final Map<String, String> ENV = loadEnv();

    private static final String URL = get("DB_URL");
    private static final String USER = get("DB_USER");
    private static final String PASSWORD = get("DB_PASSWORD");

    /** Reads KEY=VALUE pairs from the project's .env file. */
    private static Map<String, String> loadEnv() {
        Map<String, String> env = new HashMap<>();
        Path path = Paths.get(".env");
        if (!Files.exists(path)) {
            return env;
        }
        try {
            for (String line : Files.readAllLines(path)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq < 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                // Strip a single pair of surrounding quotes, if present.
                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                            || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                env.put(key, value);
            }
        } catch (IOException e) {
            System.err.println("Warning: could not read .env file: " + e.getMessage());
        }
        return env;
    }

    /** Returns the value for {@code key}, preferring a real env var over .env. */
    private static String get(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = ENV.get(key);
        }
        return value;
    }

    public static void createTables() {
        String createStudentsTable =
                "CREATE TABLE IF NOT EXISTS students (" +
                "student_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "email VARCHAR(100) UNIQUE," +
                "phone VARCHAR(20) UNIQUE," +
                "wallet_id INT UNIQUE" +
                ")";

        String createWalletsTable =
                "CREATE TABLE IF NOT EXISTS wallets (" +
                "wallet_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                "student_id INT NOT NULL UNIQUE," +
                "balance DOUBLE PRECISION NOT NULL," +
                "daily_transfer_limit DOUBLE PRECISION NOT NULL," +
                "balance_cap DOUBLE PRECISION NOT NULL," +
                "today_transferred DOUBLE PRECISION NOT NULL," +
                "FOREIGN KEY (student_id) REFERENCES students(student_id)" +
                ")";

        String transactionTable =
                "CREATE TABLE IF NOT EXISTS transactions (" +
                "transaction_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                "from_wallet_id INT," +
                "to_wallet_id INT," +
                "amount DOUBLE PRECISION NOT NULL," +
                "type VARCHAR(50) NOT NULL," +
                "transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "status VARCHAR(20) NOT NULL," +
                "FOREIGN KEY (from_wallet_id) REFERENCES wallets(wallet_id)," +
                "FOREIGN KEY (to_wallet_id) REFERENCES wallets(wallet_id)" +
                ")";

        String duesTable =
                "CREATE TABLE IF NOT EXISTS dues (" +
                "due_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                "expense_id VARCHAR(30) NOT NULL," +
                "payer_id INT NOT NULL," +
                "payee_id INT NOT NULL," +
                "amount DOUBLE PRECISION NOT NULL," +
                "status BOOLEAN NOT NULL DEFAULT FALSE," +
                "FOREIGN KEY (payer_id) REFERENCES students(student_id)," +
                "FOREIGN KEY (payee_id) REFERENCES students(student_id)" +
                ")";
        String createDB = "CREATE DATABASE IF NOT EXISTS campus_db";

        //INSERT INTO transactions " + "(txn_id, sender_id, receiver_id, amount, type, timestamp, status)

        try (Connection conn = getConnection()) {
            // conn.createStatement().execute(createDB);
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
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC driver is not available on the classpath", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);

    }
}