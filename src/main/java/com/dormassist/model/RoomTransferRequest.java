package com.dormassist.model;
import java.util.Date;

/** Yêu cầu xin đổi phòng */
public class RoomTransferRequest {
    private int id, studentId, fromRoomId, toRoomId, processedBy;
    private String reason, status, adminNotes, studentName, fromRoomNumber, toRoomNumber;
    private Date createdAt, processedAt;

    public RoomTransferRequest() {}
    public int getId() { return id; } public void setId(int v) { id=v; }
    public int getStudentId() { return studentId; } public void setStudentId(int v) { studentId=v; }
    public String getStudentName() { return studentName; } public void setStudentName(String v) { studentName=v; }
    public int getFromRoomId() { return fromRoomId; } public void setFromRoomId(int v) { fromRoomId=v; }
    public String getFromRoomNumber() { return fromRoomNumber; } public void setFromRoomNumber(String v) { fromRoomNumber=v; }
    public int getToRoomId() { return toRoomId; } public void setToRoomId(int v) { toRoomId=v; }
    public String getToRoomNumber() { return toRoomNumber; } public void setToRoomNumber(String v) { toRoomNumber=v; }
    public int getProcessedBy() { return processedBy; } public void setProcessedBy(int v) { processedBy=v; }
    public String getReason() { return reason; } public void setReason(String v) { reason=v; }
    public String getStatus() { return status; } public void setStatus(String v) { status=v; }
    public String getAdminNotes() { return adminNotes; } public void setAdminNotes(String v) { adminNotes=v; }
    public Date getCreatedAt() { return createdAt; } public void setCreatedAt(Date v) { createdAt=v; }
    public Date getProcessedAt() { return processedAt; } public void setProcessedAt(Date v) { processedAt=v; }
}
