package com.owsb.service;

import com.owsb.domain.Item;
import com.owsb.util.FileUtils;
import com.owsb.util.IDGenerator;
import com.owsb.util.Validators;
import java.util.ArrayList;
import java.util.List;

public class ItemService {
    private static final String ITEMS_FILE = "data/items.txt";
    
    public List<Item> listItems() {
        List<Item> items = new ArrayList<>();
        
        try {
            List<String> lines = FileUtils.readAllLines(ITEMS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 9) {
                    Item item = createItemFromCSV(parts);
                    items.add(item);
                }
            }
        } catch (Exception e) {
            System.err.println("Error listing items: " + e.getMessage());
        }
        
        return items;
    }
    
    public Item createItem(String itemCode, String itemName, String description, String category,
                          double unitPrice, int stockQuantity, int reorderLevel) {
        String validation = Validators.validateItem(itemCode, itemName, unitPrice, stockQuantity, reorderLevel);
        if (validation != null) {
            throw new IllegalArgumentException(validation);
        }
        
        if (isItemCodeExists(itemCode)) {
            throw new IllegalArgumentException("Item code already exists");
        }
        
        String itemId = IDGenerator.generateItemId();
        Item item = new Item(itemId, itemCode, itemName, description, category, 
                            unitPrice, stockQuantity, reorderLevel, "ACTIVE");
        
        try {
            List<String> lines = FileUtils.readAllLines(ITEMS_FILE);
            lines.add(item.toCSV());
            FileUtils.writeAllLines(ITEMS_FILE, lines);
            return item;
        } catch (Exception e) {
            throw new RuntimeException("Error creating item: " + e.getMessage());
        }
    }
    
    public boolean updateItem(Item item) {
        try {
            List<String> lines = FileUtils.readAllLines(ITEMS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 9 && parts[0].equals(item.getItemId())) {
                    lines.set(i, item.toCSV());
                    FileUtils.writeAllLines(ITEMS_FILE, lines);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating item: " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean deleteItem(String itemId) {
        try {
            List<String> lines = FileUtils.readAllLines(ITEMS_FILE);
            List<String> newLines = new ArrayList<>();
            newLines.add(lines.get(0));
            
            boolean found = false;
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 9 && !parts[0].equals(itemId)) {
                    newLines.add(lines.get(i));
                } else if (parts[0].equals(itemId)) {
                    found = true;
                }
            }
            
            if (found) {
                FileUtils.writeAllLines(ITEMS_FILE, newLines);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error deleting item: " + e.getMessage());
        }
        
        return false;
    }
    
    public Item findByCode(String itemCode) {
        try {
            List<String> lines = FileUtils.readAllLines(ITEMS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 9 && parts[1].equals(itemCode)) {
                    return createItemFromCSV(parts);
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding item: " + e.getMessage());
        }
        
        return null;
    }
    
    public Item findById(String itemId) {
        try {
            List<String> lines = FileUtils.readAllLines(ITEMS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 9 && parts[0].equals(itemId)) {
                    return createItemFromCSV(parts);
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding item: " + e.getMessage());
        }
        
        return null;
    }
    
    public List<Item> getLowStockItems() {
        List<Item> lowStockItems = new ArrayList<>();
        List<Item> allItems = listItems();
        
        for (Item item : allItems) {
            if (item.isLowStock()) {
                lowStockItems.add(item);
            }
        }
        
        return lowStockItems;
    }
    
    public boolean updateStock(String itemId, int newQuantity) {
        Item item = findById(itemId);
        if (item != null) {
            item.setStockQuantity(newQuantity);
            return updateItem(item);
        }
        return false;
    }
    
    private boolean isItemCodeExists(String itemCode) {
        return findByCode(itemCode) != null;
    }
    
    private Item createItemFromCSV(String[] parts) {
        String itemId = parts[0];
        String itemCode = parts[1];
        String itemName = parts[2];
        String description = parts[3];
        String category = parts[4];
        double unitPrice = Double.parseDouble(parts[5]);
        int stockQuantity = Integer.parseInt(parts[6]);
        int reorderLevel = Integer.parseInt(parts[7]);
        String status = parts[8];
        
        return new Item(itemId, itemCode, itemName, description, category,
                       unitPrice, stockQuantity, reorderLevel, status);
    }
    
    public List<Item> getAllItems() {
        return listItems();
    }
    
    public Item getItemById(String itemId) {
        return findById(itemId);
    }
}