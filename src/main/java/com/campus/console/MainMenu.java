package com.campus.console;
import com.campus.exception.InvalidAmountException;
import com.campus.exception.DatabaseException;
import com.campus.model.Student;
import com.campus.service.StudentService;
import com.campus.util.DBConnection;
import com.campus.util.FileLogger;

import java.util.Optional;
import java.util.Scanner;

public class MainMenu {

    private final Scanner sc = new Scanner(System.in);
    private final StudentService studentService = new StudentService();
 
    public void start() {
        System.out.println("\n=== CAMPUS PAYMENT PLATFORM ===");

        // make sure the schema exists (also adds the pin column on older databases)
        DBConnection.createTables();

        boolean running = true;
        while (running) {
            System.out.println("\n1. Register");
            System.out.println("2. Login");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
 
            String choice = sc.nextLine().trim();
 
            switch (choice) {
                case "1" -> register();
                case "2" -> {
                    Student student = login();
                    if (student != null) showMainMenu(student);
                }
                case "0" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
        sc.close();
    }
 
    // ── REGISTER ─────────────────────────────────────────────────────────────
    private void register() {
        System.out.println("\n=== REGISTER ===");
        try {
            System.out.print("Name: ");
            String name = sc.nextLine().trim();

            System.out.print("Phone (Enter to skip): ");
            String phone = sc.nextLine().trim();

            System.out.print("Email (Enter to skip): ");
            String email = sc.nextLine().trim();

            int pin = readPin("Set a numeric PIN (used for login & transfers): ");

            Student s = new Student();
            s.setName(name);
            s.setEmail(Optional.ofNullable(email.isEmpty() ? null : email));
            s.setPhone(Optional.ofNullable(phone.isEmpty() ? null : phone));
            s.setPin(pin);

            // service layer creates the student, generates the ID and sets up the wallet
            int studentId = studentService.registerStudent(s);

            System.out.println("\nRegistration successful!");
            System.out.println("Your Student ID : " + studentId);
            System.out.println("Login with your email and PIN.");
            FileLogger.logInfo("New student registered: " + studentId);

        } catch (InvalidAmountException e) {
            System.out.println("Registration failed: " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("Registration failed: email or phone may already be in use.");
            FileLogger.logError("Registration failed: " + e.getMessage());
        }
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────────
    private Student login() {
        System.out.println("\n=== LOGIN ===");
        int attempts = 0;

        while (attempts < 3) {
            System.out.print("Email: ");
            String email = sc.nextLine().trim();

            int pin = readPin("PIN: ");

            try {
                Student s = studentService.login(email, pin);
                if (s != null) {
                    System.out.println("Login successful! Welcome, " + s.getName() + ".");
                    return s;
                }
            } catch (DatabaseException e) {
                System.out.println("Login error: " + e.getMessage());
                FileLogger.logError("Login failed: " + e.getMessage());
                return null;
            }

            attempts++;
            System.out.println("Invalid credentials. Attempts left: " + (3 - attempts));
        }

        System.out.println("Too many failed attempts.");
        return null;
    }

    // reads a whole-number PIN, re-prompting until valid
    private int readPin(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("PIN must be a number.");
            }
        }
    }
 
    // ── MAIN MENU AFTER LOGIN ─────────────────────────────────────────────────
    private void showMainMenu(Student student) {
        boolean exit = false;
        while (!exit) {
           System.out.println("\n=== MAIN MENU === Welcome, " + student.getName());
            System.out.println("1. Wallet Management");
            System.out.println("2. Campus Payments");
            System.out.println("3. Split Expenses");
            System.out.println("4. Reports");
            System.out.println("5. Student Management");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
 
            String choice = sc.nextLine().trim();
 
            switch (choice) {
                case "1" -> new WalletMenu(sc).show();
                case "2" -> new PaymentMenu(sc).show();
                case "3" -> new SplitMenu(sc).show();
                case "4" -> new ReportMenu(sc).show();
                case "5" -> new StudentMenu().show();
                case "0" -> {
                    System.out.println("Logged out successfully.");
                    exit = true;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }
 
    public static void main(String[] args) {
        new MainMenu().start();
    }
}
 