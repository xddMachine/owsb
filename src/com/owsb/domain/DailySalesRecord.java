package com.owsb.domain;

import java.time.LocalDate;

public class DailySalesRecord {
    private String recordId;
    private LocalDate salesDate;
    private String itemId;
    private String itemCode;
    private String itemName;
    private int quantitySold;
    private double unitPrice;
    private double totalAmount;
    private String salesPerson;
    private String customerInfo;
    private String notes;
    
    public DailySalesRecord() {}
    
    public DailySalesRecord(String recordId, LocalDate salesDate, String itemId, String itemCode,
                           String itemName, int quantitySold, double unitPrice, 
                           String salesPerson, String customerInfo) {
        this.recordId = recordId;
        this.salesDate = salesDate;
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantitySold = quantitySold;
        this.unitPrice = unitPrice;
        this.totalAmount = quantitySold * unitPrice;
        this.salesPerson = salesPerson;
        this.customerInfo = customerInfo;
    }
    
    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }
    
    public LocalDate getSalesDate() { return salesDate; }
    public void setSalesDate(LocalDate salesDate) { this.salesDate = salesDate; }
    
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public int getQuantitySold() { return quantitySold; }
    public void setQuantitySold(int quantitySold) { 
        this.quantitySold = quantitySold;
        this.totalAmount = quantitySold * unitPrice;
    }
    
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { 
        this.unitPrice = unitPrice;
        this.totalAmount = quantitySold * unitPrice;
    }
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public String getSalesPerson() { return salesPerson; }
    public void setSalesPerson(String salesPerson) { this.salesPerson = salesPerson; }
    
    public String getCustomerInfo() { return customerInfo; }
    public void setCustomerInfo(String customerInfo) { this.customerInfo = customerInfo; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    @Override
    public String toString() {
        return "DailySalesRecord{" +
                "recordId='" + recordId + '\'' +
                ", salesDate=" + salesDate +
                ", itemCode='" + itemCode + '\'' +
                ", quantitySold=" + quantitySold +
                ", totalAmount=" + totalAmount +
                '}';
    }
    
    public String toCSV() {
        return String.join(",", recordId, salesDate.toString(), itemId, itemCode,
                          itemName, String.valueOf(quantitySold), String.valueOf(unitPrice),
                          String.valueOf(totalAmount), salesPerson, customerInfo,
                          notes != null ? notes : "");
    }
}