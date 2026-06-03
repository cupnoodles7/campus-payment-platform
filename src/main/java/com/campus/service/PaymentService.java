//Campus Payment

package com.campus.service;

import com.campus.dao.TransactionDAO;
import com.campus.dao.WalletDAO;
import com.campus.exception.*;
import com.campus.interfaces.PaymentProcessor;
import com.campus.model.Transaction;
import com.campus.model.TxnType;
import com.campus.model.Wallet;
import com.campus.util.DBConnection;
import com.campus.util.FileLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentService {

    private final WalletDAO walletDAO = new WalletDAO();
    private final TransactionDAO txnDAO = new TransactionDAO();

    public void pay(int studentId, String paymentType, double amount) {

        // map string to functional interface (PaymentProcessor)
        PaymentProcessor processor = switch (paymentType) {
            case "CANTEEN"   -> (id, amt) -> processPayment(id, amt, TxnType.CAMPUS_PAYMENT, "CANTEEN");
            case "LIBRARY"   -> (id, amt) -> processPayment(id, amt, TxnType.CAMPUS_PAYMENT, "LIBRARY");
            case "HACKATHON" -> (id, amt) -> processPayment(id, amt, TxnType.CAMPUS_PAYMENT, "HACKATHON");
            case "WORKSHOP"  -> (id, amt) -> processPayment(id, amt, TxnType.CAMPUS_PAYMENT, "WORKSHOP");
            case "HOSTEL"    -> (id, amt) -> processPayment(id, amt, TxnType.CAMPUS_PAYMENT, "HOSTEL");
            default -> throw new PaymentFailedException("Unknown payment type: " + paymentType);
        };


        try {
            processor.campus(studentId, amount);
        } catch (PaymentFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new PaymentFailedException("Payment failed: " + e.getMessage());
        }
    }

    private void processPayment(int studentId, double amount,
                                TxnType type, String label) {
        if (amount <= 0)
            throw new InvalidAmountException("Amount must be positive.");

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Wallet wallet = getWalletInTxn(studentId, conn);

            if (wallet.getBalance() < amount)
                throw new InsufficientBalanceException(
                    "Insufficient balance. Available: ₹" + wallet.getBalance());

            walletDAO.updateBalance(studentId, wallet.getBalance() - amount, conn);

            Transaction txn = new Transaction(
                UUID.randomUUID().toString(),
                studentId, 0, amount,
                type, LocalDateTime.now(), "SUCCESS"
            );
            txnDAO.insert(txn, conn);

            conn.commit();
            FileLogger.logInfo(label + " payment SUCCESS: student=" + studentId + " ₹" + amount);

        } catch (InsufficientBalanceException | InvalidAmountException e) {
            rollback(conn);
            FileLogger.logWarn(label + " payment rejected: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            rollback(conn);
            FileLogger.logError(label + " payment FAILED: " + e.getMessage());
            throw new PaymentFailedException(label + " payment failed: " + e.getMessage());
        } finally {
            resetAutoCommit(conn);
        }
    }

    private Wallet getWalletInTxn(int studentId, Connection conn) {
        String sql = "SELECT * FROM wallets WHERE student_id = ? FOR UPDATE";
        try (var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            var rs = ps.executeQuery();
            if (rs.next()) {
                Wallet w = new Wallet();
                w.setWalletId(rs.getInt("wallet_id"));
                w.setStudentId(rs.getInt("student_id"));
                w.setBalance(rs.getDouble("balance"));
                w.setDailyTransferLimit(rs.getDouble("daily_transfer_limit"));
                w.setBalanceCap(rs.getDouble("balance_cap"));
                w.setTodayTransferred(rs.getDouble("today_transferred"));
                return w;
            }
            throw new WalletNotFoundException("Wallet not found for student: " + studentId);
        } catch (SQLException e) {
            throw new DatabaseException("getWalletInTxn failed: " + e.getMessage(), e);
        }
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); }
            catch (SQLException ex) {
                FileLogger.logError("Rollback failed: " + ex.getMessage());
            }
        }
    }

    private void resetAutoCommit(Connection conn) {
        if (conn != null) {
            try { conn.setAutoCommit(true); }
            catch (SQLException ex) {
                FileLogger.logError("resetAutoCommit failed: " + ex.getMessage());
            }
        }
    }
}