package com.dormassist.model;
import java.util.Date;
public class Incident {
    private int id, reporterId, roomId, assignedTo;
    private String title, description, priority, status, resolutionNotes, reporterName, roomNumber, assigneeName;
    private Date resolvedDate, createdAt;
    public Incident() {}
    public int getId() { return id; } public void setId(int v) { id=v; }
    public int getReporterId() { return reporterId; } public void setReporterId(int v) { reporterId=v; }
    public String getReporterName() { return reporterName; } public void setReporterName(String v) { reporterName=v; }
    public int getRoomId() { return roomId; } public void setRoomId(int v) { roomId=v; }
    public String getRoomNumber() { return roomNumber; } public void setRoomNumber(String v) { roomNumber=v; }
    public int getAssignedTo() { return assignedTo; } public void setAssignedTo(int v) { assignedTo=v; }
    public String getAssigneeName() { return assigneeName; } public void setAssigneeName(String v) { assigneeName=v; }
    public String getTitle() { return title; } public void setTitle(String v) { title=v; }
    public String getDescription() { return description; } public void setDescription(String v) { description=v; }
    public String getPriority() { return priority; } public void setPriority(String v) { priority=v; }
    public String getStatus() { return status; } public void setStatus(String v) { status=v; }
    public String getResolutionNotes() { return resolutionNotes; } public void setResolutionNotes(String v) { resolutionNotes=v; }
    public Date getResolvedDate() { return resolvedDate; } public void setResolvedDate(Date v) { resolvedDate=v; }
    public Date getCreatedAt() { return createdAt; } public void setCreatedAt(Date v) { createdAt=v; }
}
