package com.dormassist.model;

import java.util.Date;

public class User {
    private int id;
    private String username, passwordHash, role, fullName, email, phone, tokenUsed;
    private boolean active;
    private Date lastLogin, createdAt;

    public User() {}
    public int getId() { return id; }
    public void setId(int v) { this.id = v; }
    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String v) { this.passwordHash = v; }
    public String getRole() { return role; }
    public void setRole(String v) { this.role = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
    public Date getLastLogin() { return lastLogin; }
    public void setLastLogin(Date v) { this.lastLogin = v; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date v) { this.createdAt = v; }
    public String getTokenUsed() { return tokenUsed; }
    public void setTokenUsed(String v) { this.tokenUsed = v; }
    @Override public String toString() { return fullName != null ? fullName : username; }
}
