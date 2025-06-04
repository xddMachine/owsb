package com.owsb.service;

import com.owsb.domain.*;
import com.owsb.util.FileUtils;
import java.util.List;

public class AuthService {
    private static final String USERS_FILE = "data/users.txt";
    private AuditService auditService;
    
    public AuthService() {
        this.auditService = new AuditService();
    }
    
    public User login(String username, String password) {
        try {
            List<String> lines = FileUtils.readAllLines(USERS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 8) {
                    String storedUsername = parts[2];
                    String storedPassword = parts[3];
                    
                    if (username.equals(storedUsername) && password.equals(storedPassword)) {
                        User user = createUserFromCSV(parts);
                        auditService.logLoginAttempt(username, true, "Successful login");
                        return user;
                    }
                }
            }
            
            auditService.logLoginAttempt(username, false, "Invalid credentials");
        } catch (Exception e) {
            auditService.logLoginAttempt(username, false, "Login error: " + e.getMessage());
            System.err.println("Error during login: " + e.getMessage());
        }
        
        return null;
    }
    
    private User createUserFromCSV(String[] parts) {
        String userId = parts[0];
        Role role = Role.valueOf(parts[1]);
        String username = parts[2];
        String password = parts[3];
        String fullName = parts[4];
        String phone = parts[5];
        String email = parts[6];
        String status = parts[7];
        
        switch (role) {
            case ADMIN:
                return new Admin(userId, username, password, fullName, phone, email, status);
            case SALES_MANAGER:
                return new SalesManager(userId, username, password, fullName, phone, email, status);
            case PURCHASE_MANAGER:
                return new PurchaseManager(userId, username, password, fullName, phone, email, status);
            case INVENTORY_MANAGER:
                return new InventoryManager(userId, username, password, fullName, phone, email, status);
            case FINANCE_MANAGER:
                return new FinanceManager(userId, username, password, fullName, phone, email, status);
            default:
                return null;
        }
    }
    
    public boolean isValidSession(User user) {
        return user != null && "ACTIVE".equals(user.getStatus());
    }
    
    public boolean hasPermission(User user, String action) {
        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            return false;
        }
        
        Role role = user.getRole();
        
        switch (action) {
            case "USER_MANAGEMENT":
                return role == Role.ADMIN;
            case "ITEM_CRUD":
                return role == Role.ADMIN || role == Role.SALES_MANAGER;
            case "SUPPLIER_CRUD":
                return role == Role.ADMIN || role == Role.SALES_MANAGER;
            case "SALES_ENTRY":
                return role == Role.SALES_MANAGER;
            case "PR_MANAGEMENT":
                return role == Role.SALES_MANAGER;
            case "PO_GENERATION":
                return role == Role.PURCHASE_MANAGER;
            case "PO_APPROVAL":
                return role == Role.FINANCE_MANAGER;
            case "STOCK_UPDATE":
                return role == Role.INVENTORY_MANAGER;
            case "PAYMENT_PROCESSING":
                return role == Role.FINANCE_MANAGER;
            case "VIEW_REPORTS":
                return true;
            default:
                return false;
        }
    }
}