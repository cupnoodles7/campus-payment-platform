package com.campus.util;

import com.campus.exception.InputCancelledException;

import java.util.Scanner;
import java.util.regex.Pattern;

public class InputValidator {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE =
            Pattern.compile("^\\+?[0-9]{10,15}$");

    private static final String BACK_KEYWORD = "b";

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE.matcher(phone.trim()).matches();
    }

    
    private static String readLine(Scanner sc, String prompt) {
        System.out.print(prompt);
        String value = sc.nextLine().trim();
        if (value.equalsIgnoreCase(BACK_KEYWORD)) {
            throw new InputCancelledException("back");
        }
        return value;
    }

    private static InputCancelledException abort(String reason) {
        System.out.println(reason + " — returning to menu. (Tip: type 'b' at any prompt to go back.)");
        return new InputCancelledException(reason);
    }

    public static String readPhone(Scanner sc, String prompt) {
        String value = readLine(sc, prompt);
        if (isValidPhone(value)) return value;
        throw abort("Invalid phone number (enter 10–15 digits, optionally starting with +)");
    }

    public static String readOptionalPhone(Scanner sc, String prompt) {
        String value = readLine(sc, prompt);
        if (value.isEmpty()) return "";
        if (isValidPhone(value)) return value;
        throw abort("Invalid phone number (enter 10–15 digits, optionally starting with +)");
    }

    public static String readOptionalEmail(Scanner sc, String prompt) {
        String value = readLine(sc, prompt);
        if (value.isEmpty()) return "";
        if (isValidEmail(value)) return value;
        throw abort("Invalid email format (press Enter at the prompt to skip it)");
    }

    public static int readInt(Scanner sc, String prompt) {
        String value = readLine(sc, prompt);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw abort("Invalid input — that is not a whole number");
        }
    }

    public static double readDouble(Scanner sc, String prompt) {
        String value = readLine(sc, prompt);
        try {
            double amount = Double.parseDouble(value);
            if (amount <= 0) {
                throw abort("Amount must be greater than 0");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw abort("Invalid input — that is not a valid amount");
        }
    }

    public static String readNonEmptyString(Scanner sc, String prompt) {
        String value = readLine(sc, prompt);
        if (!value.isEmpty()) return value;
        throw abort("Input cannot be empty");
    }
}
