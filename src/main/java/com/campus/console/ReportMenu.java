package com.campus.console;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.campus.model.Transaction;
import com.campus.model.TxnType;
import com.campus.service.ReportService;

public class ReportMenu {

    private final Scanner scanner;
    private final ReportService reportService;

    public ReportMenu(Scanner scanner) {
        this.scanner = scanner;
        this.reportService = new ReportService();
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("===== Reports & Transaction History =====");
            System.out.println("1. View all transaction history");
            System.out.println("2. Top spenders");
            System.out.println("3. Total spend by a student");
            System.out.println("4. Monthly summary");
            System.out.println("5. Filter transactions by type");
            System.out.println("6. Transaction count by type");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> printTransactions(reportService.allTransactions());
                case "2" -> showTopSpenders();
                case "3" -> showSpendByStudent();
                case "4" -> showMonthly();
                case "5" -> showByType();
                case "6" -> showCountByType();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option, try again.");
            }
        }
    }

    private void showTopSpenders() {
        int n = readInt("How many top spenders? ");
        List<Map.Entry<Integer, Double>> top = reportService.topSpenders(n);
        if (top.isEmpty()) {
            System.out.println("No spending recorded yet.");
            return;
        }
        System.out.println("\nRank | Student | Total Spent");
        int rank = 1;
        for (Map.Entry<Integer, Double> e : top) {
            System.out.printf("%-4d | %-7d | %.2f%n", rank++, e.getKey(), e.getValue());
        }
    }

    private void showSpendByStudent() {
        int id = readInt("Enter student ID: ");
        System.out.printf("Student %d has spent a total of %.2f%n", id, reportService.totalSpendByStudent(id));
    }

    private void showMonthly() {
        Map<String, Double> monthly = reportService.monthlySummary();
        if (monthly.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        System.out.println("\nMonth    | Total Amount");
        monthly.forEach((m, total) -> System.out.printf("%-8s | %.2f%n", m, total));
    }

    private void showByType() {
        System.out.print("Enter type (TRANSFER/DEPOSIT/WITHDRAW/CAMPUS_PAYMENT/SPLIT_SETTLEMENT): ");
        String input = scanner.nextLine().trim().toUpperCase();
        try {
            printTransactions(reportService.filterByType(TxnType.valueOf(input)));
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown type: " + input);
        }
    }

    private void showCountByType() {
        Map<TxnType, Long> counts = reportService.countByType();
        if (counts.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        counts.forEach((type, count) -> System.out.printf("%-16s : %d%n", type, count));
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

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }
}
