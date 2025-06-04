package com.owsb.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    
    public static List<String> readAllLines(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        File file = new File(filePath);
        
        if (!file.exists()) {
            return lines;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        
        return lines;
    }
    
    public static void writeAllLines(String filePath, List<String> lines) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.println(line);
            }
        }
    }
    
    public static void appendLine(String filePath, String line) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            writer.println(line);
        }
    }
    
    public static void appendToFile(String filePath, String content) throws IOException {
        appendLine(filePath, content);
    }
    
    public static boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }
    
    public static void createDirectoryIfNotExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    public static void initializeDataFiles() {
        createDirectoryIfNotExists("data");
        
        try {
            if (!fileExists("data/users.txt")) {
                List<String> defaultUsers = new ArrayList<>();
                defaultUsers.add("userId,role,username,password,fullName,phone,email,status");
                defaultUsers.add("U0001,ADMIN,admin,Admin@123,John Doe,012-3456789,john@example.com,ACTIVE");
                writeAllLines("data/users.txt", defaultUsers);
                
                // Initialize user counter to 1 since admin is U0001
                if (!fileExists("data/id_counter.txt")) {
                    List<String> counters = new ArrayList<>();
                    counters.add("USER,1");
                    writeAllLines("data/id_counter.txt", counters);
                }
            }
            
            if (!fileExists("data/items.txt")) {
                List<String> headers = new ArrayList<>();
                headers.add("itemId,itemCode,itemName,description,category,unitPrice,stockQuantity,reorderLevel,status");
                writeAllLines("data/items.txt", headers);
            }
            
            if (!fileExists("data/suppliers.txt")) {
                List<String> headers = new ArrayList<>();
                headers.add("supplierId,supplierCode,supplierName,contactPerson,phone,email,address,paymentTerms,status");
                writeAllLines("data/suppliers.txt", headers);
            }
            
            if (!fileExists("data/purchase_requisitions.txt")) {
                List<String> headers = new ArrayList<>();
                headers.add("prId,requestedBy,requestDate,department,priority,justification,status");
                writeAllLines("data/purchase_requisitions.txt", headers);
            }
            
            if (!fileExists("data/purchase_orders.txt")) {
                List<String> headers = new ArrayList<>();
                headers.add("poId,prId,supplierId,supplierName,orderDate,expectedDeliveryDate,createdBy,approvedBy,approvedDate,status,paymentTerms,deliveryAddress,notes");
                writeAllLines("data/purchase_orders.txt", headers);
            }
            
            if (!fileExists("data/daily_sales.txt")) {
                List<String> headers = new ArrayList<>();
                headers.add("recordId,salesDate,itemId,itemCode,itemName,quantitySold,unitPrice,totalAmount,salesPerson,customerInfo,notes");
                writeAllLines("data/daily_sales.txt", headers);
            }
            
            if (!fileExists("data/pr_lines.txt")) {
                List<String> headers = new ArrayList<>();
                headers.add("prId,itemId,itemCode,itemName,quantity,unit,estimatedPrice,specifications");
                writeAllLines("data/pr_lines.txt", headers);
            }
            
            if (!fileExists("data/po_lines.txt")) {
                List<String> headers = new ArrayList<>();
                headers.add("poId,itemId,itemCode,itemName,quantity,unit,unitPrice,specifications,receivedQuantity,status");
                writeAllLines("data/po_lines.txt", headers);
            }
            
        } catch (IOException e) {
            System.err.println("Error initializing data files: " + e.getMessage());
        }
    }
}