package com.dormassist.model;
public class Building {
    private int id, totalFloors;
    private String name, description;
    public Building() {}
    public Building(int id, String name) { this.id=id; this.name=name; }
    public int getId() { return id; } public void setId(int v) { id=v; }
    public String getName() { return name; } public void setName(String v) { name=v; }
    public int getTotalFloors() { return totalFloors; } public void setTotalFloors(int v) { totalFloors=v; }
    public String getDescription() { return description; } public void setDescription(String v) { description=v; }
    @Override public String toString() { return name; }
}
