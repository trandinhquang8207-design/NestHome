package com.dormassist.model;
import java.util.Date;

public class Token {
    private int id, usedBy, createdBy;
    private String tokenCode, role, usedByName;
    private boolean used;
    private Date usedAt, createdAt;
    public Token() {}
    public int getId() { return id; } public void setId(int v) { id=v; }
    public String getTokenCode() { return tokenCode; } public void setTokenCode(String v) { tokenCode=v; }
    public String getRole() { return role; } public void setRole(String v) { role=v; }
    public boolean isUsed() { return used; } public void setUsed(boolean v) { used=v; }
    public int getUsedBy() { return usedBy; } public void setUsedBy(int v) { usedBy=v; }
    public String getUsedByName() { return usedByName; } public void setUsedByName(String v) { usedByName=v; }
    public int getCreatedBy() { return createdBy; } public void setCreatedBy(int v) { createdBy=v; }
    public Date getUsedAt() { return usedAt; } public void setUsedAt(Date v) { usedAt=v; }
    public Date getCreatedAt() { return createdAt; } public void setCreatedAt(Date v) { createdAt=v; }
    @Override public String toString() { return tokenCode+" ["+role+"]"; }
}
