// Author: Ahana

package com.campus.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

import com.campus.dao.TransactionDAO;
import com.campus.dao.WalletDAO;
import com.campus.exception.BalanceCapExceededException;
import com.campus.exception.DailyTransferLimitException;
import com.campus.exception.DatabaseException;
import com.campus.exception.InsufficientBalanceException;
import com.campus.exception.InvalidAmountException;
import com.campus.exception.WalletNotFoundException;
import com.campus.interfaces.TransferHandler;
import com.campus.model.Transaction;
import com.campus.model.TxnType;
import com.campus.model.Wallet;
import com.campus.util.DBConnection;
import com.campus.util.FileLogger;
import com.campus.util.FraudDetector;

public class WalletService implements TransferHandler {

    private final WalletDAO walletDAO = new WalletDAO();
    private final TransactionDAO txnDAO = new TransactionDAO();

    // TransferHandler override 
    @Override
    public void handle(int from, int to, double amt) {
        transfer(from, to, amt);
    }

    // Transfer (defaults to a plain peer-to-peer TRANSFER)
    public void transfer(int senderId, int receiverId, double amt) {
        transfer(senderId, receiverId, amt, TxnType.TRANSFER);
    }

    // Transfer with an explicit type — lets callers (e.g. split settlement) label the transaction
    public void transfer(int senderId, int receiverId, double amt, TxnType type) {
        validateAmount(amt);
        FraudDetector.enforceLimit(senderId);
        checkDailyLimit(senderId, amt);

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Wallet senderWallet = walletDAO.getByStudentId(senderId);
            if (senderWallet == null) throw new WalletNotFoundException("Wallet not found: " + senderId);

            Wallet receiverWallet = walletDAO.getByStudentId(receiverId);
            if (receiverWallet == null) throw new WalletNotFoundException("Wallet not found: " + receiverId);

            if (senderWallet.getBalance() < amt) throw new InsufficientBalanceException("Insufficient balance");

            walletDAO.atomicTransfer(senderId, receiverId, amt, conn);

            FileLogger.logInfo("Transferred " + amt + " from " + senderId + " to " + receiverId);

            txnDAO.insert(new Transaction(
                UUID.randomUUID().toString(),
                senderId, receiverId, amt,
                type, LocalDateTime.now(), "SUCCESS"
            ), conn);

            conn.commit();

            if (FraudDetector.isSuspicious(senderId)) {
                String alert = "SUSPICIOUS: studentId=" + senderId
                    + " made more than 5 outgoing transactions within 5 minutes";
                FileLogger.logError(alert);
                System.out.println("⚠  " + alert);
            }

        } catch (Exception e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ignored) {} }
            if (e instanceof InsufficientBalanceException) throw (InsufficientBalanceException) e;
            if (e instanceof DailyTransferLimitException)  throw (DailyTransferLimitException) e;
            if (e instanceof WalletNotFoundException)      throw (WalletNotFoundException) e;
            throw new DatabaseException("Transfer failed", e);
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {} }
        }
    }

    // Deposit
    public void deposit(int studentId, double amt) {
        validateAmount(amt);

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Wallet wallet = walletDAO.getByStudentId(studentId);
            if (wallet == null) throw new WalletNotFoundException("Wallet not found: " + studentId);
            if (wallet.getBalance() + amt > wallet.getBalanceCap())
                throw new BalanceCapExceededException("Deposit exceeds balance cap");

            walletDAO.updateBalance(studentId, wallet.getBalance() + amt, conn);

            txnDAO.insert(new Transaction(
                UUID.randomUUID().toString(),
                studentId, studentId, amt,
                TxnType.DEPOSIT, LocalDateTime.now(), "SUCCESS"
            ), conn);

            conn.commit();

        } catch (Exception e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ignored) {} }
            if (e instanceof BalanceCapExceededException) throw (BalanceCapExceededException) e;
            if (e instanceof WalletNotFoundException)     throw (WalletNotFoundException) e;
            throw new DatabaseException("Deposit failed", e);
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {} }
        }
    }

    // Withdraw 
    public void withdraw(int studentId, double amt) {
        validateAmount(amt);
        FraudDetector.enforceLimit(studentId);

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Wallet wallet = walletDAO.getByStudentId(studentId);
            if (wallet == null) throw new WalletNotFoundException("Wallet not found: " + studentId);
            if (wallet.getBalance() < amt) throw new InsufficientBalanceException("Insufficient balance");

            walletDAO.updateBalance(studentId, wallet.getBalance() - amt, conn);

            txnDAO.insert(new Transaction(
                UUID.randomUUID().toString(),
                studentId, studentId, amt,
                TxnType.WITHDRAW, LocalDateTime.now(), "SUCCESS"
            ), conn);
            FileLogger.logInfo("Withdrew " + amt + " from studentId=" + studentId);
            conn.commit();

            if (FraudDetector.isSuspicious(studentId)) {
                String alert = "SUSPICIOUS: studentId=" + studentId
                    + " made more than 5 outgoing transactions within 5 minutes";
                FileLogger.logError(alert);
                System.out.println("⚠  " + alert);
            }

        } catch (Exception e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ignored) {} }
            if (e instanceof InsufficientBalanceException) throw (InsufficientBalanceException) e;
            if (e instanceof WalletNotFoundException)      throw (WalletNotFoundException) e;
            throw new DatabaseException("Withdraw failed", e);
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {} }
        }
    }

    // Helpers
    public double getBalance(int studentId) {
        Wallet wallet = walletDAO.getByStudentId(studentId);
        if (wallet == null) throw new WalletNotFoundException("Wallet not found: " + studentId);
        return wallet.getBalance();
    }

    public void checkDailyLimit(int studentId, double amt) {
        Wallet wallet = walletDAO.getByStudentId(studentId);
        // sender existence is validated in transfer(); skip here if no wallet yet
        if (wallet == null) return;

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        double transferredToday = txnDAO.findBetweenDates(startOfDay, LocalDateTime.now())
            .stream()
            .filter(t -> t.getSenderId() == studentId
                         && (t.getType() == TxnType.TRANSFER || t.getType() == TxnType.SPLIT_SETTLEMENT))
            .mapToDouble(Transaction::getAmount)
            .sum();

        if (transferredToday + amt > wallet.getDailyTransferLimit()) {
            FileLogger.logInfo("Daily transfer limit reached for studentId=" + studentId);
            throw new DailyTransferLimitException(
                "Daily transfer limit of " + wallet.getDailyTransferLimit()
                + " exceeded (already transferred " + transferredToday + " today)");
        }
    }

    private void validateAmount(double amt) {
        if (amt <= 0 || Double.isNaN(amt)) throw new InvalidAmountException("Amount must be positive");
    }
}