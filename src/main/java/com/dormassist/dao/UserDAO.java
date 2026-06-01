package com.dormassist.dao;

import com.dormassist.config.Databaseconfig;
import com.dormassist.model.User;
import com.dormassist.config.AppConstants;

import java.io.ByteArrayInputStream;

import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public static String hashPassword(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plain.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return plain; }
    }

    public boolean verifyPassword(String plain, String hash) {
        return hashPassword(plain).equals(hash);
    }

    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username=? AND is_active=1";
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String stored = rs.getString("password_hash");
                if (verifyPassword(password, stored)) {
                    User u = mapUser(rs);
                    try (PreparedStatement upd = c.prepareStatement("UPDATE users SET last_login=GETDATE() WHERE id=?")) {
                        upd.setInt(1, u.getId()); upd.executeUpdate();
                    }
                    return u;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean register(String username, String password, String fullName, String email,
                            String phone, String role, String tokenCode, String studentCode) {

        String checkTokenSql =
                "SELECT id FROM tokens WHERE token_code=? AND role=? AND is_used=0";

        String insertUserSql =
                "INSERT INTO users(username,password_hash,role,full_name,email,phone,token_used) " +
                        "OUTPUT INSERTED.id " +
                        "VALUES(?,?,?,?,?,?,?)";

        String findWaitingStudentSql =
                "SELECT TOP 1 id " +
                        "FROM students " +
                        "WHERE user_id IS NULL AND ( " +
                        "   (? IS NOT NULL AND student_code = ?) " +
                        "   OR (? IS NOT NULL AND email = ?) " +
                        "   OR (? IS NOT NULL AND phone = ?) " +
                        ") " +
                        "ORDER BY " +
                        "   CASE " +
                        "       WHEN (? IS NOT NULL AND student_code = ?) THEN 1 " +
                        "       WHEN (? IS NOT NULL AND email = ?) THEN 2 " +
                        "       WHEN (? IS NOT NULL AND phone = ?) THEN 3 " +
                        "       ELSE 9 " +
                        "   END, id ASC";

        String linkStudentSql =
                "UPDATE students SET " +
                        "user_id=?, " +
                        "full_name=?, " +
                        "student_code=CASE WHEN student_code IS NULL OR LTRIM(RTRIM(student_code))='' THEN ? ELSE student_code END, " +
                        "email=CASE WHEN email IS NULL OR LTRIM(RTRIM(email))='' THEN ? ELSE email END, " +
                        "phone=CASE WHEN phone IS NULL OR LTRIM(RTRIM(phone))='' THEN ? ELSE phone END, " +
                        "status=CASE WHEN status IS NULL OR LTRIM(RTRIM(status))='' THEN 'ACTIVE' ELSE status END " +
                        "WHERE id=? AND user_id IS NULL";

        String insertStudentSql =
                "INSERT INTO students(user_id,full_name,student_code,id_card,dob,gender,phone,email," +
                        "hometown,room_id,join_date,expected_leave_date,status,discipline_points,notes) " +
                        "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        String markTokenSql =
                "UPDATE tokens SET is_used=1, used_by=?, used_at=GETDATE() " +
                        "WHERE token_code=? AND role=? AND is_used=0";

        String cleanEmail = nullIfBlank(email);
        String cleanPhone = nullIfBlank(phone);
        String cleanStudentCode = nullIfBlank(studentCode);

        try (Connection c = Databaseconfig.getConnection()) {
            c.setAutoCommit(false);

            try {
                // 1. Kiểm tra token đúng vai trò và chưa sử dụng
                try (PreparedStatement ps = c.prepareStatement(checkTokenSql)) {
                    ps.setString(1, tokenCode);
                    ps.setString(2, role);

                    if (!ps.executeQuery().next()) {
                        c.rollback();
                        return false;
                    }
                }

                // 2. Tạo tài khoản user và lấy id vừa tạo
                int newUserId;

                try (PreparedStatement ps = c.prepareStatement(insertUserSql)) {
                    ps.setString(1, username);
                    ps.setString(2, hashPassword(password));
                    ps.setString(3, role);
                    ps.setString(4, fullName);
                    ps.setString(5, cleanEmail);
                    ps.setString(6, cleanPhone);
                    ps.setString(7, tokenCode);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            c.rollback();
                            return false;
                        }
                        newUserId = rs.getInt(1);
                    }
                }

                // 3. Nếu là sinh viên thì đồng bộ với hồ sơ sinh viên đã có hoặc tạo mới
                if (AppConstants.ROLE_BASE3.equals(role)) {
                    Integer waitingStudentId = null;

                    try (PreparedStatement ps = c.prepareStatement(findWaitingStudentSql)) {
                        ps.setString(1, cleanStudentCode);
                        ps.setString(2, cleanStudentCode);

                        ps.setString(3, cleanEmail);
                        ps.setString(4, cleanEmail);

                        ps.setString(5, cleanPhone);
                        ps.setString(6, cleanPhone);

                        ps.setString(7, cleanStudentCode);
                        ps.setString(8, cleanStudentCode);

                        ps.setString(9, cleanEmail);
                        ps.setString(10, cleanEmail);

                        ps.setString(11, cleanPhone);
                        ps.setString(12, cleanPhone);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                waitingStudentId = rs.getInt("id");
                            }
                        }
                    }

                    if (waitingStudentId != null) {
                        // Có hồ sơ sinh viên do admin tạo trước -> gắn tài khoản vào hồ sơ cũ
                        try (PreparedStatement ps = c.prepareStatement(linkStudentSql)) {
                            ps.setInt(1, newUserId);
                            ps.setString(2, fullName);
                            ps.setString(3, cleanStudentCode);
                            ps.setString(4, cleanEmail);
                            ps.setString(5, cleanPhone);
                            ps.setInt(6, waitingStudentId);

                            if (ps.executeUpdate() == 0) {
                                c.rollback();
                                return false;
                            }
                        }
                    } else {
                        // Chưa có hồ sơ sinh viên -> tạo hồ sơ mới
                        try (PreparedStatement ps = c.prepareStatement(insertStudentSql)) {
                            ps.setInt(1, newUserId);
                            ps.setString(2, fullName);
                            ps.setString(3, cleanStudentCode);
                            ps.setNull(4, Types.VARCHAR);
                            ps.setNull(5, Types.DATE);
                            ps.setString(6, "OTHER");
                            ps.setString(7, cleanPhone);
                            ps.setString(8, cleanEmail);
                            ps.setNull(9, Types.NVARCHAR);
                            ps.setNull(10, Types.INTEGER);
                            ps.setDate(11, new java.sql.Date(System.currentTimeMillis()));
                            ps.setNull(12, Types.DATE);
                            ps.setString(13, "ACTIVE");
                            ps.setInt(14, 100);
                            ps.setString(15, "Hồ sơ được tạo tự động khi sinh viên đăng ký tài khoản.");

                            ps.executeUpdate();
                        }
                    }
                }

                // 4. Đánh dấu token đã dùng
                try (PreparedStatement ps = c.prepareStatement(markTokenSql)) {
                    ps.setInt(1, newUserId);
                    ps.setString(2, tokenCode);
                    ps.setString(3, role);

                    if (ps.executeUpdate() == 0) {
                        c.rollback();
                        return false;
                    }
                }

                c.commit();
                return true;

            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createUser(String username, String password, String fullName, String email, String phone, String role) {
        String sql = "INSERT INTO users(username,password_hash,role,full_name,email,phone) VALUES(?,?,?,?,?,?)";
        try (Connection c = Databaseconfig.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1,username); ps.setString(2,hashPassword(password));
            ps.setString(3,role); ps.setString(4,fullName); ps.setString(5,email); ps.setString(6,phone);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        try (Connection c = Databaseconfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users ORDER BY CASE role WHEN 'ADMIN_SUPER' THEN 1 WHEN 'ADMIN_BASE' THEN 1 WHEN 'BASE2' THEN 2 WHEN 'BASE1' THEN 3 WHEN 'BASE3' THEN 4 ELSE 9 END, CASE WHEN is_active=1 THEN 1 ELSE 2 END, full_name, username")) {
            while (rs.next()) list.add(mapUser(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public User getById(int id) {
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE id=?")) {
            ps.setInt(1,id); ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }


    public User getByEmail(String email) {
        String sql = "SELECT * FROM users WHERE LOWER(email)=LOWER(?) AND is_active=1";
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean verifyPasswordById(int userId, String plainPassword) {
        String sql = "SELECT password_hash FROM users WHERE id=? AND is_active=1";
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && verifyPassword(plainPassword, rs.getString("password_hash"));
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean changePasswordWithOldPassword(int userId, String oldPass, String newPass) {
        if (!verifyPasswordById(userId, oldPass)) return false;
        return changePassword(userId, newPass);
    }

    public boolean usernameExists(String username) {
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id FROM users WHERE username=?")) {
            ps.setString(1,username); return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public boolean update(User u) {
        String sql = "UPDATE users SET full_name=?,email=?,phone=?,role=?,is_active=? WHERE id=?";
        try (Connection c = Databaseconfig.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1,u.getFullName()); ps.setString(2,u.getEmail()); ps.setString(3,u.getPhone());
            ps.setString(4,u.getRole()); ps.setBoolean(5,u.isActive()); ps.setInt(6,u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean changePassword(int userId, String newPass) {
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET password_hash=? WHERE id=?")) {
            ps.setString(1,hashPassword(newPass)); ps.setInt(2,userId); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean setActive(int id, boolean active) {
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET is_active=? WHERE id=?")) {
            ps.setBoolean(1,active); ps.setInt(2,id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public void log(int userId, String action, String module, String details) {
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO activity_logs(user_id,action,module,details) VALUES(?,?,?,?)")) {
            ps.setInt(1,userId); ps.setString(2,action); ps.setString(3,module); ps.setString(4,details);
            ps.executeUpdate();
        } catch (SQLException e) { /* best effort */ }
    }

    private String nullIfBlank(String value) {
        if (value == null) return null;
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    public boolean updateAvatar(int userId, byte[] avatarBytes) {
        String sql =
                "MERGE user_avatars AS target " +
                        "USING (SELECT ? AS user_id) AS source " +
                        "ON target.user_id = source.user_id " +
                        "WHEN MATCHED THEN " +
                        "    UPDATE SET avatar_data=?, content_type='image/png', updated_at=GETDATE() " +
                        "WHEN NOT MATCHED THEN " +
                        "    INSERT (user_id, avatar_data, content_type, updated_at) " +
                        "    VALUES (?, ?, 'image/png', GETDATE());";

        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ps.setBytes(2, avatarBytes);

            ps.setInt(3, userId);
            ps.setBytes(4, avatarBytes);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public byte[] getAvatar(int userId) {
        String sql = "SELECT avatar_data FROM user_avatars WHERE user_id=?";

        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("avatar_data");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id")); u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash")); u.setRole(rs.getString("role"));
        u.setFullName(rs.getString("full_name")); u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone")); u.setActive(rs.getBoolean("is_active"));
        u.setTokenUsed(rs.getString("token_used"));
        Timestamp ll = rs.getTimestamp("last_login"); if(ll!=null) u.setLastLogin(new java.util.Date(ll.getTime()));
        Timestamp ca = rs.getTimestamp("created_at"); if(ca!=null) u.setCreatedAt(new java.util.Date(ca.getTime()));
        return u;
    }
}
