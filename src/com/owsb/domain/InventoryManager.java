package com.owsb.domain;

public class InventoryManager extends User {
    
    public InventoryManager() {
        super();
    }
    
    public InventoryManager(String userId, String username, String password, 
                           String fullName, String phone, String email, String status) {
        super(userId, Role.INVENTORY_MANAGER, username, password, fullName, phone, email, status);
    }
}