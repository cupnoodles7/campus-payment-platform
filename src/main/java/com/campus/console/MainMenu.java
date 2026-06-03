package com.campus.console;

import java.util.Scanner;

public class MainMenu {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        StudentMenu studentMenu = new StudentMenu();

        while (true) {
            System.out.println("\n===== Campus Payment Platform =====");
            System.out.println("1. Student");
            System.out.println("2. Exit");
            System.out.print("Choice: ");

            switch (sc.nextLine().trim()) {
                case "1" -> studentMenu.show();
                case "2" -> {
                    System.out.println("Bye!");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}