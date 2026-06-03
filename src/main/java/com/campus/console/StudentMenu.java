package com.campus.console;

import com.campus.exception.*;
import com.campus.model.Student;
import com.campus.service.StudentService;
import java.util.Comparator;
import java.util.Optional;
import java.util.Scanner;

public class StudentMenu {

    private final StudentService service = new StudentService();
    private final Scanner sc = new Scanner(System.in);

    public void show() {
        while (true) {
            System.out.println("\n--- Student Menu ---");
            System.out.println("1. Register Student");
            System.out.println("2. Update Student");
            System.out.println("3. Search by ID");
            System.out.println("4. Display All");
            System.out.println("5. Back");
            System.out.print("Choice: ");

            switch (sc.nextLine().trim()) {
                case "1" -> register();
                case "2" -> update();
                case "3" -> search();
                case "4" -> displayAll();
                case "5" -> { return; }
                default  -> System.out.println("Invalid choice.");
            }
        }
    }

    private void register() {
        try {
            System.out.print("Student ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());

            System.out.print("Name: ");
            String name = sc.nextLine().trim();

            System.out.print("Email (Enter to skip): ");
            String email = sc.nextLine().trim();

            System.out.print("Phone (Enter to skip): ");
            String phone = sc.nextLine().trim();

            service.registerStudent(Student.builder()
                    .studentId(id)
                    .name(name)
                    .email(Optional.ofNullable(email.isEmpty() ? null : email))
                    .phone(Optional.ofNullable(phone.isEmpty() ? null : phone))
                    .build());

            System.out.println("Registered successfully.");
        } catch (DuplicateStudentException | InvalidAmountException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        }
    }

    private void update() {
        try {
            System.out.print("Student ID to update: ");
            int id = Integer.parseInt(sc.nextLine().trim());

            System.out.print("New Name: ");
            String name = sc.nextLine().trim();

            System.out.print("New Email (Enter to skip): ");
            String email = sc.nextLine().trim();

            System.out.print("New Phone (Enter to skip): ");
            String phone = sc.nextLine().trim();

            service.updateStudent(Student.builder()
                    .studentId(id)
                    .name(name)
                    .email(Optional.ofNullable(email.isEmpty() ? null : email))
                    .phone(Optional.ofNullable(phone.isEmpty() ? null : phone))
                    .build());

            System.out.println("Updated successfully.");
        } catch (StudentNotFoundException | InvalidAmountException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        }
    }

    private void search() {
        try {
            System.out.print("Enter Student ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.println(service.searchById(id));
        } catch (StudentNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        }
    }

    private void displayAll() {
        try {
            System.out.println("Sort by: 1. ID  2. Name A-Z  3. Name Z-A");
            System.out.print("Choice: ");
            Comparator<Student> comparator = switch (sc.nextLine().trim()) {
                case "2" -> Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER);
                case "3" -> Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER).reversed();
                default  -> Comparator.comparingInt(Student::getStudentId);
            };
            service.displayAll(comparator).forEach(System.out::println);
        } catch (StudentNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}