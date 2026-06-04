package com.campus.console;

import com.campus.exception.*;
import com.campus.service.PaymentService;
import com.campus.util.InputValidator;

import java.util.Scanner;

public class PaymentMenu {

    private final PaymentService paymentService = new PaymentService();
    private final Scanner sc;
    private final int studentId;   // the logged-in user

    public PaymentMenu(Scanner sc, int studentId) {
        this.sc = sc;
        this.studentId = studentId;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== CAMPUS PAYMENTS ===");
            System.out.println("1. Canteen");
            System.out.println("2. Library Fine");
            System.out.println("3. Hackathon Fee");
            System.out.println("4. Workshop Fee");
            System.out.println("5. Hostel Fee");
            System.out.println("0. Back");
            System.out.print("Choice: ");

            String choice = sc.nextLine().trim();

            if (choice.equals("0")) {
                back = true;
                continue;
            }

            String paymentType = switch (choice) {
                case "1" -> "CANTEEN";
                case "2" -> "LIBRARY";
                case "3" -> "HACKATHON";
                case "4" -> "WORKSHOP";
                case "5" -> "HOSTEL";
                default  -> null;
            };

            if (paymentType == null) {
                System.out.println("Invalid choice. Try again.");
                continue;
            }

            handlePayment(paymentType);
        }
    }

    private void handlePayment(String paymentType) {
        try {
            double amount = InputValidator.readDouble(sc, "Amount: Rs.");

            System.out.printf("Confirm %s payment of Rs.%.2f? (y/n): ",
                              paymentType, amount);
            String confirm = sc.nextLine().trim();

            if (!confirm.equalsIgnoreCase("y")) {
                System.out.println("Payment cancelled.");
                return;
            }

            paymentService.pay(studentId, paymentType, amount);
            System.out.printf("Payment of Rs.%.2f for %s successful!%n", amount, paymentType);

        } catch (InvalidAmountException e) {
            System.out.println("Invalid amount: " + e.getMessage());
        } catch (InsufficientBalanceException e) {
            System.out.println("Insufficient balance: " + e.getMessage());
        } catch (WalletNotFoundException e) {
            System.out.println("Wallet not found: " + e.getMessage());
        } catch (StudentNotFoundException e) {
            System.out.println("Student not found: " + e.getMessage());
        } catch (PaymentFailedException e) {
            System.out.println("Payment failed: " + e.getMessage());
        }
    }
}