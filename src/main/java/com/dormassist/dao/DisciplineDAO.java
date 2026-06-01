package com.dormassist.dao;

import com.dormassist.config.Databaseconfig;
import com.dormassist.model.*;
import java.sql.*;
import java.security.SecureRandom;
import java.util.*;

public class DisciplineDAO {
    public List<DisciplinePoint> getAll(){
        List<DisciplinePoint> l=new ArrayList<>();
        try(Connection c=Databaseconfig.getConnection();Statement st=c.createStatement();
            ResultSet rs=st.executeQuery("SELECT d.*,s.full_name AS student_name,s.discipline_points AS current_score,r.room_number,u.full_name AS creator_name FROM discipline_points d LEFT JOIN students s ON d.student_id=s.id LEFT JOIN rooms r ON s.room_id=r.id LEFT JOIN users u ON d.created_by=u.id ORDER BY ISNULL(r.room_number,'ZZZ'), s.discipline_points ASC, s.full_name ASC, d.created_at DESC")){
            while(rs.next())l.add(map(rs));
        }catch(SQLException e){e.printStackTrace();}
        return l;
    }
    public List<DisciplinePoint> getByStudent(int studentId){
        List<DisciplinePoint> l=new ArrayList<>();
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(
                "SELECT d.*,s.full_name AS student_name,s.discipline_points AS current_score,r.room_number,u.full_name AS creator_name FROM discipline_points d LEFT JOIN students s ON d.student_id=s.id LEFT JOIN rooms r ON s.room_id=r.id LEFT JOIN users u ON d.created_by=u.id WHERE d.student_id=? ORDER BY d.created_at DESC")){
            ps.setInt(1,studentId);ResultSet rs=ps.executeQuery();while(rs.next())l.add(map(rs));
        }catch(SQLException e){e.printStackTrace();}
        return l;
    }
    public boolean insert(DisciplinePoint dp){
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(
                "INSERT INTO discipline_points(student_id,points,type,reason,detail,created_by) VALUES(?,?,?,?,?,?)")){
            ps.setInt(1,dp.getStudentId());ps.setInt(2,dp.getPoints());ps.setString(3,dp.getType());
            ps.setString(4,dp.getReason());ps.setString(5,dp.getDetail());ps.setInt(6,dp.getCreatedBy());
            boolean ok=ps.executeUpdate()>0;
            if(ok){int delta="BONUS".equals(dp.getType())?dp.getPoints():-dp.getPoints();new StudentDAO().updateDisciplinePoints(dp.getStudentId(),delta);}
            return ok;
        }catch(SQLException e){e.printStackTrace();return false;}
    }
    public boolean delete(int id){
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement("DELETE FROM discipline_points WHERE id=?")){
            ps.setInt(1,id);return ps.executeUpdate()>0;
        }catch(SQLException e){e.printStackTrace();return false;}
    }
    private DisciplinePoint map(ResultSet rs) throws SQLException {
        DisciplinePoint d=new DisciplinePoint();
        d.setId(rs.getInt("id"));d.setStudentId(rs.getInt("student_id"));d.setPoints(rs.getInt("points"));
        d.setType(rs.getString("type"));d.setReason(rs.getString("reason"));d.setDetail(rs.getString("detail"));
        d.setCreatedBy(rs.getInt("created_by"));
        try{d.setStudentName(rs.getString("student_name"));d.setCreatorName(rs.getString("creator_name"));}catch(Exception ignored){}
        try{d.setRoomNumber(rs.getString("room_number"));d.setCurrentScore(rs.getInt("current_score"));}catch(Exception ignored){}
        Timestamp ca=rs.getTimestamp("created_at");if(ca!=null)d.setCreatedAt(new java.util.Date(ca.getTime()));
        return d;
    }
}
