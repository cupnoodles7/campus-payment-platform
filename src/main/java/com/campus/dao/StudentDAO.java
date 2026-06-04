package com.campus.dao;

import com.campus.exception.DatabaseException;
import com.campus.model.Student;
import com.campus.util.DBConnection;
import com.campus.util.FileLogger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDAO {

    // builds Student object from DB row — wallet NOT fetched here, done in service
    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getInt("student_id"));
        s.setName(rs.getString("name"));
        s.setEmail(Optional.ofNullable(rs.getString("email")));
        s.setPhone(Optional.ofNullable(rs.getString("phone")));
        s.setPin(rs.getInt("pin"));
        return s;
    }

    // inserts student, SQL auto generates student_id, returns it
    public int insert(Student s) {
        try (Connection conn = DBConnection.getConnection()) {
            return insert(s, conn);
        } catch (SQLException e) {
            throw new DatabaseException("Insert failed: " + e.getMessage(), e);
        }
    }

    // transactional variant — runs on the caller's connection (NOT closed here)
    public int insert(Student s, Connection conn) {
        String sql = "INSERT INTO students (name, email, phone, pin) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getEmail().orElse(null));
            ps.setString(3, s.getPhone().orElse(null));
            ps.setInt(4, s.getPin());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                FileLogger.logInfo("Student inserted — generated ID: " + id);
                return id;
            }
            throw new DatabaseException("Failed to get generated student ID", null);
        } catch (SQLException e) {
            throw new DatabaseException("Insert failed: " + e.getMessage(), e);
        }
    }

    public void update(Student s) {
        String sql = "UPDATE students SET name=?, email=?, phone=? WHERE student_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getEmail().orElse(null));
            ps.setString(3, s.getPhone().orElse(null));
            ps.setInt(4, s.getStudentId());
            ps.executeUpdate();
            FileLogger.logInfo("Student updated — ID: " + s.getStudentId());
        } catch (SQLException e) {
            throw new DatabaseException("Update failed: " + e.getMessage(), e);
        }
    }

    public Student findById(int id) {
        String sql = "SELECT * FROM students WHERE student_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Find failed: " + e.getMessage(), e);
        }
    }

    // used to enforce unique phone numbers — returns the matching student or null
    public Student findByPhone(String phone) {
        String sql = "SELECT * FROM students WHERE phone=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Find by phone failed: " + e.getMessage(), e);
        }
    }

    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM students WHERE student_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Check failed: " + e.getMessage(), e);
        }
        return false;
    }

    public List<Student> findAll() {
        String sql = "SELECT * FROM students";
        List<Student> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("FetchAll failed: " + e.getMessage(), e);
        }
        return list;
    }
}