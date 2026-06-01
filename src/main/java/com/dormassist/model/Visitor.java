package com.dormassist.model;
import java.util.Date;
import java.sql.Time;

public class Visitor {
    private int id, studentId, approvedBy;
    private String visitorName, visitorPhone, visitorIdCard, purpose, status, notes, studentName, approverName;
    private Date visitDate, approvedAt, createdAt;
    private Time visitTimeStart, visitTimeEnd;
    public Visitor() {}
    public int getId() { return id; } public void setId(int v) { id=v; }
    public int getStudentId() { return studentId; } public void setStudentId(int v) { studentId=v; }
    public String getStudentName() { return studentName; } public void setStudentName(String v) { studentName=v; }
    public int getApprovedBy() { return approvedBy; } public void setApprovedBy(int v) { approvedBy=v; }
    public String getApproverName() { return approverName; } public void setApproverName(String v) { approverName=v; }
    public String getVisitorName() { return visitorName; } public void setVisitorName(String v) { visitorName=v; }
    public String getVisitorPhone() { return visitorPhone; } public void setVisitorPhone(String v) { visitorPhone=v; }
    public String getVisitorIdCard() { return visitorIdCard; } public void setVisitorIdCard(String v) { visitorIdCard=v; }
    public String getPurpose() { return purpose; } public void setPurpose(String v) { purpose=v; }
    public String getStatus() { return status; } public void setStatus(String v) { status=v; }
    public String getNotes() { return notes; } public void setNotes(String v) { notes=v; }
    public Date getVisitDate() { return visitDate; } public void setVisitDate(Date v) { visitDate=v; }
    public Time getVisitTimeStart() { return visitTimeStart; } public void setVisitTimeStart(Time v) { visitTimeStart=v; }
    public Time getVisitTimeEnd() { return visitTimeEnd; } public void setVisitTimeEnd(Time v) { visitTimeEnd=v; }
    public Date getApprovedAt() { return approvedAt; } public void setApprovedAt(Date v) { approvedAt=v; }
    public Date getCreatedAt() { return createdAt; } public void setCreatedAt(Date v) { createdAt=v; }
}
