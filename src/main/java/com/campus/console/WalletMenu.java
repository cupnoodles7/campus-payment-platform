package com.campus.console;
import com.campus.exception.BalanceCapExceededException;
import com.campus.exception.DailyTransferLimitException;
import com.campus.exception.InsufficientBalanceException;
import com.campus.exception.InvalidAmountException;
import com.campus.exception.StudentNotFoundException;
import com.campus.exception.SuspiciousActivityException;
import com.campus.exception.WalletNotFoundException;
import com.campus.service.StudentService;
import com.campus.service.WalletService;
import com.campus.util.FileLogger;

import java.util.Scanner;

public class WalletMenu {

    private final Scanner scanner;
    private final WalletService walletService;
    private final StudentService studentService;
    private final int studentId;   // the logged-in user

    public WalletMenu(Scanner scanner, int studentId) {
        this.scanner = scanner;
        this.walletService = new WalletService();
        this.studentService = new StudentService();
        this.studentId = studentId;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("===== Wallet Menu =====");
            System.out.println("1. Add money (deposit)");
            System.out.println("2. Withdraw");
            System.out.println("3. Transfer");
            System.out.println("4. Check balance");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> handleDeposit();
                case "2" -> handleWithdraw();
                case "3" -> handleTransfer();
                case "4" -> handleBalance();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option, try again.");
            }
        }
    }

    private void handleDeposit() {
        try {
            double amount = readDouble("Enter amount to add: ");
            walletService.deposit(studentId, amount);
            System.out.println("Deposit successful.");
        } catch (InvalidAmountException | BalanceCapExceededException | WalletNotFoundException e) {
            FileLogger.logWarn("Deposit failed: " + e.getMessage());
            System.out.println("Deposit failed: " + e.getMessage());
        }
    }

    private void handleWithdraw() {
        try {
            double amount = readDouble("Enter amount to withdraw: ");
            walletService.withdraw(studentId, amount);
            System.out.println("Withdrawal successful.");
        } catch (SuspiciousActivityException e) {
            FileLogger.logWarn("Withdrawal blocked — " + e.getMessage());
            System.out.println("Withdrawal blocked: " + e.getMessage());
        } catch (InvalidAmountException | InsufficientBalanceException | WalletNotFoundException e) {
            FileLogger.logWarn("Withdrawal failed: " + e.getMessage());
            System.out.println("Withdrawal failed: " + e.getMessage());
        }
    }

    private void handleTransfer() {
        try {
            int pin = readInt("Enter your PIN to authorise this transfer: ");
            if (!studentService.verifyPin(studentId, pin)) {
                System.out.println("Incorrect PIN. Transfer cancelled.");
                FileLogger.logWarn("Transfer blocked — bad PIN for student " + studentId);
                return;
            }
            int receiverId = readInt("Enter receiver student ID: ");
            // Validate the receiver exists before asking for the amount.
            studentService.searchById(receiverId);
            double amount = readDouble("Enter amount to transfer: ");
            walletService.transfer(studentId, receiverId, amount);
            System.out.println("Transfer successful.");
        } catch (SuspiciousActivityException e) {
            FileLogger.logWarn("Transfer blocked — " + e.getMessage());
            System.out.println("Transfer blocked: " + e.getMessage());
        } catch (InvalidAmountException | InsufficientBalanceException
                 | DailyTransferLimitException | WalletNotFoundException
                 | StudentNotFoundException e) {
            FileLogger.logWarn("Transfer failed: " + e.getMessage());
            System.out.println("Transfer failed: " + e.getMessage());
        }
    }

    private void handleBalance() {
        try {
            double balance = walletService.getBalance(studentId);
            System.out.printf("Current balance: %.2f%n", balance);
        } catch (WalletNotFoundException e) {
            FileLogger.logWarn("Balance lookup failed: " + e.getMessage());
            System.out.println("Could not fetch balance: " + e.getMessage());
        }
    }

    /* ---------- input helpers (re-prompt until valid) ---------- */

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}

