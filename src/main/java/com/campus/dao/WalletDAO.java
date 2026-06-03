package com.campus.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.campus.exception.DatabaseException;
import com.campus.model.Wallet;
import com.campus.util.DBConnection;
import com.campus.util.FileLogger;

public class WalletDAO {

    //Create a wallet row for a newly registered student
    public void insert(Wallet w) {
        String sql = "INSERT INTO wallets "
                + "(student_id, balance, daily_transfer_limit, balance_cap, today_transferred) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, w.getStudentId());
            ps.setDouble(2, w.getBalance());
            ps.setDouble(3, w.getDailyTransferLimit());
            ps.setDouble(4, w.getBalanceCap());
            ps.setDouble(5, w.getTodayTransferred());
            ps.executeUpdate();
        } catch (SQLException e) {
            FileLogger.logError("WalletDAO.insert failed for student " + w.getStudentId() + ": " + e.getMessage());
            throw new DatabaseException("Failed to insert wallet for student " + w.getStudentId(), e);
        }
    }

    //Load a wallet by its owning student. Returns null if none — the service decides whether that becomes a WalletNotFoundException
    public Wallet getByStudentId(int studentId) {
        String sql = "SELECT * FROM wallets WHERE student_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            FileLogger.logError("WalletDAO.getByStudentId failed for student " + studentId + ": " + e.getMessage());
            throw new DatabaseException("Failed to load wallet for student " + studentId, e);
        }
    }
    //Set an absolute new balance. Used inside a service transaction.
    public void updateBalance(int studentId, double newBalance, Connection c) {
        String sql = "UPDATE wallets SET balance = ? WHERE student_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {   // note: c is NOT closed here
            ps.setDouble(1, newBalance);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            FileLogger.logError("WalletDAO.updateBalance failed for student " + studentId + ": " + e.getMessage());
            throw new DatabaseException("Failed to update balance for student " + studentId, e);
        }
    }

    //Set the running daily-transferred total. Used inside a service transaction
    public void updateTodayTransferred(int studentId, double newTodayTransferred, Connection c) {
        String sql = "UPDATE wallets SET today_transferred = ? WHERE student_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, newTodayTransferred);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            FileLogger.logError("WalletDAO.updateTodayTransferred failed for student " + studentId + ": " + e.getMessage());
            throw new DatabaseException("Failed to update todayTransferred for student " + studentId, e);
        }
    }

    //Move money between two wallets in arithmetic SQL on a single connection
    public void atomicTransfer(int senderId, int receiverId, double amount, Connection c) {
        String debit = "UPDATE wallets "
                + "SET balance = balance - ?, today_transferred = today_transferred + ? "
                + "WHERE student_id = ?";
        String credit = "UPDATE wallets SET balance = balance + ? WHERE student_id = ?";
        try (PreparedStatement ps1 = c.prepareStatement(debit);
             PreparedStatement ps2 = c.prepareStatement(credit)) {
            ps1.setDouble(1, amount);
            ps1.setDouble(2, amount);
            ps1.setInt(3, senderId);
            ps1.executeUpdate();

            ps2.setDouble(1, amount);
            ps2.setInt(2, receiverId);
            ps2.executeUpdate();

            FileLogger.logInfo("WalletDAO.atomicTransfer applied: " + senderId + " -> " + receiverId + " amount=" + amount);
        } catch (SQLException e) {
            FileLogger.logError("WalletDAO.atomicTransfer failed " + senderId + " -> " + receiverId
                    + " amount=" + amount + ": " + e.getMessage());
            throw new DatabaseException(
                    "Failed atomicTransfer " + senderId + " -> " + receiverId, e);
        }
    }

   //helper

    private Wallet mapRow(ResultSet rs) throws SQLException {
        return new Wallet(
                rs.getInt("wallet_id"),
                rs.getInt("student_id"),
                rs.getDouble("balance"),
                rs.getDouble("daily_transfer_limit"),
                rs.getDouble("balance_cap"),
                rs.getDouble("today_transferred"));
    }
}
