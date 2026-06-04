// Author: Ahana

package com.campus.service;

import com.campus.dao.SplitExpenseDAO;
import com.campus.dao.StudentDAO;
import com.campus.exception.InsufficientBalanceException;
import com.campus.exception.SplitExpenseException;
import com.campus.model.SplitExpense;
import com.campus.model.TxnType;
import com.campus.util.FileLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SplitExpenseService {

    private final SplitExpenseDAO splitExpenseDAO = new SplitExpenseDAO();
    private final WalletService walletService = new WalletService();
    private final StudentDAO studentDAO = new StudentDAO();

    // ── Equal split ───────────────────────────────────────────
    public String createSplit(int paidBy, List<Integer> members, double totalAmount) {
        FileLogger.logInfo("Creating equal split — paidBy: " + paidBy +
                           ", members: " + members + ", totalAmount: " + totalAmount);

        if (members == null || members.isEmpty()) {
            FileLogger.logWarn("createSplit failed — member list is empty");
            throw new SplitExpenseException("Member list cannot be empty");
        }
        if (totalAmount <= 0) {
            FileLogger.logWarn("createSplit failed — invalid totalAmount: " + totalAmount);
            throw new SplitExpenseException("Total amount must be positive");
        }
        if (members.contains(paidBy)) {
            FileLogger.logWarn("createSplit failed — paidBy " + paidBy + " is in members list");
            throw new SplitExpenseException("paidBy should not be in the members list — members are only those who owe");
        }
        if (!studentDAO.existsById(paidBy)) {
            FileLogger.logWarn("createSplit failed — payer not found: " + paidBy);
            throw new SplitExpenseException("Payer not found: " + paidBy);
        }

        validateMembers(members);
        FileLogger.logDebug("All member IDs validated successfully");

        String expenseId = UUID.randomUUID().toString();
        int totalPeople = members.size() + 1;
        double share = totalAmount / totalPeople;

        FileLogger.logDebug("expenseId: " + expenseId + ", totalPeople: " + totalPeople +
                            ", share per head: " + share);

        for (int memberId : members) {
            splitExpenseDAO.insert(new SplitExpense(
                0, expenseId, memberId, paidBy, share, false
            ));
            FileLogger.logDebug("Inserted due — expenseId: " + expenseId +
                                ", payerId: " + memberId + ", payeeId: " + paidBy +
                                ", amount: " + share);
        }

        FileLogger.logInfo("Equal split created successfully — expenseId: " + expenseId);
        return expenseId;
    }

    // ── Unequal split ─────────────────────────────────────────
    public String createUnequalSplit(int paidBy, List<Integer> members,
                                      double totalAmount, Map<Integer, Double> owedAmounts) {
        FileLogger.logInfo("Creating unequal split — paidBy: " + paidBy +
                           ", members: " + members + ", totalAmount: " + totalAmount);

        if (members == null || members.isEmpty()) {
            FileLogger.logWarn("createUnequalSplit failed — member list is empty");
            throw new SplitExpenseException("Member list cannot be empty");
        }
        if (totalAmount <= 0) {
            FileLogger.logWarn("createUnequalSplit failed — invalid totalAmount: " + totalAmount);
            throw new SplitExpenseException("Total amount must be positive");
        }
        if (members.contains(paidBy)) {
            FileLogger.logWarn("createUnequalSplit failed — paidBy " + paidBy + " is in members list");
            throw new SplitExpenseException("paidBy should not be in the members list");
        }
        if (!studentDAO.existsById(paidBy)) {
            FileLogger.logWarn("createUnequalSplit failed — payer not found: " + paidBy);
            throw new SplitExpenseException("Payer not found: " + paidBy);
        }

        validateMembers(members);
        FileLogger.logDebug("All member IDs validated successfully");

        for (int memberId : members) {
            if (!owedAmounts.containsKey(memberId)) {
                FileLogger.logWarn("createUnequalSplit failed — no amount assigned for memberId: " + memberId);
                throw new SplitExpenseException("No amount assigned for memberId: " + memberId);
            }
            if (owedAmounts.get(memberId) <= 0) {
                FileLogger.logWarn("createUnequalSplit failed — non-positive amount for memberId: " + memberId);
                throw new SplitExpenseException("Amount for memberId " + memberId + " must be positive");
            }
        }

        double memberSum = owedAmounts.values().stream().mapToDouble(Double::doubleValue).sum();
        if (memberSum > totalAmount + 0.001) {
            FileLogger.logWarn("createUnequalSplit failed — memberSum " + memberSum +
                               " > totalAmount " + totalAmount);
            throw new SplitExpenseException(
                "Sum of member shares (" + memberSum + ") cannot exceed totalAmount (" + totalAmount + ")");
        }

        String expenseId = UUID.randomUUID().toString();
        FileLogger.logDebug("expenseId: " + expenseId + ", memberSum: " + memberSum +
                            ", creator's remainder: " + (totalAmount - memberSum));

        for (int memberId : members) {
            splitExpenseDAO.insert(new SplitExpense(
                0, expenseId, memberId, paidBy, owedAmounts.get(memberId), false
            ));
            FileLogger.logDebug("Inserted due — expenseId: " + expenseId +
                                ", payerId: " + memberId + ", payeeId: " + paidBy +
                                ", amount: " + owedAmounts.get(memberId));
        }

        FileLogger.logInfo("Unequal split created successfully — expenseId: " + expenseId);
        return expenseId;
    }

    // ── Settle one specific due ───────────────────────────────
    public void settleUp(int dueId, int payerId) {
        FileLogger.logInfo("settleUp called — dueId: " + dueId + ", payerId: " + payerId);

        SplitExpense due = splitExpenseDAO.findById(dueId);

        if (due == null) {
            FileLogger.logError("settleUp failed — due not found for dueId: " + dueId);
            throw new SplitExpenseException("Due not found for dueId: " + dueId);
        }
        if (due.isStatus()) {
            FileLogger.logWarn("settleUp skipped — due already settled: " + dueId);
            throw new SplitExpenseException("Due already settled: " + dueId);
        }
        if (due.getPayerId() != payerId) {
            FileLogger.logWarn("settleUp failed — student " + payerId +
                               " is not the payer for dueId: " + dueId);
            throw new SplitExpenseException("Student " + payerId + " is not the payer for this due");
        }

        try {
            walletService.transfer(due.getPayerId(), due.getPayeeId(), due.getAmount(), TxnType.SPLIT_SETTLEMENT);
        } catch (InsufficientBalanceException e) {
            FileLogger.logError("settleUp failed — insufficient balance for dueId: " + dueId +
                                ", payerId: " + payerId + ", amount: " + due.getAmount());
            throw e;
        } catch (Exception e) {
            FileLogger.logError("settleUp failed — transfer error for dueId: " + dueId +
                                " | " + e.getMessage());
            throw e;
        }

        try {
            splitExpenseDAO.markSettled(dueId);
            FileLogger.logInfo("Due settled successfully — dueId: " + dueId +
                               ", payerId: " + payerId + ", amount: " + due.getAmount());
        } catch (Exception e) {
            FileLogger.logError("CRITICAL — transfer succeeded but markSettled failed for dueId: " +
                                dueId + " | " + e.getMessage());
            throw new SplitExpenseException(
                "Due settled in wallet but status update failed for dueId: " + dueId);
        }
    }

    // ── Settle all pending dues for a student ─────────────────
    public void settleAll(int payerId) {
        FileLogger.logInfo("settleAll called — payerId: " + payerId);

        List<SplitExpense> pending = splitExpenseDAO.findUnsettled(payerId);

        if (pending.isEmpty()) {
            FileLogger.logInfo("settleAll — no pending dues for payerId: " + payerId);
            throw new SplitExpenseException("No pending dues for student: " + payerId);
        }

        FileLogger.logDebug("settleAll — " + pending.size() + " pending dues found for payerId: " + payerId);

        List<Integer> failed = new ArrayList<>();
        int settledCount = 0;

        for (SplitExpense due : pending) {
            try {
                walletService.transfer(due.getPayerId(), due.getPayeeId(), due.getAmount(), TxnType.SPLIT_SETTLEMENT);
            } catch (InsufficientBalanceException e) {
                FileLogger.logError("settleAll stopped — insufficient balance at dueId: " +
                                    due.getDueId() + ", payerId: " + payerId);
                throw new SplitExpenseException(
                    "Insufficient balance to settle due: " + due.getDueId() +
                    ". Settled " + settledCount + " dues before this.");
            } catch (Exception e) {
                FileLogger.logError("settleAll — transfer failed for dueId: " + due.getDueId() +
                                    " | " + e.getMessage());
                failed.add(due.getDueId());
                continue;
            }

            try {
                splitExpenseDAO.markSettled(due.getDueId());
                FileLogger.logInfo("Settled due — dueId: " + due.getDueId() +
                                   ", amount: " + due.getAmount());
                settledCount++;
            } catch (Exception e) {
                FileLogger.logError("CRITICAL — transfer succeeded but markSettled failed for dueId: " +
                                    due.getDueId() + " | " + e.getMessage());
                throw new SplitExpenseException(
                    "Due settled in wallet but status update failed for dueId: " + due.getDueId() +
                    ". Settled " + settledCount + " dues before this.");
            }
        }

        if (!failed.isEmpty()) {
            FileLogger.logError("settleAll completed with failures — failed dueIds: " + failed);
            throw new SplitExpenseException("Failed to settle due IDs: " + failed);
        }

        FileLogger.logInfo("settleAll completed successfully for payerId: " + payerId);
    }

    // ── View all pending dues for a student ───────────────────
    public List<SplitExpense> listPending(int payerId) {
        FileLogger.logDebug("listPending called — payerId: " + payerId);
        List<SplitExpense> pending = splitExpenseDAO.findUnsettled(payerId);
        FileLogger.logInfo("listPending — found " + pending.size() + " dues for payerId: " + payerId);
        return pending;
    }

    // ── View all dues (settled + unsettled) for a student ─────
    public List<SplitExpense> listAll(int payerId) {
        FileLogger.logDebug("listAll called — payerId: " + payerId);
        List<SplitExpense> all = splitExpenseDAO.findByMember(payerId);
        FileLogger.logInfo("listAll — found " + all.size() + " total dues for payerId: " + payerId);
        return all;
    }

    // ── View all dues under one split ─────────────────────────
    public List<SplitExpense> listByExpense(String expenseId) {
        FileLogger.logDebug("listByExpense called — expenseId: " + expenseId);
        List<SplitExpense> dues = splitExpenseDAO.findByExpenseId(expenseId);
        if (dues.isEmpty()) {
            FileLogger.logWarn("listByExpense — no dues found for expenseId: " + expenseId);
            throw new SplitExpenseException("No dues found for expenseId: " + expenseId);
        }
        FileLogger.logInfo("listByExpense — found " + dues.size() + " dues for expenseId: " + expenseId);
        return dues;
    }

    // ── Validate member IDs exist in DB ───────────────────────
    private void validateMembers(List<Integer> members) {
        List<Integer> invalid = new ArrayList<>();
        for (int memberId : members) {
            if (!studentDAO.existsById(memberId))
                invalid.add(memberId);
        }
        if (!invalid.isEmpty()) {
            FileLogger.logWarn("validateMembers failed — invalid member IDs: " + invalid);
            throw new SplitExpenseException("Invalid member IDs — students not found: " + invalid);
        }
    }
}