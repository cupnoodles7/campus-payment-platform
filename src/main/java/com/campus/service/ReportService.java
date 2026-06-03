package com.campus.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.campus.dao.TransactionDAO;
import com.campus.model.Transaction;
import com.campus.model.TxnType;

public class ReportService {

    private final TransactionDAO txnDAO = new TransactionDAO();

    //"Spend" = money leaving the wallet. Of the five types only DEPOSIT is money coming IN, so everything except a deposit counts as spending.
    private boolean isSpend(Transaction t) {
        return t.getType() != TxnType.DEPOSIT;
    }

    //Full history (newest first)
    public List<Transaction> allTransactions() {
        return txnDAO.findAll();
    }

    //Every transaction of one type
    public List<Transaction> filterByType(TxnType type) {
        return txnDAO.findAll().stream()
                .filter(t -> t.getType() == type)
                .collect(Collectors.toList());
    }

    //History for one student (as sender or receiver)
    public List<Transaction> historyForStudent(int studentId) {
        return txnDAO.findByStudentId(studentId);
    }

    //Total amount SPENT by each student
    public Map<Integer, Double> totalSpendByStudent() {
        return txnDAO.findAll().stream()
                .filter(this::isSpend)
                .collect(Collectors.groupingBy(
                        Transaction::getSenderId,
                        Collectors.summingDouble(Transaction::getAmount)));
    }

    //Total amount SPENT by a single student
    public double totalSpendByStudent(int studentId) {
        return txnDAO.findAll().stream()
                .filter(this::isSpend)
                .filter(t -> t.getSenderId() == studentId)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    //Top N spenders
    public List<Map.Entry<Integer, Double>> topSpenders(int n) {
        return totalSpendByStudent().entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    //Total transaction amount per month
    public Map<String, Double> monthlySummary() {
        DateTimeFormatter ym = DateTimeFormatter.ofPattern("yyyy-MM");
        return txnDAO.findAll().stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTimestamp().format(ym),
                        TreeMap::new,
                        Collectors.summingDouble(Transaction::getAmount)));
    }

    //How many transactions of each type exist
    public Map<TxnType, Long> countByType() {
        return txnDAO.findAll().stream()
                .collect(Collectors.groupingBy(Transaction::getType, Collectors.counting()));
    }
}
