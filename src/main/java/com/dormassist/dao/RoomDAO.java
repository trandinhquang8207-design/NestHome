package com.dormassist.dao;

import com.dormassist.config.Databaseconfig;
import com.dormassist.model.*;

import java.sql.*;
import java.util.*;

public class RoomDAO {
    public List<Room> getAll() {
        List<Room> list=new ArrayList<>();
        String sql="SELECT r.*,b.name AS building_name FROM rooms r LEFT JOIN buildings b ON r.building_id=b.id ORDER BY CASE r.status WHEN 'AVAILABLE' THEN 1 WHEN 'FULL' THEN 2 WHEN 'MAINTENANCE' THEN 3 WHEN 'CLOSED' THEN 4 ELSE 9 END, ISNULL(b.name,''), r.floor, r.room_number";
        try(Connection c=Databaseconfig.getConnection();Statement st=c.createStatement();ResultSet rs=st.executeQuery(sql)){
            while(rs.next()) list.add(map(rs));
        }catch(SQLException e){e.printStackTrace();}
        return list;
    }
    public List<Room> getAvailable() {
        List<Room> list=new ArrayList<>();
        String sql="SELECT r.*,b.name AS building_name FROM rooms r LEFT JOIN buildings b ON r.building_id=b.id WHERE r.status='AVAILABLE' AND r.current_occupants<r.capacity ORDER BY CASE r.status WHEN 'AVAILABLE' THEN 1 WHEN 'FULL' THEN 2 WHEN 'MAINTENANCE' THEN 3 WHEN 'CLOSED' THEN 4 ELSE 9 END, ISNULL(b.name,''), r.floor, r.room_number";
        try(Connection c=Databaseconfig.getConnection();Statement st=c.createStatement();ResultSet rs=st.executeQuery(sql)){
            while(rs.next()) list.add(map(rs));
        }catch(SQLException e){e.printStackTrace();}
        return list;
    }
    public Room getById(int id){
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement("SELECT r.*,b.name AS building_name FROM rooms r LEFT JOIN buildings b ON r.building_id=b.id WHERE r.id=?")){
            ps.setInt(1,id);ResultSet rs=ps.executeQuery();if(rs.next())return map(rs);
        }catch(SQLException e){e.printStackTrace();}
        return null;
    }
    public boolean insert(Room r){
        String sql="INSERT INTO rooms(room_number,floor,building_id,capacity,current_occupants,status,rent_price,room_type,notes) VALUES(?,?,?,?,?,?,?,?,?)";
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setString(1,r.getRoomNumber());ps.setInt(2,r.getFloor());ps.setInt(3,r.getBuildingId());
            ps.setInt(4,r.getCapacity());ps.setInt(5,0);ps.setString(6,r.getStatus());
            ps.setDouble(7,r.getRentPrice());ps.setString(8,r.getRoomType());ps.setString(9,r.getNotes());
            return ps.executeUpdate()>0;
        }catch(SQLException e){e.printStackTrace();return false;}
    }
    public boolean update(Room r){
        String sql="UPDATE rooms SET room_number=?,floor=?,building_id=?,capacity=?,status=?,rent_price=?,room_type=?,notes=? WHERE id=?";
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setString(1,r.getRoomNumber());ps.setInt(2,r.getFloor());ps.setInt(3,r.getBuildingId());
            ps.setInt(4,r.getCapacity());ps.setString(5,r.getStatus());ps.setDouble(6,r.getRentPrice());
            ps.setString(7,r.getRoomType());ps.setString(8,r.getNotes());ps.setInt(9,r.getId());
            return ps.executeUpdate()>0;
        }catch(SQLException e){e.printStackTrace();return false;}
    }
    public boolean delete(int id){
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement("DELETE FROM rooms WHERE id=?")){
            ps.setInt(1,id);return ps.executeUpdate()>0;
        }catch(SQLException e){e.printStackTrace();return false;}
    }
    public void updateOccupants(int roomId){
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(
                "UPDATE rooms SET current_occupants=(SELECT COUNT(*) FROM students WHERE room_id=? AND status='ACTIVE') WHERE id=?")){
            ps.setInt(1,roomId);ps.setInt(2,roomId);ps.executeUpdate();
        }catch(SQLException e){e.printStackTrace();}
    }
    public int countTotal(){return q("SELECT COUNT(*) FROM rooms");}
    public int countAvailable(){return q("SELECT COUNT(*) FROM rooms WHERE status='AVAILABLE'");}
    public int countFull(){return q("SELECT COUNT(*) FROM rooms WHERE status='FULL'");}
    public int countMaintenance(){return q("SELECT COUNT(*) FROM rooms WHERE status='MAINTENANCE'");}
    public int countTotalOccupants(){return q("SELECT ISNULL(SUM(current_occupants),0) FROM rooms");}
    private int q(String sql){
        try(Connection c=Databaseconfig.getConnection();Statement st=c.createStatement();ResultSet rs=st.executeQuery(sql)){
            if(rs.next())return rs.getInt(1);
        }catch(SQLException e){e.printStackTrace();}
        return 0;
    }
    public List<Building> getAllBuildings(){
        List<Building> list=new ArrayList<>();
        try(Connection c=Databaseconfig.getConnection();Statement st=c.createStatement();
            ResultSet rs=st.executeQuery("SELECT * FROM buildings ORDER BY name")){
            while(rs.next()){Building b=new Building();b.setId(rs.getInt("id"));b.setName(rs.getString("name"));
                b.setTotalFloors(rs.getInt("total_floors"));b.setDescription(rs.getString("description"));list.add(b);}
        }catch(SQLException e){e.printStackTrace();}
        return list;
    }
    private Room map(ResultSet rs) throws SQLException {
        Room r=new Room();
        r.setId(rs.getInt("id"));r.setRoomNumber(rs.getString("room_number"));r.setFloor(rs.getInt("floor"));
        r.setBuildingId(rs.getInt("building_id"));r.setCapacity(rs.getInt("capacity"));
        r.setCurrentOccupants(rs.getInt("current_occupants"));r.setStatus(rs.getString("status"));
        r.setRentPrice(rs.getDouble("rent_price"));r.setRoomType(rs.getString("room_type"));r.setNotes(rs.getString("notes"));
        try{r.setBuildingName(rs.getString("building_name"));}catch(Exception ignored){}
        return r;
    }
}
