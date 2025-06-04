package com.owsb.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DailySalesTransaction {
    private String transactionId;
    private LocalDate salesDate;
    private String salesPersonId;
    private double totalAmount;
    private List<DailySalesRecord> salesItems;
    private String customerInfo;
    private String notes;
    
    public DailySalesTransaction() {
        this.salesItems = new ArrayList<>();
    }
    
    public DailySalesTransaction(String transactionId, LocalDate salesDate, String salesPersonId) {
        this.transactionId = transactionId;
        this.salesDate = salesDate;
        this.salesPersonId = salesPersonId;
        this.salesItems = new ArrayList<>();
        this.totalAmount = 0.0;
    }
    
    public void addSalesItem(String itemId, String itemCode, String itemName, int quantity, double unitPrice) {
        String recordId = transactionId + "_" + (salesItems.size() + 1);
        DailySalesRecord item = new DailySalesRecord(recordId, salesDate, itemId, itemCode,
                                                    itemName, quantity, unitPrice, salesPersonId, customerInfo);
        salesItems.add(item);
        calculateTotal();
    }
    
    public void calculateTotal() {
        totalAmount = salesItems.stream().mapToDouble(DailySalesRecord::getTotalAmount).sum();
    }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public LocalDate getSalesDate() { return salesDate; }
    public void setSalesDate(LocalDate salesDate) { this.salesDate = salesDate; }
    
    public String getSalesPersonId() { return salesPersonId; }
    public void setSalesPersonId(String salesPersonId) { this.salesPersonId = salesPersonId; }
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public List<DailySalesRecord> getSalesItems() { return salesItems; }
    public void setSalesItems(List<DailySalesRecord> salesItems) { 
        this.salesItems = salesItems; 
        calculateTotal();
    }
    
    public String getCustomerInfo() { return customerInfo; }
    public void setCustomerInfo(String customerInfo) { this.customerInfo = customerInfo; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}