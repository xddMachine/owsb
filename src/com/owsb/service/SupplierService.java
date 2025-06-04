package com.owsb.service;

import com.owsb.domain.Supplier;
import com.owsb.util.FileUtils;
import com.owsb.util.IDGenerator;
import com.owsb.util.Validators;
import java.util.ArrayList;
import java.util.List;

public class SupplierService {
    private static final String SUPPLIERS_FILE = "data/suppliers.txt";
    
    public List<Supplier> listSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        
        try {
            List<String> lines = FileUtils.readAllLines(SUPPLIERS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 9) {
                    Supplier supplier = createSupplierFromCSV(parts);
                    suppliers.add(supplier);
                }
            }
        } catch (Exception e) {
            System.err.println("Error listing suppliers: " + e.getMessage());
        }
        
        return suppliers;
    }
    
    public List<Supplier> getAllSuppliers() {
        return listSuppliers();
    }
    
    public Supplier getSupplierById(String supplierId) {
        try {
            List<String> lines = FileUtils.readAllLines(SUPPLIERS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 9 && parts[0].equals(supplierId)) {
                    return createSupplierFromCSV(parts);
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting supplier by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    public Supplier createSupplier(String supplierCode, String supplierName, String contactPerson,
                                  String phone, String email, String address, String paymentTerms) {
        String validation = Validators.validateSupplier(supplierCode, supplierName, contactPerson, phone, email);
        if (validation != null) {
            throw new IllegalArgumentException(validation);
        }
        
        if (isSupplierCodeExists(supplierCode)) {
            throw new IllegalArgumentException("Supplier code already exists");
        }
        
        String supplierId = IDGenerator.generateSupplierId();
        Supplier supplier = new Supplier(supplierId, supplierCode, supplierName, contactPerson,
                                        phone, email, address, paymentTerms, "ACTIVE");
        
        try {
            List<String> lines = FileUtils.readAllLines(SUPPLIERS_FILE);
            lines.add(supplier.toCSV());
            FileUtils.writeAllLines(SUPPLIERS_FILE, lines);
            return supplier;
        } catch (Exception e) {
            throw new RuntimeException("Error creating supplier: " + e.getMessage());
        }
    }
    
    public boolean updateSupplier(Supplier supplier) {
        try {
            List<String> lines = FileUtils.readAllLines(SUPPLIERS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 9 && parts[0].equals(supplier.getSupplierId())) {
                    lines.set(i, supplier.toCSV());
                    FileUtils.writeAllLines(SUPPLIERS_FILE, lines);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating supplier: " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean deleteSupplier(String supplierId) {
        try {
            List<String> lines = FileUtils.readAllLines(SUPPLIERS_FILE);
            List<String> newLines = new ArrayList<>();
            newLines.add(lines.get(0));
            
            boolean found = false;
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 9 && !parts[0].equals(supplierId)) {
                    newLines.add(lines.get(i));
                } else if (parts[0].equals(supplierId)) {
                    found = true;
                }
            }
            
            if (found) {
                FileUtils.writeAllLines(SUPPLIERS_FILE, newLines);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error deleting supplier: " + e.getMessage());
        }
        
        return false;
    }
    
    public Supplier findByCode(String supplierCode) {
        try {
            List<String> lines = FileUtils.readAllLines(SUPPLIERS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 9 && parts[1].equals(supplierCode)) {
                    return createSupplierFromCSV(parts);
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding supplier: " + e.getMessage());
        }
        
        return null;
    }
    
    public Supplier findById(String supplierId) {
        try {
            List<String> lines = FileUtils.readAllLines(SUPPLIERS_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 9 && parts[0].equals(supplierId)) {
                    return createSupplierFromCSV(parts);
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding supplier: " + e.getMessage());
        }
        
        return null;
    }
    
    private boolean isSupplierCodeExists(String supplierCode) {
        return findByCode(supplierCode) != null;
    }
    
    private Supplier createSupplierFromCSV(String[] parts) {
        String supplierId = parts[0];
        String supplierCode = parts[1];
        String supplierName = parts[2];
        String contactPerson = parts[3];
        String phone = parts[4];
        String email = parts[5];
        String address = parts[6];
        String paymentTerms = parts[7];
        String status = parts[8];
        
        return new Supplier(supplierId, supplierCode, supplierName, contactPerson,
                           phone, email, address, paymentTerms, status);
    }
}