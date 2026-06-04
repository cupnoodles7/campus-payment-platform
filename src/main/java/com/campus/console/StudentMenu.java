package com.campus.console;

import com.campus.exception.*;
import com.campus.model.Student;
import com.campus.service.StudentService;
import com.campus.util.InputValidator;
import java.util.Comparator;
import java.util.Optional;
import java.util.Scanner;

public class StudentMenu {

    private final StudentService service = new StudentService();
    private final Scanner sc;
    private final int studentId;   // the logged-in user

    public StudentMenu(Scanner sc, int studentId) {
        this.sc = sc;
        this.studentId = studentId;
    }

    public void show() {
        while (true) {
            System.out.println("\n--- Student Menu ---");
            System.out.println("1. Update my profile");
            System.out.println("2. Search by ID");
            System.out.println("3. Display All");
            System.out.println("4. Back");
            System.out.print("Choice: ");

            switch (sc.nextLine().trim()) {
                case "1" -> update();
                case "2" -> search();
                case "3" -> displayAll();
                case "4" -> { return; }
                default  -> System.out.println("Invalid choice.");
            }
        }
    }

    private void update() {
        try {
            System.out.print("New Name: ");
            String name = sc.nextLine().trim();

            String email = InputValidator.readOptionalEmail(sc, "New Email (Enter to skip): ");

            String phone = InputValidator.readPhone(sc, "New Phone: ");

            Student s = new Student();
            s.setStudentId(studentId);
            s.setName(name);
            s.setEmail(Optional.ofNullable(email.isEmpty() ? null : email));
            s.setPhone(Optional.of(phone));

            service.updateStudent(s);
            System.out.println("Updated successfully.");

        } catch (StudentNotFoundException | InvalidAmountException | DuplicateStudentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void search() {
        try {
            System.out.print("Enter Student ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.println(service.searchById(id).display());
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
            service.displayAll(comparator).forEach(s -> System.out.println(s.display()));
        } catch (StudentNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}