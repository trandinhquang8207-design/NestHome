package com.dormassist.model;
import java.util.Date;
public class Bill {
    private int id, roomId, billMonth, billYear, paidBy, createdBy;
    private double electricConsumption, electricAmount, waterConsumption, waterAmount, rentAmount, serviceAmount, totalAmount;
    private String status, notes, roomNumber;
    private Date dueDate, paidDate, createdAt;
    public Bill() {}
    public int getId() { return id; } public void setId(int v) { id=v; }
    public int getRoomId() { return roomId; } public void setRoomId(int v) { roomId=v; }
    public String getRoomNumber() { return roomNumber; } public void setRoomNumber(String v) { roomNumber=v; }
    public int getBillMonth() { return billMonth; } public void setBillMonth(int v) { billMonth=v; }
    public int getBillYear() { return billYear; } public void setBillYear(int v) { billYear=v; }
    public double getElectricConsumption() { return electricConsumption; } public void setElectricConsumption(double v) { electricConsumption=v; }
    public double getElectricAmount() { return electricAmount; } public void setElectricAmount(double v) { electricAmount=v; }
    public double getWaterConsumption() { return waterConsumption; } public void setWaterConsumption(double v) { waterConsumption=v; }
    public double getWaterAmount() { return waterAmount; } public void setWaterAmount(double v) { waterAmount=v; }
    public double getRentAmount() { return rentAmount; } public void setRentAmount(double v) { rentAmount=v; }
    public double getServiceAmount() { return serviceAmount; } public void setServiceAmount(double v) { serviceAmount=v; }
    public double getTotalAmount() { return totalAmount; } public void setTotalAmount(double v) { totalAmount=v; }
    public String getStatus() { return status; } public void setStatus(String v) { status=v; }
    public String getNotes() { return notes; } public void setNotes(String v) { notes=v; }
    public int getPaidBy() { return paidBy; } public void setPaidBy(int v) { paidBy=v; }
    public int getCreatedBy() { return createdBy; } public void setCreatedBy(int v) { createdBy=v; }
    public Date getDueDate() { return dueDate; } public void setDueDate(Date v) { dueDate=v; }
    public Date getPaidDate() { return paidDate; } public void setPaidDate(Date v) { paidDate=v; }
    public Date getCreatedAt() { return createdAt; } public void setCreatedAt(Date v) { createdAt=v; }
    public String getPeriod() { return billMonth+"/"+billYear; }
}
