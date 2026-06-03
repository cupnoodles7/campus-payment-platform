package com.campus.console;
import com.campus.exception.BalanceCapExceededException;
import com.campus.exception.DailyTransferLimitException;
import com.campus.exception.InsufficientBalanceException;
import com.campus.exception.InvalidAmountException;
import com.campus.exception.WalletNotFoundException;
import com.campus.service.WalletService;
import com.campus.util.FileLogger;

import java.util.Scanner;

public class WalletMenu {

    private final Scanner scanner;
    private final WalletService walletService;

    public WalletMenu(Scanner scanner) {
        this.scanner = scanner;
        this.walletService = new WalletService();
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
            int studentId = readInt("Enter student ID: ");
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
            int studentId = readInt("Enter student ID: ");
            double amount = readDouble("Enter amount to withdraw: ");
            walletService.withdraw(studentId, amount);
            System.out.println("Withdrawal successful.");
        } catch (InvalidAmountException | InsufficientBalanceException | WalletNotFoundException e) {
            FileLogger.logWarn("Withdrawal failed: " + e.getMessage());
            System.out.println("Withdrawal failed: " + e.getMessage());
        }
    }

    private void handleTransfer() {
        try {
            int senderId = readInt("Enter your (sender) student ID: ");
            int receiverId = readInt("Enter receiver student ID: ");
            double amount = readDouble("Enter amount to transfer: ");
            walletService.transfer(senderId, receiverId, amount);
            System.out.println("Transfer successful.");
        } catch (InvalidAmountException | InsufficientBalanceException
                 | DailyTransferLimitException | WalletNotFoundException e) {
            FileLogger.logWarn("Transfer failed: " + e.getMessage());
            System.out.println("Transfer failed: " + e.getMessage());
        }
    }

    private void handleBalance() {
        try {
            int studentId = readInt("Enter student ID: ");
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

