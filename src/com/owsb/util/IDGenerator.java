package com.owsb.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class IDGenerator {
    private static final String COUNTER_FILE = "data/id_counter.txt";
    private static Map<String, Integer> counters = new HashMap<>();
    
    static {
        loadCounters();
    }
    
    public static synchronized String generateUserId() {
        return generateId("USER", "U");
    }
    
    public static synchronized String generateItemId() {
        return generateId("ITEM", "I");
    }
    
    public static synchronized String generateSupplierId() {
        return generateId("SUPPLIER", "S");
    }
    
    public static synchronized String generatePRId() {
        return generateId("PR", "PR");
    }
    
    public static synchronized String generatePOId() {
        return generateId("PO", "PO");
    }
    
    public static synchronized String generateSalesRecordId() {
        return generateId("SALES", "SR");
    }
    
    private static String generateId(String type, String prefix) {
        int current = counters.getOrDefault(type, 0) + 1;
        counters.put(type, current);
        saveCounters();
        return String.format("%s%04d", prefix, current);
    }
    
    private static void loadCounters() {
        File file = new File(COUNTER_FILE);
        if (!file.exists()) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    counters.put(parts[0], Integer.parseInt(parts[1]));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading ID counters: " + e.getMessage());
        }
    }
    
    private static void saveCounters() {
        File file = new File(COUNTER_FILE);
        file.getParentFile().mkdirs();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (Map.Entry<String, Integer> entry : counters.entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }
        } catch (IOException e) {
            System.err.println("Error saving ID counters: " + e.getMessage());
        }
    }
}