package com.campus.util;

import java.util.Scanner;
import java.util.regex.Pattern;

public class InputValidator {

    // basic email: something@something.tld
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    // 10–15 digits, optionally prefixed with a single +
    private static final Pattern PHONE =
            Pattern.compile("^\\+?[0-9]{10,15}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE.matcher(phone.trim()).matches();
    }

    // required phone — loops until a validly-formatted number is entered
    public static String readPhone(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = sc.nextLine().trim();
            if (isValidPhone(value)) return value;
            System.out.println("Invalid phone number. Enter 10–15 digits (optionally starting with +).");
        }
    }

    // optional email — returns "" if skipped, otherwise loops until a valid email is entered
    public static String readOptionalEmail(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = sc.nextLine().trim();
            if (value.isEmpty()) return "";
            if (isValidEmail(value)) return value;
            System.out.println("Invalid email format. Try again, or press Enter to skip.");
        }
    }

    public static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number.");
            }
        }
    }

    public static double readDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(sc.nextLine().trim());
                if (value <= 0) {
                    System.out.println("Amount must be greater than 0.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid amount.");
            }
        }
    }

    public static String readNonEmptyString(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = sc.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("Input cannot be empty.");
        }
    }
}