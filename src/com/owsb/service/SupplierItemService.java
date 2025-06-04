package com.owsb.service;

import com.owsb.domain.Item;
import com.owsb.domain.Supplier;
import com.owsb.util.FileUtils;
import java.util.ArrayList;
import java.util.List;

public class SupplierItemService {
    private static final String SUPPLIER_ITEMS_FILE = "data/supplier_items.txt";
    private ItemService itemService;
    private SupplierService supplierService;
    
    public SupplierItemService() {
        this.itemService = new ItemService();
        this.supplierService = new SupplierService();
        initializeFileIfNotExists();
    }
    
    private void initializeFileIfNotExists() {
        try {
            List<String> lines = FileUtils.readAllLines(SUPPLIER_ITEMS_FILE);
            if (lines.isEmpty()) {
                List<String> headers = new ArrayList<>();
                headers.add("supplier_id,item_id,price,lead_time_days");
                FileUtils.writeAllLines(SUPPLIER_ITEMS_FILE, headers);
            }
        } catch (Exception e) {
            // File doesn't exist, create it with headers
            try {
                List<String> headers = new ArrayList<>();
                headers.add("supplier_id,item_id,price,lead_time_days");
                FileUtils.writeAllLines(SUPPLIER_ITEMS_FILE, headers);
            } catch (Exception ex) {
                System.err.println("Error creating supplier items file: " + ex.getMessage());
            }
        }
    }
    
    public boolean addSupplierItem(String supplierId, String itemId, double price, int leadTimeDays) {
        // Check if relationship already exists
        if (isSupplierItemExists(supplierId, itemId)) {
            throw new IllegalArgumentException("Supplier-Item relationship already exists");
        }
        
        // Validate supplier and item exist
        Supplier supplier = supplierService.findById(supplierId);
        Item item = itemService.findById(itemId);
        
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier not found");
        }
        if (item == null) {
            throw new IllegalArgumentException("Item not found");
        }
        
        try {
            List<String> lines = FileUtils.readAllLines(SUPPLIER_ITEMS_FILE);
            String newLine = String.format("%s,%s,%.2f,%d", supplierId, itemId, price, leadTimeDays);
            lines.add(newLine);
            FileUtils.writeAllLines(SUPPLIER_ITEMS_FILE, lines);
            return true;
        } catch (Exception e) {
            System.err.println("Error adding supplier-item relationship: " + e.getMessage());
            return false;
        }
    }
    
    public boolean removeSupplierItem(String supplierId, String itemId) {
        try {
            List<String> lines = FileUtils.readAllLines(SUPPLIER_ITEMS_FILE);
            List<String> newLines = new ArrayList<>();
            newLines.add(lines.get(0)); // Keep header
            
            boolean found = false;
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 4 && 
                    !(parts[0].equals(supplierId) && parts[1].equals(itemId))) {
                    newLines.add(lines.get(i));
                } else if (parts[0].equals(supplierId) && parts[1].equals(itemId)) {
                    found = true;
                }
            }
            
            if (found) {
                FileUtils.writeAllLines(SUPPLIER_ITEMS_FILE, newLines);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error removing supplier-item relationship: " + e.getMessage());
        }
        
        return false;
    }
    
    public List<Supplier> getSuppliersForItem(String itemId) {
        List<Supplier> suppliers = new ArrayList<>();
        
        try {
            List<String> lines = FileUtils.readAllLines(SUPPLIER_ITEMS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 4 && parts[1].equals(itemId)) {
                    Supplier supplier = supplierService.findById(parts[0]);
                    if (supplier != null) {
                        suppliers.add(supplier);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting suppliers for item: " + e.getMessage());
        }
        
        return suppliers;
    }
    
    public List<Item> getItemsForSupplier(String supplierId) {
        List<Item> items = new ArrayList<>();
        
        try {
            List<String> lines = FileUtils.readAllLines(SUPPLIER_ITEMS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 4 && parts[0].equals(supplierId)) {
                    Item item = itemService.findById(parts[1]);
                    if (item != null) {
                        items.add(item);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting items for supplier: " + e.getMessage());
        }
        
        return items;
    }
    
    public Double getSupplierItemPrice(String supplierId, String itemId) {
        try {
            List<String> lines = FileUtils.readAllLines(SUPPLIER_ITEMS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 4 && 
                    parts[0].equals(supplierId) && parts[1].equals(itemId)) {
                    return Double.parseDouble(parts[2]);
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting supplier item price: " + e.getMessage());
        }
        
        return null;
    }
    
    private boolean isSupplierItemExists(String supplierId, String itemId) {
        try {
            List<String> lines = FileUtils.readAllLines(SUPPLIER_ITEMS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 4 && 
                    parts[0].equals(supplierId) && parts[1].equals(itemId)) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking supplier-item relationship: " + e.getMessage());
        }
        
        return false;
    }
    
    public List<Supplier> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }
    
    public Supplier findSupplierById(String supplierId) {
        return supplierService.findById(supplierId);
    }
}