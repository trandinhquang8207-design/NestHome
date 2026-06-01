package com.dormassist.dao;

import com.dormassist.config.Databaseconfig;
import com.dormassist.model.*;
import java.sql.*;
import java.security.SecureRandom;
import java.util.*;

public class AssetDAO {
    public List<Asset> getAll(){
        List<Asset> l=new ArrayList<>();
        try(Connection c=Databaseconfig.getConnection();Statement st=c.createStatement();
            ResultSet rs=st.executeQuery("SELECT a.*,r.room_number FROM assets a LEFT JOIN rooms r ON a.room_id=r.id ORDER BY ISNULL(r.room_number,'ZZZ'), a.category, a.asset_name, CASE a.condition_status WHEN 'BROKEN' THEN 1 WHEN 'FAIR' THEN 2 WHEN 'GOOD' THEN 3 ELSE 9 END")){
            while(rs.next())l.add(map(rs));
        }catch(SQLException e){e.printStackTrace();}
        return l;
    }
    public boolean insert(Asset a){
        String sql="INSERT INTO assets(room_id,asset_name,asset_code,category,quantity,condition_status,purchase_price,notes) VALUES(?,?,?,?,?,?,?,?)";
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setObject(1,a.getRoomId()>0?a.getRoomId():null);ps.setString(2,a.getAssetName());
            ps.setString(3,a.getAssetCode());ps.setString(4,a.getCategory());ps.setInt(5,a.getQuantity());
            ps.setString(6,a.getConditionStatus());ps.setDouble(7,a.getPurchasePrice());ps.setString(8,a.getNotes());
            return ps.executeUpdate()>0;
        }catch(SQLException e){e.printStackTrace();return false;}
    }
    public boolean update(Asset a){
        String sql="UPDATE assets SET room_id=?,asset_name=?,asset_code=?,category=?,quantity=?,condition_status=?,purchase_price=?,notes=? WHERE id=?";
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setObject(1,a.getRoomId()>0?a.getRoomId():null);ps.setString(2,a.getAssetName());
            ps.setString(3,a.getAssetCode());ps.setString(4,a.getCategory());ps.setInt(5,a.getQuantity());
            ps.setString(6,a.getConditionStatus());ps.setDouble(7,a.getPurchasePrice());
            ps.setString(8,a.getNotes());ps.setInt(9,a.getId());return ps.executeUpdate()>0;
        }catch(SQLException e){e.printStackTrace();return false;}
    }
    public boolean delete(int id){
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement("DELETE FROM assets WHERE id=?")){
            ps.setInt(1,id);return ps.executeUpdate()>0;
        }catch(SQLException e){e.printStackTrace();return false;}
    }
    private Asset map(ResultSet rs) throws SQLException {
        Asset a=new Asset();
        a.setId(rs.getInt("id"));a.setRoomId(rs.getInt("room_id"));
        try{a.setRoomNumber(rs.getString("room_number"));}catch(Exception ignored){}
        a.setAssetName(rs.getString("asset_name"));a.setAssetCode(rs.getString("asset_code"));
        a.setCategory(rs.getString("category"));a.setQuantity(rs.getInt("quantity"));
        a.setConditionStatus(rs.getString("condition_status"));a.setNotes(rs.getString("notes"));
        a.setPurchasePrice(rs.getDouble("purchase_price"));return a;
    }
}
