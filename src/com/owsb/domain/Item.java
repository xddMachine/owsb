package com.owsb.domain;

public class Item {
    private String itemId;
    private String itemCode;
    private String itemName;
    private String description;
    private String category;
    private double unitPrice;
    private int stockQuantity;
    private int reorderLevel;
    private String status;
    
    public Item() {}
    
    public Item(String itemId, String itemCode, String itemName, String description,
                String category, double unitPrice, int stockQuantity, 
                int reorderLevel, String status) {
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.description = description;
        this.category = category;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.reorderLevel = reorderLevel;
        this.status = status;
    }
    
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public boolean isLowStock() {
        return stockQuantity <= reorderLevel;
    }
    
    @Override
    public String toString() {
        return "Item{" +
                "itemId='" + itemId + '\'' +
                ", itemCode='" + itemCode + '\'' +
                ", itemName='" + itemName + '\'' +
                ", stockQuantity=" + stockQuantity +
                ", unitPrice=" + unitPrice +
                '}';
    }
    
    public String toCSV() {
        return String.join(",", itemId, itemCode, itemName, description,
                          category, String.valueOf(unitPrice), String.valueOf(stockQuantity),
                          String.valueOf(reorderLevel), status);
    }
}