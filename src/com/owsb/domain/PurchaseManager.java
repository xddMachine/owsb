package com.owsb.domain;

public class PurchaseManager extends User {
    
    public PurchaseManager() {
        super();
    }
    
    public PurchaseManager(String userId, String username, String password, 
                          String fullName, String phone, String email, String status) {
        super(userId, Role.PURCHASE_MANAGER, username, password, fullName, phone, email, status);
    }
}