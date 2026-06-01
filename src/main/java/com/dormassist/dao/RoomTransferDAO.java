package com.dormassist.dao;

import com.dormassist.config.Databaseconfig;
import com.dormassist.model.RoomTransferRequest;
import java.sql.*;
import java.util.*;

public class RoomTransferDAO {
    private static final String BASE =
        "SELECT rt.*, s.full_name AS student_name, r1.room_number AS from_room_number, r2.room_number AS to_room_number " +
        "FROM room_transfer_requests rt " +
        "LEFT JOIN students s ON rt.student_id=s.id " +
        "LEFT JOIN rooms r1 ON rt.from_room_id=r1.id " +
        "LEFT JOIN rooms r2 ON rt.to_room_id=r2.id";

    public List<RoomTransferRequest> getAll(){
        List<RoomTransferRequest> l=new ArrayList<>();
        try(Connection c=Databaseconfig.getConnection();Statement st=c.createStatement();
            ResultSet rs=st.executeQuery(BASE+" ORDER BY CASE rt.status WHEN 'PENDING' THEN 1 WHEN 'APPROVED' THEN 2 WHEN 'REJECTED' THEN 3 ELSE 4 END, rt.created_at DESC")){
            while(rs.next())l.add(map(rs));
        }catch(SQLException e){e.printStackTrace();}
        return l;
    }

    public List<RoomTransferRequest> getByStudent(int studentId){
        List<RoomTransferRequest> l=new ArrayList<>();
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(BASE+" WHERE rt.student_id=? ORDER BY CASE rt.status WHEN 'PENDING' THEN 1 WHEN 'APPROVED' THEN 2 WHEN 'REJECTED' THEN 3 ELSE 4 END, rt.created_at DESC")){
            ps.setInt(1,studentId);ResultSet rs=ps.executeQuery();while(rs.next())l.add(map(rs));
        }catch(SQLException e){e.printStackTrace();}
        return l;
    }

    public boolean insert(RoomTransferRequest r){
        String sql="INSERT INTO room_transfer_requests(student_id,from_room_id,to_room_id,reason,status) VALUES(?,?,?,?,?)";
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,r.getStudentId());
            ps.setObject(2,r.getFromRoomId()>0?r.getFromRoomId():null);
            ps.setObject(3,r.getToRoomId()>0?r.getToRoomId():null);
            ps.setString(4,r.getReason());ps.setString(5,"PENDING");
            return ps.executeUpdate()>0;
        }catch(SQLException e){e.printStackTrace();return false;}
    }

    public boolean updateStatus(int id,String status,int processedBy,String notes){
        String sql="UPDATE room_transfer_requests SET status=?,processed_by=?,admin_notes=?,processed_at=GETDATE() WHERE id=?";
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setString(1,status);ps.setInt(2,processedBy);ps.setString(3,notes);ps.setInt(4,id);
            return ps.executeUpdate()>0;
        }catch(SQLException e){e.printStackTrace();return false;}
    }

    public boolean delete(int id){
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement("DELETE FROM room_transfer_requests WHERE id=?")){
            ps.setInt(1,id);return ps.executeUpdate()>0;
        }catch(SQLException e){e.printStackTrace();return false;}
    }

    private RoomTransferRequest map(ResultSet rs) throws SQLException {
        RoomTransferRequest r=new RoomTransferRequest();
        r.setId(rs.getInt("id"));r.setStudentId(rs.getInt("student_id"));
        try{r.setStudentName(rs.getString("student_name"));}catch(Exception ignored){}
        r.setFromRoomId(rs.getInt("from_room_id"));r.setToRoomId(rs.getInt("to_room_id"));
        try{r.setFromRoomNumber(rs.getString("from_room_number"));r.setToRoomNumber(rs.getString("to_room_number"));}catch(Exception ignored){}
        r.setReason(rs.getString("reason"));r.setStatus(rs.getString("status"));
        r.setAdminNotes(rs.getString("admin_notes"));r.setProcessedBy(rs.getInt("processed_by"));
        Timestamp ca=rs.getTimestamp("created_at");if(ca!=null)r.setCreatedAt(new java.util.Date(ca.getTime()));
        Timestamp pa=rs.getTimestamp("processed_at");if(pa!=null)r.setProcessedAt(new java.util.Date(pa.getTime()));
        return r;
    }
}
