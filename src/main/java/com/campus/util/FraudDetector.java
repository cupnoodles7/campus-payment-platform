package com.campus.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.campus.exception.SuspiciousActivityException;
import com.campus.model.TxnType;

public class FraudDetector {

    // A student making more than this many outgoing transactions inside the window is suspicious.
    private static final int MAX_OUTGOING = 10;
    private static final int WINDOW_MINUTES = 5;
    
    /**
     * Blocks an outgoing transaction when the student has already reached the
     * limit ({@value #MAX_OUTGOING} outgoing transactions within the last
     * {@value #WINDOW_MINUTES} minutes). Call this BEFORE performing the
     * transaction so no money moves when the limit is exceeded.
     *
     * @throws SuspiciousActivityException if the student is over the limit
     */
    public static void enforceLimit(int studentId) {
        if (isSuspicious(studentId)) {
            throw new SuspiciousActivityException(
                "Too many transactions: limit of " + MAX_OUTGOING
                + " outgoing transactions within " + WINDOW_MINUTES
                + " minutes reached. Please try again later.");
        }
    }

    public static boolean isSuspicious(int studentId) {

        String query = """
                SELECT COUNT(*)
                FROM transactions
                WHERE sender_id = ?
                  AND type <> ?
                  AND timestamp >= ?
                """;

        Timestamp windowStart =
                Timestamp.valueOf(LocalDateTime.now().minusMinutes(WINDOW_MINUTES));

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, studentId);
            stmt.setString(2, TxnType.DEPOSIT.name());
            stmt.setTimestamp(3, windowStart);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > MAX_OUTGOING;
                }
                return false;
            }

        } catch (Exception e) {
            FileLogger.logError("Failed fraud detection: " + e.getMessage());
            return false;
        }
    }
}
