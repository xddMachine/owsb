package com.owsb.service;

import com.owsb.domain.*;
import java.util.List;

public class SampleDataService {
    private ItemService itemService;
    private SupplierService supplierService;
    private SupplierItemService supplierItemService;
    
    public SampleDataService() {
        this.itemService = new ItemService();
        this.supplierService = new SupplierService();
        this.supplierItemService = new SupplierItemService();
    }
    
    public void initializeSampleData() {
        try {
            // Create sample suppliers if they don't exist
            createSampleSuppliers();
            
            // Create sample items if they don't exist
            createSampleItems();
            
            // Create supplier-item relationships
            createSupplierItemRelationships();
            
            System.out.println("Sample data initialized successfully.");
        } catch (Exception e) {
            System.err.println("Error initializing sample data: " + e.getMessage());
        }
    }
    
    private void createSampleSuppliers() {
        // Suppliers already exist in the system, no need to create new ones
        System.out.println("Using existing suppliers in the system.");
    }
    
    private void createSampleItems() {
        // Items already exist in the system, no need to create new ones
        System.out.println("Using existing items in the system.");
    }
    
    private void createSupplierItemRelationships() {
        try {
            // Get all suppliers and items
            List<Supplier> suppliers = supplierService.getAllSuppliers();
            List<Item> items = itemService.getAllItems();
            
            if (suppliers.size() < 3 || items.size() < 3) {
                System.out.println("Not enough suppliers or items to create relationships.");
                return;
            }
            
            // Find specific suppliers and items using the actual data in the system
            Supplier techWorld = findSupplierByName(suppliers, "TechWorld Solutions");
            Supplier officeMax = findSupplierByName(suppliers, "OfficeMax Supply Co");
            Supplier furnitureplus = findSupplierByName(suppliers, "Furniture Plus Ltd");
            Supplier electronicsDirect = findSupplierByName(suppliers, "Electronics Direct");
            
            // Use existing items from the system
            Item laptop = findItemByCode(items, "LAPTOP001");  // Dell Laptop
            Item mouse = findItemByCode(items, "MOUSE001");   // Wireless Mouse
            Item desk = findItemByCode(items, "DESK001");     // Office Desk
            Item chair = findItemByCode(items, "CHAIR001");   // Office Chair
            Item paper = findItemByCode(items, "PAPER001");   // Copy Paper
            
            // Create many-to-many relationships with different prices for existing items
            
            // Dell Laptop - can be supplied by TechWorld and Electronics Direct
            if (laptop != null && techWorld != null) {
                addSupplierItemIfNotExists(techWorld.getSupplierId(), laptop.getItemId(), 44000.00, 5);
            }
            if (laptop != null && electronicsDirect != null) {
                addSupplierItemIfNotExists(electronicsDirect.getSupplierId(), laptop.getItemId(), 43500.00, 7);
            }
            
            // Wireless Mouse - can be supplied by TechWorld and Electronics Direct
            if (mouse != null && techWorld != null) {
                addSupplierItemIfNotExists(techWorld.getSupplierId(), mouse.getItemId(), 8300.00, 3);
            }
            if (mouse != null && electronicsDirect != null) {
                addSupplierItemIfNotExists(electronicsDirect.getSupplierId(), mouse.getItemId(), 8200.00, 4);
            }
            
            // Office Chair - can be supplied by OfficeMax and Furniture Plus
            if (chair != null && officeMax != null) {
                addSupplierItemIfNotExists(officeMax.getSupplierId(), chair.getItemId(), 17500.00, 7);
            }
            if (chair != null && furnitureplus != null) {
                addSupplierItemIfNotExists(furnitureplus.getSupplierId(), chair.getItemId(), 17200.00, 10);
            }
            
            // Office Desk - can be supplied by OfficeMax and Furniture Plus
            if (desk != null && officeMax != null) {
                addSupplierItemIfNotExists(officeMax.getSupplierId(), desk.getItemId(), 24500.00, 10);
            }
            if (desk != null && furnitureplus != null) {
                addSupplierItemIfNotExists(furnitureplus.getSupplierId(), desk.getItemId(), 24000.00, 12);
            }
            
            // Copy Paper - can be supplied by OfficeMax only
            if (paper != null && officeMax != null) {
                addSupplierItemIfNotExists(officeMax.getSupplierId(), paper.getItemId(), 240.00, 3);
            }
            
            System.out.println("Supplier-item relationships created successfully.");
            
        } catch (Exception e) {
            System.err.println("Error creating supplier-item relationships: " + e.getMessage());
        }
    }
    
    private void addSupplierItemIfNotExists(String supplierId, String itemId, double price, int leadTime) {
        try {
            // Check if relationship already exists
            Double existingPrice = supplierItemService.getSupplierItemPrice(supplierId, itemId);
            if (existingPrice == null) {
                supplierItemService.addSupplierItem(supplierId, itemId, price, leadTime);
                System.out.println("Added supplier-item relationship: " + supplierId + " -> " + itemId + " @ $" + price);
            }
        } catch (Exception e) {
            // Relationship might already exist, which is fine
            System.out.println("Supplier-item relationship already exists: " + supplierId + " -> " + itemId);
        }
    }
    
    private Supplier findSupplierByName(List<Supplier> suppliers, String name) {
        for (Supplier supplier : suppliers) {
            if (supplier.getSupplierName().equals(name)) {
                return supplier;
            }
        }
        return null;
    }
    
    private Item findItemByCode(List<Item> items, String code) {
        for (Item item : items) {
            if (item.getItemCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}