package com.owsb.service;

import com.owsb.domain.*;
import com.owsb.util.FileUtils;
import com.owsb.util.IDGenerator;
import com.owsb.util.Validators;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private static final String USERS_FILE = "data/users.txt";
    
    public List<User> listUsers() {
        List<User> users = new ArrayList<>();
        
        try {
            List<String> lines = FileUtils.readAllLines(USERS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 8) {
                    User user = createUserFromCSV(parts);
                    if (user != null) {
                        users.add(user);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error listing users: " + e.getMessage());
        }
        
        return users;
    }
    
    public User createUser(Role role, String username, String password, String fullName, 
                          String phone, String email) {
        String validation = Validators.validateUser(username, password, email, phone, fullName);
        if (validation != null) {
            throw new IllegalArgumentException(validation);
        }
        
        if (isUsernameExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        String userId = IDGenerator.generateUserId();
        User user = createUserByRole(userId, role, username, password, fullName, phone, email, "ACTIVE");
        
        try {
            List<String> lines = FileUtils.readAllLines(USERS_FILE);
            lines.add(user.toCSV());
            FileUtils.writeAllLines(USERS_FILE, lines);
            return user;
        } catch (Exception e) {
            throw new RuntimeException("Error creating user: " + e.getMessage());
        }
    }
    
    public boolean updateUser(User user) {
        try {
            List<String> lines = FileUtils.readAllLines(USERS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 8 && parts[0].equals(user.getUserId())) {
                    lines.set(i, user.toCSV());
                    FileUtils.writeAllLines(USERS_FILE, lines);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean deleteUser(String userId) {
        try {
            List<String> lines = FileUtils.readAllLines(USERS_FILE);
            List<String> newLines = new ArrayList<>();
            newLines.add(lines.get(0));
            
            boolean found = false;
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 8 && !parts[0].equals(userId)) {
                    newLines.add(lines.get(i));
                } else if (parts[0].equals(userId)) {
                    found = true;
                }
            }
            
            if (found) {
                FileUtils.writeAllLines(USERS_FILE, newLines);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
        
        return false;
    }
    
    public User findUserById(String userId) {
        try {
            List<String> lines = FileUtils.readAllLines(USERS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 8 && parts[0].equals(userId)) {
                    return createUserFromCSV(parts);
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        
        return null;
    }
    
    private boolean isUsernameExists(String username) {
        try {
            List<String> lines = FileUtils.readAllLines(USERS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 8 && parts[2].equals(username)) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking username: " + e.getMessage());
        }
        
        return false;
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
        
        return createUserByRole(userId, role, username, password, fullName, phone, email, status);
    }
    
    private User createUserByRole(String userId, Role role, String username, String password,
                                 String fullName, String phone, String email, String status) {
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
}