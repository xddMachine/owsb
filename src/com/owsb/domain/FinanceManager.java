package com.owsb.domain;

public class FinanceManager extends User {
    
    public FinanceManager() {
        super();
    }
    
    public FinanceManager(String userId, String username, String password, 
                         String fullName, String phone, String email, String status) {
        super(userId, Role.FINANCE_MANAGER, username, password, fullName, phone, email, status);
    }
}