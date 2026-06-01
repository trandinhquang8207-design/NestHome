package com.dormassist.model;
import java.util.Date;
public class Student {
    private int id, userId, roomId, disciplinePoints;
    private String fullName, studentCode, idCard, gender, phone, email, hometown, status, notes, roomNumber;
    private Date dob, joinDate, expectedLeaveDate, createdAt;
    public Student() { this.disciplinePoints = 100; }
    public int getId() { return id; } public void setId(int v) { id=v; }
    public int getUserId() { return userId; } public void setUserId(int v) { userId=v; }
    public int getRoomId() { return roomId; } public void setRoomId(int v) { roomId=v; }
    public String getRoomNumber() { return roomNumber; } public void setRoomNumber(String v) { roomNumber=v; }
    public String getFullName() { return fullName; } public void setFullName(String v) { fullName=v; }
    public String getStudentCode() { return studentCode; } public void setStudentCode(String v) { studentCode=v; }
    public String getIdCard() { return idCard; } public void setIdCard(String v) { idCard=v; }
    public String getGender() { return gender; } public void setGender(String v) { gender=v; }
    public String getPhone() { return phone; } public void setPhone(String v) { phone=v; }
    public String getEmail() { return email; } public void setEmail(String v) { email=v; }
    public String getHometown() { return hometown; } public void setHometown(String v) { hometown=v; }
    public String getStatus() { return status; } public void setStatus(String v) { status=v; }
    public String getNotes() { return notes; } public void setNotes(String v) { notes=v; }
    public int getDisciplinePoints() { return disciplinePoints; } public void setDisciplinePoints(int v) { disciplinePoints=v; }
    public Date getDob() { return dob; } public void setDob(Date v) { dob=v; }
    public Date getJoinDate() { return joinDate; } public void setJoinDate(Date v) { joinDate=v; }
    public Date getExpectedLeaveDate() { return expectedLeaveDate; } public void setExpectedLeaveDate(Date v) { expectedLeaveDate=v; }
    public Date getCreatedAt() { return createdAt; } public void setCreatedAt(Date v) { createdAt=v; }
    @Override public String toString() { return fullName+(studentCode!=null?" ("+studentCode+")":""); }
}
