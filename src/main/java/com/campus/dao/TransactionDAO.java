package com.campus.dao;

import com.campus.exception.DatabaseException;
import com.campus.model.Transaction;
import com.campus.model.TxnType;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.campus.util.FileLogger;

public class TransactionDAO {

    public void insert(Transaction txn, Connection conn) {
        String sql = "INSERT INTO transactions " + "(txn_id, sender_id, receiver_id, amount, type, timestamp, status) " + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txn.getTxnId());
            ps.setInt   (2, txn.getSenderId());
            ps.setInt   (3, txn.getReceiverId());
            ps.setDouble(4, txn.getAmount());
            ps.setString(5, txn.getType().name());
            ps.setTimestamp(6, Timestamp.valueOf(txn.getTimestamp()));
            ps.setString(7, txn.getStatus());
            ps.executeUpdate();
            FileLogger.logInfo("Inserted transaction: " + txn);
        } catch (SQLException e) {
            FileLogger.logError("Failed to insert transaction: " + e.getMessage());
            throw new DatabaseException("Failed to insert transaction: " + e.getMessage(), e);
        }
    }

    public List<Transaction> findByStudentId(int studentId) {
        String sql = "SELECT * FROM transactions " + "WHERE sender_id = ? OR receiver_id = ? " + "ORDER BY timestamp DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = com.campus.util.DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, studentId);
            ResultSet rs = ps.executeQuery();
            FileLogger.logInfo("Finding transactions for studentId=" + studentId);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            FileLogger.logError("findByStudentId failed for id=" + studentId + ": " + e.getMessage());
            throw new DatabaseException("findByStudentId failed: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Transaction> findByType(TxnType type) {
        String sql = "SELECT * FROM transactions WHERE type = ? ORDER BY timestamp DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = com.campus.util.DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            FileLogger.logInfo("Finding transactions for type=" + type);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            FileLogger.logError("findByType failed for type=" + type + ": " + e.getMessage());
            throw new DatabaseException("findByType failed: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Transaction> findBetweenDates(LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT * FROM transactions WHERE timestamp BETWEEN ? AND ? " +
                     "ORDER BY timestamp DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = com.campus.util.DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from));
            ps.setTimestamp(2, Timestamp.valueOf(to));
            ResultSet rs = ps.executeQuery();
            FileLogger.logInfo("Finding transactions between dates: " + from + " to " + to);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            FileLogger.logError("findBetweenDates failed for from=" + from + ", to=" + to + ": " + e.getMessage());
            throw new DatabaseException("findBetweenDates failed: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Transaction> findAll() {
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = com.campus.util.DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            FileLogger.logInfo("Finding all transactions.");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            FileLogger.logError("findAll failed: " + e.getMessage());
            throw new DatabaseException("findAll failed: " + e.getMessage(), e);
        }
        return list;
    }

    // ── helper ──────────────────────────────────────────────────────────────
    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
            rs.getString("txn_id"),
            rs.getInt("sender_id"),
            rs.getInt("receiver_id"),
            rs.getDouble("amount"),
            TxnType.valueOf(rs.getString("type")),
            rs.getTimestamp("timestamp").toLocalDateTime(),
            rs.getString("status")
        );
    }
}