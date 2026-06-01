package com.dormassist.dao;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Databaseconfig;
import com.dormassist.model.Token;
import java.sql.*;
import java.util.*;

public class TokenDAO {
    public List<Token> getAll(){
        List<Token> l=new ArrayList<>();
        String sql = "SELECT t.*, u.full_name AS used_by_name " +
            "FROM tokens t LEFT JOIN users u ON t.used_by=u.id " +
            "ORDER BY CASE WHEN t.is_used=0 THEN 1 ELSE 2 END, " +
            "CASE t.role WHEN 'ADMIN_SUPER' THEN 1 WHEN 'ADMIN_BASE' THEN 1 WHEN 'BASE2' THEN 2 WHEN 'BASE1' THEN 3 WHEN 'BASE3' THEN 4 ELSE 9 END, " +
            "t.created_at DESC, t.id DESC";
        try(Connection c=Databaseconfig.getConnection();Statement st=c.createStatement(); ResultSet rs=st.executeQuery(sql)){
            while(rs.next()) l.add(map(rs));
        }catch(SQLException e){e.printStackTrace();}
        return l;
    }

    public String generateToken(String role,int createdBy){
        String prefix = prefixForRole(role);
        try(Connection c=Databaseconfig.getConnection()){
            c.setAutoCommit(false);
            try {
                int next = 1;
                try (PreparedStatement seq = c.prepareStatement(
                    "SELECT ISNULL(MAX(TRY_CONVERT(INT, RIGHT(token_code, 4))),0) + 1 FROM tokens WHERE token_code LIKE ?")) {
                    seq.setString(1, prefix + "-%");
                    ResultSet rs = seq.executeQuery();
                    if (rs.next()) next = Math.max(1, rs.getInt(1));
                }
                for (int i=0; i<50; i++) {
                    String code = prefix + "-" + String.format("%04d", next + i);
                    try(PreparedStatement ps=c.prepareStatement("INSERT INTO tokens(token_code,role,created_by,is_used) VALUES(?,?,?,0)")){
                        ps.setString(1,code); ps.setString(2,role); ps.setInt(3,createdBy);
                        ps.executeUpdate(); c.commit(); return code;
                    } catch (SQLException dup) {
                        if (dup.getErrorCode() != 2627 && dup.getErrorCode() != 2601) throw dup;
                    }
                }
                c.rollback();
            } catch(SQLException e) { c.rollback(); throw e; }
        }catch(SQLException e){ e.printStackTrace(); }
        return null;
    }

    public boolean isValid(String code,String role){
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(
                "SELECT id FROM tokens WHERE token_code=? AND role=? AND is_used=0")){
            ps.setString(1,code); ps.setString(2,role); return ps.executeQuery().next();
        }catch(SQLException e){return false;}
    }

    public boolean delete(int id){
        try(Connection c=Databaseconfig.getConnection();PreparedStatement ps=c.prepareStatement(
                "DELETE FROM tokens WHERE id=? AND is_used=0")){
            ps.setInt(1,id);return ps.executeUpdate()>0;
        }catch(SQLException e){e.printStackTrace();return false;}
    }

    private String prefixForRole(String role){
        if (AppConstants.ROLE_BASE3.equals(role)) return "SVVKU";
        if (AppConstants.ROLE_BASE1.equals(role)) return "TTVKU";
        if (AppConstants.ROLE_BASE2.equals(role)) return "TPVKU";
        if (AppConstants.ROLE_ADMIN_SUPER.equals(role) || AppConstants.ROLE_ADMIN_BASE.equals(role)) return "ADMINVKU";
        return "VKU";
    }

    private Token map(ResultSet rs) throws SQLException {
        Token t=new Token();
        t.setId(rs.getInt("id")); t.setTokenCode(rs.getString("token_code"));
        t.setRole(rs.getString("role")); t.setUsed(rs.getBoolean("is_used"));
        t.setUsedBy(rs.getInt("used_by")); t.setCreatedBy(rs.getInt("created_by"));
        try { t.setUsedByName(rs.getString("used_by_name")); } catch (Exception ignored) {}
        Timestamp ca=rs.getTimestamp("created_at"); if(ca!=null)t.setCreatedAt(new java.util.Date(ca.getTime()));
        Timestamp ua=rs.getTimestamp("used_at"); if(ua!=null)t.setUsedAt(new java.util.Date(ua.getTime()));
        return t;
    }
}
