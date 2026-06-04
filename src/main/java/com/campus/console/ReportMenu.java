package com.campus.console;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.campus.model.Transaction;
import com.campus.model.TxnType;
import com.campus.service.ReportService;

public class ReportMenu {

    private final Scanner scanner;
    private final ReportService reportService;
    private final int studentId;   // the logged-in user — reports are scoped to them

    public ReportMenu(Scanner scanner, int studentId) {
        this.scanner = scanner;
        this.reportService = new ReportService();
        this.studentId = studentId;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("===== My Reports & Transaction History =====");
            System.out.println("1. My transaction history");
            System.out.println("2. My total spend");
            System.out.println("3. My transactions by type");
            System.out.println("4. My spend by campus-payment category");
            System.out.println("5. My top spends");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> printTransactions(reportService.historyForStudent(studentId));
                case "2" -> showMySpend();
                case "3" -> showByType();
                case "4" -> showByCategory();
                case "5" -> printTransactions(reportService.topSpendsForStudent(studentId));
                case "0" -> back = true;
                default  -> System.out.println("Invalid option, try again.");
            }
        }
    }

    private void showMySpend() {
        System.out.printf("You have spent a total of %.2f%n", reportService.totalSpendByStudent(studentId));
    }

    private void showByType() {
        System.out.print("Enter type (TRANSFER/DEPOSIT/WITHDRAW/CAMPUS_PAYMENT/SPLIT_SETTLEMENT): ");
        String input = scanner.nextLine().trim().toUpperCase();
        try {
            TxnType type = TxnType.valueOf(input);
            List<Transaction> mine = reportService.historyForStudent(studentId).stream()
                    .filter(t -> t.getType() == type)
                    .collect(Collectors.toList());
            printTransactions(mine);
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown type: " + input);
        }
    }

    private void showByCategory() {
        Map<String, Double> byCategory = reportService.spendByCategory(studentId);
        if (byCategory.isEmpty()) {
            System.out.println("No campus payments yet.");
            return;
        }
        System.out.println("\nMy spend by campus-payment category:");
        byCategory.forEach((category, total) -> System.out.printf("  %-12s %.2f%n", category, total));
    }

    private void printTransactions(List<Transaction> txns) {
        if (txns.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }
        System.out.println("\nTxnId      | Type            | From -> To    | Amount   | Status  | Time");
        System.out.println("-------------------------------------------------------------------------------");
        for (Transaction t : txns) {
            String shortId = t.getTxnId().length() > 8 ? t.getTxnId().substring(0, 8) : t.getTxnId();
            System.out.printf("%-10s | %-15s | %4d -> %-5d | %8.2f | %-7s | %s%n",
                    shortId, t.getType(), t.getSenderId(), t.getReceiverId(),
                    t.getAmount(), t.getStatus(), t.getTimestamp());
        }
        System.out.println(txns.size() + " transaction(s).");
    }
}
