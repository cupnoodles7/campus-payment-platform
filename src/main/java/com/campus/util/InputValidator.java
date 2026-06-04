package com.campus.util;

import com.campus.exception.InputCancelledException;

import java.util.Scanner;
import java.util.regex.Pattern;

public class InputValidator {

    // basic email: something@something.tld
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    // 10–15 digits, optionally prefixed with a single +
    private static final Pattern PHONE =
            Pattern.compile("^\\+?[0-9]{10,15}$");

    // Typing this at any prompt cancels the current action and returns to the menu.
    private static final String BACK_KEYWORD = "b";

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE.matcher(phone.trim()).matches();
    }

    /**
     * Reads one trimmed line. Throws {@link InputCancelledException} (caught by the
     * menu loop) if the user typed the back keyword, so callers never have to handle
     * the back-to-menu case explicitly.
     */
    private static String readLine(Scanner sc, String prompt) {
        System.out.print(prompt);
        String value = sc.nextLine().trim();
        if (value.equalsIgnoreCase(BACK_KEYWORD)) {
            throw new InputCancelledException("back");
        }
        return value;
    }

    /** Prints the reason, then aborts to the menu (reminding the user about 'b'). */
    private static InputCancelledException abort(String reason) {
        System.out.println(reason + " — returning to menu. (Tip: type 'b' at any prompt to go back.)");
        return new InputCancelledException(reason);
    }

    // required phone — valid number returned, otherwise back to the menu
    public static String readPhone(Scanner sc, String prompt) {
        String value = readLine(sc, prompt);
        if (isValidPhone(value)) return value;
        throw abort("Invalid phone number (enter 10–15 digits, optionally starting with +)");
    }

    // optional email — "" if skipped (blank), valid email returned, otherwise back to the menu
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

    // positive monetary amount — must parse and be greater than 0
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
