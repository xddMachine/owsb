package com.owsb.domain;

public class SalesManager extends User {
    
    public SalesManager() {
        super();
    }
    
    public SalesManager(String userId, String username, String password, 
                       String fullName, String phone, String email, String status) {
        super(userId, Role.SALES_MANAGER, username, password, fullName, phone, email, status);
    }
}