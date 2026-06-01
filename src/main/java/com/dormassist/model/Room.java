package com.dormassist.model;
import java.util.Date;

public class Room {
    private int id, floor, buildingId, capacity, currentOccupants;
    private String roomNumber, status, roomType, notes, buildingName;
    private double rentPrice;
    public Room() {}
    public int getId() { return id; } public void setId(int v) { id=v; }
    public String getRoomNumber() { return roomNumber; } public void setRoomNumber(String v) { roomNumber=v; }
    public int getFloor() { return floor; } public void setFloor(int v) { floor=v; }
    public int getBuildingId() { return buildingId; } public void setBuildingId(int v) { buildingId=v; }
    public String getBuildingName() { return buildingName; } public void setBuildingName(String v) { buildingName=v; }
    public int getCapacity() { return capacity; } public void setCapacity(int v) { capacity=v; }
    public int getCurrentOccupants() { return currentOccupants; } public void setCurrentOccupants(int v) { currentOccupants=v; }
    public String getStatus() { return status; } public void setStatus(String v) { status=v; }
    public String getRoomType() { return roomType; } public void setRoomType(String v) { roomType=v; }
    public String getNotes() { return notes; } public void setNotes(String v) { notes=v; }
    public double getRentPrice() { return rentPrice; } public void setRentPrice(double v) { rentPrice=v; }
    public int getAvailable() { return capacity - currentOccupants; }
    @Override public String toString() { return roomNumber + (buildingName!=null?" - "+buildingName:""); }
}
