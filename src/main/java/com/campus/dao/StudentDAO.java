package com.campus.dao;

import com.campus.exception.DatabaseException;
import com.campus.model.Student;
import com.campus.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDAO {

    private Student mapRow(ResultSet rs) throws SQLException {
        return Student.builder()
                .studentId(rs.getInt("student_id"))
                .name(rs.getString("name"))
                .email(Optional.ofNullable(rs.getString("email")))
                .phone(Optional.ofNullable(rs.getString("phone")))
                .build();
    }

    public void insert(Student s) {
        String sql = "INSERT INTO students (student_id, name, email, phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getStudentId());
            ps.setString(2, s.getName());
            ps.setString(3, s.getEmail().orElse(null));
            ps.setString(4, s.getPhone().orElse(null));
            ps.executeUpdate();
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
        } catch (SQLException e) {
            throw new DatabaseException("Update failed: " + e.getMessage(), e);
        }
    }

    public Student findById(int id) {
        String sql = "SELECT * FROM students WHERE student_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Find failed: " + e.getMessage(), e);
        }
    }

    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM students WHERE student_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
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
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("FetchAll failed: " + e.getMessage(), e);
        }
        return list;
    }
}