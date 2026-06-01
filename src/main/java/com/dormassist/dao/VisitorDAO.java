package com.dormassist.dao;

import com.dormassist.config.Databaseconfig;
import com.dormassist.model.Visitor;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class VisitorDAO {
    private static final String BASE =
        "SELECT v.*, s.full_name AS student_name, u.full_name AS approver_name " +
        "FROM visitors v " +
        "LEFT JOIN students s ON v.student_id=s.id " +
        "LEFT JOIN users u ON v.approved_by=u.id";

    public List<Visitor> getAll() {
        List<Visitor> l = new ArrayList<>();
        try (Connection c = Databaseconfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(BASE + " ORDER BY CASE v.status WHEN 'CHECKED_IN' THEN 1 WHEN 'PENDING' THEN 2 WHEN 'APPROVED' THEN 3 WHEN 'CHECKED_OUT' THEN 4 WHEN 'REJECTED' THEN 5 ELSE 9 END, v.visit_date DESC, v.created_at DESC")) {
            while (rs.next()) l.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return l;
    }

    public boolean insert(Visitor v) {
        String sql = "INSERT INTO visitors(student_id,visitor_name,visitor_phone,visitor_id_card," +
                     "visit_date,visit_time_start,visit_time_end,purpose,status) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, v.getStudentId());
            ps.setString(2, v.getVisitorName());
            ps.setString(3, v.getVisitorPhone());
            ps.setString(4, v.getVisitorIdCard());
            // Dùng java.sql.Date rõ ràng
            ps.setObject(5, v.getVisitDate() != null ? new Date(v.getVisitDate().getTime()) : null);
            ps.setTime(6, v.getVisitTimeStart());
            ps.setTime(7, v.getVisitTimeEnd());
            ps.setString(8, v.getPurpose());
            ps.setString(9, "PENDING");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateStatus(int id, String status, int approvedBy) {
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "UPDATE visitors SET status=?, approved_by=?, approved_at=GETDATE() WHERE id=?")) {
            ps.setString(1, status);
            ps.setObject(2, approvedBy > 0 ? approvedBy : null);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM visitors WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Visitor map(ResultSet rs) throws SQLException {
        Visitor v = new Visitor();
        v.setId(rs.getInt("id"));
        v.setStudentId(rs.getInt("student_id"));
        try {
            v.setStudentName(rs.getString("student_name"));
            v.setApproverName(rs.getString("approver_name"));
        } catch (Exception ignored) {}
        v.setVisitorName(rs.getString("visitor_name"));
        v.setVisitorPhone(rs.getString("visitor_phone"));
        v.setVisitorIdCard(rs.getString("visitor_id_card"));
        v.setPurpose(rs.getString("purpose"));
        v.setStatus(rs.getString("status"));

        // Dùng java.sql.Date rõ ràng rồi convert sang java.util.Date
        Date vd = rs.getDate("visit_date");
        if (vd != null) v.setVisitDate(new java.util.Date(vd.getTime()));

        v.setVisitTimeStart(rs.getTime("visit_time_start"));
        v.setVisitTimeEnd(rs.getTime("visit_time_end"));

        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) v.setCreatedAt(new java.util.Date(ca.getTime()));
        return v;
    }
}
