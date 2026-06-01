package com.dormassist.model;
import java.util.Date;

public class PriceConfig {
    private int id, createdBy;
    private double electricPrice, waterPrice, serviceFee;
    private Date effectiveDate, createdAt;
    public PriceConfig() {}
    public int getId() { return id; } public void setId(int v) { id=v; }
    public double getElectricPrice() { return electricPrice; } public void setElectricPrice(double v) { electricPrice=v; }
    public double getWaterPrice() { return waterPrice; } public void setWaterPrice(double v) { waterPrice=v; }
    public double getServiceFee() { return serviceFee; } public void setServiceFee(double v) { serviceFee=v; }
    public Date getEffectiveDate() { return effectiveDate; } public void setEffectiveDate(Date v) { effectiveDate=v; }
    public int getCreatedBy() { return createdBy; } public void setCreatedBy(int v) { createdBy=v; }
    public Date getCreatedAt() { return createdAt; } public void setCreatedAt(Date v) { createdAt=v; }
}
