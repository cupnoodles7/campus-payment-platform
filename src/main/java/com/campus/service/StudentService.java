package com.campus.service;

import com.campus.dao.StudentDAO;
import com.campus.dao.WalletDAO;
import com.campus.exception.*;
import com.campus.model.Student;
import com.campus.model.Wallet;
import com.campus.util.DBConnection;
import com.campus.util.FileLogger;
import com.campus.util.InputValidator;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class StudentService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final WalletDAO walletDAO = new WalletDAO();

    // register — returns generated student ID so menu can display it.
    // All inserts run on a single transaction: if any step fails the whole thing
    // rolls back, so we never leave a half-written student/wallet behind.
    public int registerStudent(Student s) {
        // phone is mandatory and must be unique; email is optional. Login is by ID + PIN.
        String phone = requirePhone(s);
        validateEmailIfPresent(s);
        Student existing = studentDAO.findByPhone(phone);
        if (existing != null)
            throw new DuplicateStudentException("Phone number " + phone + " is already registered.");

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // step 1 — SQL auto generates student_id
            int generatedStudentId = studentDAO.insert(s, conn);

            // step 2 — create wallet using generated student_id, get generated wallet_id back
            int walletId = walletDAO.insert(
                    new Wallet(0, generatedStudentId, 0.0, 5000.0, 20000.0, 0.0), conn);

            // step 3 — link wallet_id into student row
            studentDAO.updateWalletId(generatedStudentId, walletId, conn);

            conn.commit();
            FileLogger.logInfo("Student registered — ID: " + generatedStudentId +
                               ", WalletID: " + walletId);
            return generatedStudentId;
        } catch (SQLException | DatabaseException e) {
            rollbackQuietly(conn);
            FileLogger.logError("Registration rolled back: " + e.getMessage());
            throw new DatabaseException("Registration failed: " + e.getMessage(), e);
        } finally {
            closeQuietly(conn);
        }
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                FileLogger.logError("Rollback failed: " + ex.getMessage());
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                FileLogger.logError("Closing connection failed: " + ex.getMessage());
            }
        }
    }

    // login by Student ID + PIN — returns the student (with wallet) or null if credentials are wrong
    public Student login(int studentId, int pin) {
        Student s = studentDAO.findById(studentId);
        if (s == null || s.getPin() != pin) {
            FileLogger.logWarn("Login failed for Student ID: " + studentId);
            return null;
        }
        // fetch wallet object and attach to student at runtime
        s.setWallet(walletDAO.getByStudentId(studentId));
        FileLogger.logInfo("Student logged in — ID: " + studentId);
        return s;
    }

    // verifies a student's PIN — used to authorise wallet transfers
    public boolean verifyPin(int studentId, int pin) {
        Student s = studentDAO.findById(studentId);
        return s != null && s.getPin() == pin;
    }

    public void updateStudent(Student s) {
        if (studentDAO.findById(s.getStudentId()) == null)
            throw new StudentNotFoundException("Student ID " + s.getStudentId() + " not found.");

        String phone = requirePhone(s);
        validateEmailIfPresent(s);
        Student existing = studentDAO.findByPhone(phone);
        if (existing != null && existing.getStudentId() != s.getStudentId())
            throw new DuplicateStudentException("Phone number " + phone + " is already registered.");

        studentDAO.update(s);
        FileLogger.logInfo("Student updated — ID: " + s.getStudentId());
    }

    // ensures a present, validly-formatted phone number, returns the trimmed value
    private String requirePhone(Student s) {
        String phone = s.getPhone().map(String::trim).orElse("");
        if (phone.isEmpty())
            throw new InvalidAmountException("Phone number is required.");
        if (!InputValidator.isValidPhone(phone))
            throw new InvalidAmountException("Invalid phone number format: " + phone);
        return phone;
    }

    // email is optional, but if provided it must be validly formatted
    private void validateEmailIfPresent(Student s) {
        s.getEmail().map(String::trim).filter(e -> !e.isEmpty()).ifPresent(email -> {
            if (!InputValidator.isValidEmail(email))
                throw new InvalidAmountException("Invalid email format: " + email);
        });
    }

    public Student searchById(int id) {
        Student s = studentDAO.findById(id);
        if (s == null)
            throw new StudentNotFoundException("Student ID " + id + " not found.");

        // fetch wallet object and attach to student
        s.setWallet(walletDAO.getByStudentId(id));
        return s;
    }

    public List<Student> displayAll(Comparator<Student> comparator) {
        List<Student> list = studentDAO.findAll();
        if (list.isEmpty())
            throw new StudentNotFoundException("No students registered yet.");

        list.sort(comparator);
        return list;
    }
}