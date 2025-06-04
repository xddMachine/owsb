package com.owsb.service;

import com.owsb.domain.DailySalesRecord;
import com.owsb.domain.Item;
import com.owsb.util.FileUtils;
import com.owsb.util.IDGenerator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesService {
    private static final String SALES_FILE = "data/daily_sales.txt";
    private ItemService itemService;
    
    public SalesService() {
        this.itemService = new ItemService();
    }
    
    public DailySalesRecord recordDailySales(String itemCode, int quantitySold, double unitPrice,
                                           String salesPerson, String customerInfo) {
        Item item = itemService.findByCode(itemCode);
        if (item == null) {
            throw new IllegalArgumentException("Item not found with code: " + itemCode);
        }
        
        if (item.getStockQuantity() < quantitySold) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + item.getStockQuantity());
        }
        
        String recordId = IDGenerator.generateSalesRecordId();
        DailySalesRecord record = new DailySalesRecord(recordId, LocalDate.now(), 
                                                      item.getItemId(), item.getItemCode(),
                                                      item.getItemName(), quantitySold, unitPrice,
                                                      salesPerson, customerInfo);
        
        try {
            List<String> lines = FileUtils.readAllLines(SALES_FILE);
            lines.add(record.toCSV());
            FileUtils.writeAllLines(SALES_FILE, lines);
            
            updateStockOnSale(item.getItemId(), quantitySold);
            return record;
        } catch (Exception e) {
            throw new RuntimeException("Error recording sales: " + e.getMessage());
        }
    }
    
    public boolean updateStockOnSale(String itemId, int quantitySold) {
        Item item = itemService.findById(itemId);
        if (item != null) {
            int newQuantity = item.getStockQuantity() - quantitySold;
            if (newQuantity < 0) {
                return false;
            }
            return itemService.updateStock(itemId, newQuantity);
        }
        return false;
    }
    
    public List<DailySalesRecord> listSalesRecords() {
        List<DailySalesRecord> records = new ArrayList<>();
        
        try {
            List<String> lines = FileUtils.readAllLines(SALES_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 11) {
                    DailySalesRecord record = createSalesRecordFromCSV(parts);
                    records.add(record);
                }
            }
        } catch (Exception e) {
            System.err.println("Error listing sales records: " + e.getMessage());
        }
        
        return records;
    }
    
    public List<DailySalesRecord> getSalesRecordsByDate(LocalDate date) {
        List<DailySalesRecord> allRecords = listSalesRecords();
        List<DailySalesRecord> filteredRecords = new ArrayList<>();
        
        for (DailySalesRecord record : allRecords) {
            if (record.getSalesDate().equals(date)) {
                filteredRecords.add(record);
            }
        }
        
        return filteredRecords;
    }
    
    public List<DailySalesRecord> getSalesRecordsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<DailySalesRecord> allRecords = listSalesRecords();
        List<DailySalesRecord> filteredRecords = new ArrayList<>();
        
        for (DailySalesRecord record : allRecords) {
            LocalDate salesDate = record.getSalesDate();
            if (!salesDate.isBefore(startDate) && !salesDate.isAfter(endDate)) {
                filteredRecords.add(record);
            }
        }
        
        return filteredRecords;
    }
    
    public double getTotalSalesAmount(LocalDate startDate, LocalDate endDate) {
        List<DailySalesRecord> records = getSalesRecordsByDateRange(startDate, endDate);
        return records.stream().mapToDouble(DailySalesRecord::getTotalAmount).sum();
    }
    
    private DailySalesRecord createSalesRecordFromCSV(String[] parts) {
        String recordId = parts[0];
        LocalDate salesDate = LocalDate.parse(parts[1]);
        String itemId = parts[2];
        String itemCode = parts[3];
        String itemName = parts[4];
        int quantitySold = Integer.parseInt(parts[5]);
        double unitPrice = Double.parseDouble(parts[6]);
        double totalAmount = Double.parseDouble(parts[7]);
        String salesPerson = parts[8];
        String customerInfo = parts[9];
        String notes = parts.length > 10 ? parts[10] : "";
        
        DailySalesRecord record = new DailySalesRecord(recordId, salesDate, itemId, itemCode,
                                                      itemName, quantitySold, unitPrice,
                                                      salesPerson, customerInfo);
        record.setNotes(notes);
        return record;
    }
    
    public String generateSalesRecordId() {
        return IDGenerator.generateSalesRecordId();
    }
    
    public boolean saveDailySalesRecord(DailySalesRecord record) {
        try {
            List<String> lines = FileUtils.readAllLines(SALES_FILE);
            lines.add(record.toCSV());
            FileUtils.writeAllLines(SALES_FILE, lines);
            return true;
        } catch (Exception e) {
            System.err.println("Error saving sales record: " + e.getMessage());
            return false;
        }
    }
}