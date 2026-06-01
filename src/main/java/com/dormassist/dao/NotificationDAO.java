package com.dormassist.dao;

import com.dormassist.config.Databaseconfig;
import com.dormassist.model.*;
import java.sql.*;
import java.security.SecureRandom;
import java.util.*;

public class NotificationDAO {
    public List<Notification> getAll(){
        List<Notification> l=new ArrayList<>();
        try(Connection c=Databaseconfig.getConnection();Statement st=c.createStatement();
            ResultSet rs=st.executeQuery("SELECT n.*,u.full_name AS sender_name FROM notifications n LEFT JOIN users u ON n.sender_id=u.id ORDER BY CASE WHEN n.is_important=1 THEN 1 ELSE 2 END, n.created_at DESC")){
            while(rs.next())l.add(map(rs));
        }catch(SQLException e){e.printStackTrace();}
        return l;
    }
    /** Lấy mới nhất cho dashboard - đa luồng */
    public List<Notification> getLatest(int limit){
        List<Notification> l=new ArrayList<>();
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(
                "SELECT TOP (?) n.*,u.full_name AS sender_name FROM notifications n LEFT JOIN users u ON n.sender_id=u.id ORDER BY CASE WHEN n.is_important=1 THEN 1 ELSE 2 END, n.created_at DESC")){
            ps.setInt(1,limit);ResultSet rs=ps.executeQuery();while(rs.next())l.add(map(rs));
        }catch(SQLException e){e.printStackTrace();}
        return l;
    }
    public boolean insert(Notification n){
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(
                "INSERT INTO notifications(sender_id,title,content,target_role,target_room_id,is_important) VALUES(?,?,?,?,?,?)")){
            ps.setInt(1,n.getSenderId());ps.setString(2,n.getTitle());ps.setString(3,n.getContent());
            ps.setString(4,n.getTargetRole());ps.setObject(5,n.getTargetRoomId()>0?n.getTargetRoomId():null);
            ps.setBoolean(6,n.isImportant());return ps.executeUpdate()>0;
        }catch(SQLException e){e.printStackTrace();return false;}
    }
    public boolean delete(int id){
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement("DELETE FROM notifications WHERE id=?")){
            ps.setInt(1,id);return ps.executeUpdate()>0;
        }catch(SQLException e){e.printStackTrace();return false;}
    }
    private Notification map(ResultSet rs) throws SQLException {
        Notification n=new Notification();
        n.setId(rs.getInt("id"));n.setSenderId(rs.getInt("sender_id"));
        try{n.setSenderName(rs.getString("sender_name"));}catch(Exception ignored){}
        n.setTitle(rs.getString("title"));n.setContent(rs.getString("content"));
        n.setTargetRole(rs.getString("target_role"));n.setImportant(rs.getBoolean("is_important"));
        Timestamp ca=rs.getTimestamp("created_at");if(ca!=null)n.setCreatedAt(new java.util.Date(ca.getTime()));
        return n;
    }
}
