package com.campus.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.campus.dao.TransactionDAO;
import com.campus.model.Transaction;
import com.campus.model.TxnType;

public class ReportService {

    private final TransactionDAO txnDAO = new TransactionDAO();

    private boolean isSpend(Transaction t) {
        return t.getType() != TxnType.DEPOSIT;
    }

    /** Full history for one student (as sender or receiver), newest first. */
    public List<Transaction> historyForStudent(int studentId) {
        return txnDAO.findByStudentId(studentId);
    }

    /** Total amount this student has spent. */
    public double totalSpendByStudent(int studentId) {
        return txnDAO.findByStudentId(studentId).stream()
                .filter(this::isSpend)
                .filter(t -> t.getSenderId() == studentId)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /** This student's campus-payment spend per category (CANTEEN, LIBRARY, ...). */
    public Map<String, Double> spendByCategory(int studentId) {
        return txnDAO.findByStudentId(studentId).stream()
                .filter(t -> t.getType() == TxnType.CAMPUS_PAYMENT)
                .filter(t -> t.getCategory() != null)
                .filter(t -> t.getSenderId() == studentId)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)));
    }

    /** This student's own spends, largest amount first. */
    public List<Transaction> topSpendsForStudent(int studentId) {
        return txnDAO.findByStudentId(studentId).stream()
                .filter(this::isSpend)
                .filter(t -> t.getSenderId() == studentId)
                .sorted(Comparator.comparingDouble(Transaction::getAmount).reversed())
                .collect(Collectors.toList());
    }
}
