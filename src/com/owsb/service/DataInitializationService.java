package com.owsb.service;

import com.owsb.domain.Role;
import com.owsb.util.FileUtils;

public class DataInitializationService {
    private UserService userService;
    private ItemService itemService;
    private SupplierService supplierService;
    
    public DataInitializationService() {
        this.userService = new UserService();
        this.itemService = new ItemService();
        this.supplierService = new SupplierService();
    }
    
    public void initializeSampleData() {
        initializeUsers();
        initializeItems();
        initializeSuppliers();
    }
    
    private void initializeUsers() {
        try {
            // Check if users already exist (besides admin)
            if (userService.listUsers().size() <= 1) {
                System.out.println("Creating sample users...");
                
                // Create Sales Manager
                try {
                    userService.createUser(Role.SALES_MANAGER, "sales_mgr", "Sales@123", 
                                         "Sarah Johnson", "012-3456-7890", "sarah.johnson@owsb.com");
                    System.out.println("Created Sales Manager: sales_mgr");
                } catch (Exception e) {
                    System.err.println("Failed to create Sales Manager: " + e.getMessage());
                }
                
                // Create Purchase Manager
                try {
                    userService.createUser(Role.PURCHASE_MANAGER, "purchase_mgr", "Purchase@123", 
                                         "Michael Chen", "012-3456-7891", "michael.chen@owsb.com");
                    System.out.println("Created Purchase Manager: purchase_mgr");
                } catch (Exception e) {
                    System.err.println("Failed to create Purchase Manager: " + e.getMessage());
                }
                
                // Create Inventory Manager
                try {
                    userService.createUser(Role.INVENTORY_MANAGER, "inventory_mgr", "Inventory@123", 
                                         "Emily Rodriguez", "012-3456-7892", "emily.rodriguez@owsb.com");
                    System.out.println("Created Inventory Manager: inventory_mgr");
                } catch (Exception e) {
                    System.err.println("Failed to create Inventory Manager: " + e.getMessage());
                }
                
                // Create Finance Manager
                try {
                    userService.createUser(Role.FINANCE_MANAGER, "finance_mgr", "Finance@123", 
                                         "David Thompson", "012-3456-7893", "david.thompson@owsb.com");
                    System.out.println("Created Finance Manager: finance_mgr");
                } catch (Exception e) {
                    System.err.println("Failed to create Finance Manager: " + e.getMessage());
                }
                
                System.out.println("Sample users creation completed!");
            } else {
                System.out.println("Sample users already exist, skipping creation.");
            }
        } catch (Exception e) {
            System.err.println("Error in user initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeItems() {
        try {
            // Check if items already exist
            if (itemService.listItems().isEmpty()) {
                // Create 5 sample items
                itemService.createItem("LAPTOP001", "Dell Laptop", "Dell Inspiron 15 3000", 
                                     "Electronics", 45000.00, 25, 5);
                
                itemService.createItem("MOUSE001", "Wireless Mouse", "Logitech MX Master 3", 
                                     "Electronics", 8500.00, 50, 10);
                
                itemService.createItem("DESK001", "Office Desk", "Standing Desk Adjustable", 
                                     "Furniture", 25000.00, 15, 3);
                
                itemService.createItem("CHAIR001", "Office Chair", "Ergonomic Office Chair", 
                                     "Furniture", 18000.00, 20, 5);
                
                itemService.createItem("PAPER001", "Copy Paper", "A4 Copy Paper 500 sheets", 
                                     "Office Supplies", 250.00, 200, 50);
                
                System.out.println("Sample items created successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error creating sample items: " + e.getMessage());
        }
    }
    
    private void initializeSuppliers() {
        try {
            // Check if suppliers already exist
            if (supplierService.listSuppliers().isEmpty()) {
                // Create 5 sample suppliers
                supplierService.createSupplier("TECH001", "TechWorld Solutions", "Alice Wang", 
                                             "011-2233-4455", "alice@techworld.com", 
                                             "123 Tech Street, Tech City", "Net 30");
                
                supplierService.createSupplier("OFFICE001", "OfficeMax Supply Co", "Bob Smith", 
                                             "011-2233-4456", "bob@officemax.com", 
                                             "456 Office Avenue, Business District", "Net 15");
                
                supplierService.createSupplier("FURN001", "Furniture Plus Ltd", "Carol Brown", 
                                             "011-2233-4457", "carol@furnitureplus.com", 
                                             "789 Furniture Row, Design Quarter", "Net 45");
                
                supplierService.createSupplier("ELEC001", "Electronics Direct", "Daniel Lee", 
                                             "011-2233-4458", "daniel@electronicsdirect.com", 
                                             "321 Electronics Blvd, Innovation Hub", "Net 30");
                
                supplierService.createSupplier("SUPPLY001", "General Supplies Inc", "Emma Davis", 
                                             "011-2233-4459", "emma@generalsupplies.com", 
                                             "654 Supply Chain Drive, Industrial Zone", "Net 30");
                
                System.out.println("Sample suppliers created successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error creating sample suppliers: " + e.getMessage());
        }
    }
}