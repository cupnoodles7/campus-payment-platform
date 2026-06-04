package com.campus.service;

import com.campus.dao.StudentDAO;
import com.campus.dao.WalletDAO;
import com.campus.exception.*;
import com.campus.model.Student;
import com.campus.model.Wallet;
import com.campus.util.FileLogger;
import java.util.Comparator;
import java.util.List;

public class StudentService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final WalletDAO walletDAO = new WalletDAO();

    // register — returns generated student ID so menu can display it
    public int registerStudent(Student s) {
        if (s.getEmail().isEmpty() && s.getPhone().isEmpty())
            throw new InvalidAmountException("Either email or phone must be provided.");

        // step 1 — SQL auto generates student_id
        int generatedStudentId = studentDAO.insert(s);

        // step 2 — create wallet using generated student_id
        walletDAO.insert(new Wallet(0, generatedStudentId, 0.0, 5000.0, 20000.0, 0.0));

        // step 3 — fetch wallet object to get generated wallet_id
        Wallet wallet = walletDAO.getByStudentId(generatedStudentId);

        // step 4 — link wallet_id into student row
        studentDAO.updateWalletId(generatedStudentId, wallet.getWalletId());

        FileLogger.logInfo("Student registered — ID: " + generatedStudentId +
                           ", WalletID: " + wallet.getWalletId());
        return generatedStudentId;
    }

    // login — called from MainMenu using student ID only
    public Student login(int studentId) {
        Student s = studentDAO.findById(studentId);
        if (s == null) {
            FileLogger.logWarn("Login failed — Student ID " + studentId + " not found.");
            throw new StudentNotFoundException("Student ID " + studentId + " not found.");
        }
        // fetch wallet object and attach to student at runtime
        s.setWallet(walletDAO.getByStudentId(studentId));
        FileLogger.logInfo("Student logged in — ID: " + studentId);
        return s;
    }

    // login by email + PIN — returns the student (with wallet) or null if credentials are wrong
    public Student login(String email, int pin) {
        Student s = studentDAO.findByEmail(email);
        if (s == null || s.getPin() != pin) {
            FileLogger.logWarn("Login failed for email: " + email);
            return null;
        }
        s.setWallet(walletDAO.getByStudentId(s.getStudentId()));
        FileLogger.logInfo("Student logged in — ID: " + s.getStudentId());
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

        if (s.getEmail().isEmpty() && s.getPhone().isEmpty())
            throw new InvalidAmountException("Either email or phone must be provided.");

        studentDAO.update(s);
        FileLogger.logInfo("Student updated — ID: " + s.getStudentId());
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