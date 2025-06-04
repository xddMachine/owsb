package com.owsb.util;

import java.util.regex.Pattern;

public class Validators {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[0-9-+()\\s]{10,15}$");
    
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
    
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }
    
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    public static boolean isPositiveNumber(double value) {
        return value > 0;
    }
    
    public static boolean isPositiveInteger(int value) {
        return value > 0;
    }
    
    public static boolean isNonNegativeInteger(int value) {
        return value >= 0;
    }
    
    public static String validateUser(String username, String password, String email, String phone, String fullName) {
        if (!isNotEmpty(username)) {
            return "Username is required";
        }
        if (!isValidUsername(username)) {
            return "Username must be 3-20 characters, alphanumeric and underscore only";
        }
        if (!isValidPassword(password)) {
            return "Password must be at least 6 characters";
        }
        if (!isNotEmpty(email) || !isValidEmail(email)) {
            return "Valid email is required";
        }
        if (!isNotEmpty(phone) || !isValidPhone(phone)) {
            return "Valid phone number is required";
        }
        if (!isNotEmpty(fullName)) {
            return "Full name is required";
        }
        return null;
    }
    
    public static String validateItem(String itemCode, String itemName, double unitPrice, int stockQuantity, int reorderLevel) {
        if (!isNotEmpty(itemCode)) {
            return "Item code is required";
        }
        if (!isNotEmpty(itemName)) {
            return "Item name is required";
        }
        if (!isPositiveNumber(unitPrice)) {
            return "Unit price must be positive";
        }
        if (!isNonNegativeInteger(stockQuantity)) {
            return "Stock quantity must be non-negative";
        }
        if (!isNonNegativeInteger(reorderLevel)) {
            return "Reorder level must be non-negative";
        }
        return null;
    }
    
    public static String validateSupplier(String supplierCode, String supplierName, String contactPerson, String phone, String email) {
        if (!isNotEmpty(supplierCode)) {
            return "Supplier code is required";
        }
        if (!isNotEmpty(supplierName)) {
            return "Supplier name is required";
        }
        if (!isNotEmpty(contactPerson)) {
            return "Contact person is required";
        }
        if (!isNotEmpty(phone) || !isValidPhone(phone)) {
            return "Valid phone number is required";
        }
        if (!isNotEmpty(email) || !isValidEmail(email)) {
            return "Valid email is required";
        }
        return null;
    }
}