package com.campus.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FraudDetector {

    public static boolean isFraudulent(int walletId) {

        String query = """
                SELECT transaction_time
                FROM transactions
                WHERE wallet_id = ?
                ORDER BY transaction_time DESC
                LIMIT 10
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, walletId);

            ResultSet rs = stmt.executeQuery();

            List<LocalDateTime> times = resultSetStream(rs)
                    .map(resultSet -> {
                        try {
                            Timestamp ts = resultSet.getTimestamp("transaction_time");
                            return ts.toLocalDateTime();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();

            if (times.size() < 10) {
                return false;
            }

            LocalDateTime newest = times.get(0);
            LocalDateTime oldest = times.get(9);

            return Duration.between(oldest, newest).toMinutes() <= 5;

        } catch (Exception e) {
            FileLogger.logError(
                    "Failed fraud detection: " + e.getMessage());
            return false;
        }
    }

    private static Stream<ResultSet> resultSetStream(ResultSet rs) {

        var iterator = new java.util.Iterator<ResultSet>() {

            private boolean hasNext = advance();

            private boolean advance() {
                try {
                    return rs.next();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public ResultSet next() {
                ResultSet current = rs;
                hasNext = advance();
                return current;
            }
        };

        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        iterator,
                        Spliterator.ORDERED),
                false
        );
    }
}