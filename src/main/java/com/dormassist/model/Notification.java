package com.dormassist.model;
import java.util.Date;
public class Notification {
    private int id, senderId, targetRoomId;
    private String title, content, targetRole, senderName;
    private boolean important, isRead;
    private Date createdAt;
    public Notification() {}
    public int getId() { return id; } public void setId(int v) { id=v; }
    public int getSenderId() { return senderId; } public void setSenderId(int v) { senderId=v; }
    public String getSenderName() { return senderName; } public void setSenderName(String v) { senderName=v; }
    public int getTargetRoomId() { return targetRoomId; } public void setTargetRoomId(int v) { targetRoomId=v; }
    public String getTitle() { return title; } public void setTitle(String v) { title=v; }
    public String getContent() { return content; } public void setContent(String v) { content=v; }
    public String getTargetRole() { return targetRole; } public void setTargetRole(String v) { targetRole=v; }
    public boolean isImportant() { return important; } public void setImportant(boolean v) { important=v; }
    public boolean isRead() { return isRead; } public void setRead(boolean v) { isRead=v; }
    public Date getCreatedAt() { return createdAt; } public void setCreatedAt(Date v) { createdAt=v; }
}
