package com.owsb.service;

import com.owsb.domain.*;
import com.owsb.util.FileUtils;
import com.owsb.util.IDGenerator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class POService {
    private static final String PO_FILE = "data/purchase_orders.txt";
    private static final String PO_LINES_FILE = "data/po_lines.txt";
    private ItemService itemService;
    
    public POService() {
        this.itemService = new ItemService();
    }
    
    public String generatePOId() {
        return IDGenerator.generatePOId();
    }
    
    public boolean createPO(PurchaseOrder po, List<PurchaseOrderLine> lines) {
        try {
            // Save PO
            String poLine = po.toCSV();
            FileUtils.appendToFile(PO_FILE, poLine);
            
            // Save PO lines
            for (PurchaseOrderLine line : lines) {
                String lineCsv = line.toCSV();
                FileUtils.appendToFile(PO_LINES_FILE, lineCsv);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error creating PO: " + e.getMessage());
            return false;
        }
    }
    
    public PurchaseOrder generatePOFromPR(PurchaseRequisition pr, String supplierId, String supplierName,
                                         LocalDate expectedDeliveryDate, String createdBy,
                                         String paymentTerms, String deliveryAddress) {
        List<PurchaseOrderLine> poLines = new ArrayList<>();
        
        for (PurchaseRequisitionLine prLine : pr.getLines()) {
            PurchaseOrderLine poLine = new PurchaseOrderLine(
                "", // PO ID will be set in generatePO
                prLine.getItemId(),
                prLine.getItemCode(),
                prLine.getItemName(),
                prLine.getQuantity(),
                prLine.getUnit(),
                prLine.getEstimatedPrice(),
                prLine.getSpecifications()
            );
            poLines.add(poLine);
        }
        
        return generatePO(pr.getPrId(), supplierId, supplierName, expectedDeliveryDate,
                         createdBy, paymentTerms, deliveryAddress, poLines);
    }
    
    public PurchaseOrder generatePO(String prId, String supplierId, String supplierName,
                                   LocalDate expectedDeliveryDate, String createdBy,
                                   String paymentTerms, String deliveryAddress,
                                   List<PurchaseOrderLine> lines) {
        String poId = IDGenerator.generatePOId();
        PurchaseOrder po = new PurchaseOrder(poId, prId, supplierId, supplierName,
                                            LocalDate.now(), expectedDeliveryDate, createdBy,
                                            "PENDING_APPROVAL", paymentTerms, deliveryAddress);
        
        try {
            List<String> poLines = FileUtils.readAllLines(PO_FILE);
            poLines.add(po.toCSV());
            FileUtils.writeAllLines(PO_FILE, poLines);
            
            List<String> lineFileLines = FileUtils.readAllLines(PO_LINES_FILE);
            for (PurchaseOrderLine line : lines) {
                line.setPoId(poId);
                lineFileLines.add(line.toCSV());
            }
            FileUtils.writeAllLines(PO_LINES_FILE, lineFileLines);
            
            po.setLines(lines);
            return po;
        } catch (Exception e) {
            throw new RuntimeException("Error generating PO: " + e.getMessage());
        }
    }
    
    public boolean approvePO(String poId, String approvedBy) {
        PurchaseOrder po = findPOById(poId);
        if (po != null && "CONFIRMED".equals(po.getStatus())) {
            po.setStatus("APPROVED");
            po.setApprovedBy(approvedBy);
            po.setApprovedDate(LocalDate.now());
            return updatePO(po);
        }
        return false;
    }
    
    public boolean rejectPO(String poId, String rejectedBy, String reason) {
        PurchaseOrder po = findPOById(poId);
        if (po != null && "CONFIRMED".equals(po.getStatus())) {
            po.setStatus("REJECTED");
            po.setApprovedBy(rejectedBy);
            po.setApprovedDate(LocalDate.now());
            po.setNotes(reason);
            return updatePO(po);
        }
        return false;
    }
    
    public List<PurchaseOrder> listPOs() {
        List<PurchaseOrder> pos = new ArrayList<>();
        
        try {
            List<String> lines = FileUtils.readAllLines(PO_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 13) {
                    PurchaseOrder po = createPOFromCSV(parts);
                    po.setLines(loadPOLines(po.getPoId()));
                    pos.add(po);
                }
            }
        } catch (Exception e) {
            System.err.println("Error listing POs: " + e.getMessage());
        }
        
        return pos;
    }
    
    public List<PurchaseOrder> getPOsByStatus(String status) {
        List<PurchaseOrder> filteredPOs = new ArrayList<>();
        List<PurchaseOrder> allPOs = listPOs();
        
        for (PurchaseOrder po : allPOs) {
            if (status.equals(po.getStatus())) {
                filteredPOs.add(po);
            }
        }
        
        return filteredPOs;
    }
    
    public List<PurchaseOrderLine> getPOLines(String poId) {
        return loadPOLines(poId);
    }
    
    public boolean updatePO(PurchaseOrder po) {
        try {
            List<String> lines = FileUtils.readAllLines(PO_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 13 && parts[0].equals(po.getPoId())) {
                    lines.set(i, po.toCSV());
                    FileUtils.writeAllLines(PO_FILE, lines);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating PO: " + e.getMessage());
        }
        
        return false;
    }
    
    public PurchaseOrder findPOById(String poId) {
        try {
            List<String> lines = FileUtils.readAllLines(PO_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 13 && parts[0].equals(poId)) {
                    PurchaseOrder po = createPOFromCSV(parts);
                    po.setLines(loadPOLines(poId));
                    return po;
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding PO: " + e.getMessage());
        }
        
        return null;
    }
    
    public List<PurchaseOrder> getPendingApprovalPOs() {
        List<PurchaseOrder> allPOs = listPOs();
        List<PurchaseOrder> pendingPOs = new ArrayList<>();
        
        for (PurchaseOrder po : allPOs) {
            if ("PENDING_APPROVAL".equals(po.getStatus())) {
                pendingPOs.add(po);
            }
        }
        
        return pendingPOs;
    }
    
    public List<PurchaseOrder> getApprovedPOs() {
        List<PurchaseOrder> allPOs = listPOs();
        List<PurchaseOrder> approvedPOs = new ArrayList<>();
        
        for (PurchaseOrder po : allPOs) {
            if ("APPROVED".equals(po.getStatus()) || "CONFIRMED".equals(po.getStatus()) || "RECEIVED".equals(po.getStatus())) {
                approvedPOs.add(po);
            }
        }
        
        return approvedPOs;
    }
    
    public List<PurchaseOrder> getDraftPOs() {
        List<PurchaseOrder> allPOs = listPOs();
        List<PurchaseOrder> draftPOs = new ArrayList<>();
        
        for (PurchaseOrder po : allPOs) {
            if ("DRAFT".equals(po.getStatus())) {
                draftPOs.add(po);
            }
        }
        
        return draftPOs;
    }
    
    public List<PurchaseOrder> getConfirmedPOs() {
        List<PurchaseOrder> allPOs = listPOs();
        List<PurchaseOrder> confirmedPOs = new ArrayList<>();
        
        for (PurchaseOrder po : allPOs) {
            if ("CONFIRMED".equals(po.getStatus())) {
                confirmedPOs.add(po);
            }
        }
        
        return confirmedPOs;
    }
    
    public boolean updateStockOnReceive(String poId, String itemId, int receivedQuantity) {
        try {
            List<String> lines = FileUtils.readAllLines(PO_LINES_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 10 && parts[0].equals(poId) && parts[1].equals(itemId)) {
                    int currentReceived = Integer.parseInt(parts[8]);
                    int newReceived = currentReceived + receivedQuantity;
                    parts[8] = String.valueOf(newReceived);
                    
                    int orderedQuantity = Integer.parseInt(parts[4]);
                    if (newReceived >= orderedQuantity) {
                        parts[9] = "COMPLETED";
                    } else {
                        parts[9] = "PARTIALLY_RECEIVED";
                    }
                    
                    lines.set(i, String.join(",", parts));
                    FileUtils.writeAllLines(PO_LINES_FILE, lines);
                    
                    Item item = itemService.findById(itemId);
                    if (item != null) {
                        int newStock = item.getStockQuantity() + receivedQuantity;
                        itemService.updateStock(itemId, newStock);
                    }
                    
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating stock on receive: " + e.getMessage());
        }
        
        return false;
    }
    
    private List<PurchaseOrderLine> loadPOLines(String poId) {
        List<PurchaseOrderLine> lines = new ArrayList<>();
        
        try {
            List<String> fileLines = FileUtils.readAllLines(PO_LINES_FILE);
            
            for (int i = 1; i < fileLines.size(); i++) {
                String[] parts = fileLines.get(i).split(",");
                if (parts.length >= 10 && parts[0].equals(poId)) {
                    PurchaseOrderLine line = createPOLineFromCSV(parts);
                    lines.add(line);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading PO lines: " + e.getMessage());
        }
        
        return lines;
    }
    
    private PurchaseOrder createPOFromCSV(String[] parts) {
        String poId = parts[0];
        String prId = parts[1];
        String supplierId = parts[2];
        String supplierName = parts[3];
        LocalDate orderDate = LocalDate.parse(parts[4]);
        LocalDate expectedDeliveryDate = !parts[5].isEmpty() ? LocalDate.parse(parts[5]) : null;
        String createdBy = parts[6];
        String approvedBy = !parts[7].isEmpty() ? parts[7] : null;
        LocalDate approvedDate = !parts[8].isEmpty() ? LocalDate.parse(parts[8]) : null;
        String status = parts[9];
        String paymentTerms = parts[10];
        String deliveryAddress = parts[11];
        String notes = parts[12];
        
        PurchaseOrder po = new PurchaseOrder(poId, prId, supplierId, supplierName,
                                           orderDate, expectedDeliveryDate, createdBy,
                                           status, paymentTerms, deliveryAddress);
        po.setApprovedBy(approvedBy);
        po.setApprovedDate(approvedDate);
        po.setNotes(notes);
        
        return po;
    }
    
    private PurchaseOrderLine createPOLineFromCSV(String[] parts) {
        String poId = parts[0];
        String itemId = parts[1];
        String itemCode = parts[2];
        String itemName = parts[3];
        int quantity = Integer.parseInt(parts[4]);
        String unit = parts[5];
        double unitPrice = Double.parseDouble(parts[6]);
        String specifications = parts[7];
        int receivedQuantity = Integer.parseInt(parts[8]);
        String status = parts[9];
        
        PurchaseOrderLine line = new PurchaseOrderLine(poId, itemId, itemCode, itemName,
                                                      quantity, unit, unitPrice, specifications);
        line.setReceivedQuantity(receivedQuantity);
        line.setStatus(status);
        
        return line;
    }
    
    /**
     * Check if all items in a PO are fully received and update PO status accordingly
     */
    public boolean checkAndUpdatePOStatus(String poId) {
        try {
            List<PurchaseOrderLine> poLines = getPOLines(poId);
            boolean allReceived = true;
            
            // Check if all items are fully received
            for (PurchaseOrderLine line : poLines) {
                if (line.getReceivedQuantity() < line.getQuantity()) {
                    allReceived = false;
                    break;
                }
            }
            
            // Update PO status if all items are received
            if (allReceived) {
                PurchaseOrder po = findPOById(poId);
                if (po != null && "CONFIRMED".equals(po.getStatus())) {
                    po.setStatus("RECEIVED");
                    updatePO(po);
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error checking PO status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update received quantity for a specific PO line and check PO status
     */
    public boolean updatePOLineReceived(String poId, String itemId, int newReceivedQuantity) {
        try {
            List<String> lines = FileUtils.readAllLines(PO_LINES_FILE);
            boolean updated = false;
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 10 && parts[0].equals(poId) && parts[1].equals(itemId)) {
                    // Update received quantity
                    parts[8] = String.valueOf(newReceivedQuantity);
                    
                    // Update line status
                    int orderedQuantity = Integer.parseInt(parts[4]);
                    if (newReceivedQuantity >= orderedQuantity) {
                        parts[9] = "RECEIVED";
                    } else if (newReceivedQuantity > 0) {
                        parts[9] = "PARTIALLY_RECEIVED";
                    } else {
                        parts[9] = "PENDING";
                    }
                    
                    lines.set(i, String.join(",", parts));
                    updated = true;
                    break;
                }
            }
            
            if (updated) {
                FileUtils.writeAllLines(PO_LINES_FILE, lines);
                
                // Check and update overall PO status
                checkAndUpdatePOStatus(poId);
                
                return true;
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error updating PO line received quantity: " + e.getMessage());
            return false;
        }
    }
}