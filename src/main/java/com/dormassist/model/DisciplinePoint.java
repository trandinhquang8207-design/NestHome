package com.dormassist.model;
import java.util.Date;

public class DisciplinePoint {
    private int id, studentId, points, createdBy, currentScore;
    private String type, reason, detail, studentName, creatorName, roomNumber;
    private Date createdAt;
    public DisciplinePoint() {}
    public int getId() { return id; } public void setId(int v) { id=v; }
    public int getStudentId() { return studentId; } public void setStudentId(int v) { studentId=v; }
    public String getStudentName() { return studentName; } public void setStudentName(String v) { studentName=v; }
    public int getPoints() { return points; } public void setPoints(int v) { points=v; }
    public int getCreatedBy() { return createdBy; } public void setCreatedBy(int v) { createdBy=v; }
    public int getCurrentScore() { return currentScore; } public void setCurrentScore(int v) { currentScore=v; }
    public String getCreatorName() { return creatorName; } public void setCreatorName(String v) { creatorName=v; }
    public String getRoomNumber() { return roomNumber; } public void setRoomNumber(String v) { roomNumber=v; }
    public String getType() { return type; } public void setType(String v) { type=v; }
    public String getReason() { return reason; } public void setReason(String v) { reason=v; }
    public String getDetail() { return detail; } public void setDetail(String v) { detail=v; }
    public Date getCreatedAt() { return createdAt; } public void setCreatedAt(Date v) { createdAt=v; }
}
