// Author: Ahana

package com.campus.console;

import com.campus.exception.DailyTransferLimitException;
import com.campus.exception.DatabaseException;
import com.campus.exception.InputCancelledException;
import com.campus.exception.InsufficientBalanceException;
import com.campus.exception.InvalidAmountException;
import com.campus.exception.SplitExpenseException;
import com.campus.exception.WalletNotFoundException;
import com.campus.model.SplitExpense;
import com.campus.service.SplitExpenseService;
import com.campus.util.InputValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SplitMenu {

    private final Scanner scanner;
    private final SplitExpenseService splitExpenseService;
    private final int studentId;   // the logged-in user (payer)

    public SplitMenu(Scanner scanner, int studentId) {
        this.scanner = scanner;
        this.splitExpenseService = new SplitExpenseService();
        this.studentId = studentId;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("===== Split Expense Menu =====");
            System.out.println("1. Create equal split");
            System.out.println("2. Create unequal split");
            System.out.println("3. Settle a due");
            System.out.println("4. Settle all dues");
            System.out.println("5. View pending dues");
            System.out.println("6. View all dues");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> handleEqualSplit();
                    case "2" -> handleUnequalSplit();
                    case "3" -> handleSettleUp();
                    case "4" -> handleSettleAll();
                    case "5" -> handleListPending();
                    case "6" -> handleListAll();
                    case "0" -> back = true;
                    default  -> System.out.println("Invalid option, try again.");
                }
            } catch (InputCancelledException e) {
                System.out.println("Cancelled — back to the split menu.");
            }
        }
    }

    // ── Equal split ───────────────────────────────────────────
    private void handleEqualSplit() {
        int paidBy = studentId;

        // Phase A: collect valid member IDs (validated as each is entered)
        int memberCount = InputValidator.readInt(scanner, "Enter number of people who owe (excluding yourself): ");
        List<Integer> members = readMemberIds(memberCount, paidBy);

        // Phase B: only the amount can fail now — re-prompt just the amount on error
        while (true) {
            try {
                double totalAmount = InputValidator.readDouble(scanner, "Enter total amount spent: ");

                String expenseId = splitExpenseService.createSplit(paidBy, members, totalAmount);
                System.out.println("Equal split created successfully.");
                System.out.println("Expense ID: " + expenseId);
                System.out.printf("Each person owes: %.2f%n", totalAmount / (members.size() + 1));
                return;

            } catch (SplitExpenseException e) {
                System.out.println("Error: " + e.getMessage());
                System.out.print("Re-enter the amount? (yes/no): ");
                String retry = scanner.nextLine().trim();
                if (retry.equalsIgnoreCase("yes")) {
                    continue;
                }
                System.out.println("Returning to Split Menu.");
                return;
            }
        }
    }

    // ── Unequal split ─────────────────────────────────────────
    private void handleUnequalSplit() {
        int paidBy = studentId;

        // Phase A: collect valid member IDs (validated as each is entered)
        int memberCount = InputValidator.readInt(scanner, "Enter number of people who owe (excluding yourself): ");
        List<Integer> members = readMemberIds(memberCount, paidBy);

        // Phase B: only the amounts can fail now — re-prompt just the amounts on error
        while (true) {
            try {
                Map<Integer, Double> owedAmounts = new HashMap<>();
                for (int memberId : members) {
                    double amount = InputValidator.readDouble(scanner, "Enter amount owed by member " + memberId + ": ");
                    owedAmounts.put(memberId, amount);
                }

                double totalAmount = InputValidator.readDouble(scanner, "Enter total amount spent: ");

                String expenseId = splitExpenseService.createUnequalSplit(
                    paidBy, members, totalAmount, owedAmounts);
                System.out.println("Unequal split created successfully.");
                System.out.println("Expense ID: " + expenseId);
                return;

            } catch (SplitExpenseException e) {
                System.out.println("Error: " + e.getMessage());
                System.out.print("Re-enter the amounts? (yes/no): ");
                String retry = scanner.nextLine().trim();
                if (retry.equalsIgnoreCase("yes")) {
                    continue;
                }
                System.out.println("Returning to Split Menu.");
                return;
            }
        }
    }

    // ── Read member IDs, validating each on entry (re-prompts the single bad one) ──
    private List<Integer> readMemberIds(int memberCount, int paidBy) {
        List<Integer> members = new ArrayList<>();
        for (int i = 1; i <= memberCount; i++) {
            while (true) {
                int memberId = InputValidator.readInt(scanner, "Enter student ID for member " + i + ": ");
                if (memberId == paidBy) {
                    System.out.println("That's you (the payer) — members are only those who owe. Try another ID.");
                    continue;
                }
                if (members.contains(memberId)) {
                    System.out.println("Member " + memberId + " already added. Try another ID.");
                    continue;
                }
                if (!splitExpenseService.memberExists(memberId)) {
                    System.out.println("Student " + memberId + " not found. Try another ID.");
                    continue;
                }
                members.add(memberId);
                break;
            }
        }
        return members;
    }

    // ── Settle one due ────────────────────────────────────────
    private void handleSettleUp() {
        try {
            int payerId = studentId;
            int dueId   = InputValidator.readInt(scanner, "Enter due ID to settle: ");
            splitExpenseService.settleUp(dueId, payerId);
            System.out.println("Due settled successfully.");
        } catch (SplitExpenseException | InsufficientBalanceException
                 | DailyTransferLimitException | WalletNotFoundException
                 | InvalidAmountException | DatabaseException e) {
            System.out.println("Failed to settle due: " + e.getMessage());
        }
    }

    // ── Settle all dues ───────────────────────────────────────
    private void handleSettleAll() {
        try {
            int payerId = studentId;

            List<SplitExpense> pending = splitExpenseService.listPending(payerId);
            if (pending.isEmpty()) {
                System.out.println("No pending dues.");
                return;
            }
            System.out.println("Pending dues:");
            printDues(pending);

            System.out.print("Confirm settle all? (yes/no): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Settle all cancelled.");
                return;
            }

            splitExpenseService.settleAll(payerId);
            System.out.println("All dues settled successfully.");

        } catch (SplitExpenseException | InsufficientBalanceException
                 | DailyTransferLimitException | WalletNotFoundException
                 | InvalidAmountException | DatabaseException e) {
            System.out.println("Failed to settle all dues: " + e.getMessage());
        }
    }

    // ── View pending dues ─────────────────────────────────────
    private void handleListPending() {
        try {
            int payerId = studentId;
            List<SplitExpense> pending = splitExpenseService.listPending(payerId);
            if (pending.isEmpty()) {
                System.out.println("No pending dues.");
                return;
            }
            System.out.println("Pending dues:");
            printDues(pending);
        } catch (SplitExpenseException e) {
            System.out.println("Could not fetch pending dues: " + e.getMessage());
        }
    }

    // ── View all dues ─────────────────────────────────────────
    private void handleListAll() {
        try {
            int payerId = studentId;
            List<SplitExpense> all = splitExpenseService.listAll(payerId);
            if (all.isEmpty()) {
                System.out.println("No dues found.");
                return;
            }
            System.out.println("All dues:");
            printDues(all);
        } catch (SplitExpenseException e) {
            System.out.println("Could not fetch dues: " + e.getMessage());
        }
    }

    // ── Display helper ────────────────────────────────────────
    private void printDues(List<SplitExpense> dues) {
        System.out.println("--------------------------------------------");
        for (SplitExpense due : dues) {
            System.out.printf("Due ID: %-5d | Pay ₹%-8.2f to student %-5d | %s%n",
                due.getDueId(),
                due.getAmount(),
                due.getPayeeId(),
                due.isStatus() ? "Settled" : "Pending"
            );
        }
        System.out.println("--------------------------------------------");
    }

}