package com.owsb.domain;

public class PurchaseOrderLine {
    private String poId;
    private String itemId;
    private String itemCode;
    private String itemName;
    private int quantity;
    private String unit;
    private double unitPrice;
    private String specifications;
    private int receivedQuantity;
    private String status;
    private int lineNumber;
    private double lineTotal;
    
    public PurchaseOrderLine() {}
    
    public PurchaseOrderLine(String poId, String itemId, String itemCode, String itemName,
                            int quantity, String unit, double unitPrice, String specifications) {
        this.poId = poId;
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.specifications = specifications;
        this.receivedQuantity = 0;
        this.status = "PENDING";
    }
    
    public String getPoId() { return poId; }
    public void setPoId(String poId) { this.poId = poId; }
    
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
    
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    
    public int getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(int receivedQuantity) { this.receivedQuantity = receivedQuantity; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }
    
    public void setLineTotal(double lineTotal) { this.lineTotal = lineTotal; }
    
    public double getLineTotal() {
        return quantity * unitPrice;
    }
    
    public boolean isFullyReceived() {
        return receivedQuantity >= quantity;
    }
    
    @Override
    public String toString() {
        return "POLine{" +
                "itemCode='" + itemCode + '\'' +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", receivedQuantity=" + receivedQuantity +
                '}';
    }
    
    public String toCSV() {
        return String.join(",", 
                          escapeCSV(poId), 
                          escapeCSV(itemId), 
                          escapeCSV(itemCode), 
                          escapeCSV(itemName),
                          String.valueOf(quantity), 
                          escapeCSV(unit), 
                          String.valueOf(unitPrice),
                          escapeCSV(specifications != null ? specifications : ""), 
                          String.valueOf(receivedQuantity), 
                          escapeCSV(status));
    }
    
    private String escapeCSV(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}