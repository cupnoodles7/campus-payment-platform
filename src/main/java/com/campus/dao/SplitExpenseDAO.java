// Author: Ahana

package com.campus.dao;

import com.campus.exception.DatabaseException;
import com.campus.model.SplitExpense;
import com.campus.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SplitExpenseDAO {

    // dueId excluded — SQL auto-increments it
    public void insert(SplitExpense due) {
        String sql = "INSERT INTO dues (expense_id, payer_id, payee_id, amount, status) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, due.getExpenseId());
            ps.setInt(2, due.getPayerId());
            ps.setInt(3, due.getPayeeId());
            ps.setDouble(4, due.getAmount());
            ps.setBoolean(5, due.isStatus());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert due for expenseId: " + due.getExpenseId(), e);
        }
    }

    // All dues (settled + unsettled) for a student
    public List<SplitExpense> findByMember(int payerId) {
        String sql = "SELECT * FROM dues WHERE payer_id = ?";
        List<SplitExpense> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, payerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Finding dues failed for payerId: " + payerId, e);
        }
        return list;
    }

    // Only unsettled dues — for dues table view
    public List<SplitExpense> findUnsettled(int payerId) {
        String sql = "SELECT * FROM dues WHERE payer_id = ? AND status = FALSE ORDER BY due_id";
        List<SplitExpense> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, payerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Finding unsettled dues failed for payerId: " + payerId, e);
        }
        return list;
    }

    // All dues under one split — group by expenseId
    public List<SplitExpense> findByExpenseId(String expenseId) {
        String sql = "SELECT * FROM dues WHERE expense_id = ?";
        List<SplitExpense> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, expenseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Finding dues failed for expenseId: " + expenseId, e);
        }
        return list;
    }

    // Fetch one due by its dueID
    public SplitExpense findById(int dueId) {
        String sql = "SELECT * FROM dues WHERE due_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dueId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("findById failed for dueId: " + dueId, e);
        }
    }

    // Flip status to TRUE for one due record
    public void markSettled(int dueId) {
        String sql = "UPDATE dues SET status = TRUE WHERE due_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dueId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DatabaseException("No due found to settle for dueId: " + dueId);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Settling dues failed for dueId: " + dueId, e);
        }
    }

    // helper
    private SplitExpense mapRow(ResultSet rs) throws SQLException {
        return new SplitExpense(
            rs.getInt("due_id"),
            rs.getString("expense_id"),
            rs.getInt("payer_id"),
            rs.getInt("payee_id"),
            rs.getDouble("amount"),
            rs.getBoolean("status")
        );
    }
}