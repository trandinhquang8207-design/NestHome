package com.dormassist.dao;

import com.dormassist.config.Databaseconfig;
import java.security.SecureRandom;
import java.sql.*;

public class PasswordResetDAO {
    private final SecureRandom random = new SecureRandom();
    public String generateCode() { return String.format("%06d", random.nextInt(1000000)); }
    public boolean createCode(int userId, String email, String code) {
        try (Connection c = Databaseconfig.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement("UPDATE password_reset_codes SET used=1 WHERE user_id=? AND used=0")) { ps.setInt(1, userId); ps.executeUpdate(); }
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO password_reset_codes(user_id,email,code_hash,expires_at,used) VALUES(?,?,?,DATEADD(MINUTE,10,GETDATE()),0)")) {
                ps.setInt(1, userId); ps.setString(2, email); ps.setString(3, UserDAO.hashPassword(code)); ps.executeUpdate();
            }
            c.commit(); return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public boolean verifyCode(int userId, String code) {
        try (Connection c = Databaseconfig.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT TOP 1 code_hash FROM password_reset_codes WHERE user_id=? AND used=0 AND expires_at>=GETDATE() ORDER BY created_at DESC")) {
            ps.setInt(1, userId); ResultSet rs = ps.executeQuery(); return rs.next() && UserDAO.hashPassword(code).equals(rs.getString("code_hash"));
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public boolean resetPassword(int userId, String code, String newPassword) {
        if (!verifyCode(userId, code)) return false;
        try (Connection c = Databaseconfig.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement("UPDATE users SET password_hash=? WHERE id=?")) { ps.setString(1, UserDAO.hashPassword(newPassword)); ps.setInt(2, userId); ps.executeUpdate(); }
            try (PreparedStatement ps = c.prepareStatement("UPDATE password_reset_codes SET used=1 WHERE user_id=? AND used=0")) { ps.setInt(1, userId); ps.executeUpdate(); }
            c.commit(); return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public int countRecentRequests(int userId) {
        try (Connection c = Databaseconfig.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM password_reset_codes WHERE user_id=? AND created_at>=DATEADD(MINUTE,-15,GETDATE())")) {
            ps.setInt(1, userId); ResultSet rs = ps.executeQuery(); return rs.next()?rs.getInt(1):0;
        } catch (SQLException e) { return 0; }
    }
}
