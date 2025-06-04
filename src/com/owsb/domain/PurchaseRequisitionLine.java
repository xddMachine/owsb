package com.owsb.domain;

public class PurchaseRequisitionLine {
    private String prId;
    private String itemId;
    private String itemCode;
    private String itemName;
    private int quantity;
    private String unit;
    private double estimatedPrice;
    private String specifications;
    private String supplierId;
    
    public PurchaseRequisitionLine() {}
    
    public PurchaseRequisitionLine(String prId, String itemId, String itemCode, String itemName,
                                  int quantity, String unit, double estimatedPrice, String specifications) {
        this.prId = prId;
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unit = unit;
        this.estimatedPrice = estimatedPrice;
        this.specifications = specifications;
    }
    
    public String getPrId() { return prId; }
    public void setPrId(String prId) { this.prId = prId; }
    
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public double getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(double estimatedPrice) { this.estimatedPrice = estimatedPrice; }
    
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    
    public double getUnitPrice() { return estimatedPrice; }
    
    public double getLineTotal() {
        return quantity * estimatedPrice;
    }
    
    @Override
    public String toString() {
        return "PRLine{" +
                "itemCode='" + itemCode + '\'' +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", estimatedPrice=" + estimatedPrice +
                '}';
    }
    
    public String toCSV() {
        return String.join(",", prId, itemId, itemCode, itemName,
                          String.valueOf(quantity), unit, String.valueOf(estimatedPrice), specifications);
    }
}