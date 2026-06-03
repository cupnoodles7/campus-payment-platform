package main.java.com.campus.service;

import main.java.com.campus.dao.StudentDAO;
//import main.java.com.campus.dao.WalletDAO;
import main.java.com.campus.exception.*;
import main.java.com.campus.model.Student;
//import main.java.com.campus.model.Wallet;
//import main.java.com.campus.util.FileLogger;
import java.util.Comparator;
import java.util.List;

public class StudentService {

    private final StudentDAO studentDAO = new StudentDAO();
    //private final WalletDAO walletDAO = new WalletDAO();

    public void registerStudent(Student s) {
        if (s.getEmail().isEmpty() && s.getPhone().isEmpty())
            throw new InvalidAmountException("Either email or phone must be provided.");

        if (studentDAO.existsById(s.getStudentId()))
            throw new DuplicateStudentException("Student ID " + s.getStudentId() + " already exists.");

        studentDAO.insert(s);
        /*walletDAO.insert(Wallet.builder()
                .studentId(s.getStudentId())
                .balance(0.0)
                .dailyTransferLimit(5000.0)
                .balanceCap(20000.0)
                .todayTransferred(0.0)
                .build());*/
        //FileLogger.logInfo("Student registered — ID: " + s.getStudentId());
    }

    public void updateStudent(Student s) {
        if (studentDAO.findById(s.getStudentId()) == null)
            throw new StudentNotFoundException("Student ID " + s.getStudentId() + " not found.");

        if (s.getEmail().isEmpty() && s.getPhone().isEmpty())
            throw new InvalidAmountException("Either email or phone must be provided.");

        studentDAO.update(s);
        //FileLogger.logInfo("Student updated — ID: " + s.getStudentId());
    }

    public Student searchById(int id) {
        Student s = studentDAO.findById(id);
        if (s == null)
            throw new StudentNotFoundException("Student ID " + id + " not found.");

        //s.setWallet(walletDAO.getByStudentId(id));
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