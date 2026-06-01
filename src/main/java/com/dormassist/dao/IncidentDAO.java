package com.dormassist.dao;

import com.dormassist.config.Databaseconfig;
import com.dormassist.model.Incident;
import java.sql.*;
import java.util.*;

public class IncidentDAO {
    private static final String BASE = "SELECT i.*,u.full_name AS reporter_name,r.room_number,a.full_name AS assignee_name FROM incidents i LEFT JOIN users u ON i.reporter_id=u.id LEFT JOIN rooms r ON i.room_id=r.id LEFT JOIN users a ON i.assigned_to=a.id";

    public List<Incident> getAll(){
        List<Incident> l=new ArrayList<>();
        try(Connection c=Databaseconfig.getConnection();Statement st=c.createStatement();
            ResultSet rs=st.executeQuery(BASE+" ORDER BY CASE i.priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 ELSE 4 END, CASE i.status WHEN 'PENDING' THEN 1 WHEN 'IN_PROGRESS' THEN 2 WHEN 'RESOLVED' THEN 3 WHEN 'CANCELLED' THEN 4 ELSE 9 END, i.created_at DESC")){
            while(rs.next())l.add(map(rs));
        }catch(SQLException e){e.printStackTrace();}
        return l;
    }
    public List<Incident> getByReporter(int userId){
        List<Incident> l=new ArrayList<>();
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(BASE+" WHERE i.reporter_id=? ORDER BY CASE i.priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 ELSE 4 END, CASE i.status WHEN 'PENDING' THEN 1 WHEN 'IN_PROGRESS' THEN 2 WHEN 'RESOLVED' THEN 3 ELSE 9 END, i.created_at DESC")){
            ps.setInt(1,userId);ResultSet rs=ps.executeQuery();while(rs.next())l.add(map(rs));
        }catch(SQLException e){e.printStackTrace();}
        return l;
    }
    /** Lấy mới nhất cho dashboard - đa luồng */
    public List<Incident> getLatest(int limit){
        List<Incident> l=new ArrayList<>();
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(
                "SELECT TOP (?) " + BASE.substring(7) + " WHERE i.status != 'RESOLVED' ORDER BY CASE i.priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 ELSE 4 END, i.created_at DESC")){
            ps.setInt(1,limit);ResultSet rs=ps.executeQuery();while(rs.next())l.add(map(rs));
        }catch(SQLException e){e.printStackTrace();}
        return l;
    }
    public boolean insert(Incident i){
        String sql="INSERT INTO incidents(reporter_id,room_id,title,description,priority,status) VALUES(?,?,?,?,?,?)";
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,i.getReporterId());ps.setObject(2,i.getRoomId()>0?i.getRoomId():null);
            ps.setString(3,i.getTitle());ps.setString(4,i.getDescription());
            ps.setString(5,i.getPriority());ps.setString(6,"PENDING");
            return ps.executeUpdate()>0;
        }catch(SQLException e){e.printStackTrace();return false;}
    }
    public boolean updateStatus(int id,String status,int assignedTo,String resolution){
        String sql="UPDATE incidents SET status=?,assigned_to=?,resolution_notes=?,resolved_date=? WHERE id=?";
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setString(1,status);ps.setObject(2,assignedTo>0?assignedTo:null);
            ps.setString(3,resolution);
            ps.setObject(4,"RESOLVED".equals(status)?new Timestamp(System.currentTimeMillis()):null);
            ps.setInt(5,id);return ps.executeUpdate()>0;
        }catch(SQLException e){e.printStackTrace();return false;}
    }
    /** Xác nhận đã sửa chữa xong */
    public boolean markResolved(int id, int userId, String notes){
        return updateStatus(id,"RESOLVED",userId,notes);
    }
    public boolean delete(int id){
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement("DELETE FROM incidents WHERE id=?")){
            ps.setInt(1,id);return ps.executeUpdate()>0;
        }catch(SQLException e){e.printStackTrace();return false;}
    }
    public int countPending(){
        try(Connection c=Databaseconfig.getConnection();Statement st=c.createStatement();
            ResultSet rs=st.executeQuery("SELECT COUNT(*) FROM incidents WHERE status='PENDING'")){
            if(rs.next())return rs.getInt(1);
        }catch(SQLException e){e.printStackTrace();}
        return 0;
    }
    private Incident map(ResultSet rs) throws SQLException {
        Incident i=new Incident();
        i.setId(rs.getInt("id"));i.setReporterId(rs.getInt("reporter_id"));
        try{i.setReporterName(rs.getString("reporter_name"));i.setRoomNumber(rs.getString("room_number"));i.setAssigneeName(rs.getString("assignee_name"));}catch(Exception ignored){}
        i.setRoomId(rs.getInt("room_id"));i.setTitle(rs.getString("title"));
        i.setDescription(rs.getString("description"));i.setPriority(rs.getString("priority"));
        i.setStatus(rs.getString("status"));i.setResolutionNotes(rs.getString("resolution_notes"));
        i.setAssignedTo(rs.getInt("assigned_to"));
        Timestamp ca=rs.getTimestamp("created_at");if(ca!=null)i.setCreatedAt(new java.util.Date(ca.getTime()));
        return i;
    }
}
