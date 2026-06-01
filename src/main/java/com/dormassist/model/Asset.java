package com.dormassist.model;
import java.util.Date;
public class Asset {
    private int id, roomId, quantity;
    private String assetName, assetCode, category, conditionStatus, notes, roomNumber;
    private double purchasePrice;
    private Date purchaseDate, createdAt;
    public Asset() {}
    public int getId() { return id; } public void setId(int v) { id=v; }
    public int getRoomId() { return roomId; } public void setRoomId(int v) { roomId=v; }
    public String getRoomNumber() { return roomNumber; } public void setRoomNumber(String v) { roomNumber=v; }
    public String getAssetName() { return assetName; } public void setAssetName(String v) { assetName=v; }
    public String getAssetCode() { return assetCode; } public void setAssetCode(String v) { assetCode=v; }
    public String getCategory() { return category; } public void setCategory(String v) { category=v; }
    public String getConditionStatus() { return conditionStatus; } public void setConditionStatus(String v) { conditionStatus=v; }
    public String getNotes() { return notes; } public void setNotes(String v) { notes=v; }
    public int getQuantity() { return quantity; } public void setQuantity(int v) { quantity=v; }
    public double getPurchasePrice() { return purchasePrice; } public void setPurchasePrice(double v) { purchasePrice=v; }
    public Date getPurchaseDate() { return purchaseDate; } public void setPurchaseDate(Date v) { purchaseDate=v; }
    public Date getCreatedAt() { return createdAt; } public void setCreatedAt(Date v) { createdAt=v; }
    @Override public String toString() { return assetName+(assetCode!=null?" ["+assetCode+"]":""); }
}
