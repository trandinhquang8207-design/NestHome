package com.dormassist.dao;

import com.dormassist.config.Databaseconfig;
import com.dormassist.model.Bill;
import com.dormassist.model.PriceConfig;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    public List<Bill> getAll() {
        List<Bill> l = new ArrayList<>();
        String sql = "SELECT b.*, r.room_number FROM bills b LEFT JOIN rooms r ON b.room_id=r.id ORDER BY CASE b.status WHEN 'OVERDUE' THEN 1 WHEN 'UNPAID' THEN 2 WHEN 'PAID' THEN 3 WHEN 'CANCELLED' THEN 4 ELSE 5 END, b.due_date ASC, b.bill_year DESC, b.bill_month DESC";
        try (Connection c = Databaseconfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) l.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return l;
    }

    public List<Bill> getByRoom(int roomId) {
        List<Bill> l = new ArrayList<>();
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "SELECT b.*, r.room_number FROM bills b LEFT JOIN rooms r ON b.room_id=r.id WHERE b.room_id=? ORDER BY CASE b.status WHEN 'OVERDUE' THEN 1 WHEN 'UNPAID' THEN 2 WHEN 'PAID' THEN 3 WHEN 'CANCELLED' THEN 4 ELSE 5 END, b.due_date ASC, b.bill_year DESC, b.bill_month DESC")) {
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) l.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return l;
    }

    public Bill getById(int id) {
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "SELECT b.*, r.room_number FROM bills b LEFT JOIN rooms r ON b.room_id=r.id WHERE b.id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean insert(Bill b) {
        String sql = "INSERT INTO bills(room_id,bill_month,bill_year,electric_consumption,electric_amount," +
                     "water_consumption,water_amount,rent_amount,service_amount,total_amount,status,due_date,notes,created_by) " +
                     "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, b.getRoomId());
            ps.setInt(2, b.getBillMonth());
            ps.setInt(3, b.getBillYear());
            ps.setDouble(4, b.getElectricConsumption());
            ps.setDouble(5, b.getElectricAmount());
            ps.setDouble(6, b.getWaterConsumption());
            ps.setDouble(7, b.getWaterAmount());
            ps.setDouble(8, b.getRentAmount());
            ps.setDouble(9, b.getServiceAmount());
            ps.setDouble(10, b.getTotalAmount());
            ps.setString(11, b.getStatus());
            ps.setObject(12, b.getDueDate() != null ? new Date(b.getDueDate().getTime()) : null);
            ps.setString(13, b.getNotes());
            ps.setInt(14, b.getCreatedBy());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean markPaid(int billId, int userId) {
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "UPDATE bills SET status='PAID', paid_date=GETDATE(), paid_by=? WHERE id=?")) {
            ps.setInt(1, userId); ps.setInt(2, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM bills WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public double getTotalRevenue() {
        try (Connection c = Databaseconfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT ISNULL(SUM(total_amount),0) FROM bills WHERE status='PAID'")) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int countUnpaid() {
        try (Connection c = Databaseconfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM bills WHERE status='UNPAID'")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public PriceConfig getLatestPriceConfig() {
        try (Connection c = Databaseconfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT TOP 1 * FROM price_config ORDER BY effective_date DESC")) {
            if (rs.next()) {
                PriceConfig p = new PriceConfig();
                p.setId(rs.getInt("id"));
                p.setElectricPrice(rs.getDouble("electric_price"));
                p.setWaterPrice(rs.getDouble("water_price"));
                p.setServiceFee(rs.getDouble("service_fee"));
                Date ed = rs.getDate("effective_date");
                if (ed != null) p.setEffectiveDate(new java.util.Date(ed.getTime()));
                return p;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        PriceConfig def = new PriceConfig();
        def.setElectricPrice(3500); def.setWaterPrice(15000); def.setServiceFee(50000);
        return def;
    }

    public boolean savePriceConfig(PriceConfig p, int userId) {
        try (Connection c = Databaseconfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "INSERT INTO price_config(electric_price,water_price,service_fee,effective_date,created_by) VALUES(?,?,?,CAST(GETDATE() AS DATE),?)")) {
            ps.setDouble(1, p.getElectricPrice());
            ps.setDouble(2, p.getWaterPrice());
            ps.setDouble(3, p.getServiceFee());
            ps.setInt(4, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Bill map(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setId(rs.getInt("id"));
        b.setRoomId(rs.getInt("room_id"));
        try { b.setRoomNumber(rs.getString("room_number")); } catch (Exception ignored) {}
        b.setBillMonth(rs.getInt("bill_month"));
        b.setBillYear(rs.getInt("bill_year"));
        b.setElectricConsumption(rs.getDouble("electric_consumption"));
        b.setElectricAmount(rs.getDouble("electric_amount"));
        b.setWaterConsumption(rs.getDouble("water_consumption"));
        b.setWaterAmount(rs.getDouble("water_amount"));
        b.setRentAmount(rs.getDouble("rent_amount"));
        b.setServiceAmount(rs.getDouble("service_amount"));
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setStatus(rs.getString("status"));
        b.setNotes(rs.getString("notes"));
        b.setCreatedBy(rs.getInt("created_by"));

        // Dùng java.sql.Date rõ ràng
        Date dd = rs.getDate("due_date");
        if (dd != null) b.setDueDate(new java.util.Date(dd.getTime()));
        Date pd = rs.getDate("paid_date");
        if (pd != null) b.setPaidDate(new java.util.Date(pd.getTime()));
        return b;
    }
}
