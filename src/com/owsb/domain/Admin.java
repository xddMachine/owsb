package com.owsb.domain;

public class Admin extends User {
    
    public Admin() {
        super();
    }
    
    public Admin(String userId, String username, String password, 
                 String fullName, String phone, String email, String status) {
        super(userId, Role.ADMIN, username, password, fullName, phone, email, status);
    }
}