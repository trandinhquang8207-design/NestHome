package com.dormassist.dao;

import com.dormassist.config.Databaseconfig;
import com.dormassist.model.Student;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public List<Student> getAll() {
        List<Student> l = new ArrayList<>();
        String sql = "SELECT s.*, r.room_number FROM students s LEFT JOIN rooms r ON s.room_id=r.id ORDER BY CASE s.status WHEN 'ACTIVE' THEN 1 WHEN 'TEMPORARY_ABSENT' THEN 2 WHEN 'MOVED_OUT' THEN 3 ELSE 9 END, CASE WHEN s.room_id IS NULL THEN 2 ELSE 1 END, ISNULL(r.room_number,'ZZZ'), s.full_name";
        try (Connection c = Databaseconfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) l.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return l;
    }

    public Student getById(int id) {
        String sql = "SELECT s.*, r.room_number FROM students s LEFT JOIN rooms r ON s.room_id=r.id WHERE s.id=?";
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Student getByUserId(int userId) {
        String sql = "SELECT s.*, r.room_number FROM students s LEFT JOIN rooms r ON s.room_id=r.id WHERE s.user_id=?";
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean insert(Student s) {
        String sql = "INSERT INTO students(user_id,full_name,student_code,id_card,dob,gender,phone,email," +
                     "hometown,room_id,join_date,expected_leave_date,status,discipline_points,notes) " +
                     "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, s.getUserId() > 0 ? s.getUserId() : null);
            ps.setString(2, s.getFullName());
            ps.setString(3, s.getStudentCode());
            ps.setString(4, s.getIdCard());
            ps.setObject(5, s.getDob() != null ? new Date(s.getDob().getTime()) : null);
            ps.setString(6, s.getGender());
            ps.setString(7, s.getPhone());
            ps.setString(8, s.getEmail());
            ps.setString(9, s.getHometown());
            ps.setObject(10, s.getRoomId() > 0 ? s.getRoomId() : null);
            ps.setObject(11, s.getJoinDate() != null ? new Date(s.getJoinDate().getTime()) : null);
            ps.setObject(12, s.getExpectedLeaveDate() != null ? new Date(s.getExpectedLeaveDate().getTime()) : null);
            ps.setString(13, s.getStatus() != null ? s.getStatus() : "ACTIVE");
            ps.setInt(14, 100);
            ps.setString(15, s.getNotes());
            boolean ok = ps.executeUpdate() > 0;
            if (ok && s.getRoomId() > 0) new RoomDAO().updateOccupants(s.getRoomId());
            return ok;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Student s) {
        String sql = "UPDATE students SET full_name=?,student_code=?,id_card=?,dob=?,gender=?," +
                     "phone=?,email=?,hometown=?,room_id=?,join_date=?,expected_leave_date=?," +
                     "status=?,notes=? WHERE id=?";
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getFullName());
            ps.setString(2, s.getStudentCode());
            ps.setString(3, s.getIdCard());
            ps.setObject(4, s.getDob() != null ? new Date(s.getDob().getTime()) : null);
            ps.setString(5, s.getGender());
            ps.setString(6, s.getPhone());
            ps.setString(7, s.getEmail());
            ps.setString(8, s.getHometown());
            ps.setObject(9, s.getRoomId() > 0 ? s.getRoomId() : null);
            ps.setObject(10, s.getJoinDate() != null ? new Date(s.getJoinDate().getTime()) : null);
            ps.setObject(11, s.getExpectedLeaveDate() != null ? new Date(s.getExpectedLeaveDate().getTime()) : null);
            ps.setString(12, s.getStatus());
            ps.setString(13, s.getNotes());
            ps.setInt(14, s.getId());
            boolean ok = ps.executeUpdate() > 0;
            if (ok && s.getRoomId() > 0) new RoomDAO().updateOccupants(s.getRoomId());
            return ok;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateDisciplinePoints(int id, int delta) {
        String sql = "UPDATE students SET discipline_points = CASE " +
                     "WHEN discipline_points + ? < 0 THEN 0 " +
                     "WHEN discipline_points + ? > 100 THEN 100 " +
                     "ELSE discipline_points + ? END WHERE id=?";
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, delta);
            ps.setInt(3, delta);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        // Giữ tên hàm delete để code cũ không bị lỗi,
        // nhưng bên trong không xóa cứng nữa.
        return moveOutAndLockAccount(id, -1);
    }

    public boolean moveOutAndLockAccount(int studentId, int processedBy) {
        String selectSql =
                "SELECT id, user_id, room_id, status, notes " +
                        "FROM students WHERE id=?";

        String updateStudentSql =
                "UPDATE students SET " +
                        "status='MOVED_OUT', " +
                        "room_id=NULL, " +
                        "expected_leave_date = CASE " +
                        "   WHEN expected_leave_date IS NOT NULL THEN expected_leave_date " +
                        "   WHEN join_date IS NOT NULL AND join_date > CAST(GETDATE() AS DATE) THEN join_date " +
                        "   ELSE CAST(GETDATE() AS DATE) " +
                        "END, " +
                        "notes=? " +
                        "WHERE id=?";

        String lockUserSql =
                "UPDATE users SET is_active=0 " +
                        "WHERE id=? AND role='BASE3'";

        String updateOldRoomSql =
                "UPDATE rooms SET current_occupants = (" +
                        "   SELECT COUNT(*) FROM students " +
                        "   WHERE room_id=? AND status='ACTIVE'" +
                        ") WHERE id=?";

        try (Connection c = Databaseconfig.getConnection()) {
            c.setAutoCommit(false);

            try {
                Integer linkedUserId = null;
                Integer oldRoomId = null;
                String oldNotes = null;

                try (PreparedStatement ps = c.prepareStatement(selectSql)) {
                    ps.setInt(1, studentId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            c.rollback();
                            return false;
                        }

                        int tmpUserId = rs.getInt("user_id");
                        if (!rs.wasNull()) linkedUserId = tmpUserId;

                        int tmpRoomId = rs.getInt("room_id");
                        if (!rs.wasNull()) oldRoomId = tmpRoomId;

                        oldNotes = rs.getString("notes");
                    }
                }

                String systemNote = "Hồ sơ đã được chuyển sang trạng thái Đã chuyển đi. "
                        + "Tài khoản liên kết, nếu có, đã bị khóa.";

                String newNotes;
                if (oldNotes == null || oldNotes.trim().isEmpty()) {
                    newNotes = systemNote;
                } else if (oldNotes.contains(systemNote)) {
                    newNotes = oldNotes;
                } else {
                    newNotes = oldNotes + "\n" + systemNote;
                }

                try (PreparedStatement ps = c.prepareStatement(updateStudentSql)) {
                    ps.setString(1, newNotes);
                    ps.setInt(2, studentId);

                    if (ps.executeUpdate() == 0) {
                        c.rollback();
                        return false;
                    }
                }

                if (linkedUserId != null) {
                    try (PreparedStatement ps = c.prepareStatement(lockUserSql)) {
                        ps.setInt(1, linkedUserId);
                        ps.executeUpdate();
                    }
                }

                if (oldRoomId != null) {
                    try (PreparedStatement ps = c.prepareStatement(updateOldRoomSql)) {
                        ps.setInt(1, oldRoomId);
                        ps.setInt(2, oldRoomId);
                        ps.executeUpdate();
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

    public int countTotal() {
        try (Connection c = Databaseconfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students WHERE status='ACTIVE'")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private Student map(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        try { s.setUserId(rs.getInt("user_id")); } catch (Exception ignored) {}
        s.setFullName(rs.getString("full_name"));
        s.setStudentCode(rs.getString("student_code"));
        s.setIdCard(rs.getString("id_card"));
        s.setGender(rs.getString("gender"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setHometown(rs.getString("hometown"));
        s.setStatus(rs.getString("status"));
        s.setNotes(rs.getString("notes"));
        s.setDisciplinePoints(rs.getInt("discipline_points"));
        try {
            s.setRoomId(rs.getInt("room_id"));
            s.setRoomNumber(rs.getString("room_number"));
        } catch (Exception ignored) {}

        // Dùng java.sql.Date rõ ràng, sau đó convert sang java.util.Date
        Date dob = rs.getDate("dob");
        if (dob != null) s.setDob(new java.util.Date(dob.getTime()));

        Date jd = rs.getDate("join_date");
        if (jd != null) s.setJoinDate(new java.util.Date(jd.getTime()));

        Date ed = rs.getDate("expected_leave_date");
        if (ed != null) s.setExpectedLeaveDate(new java.util.Date(ed.getTime()));

        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) s.setCreatedAt(new java.util.Date(ca.getTime()));

        return s;
    }
}
