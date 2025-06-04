package com.owsb.service;

import com.owsb.domain.PurchaseRequisition;
import com.owsb.domain.PurchaseRequisitionLine;
import com.owsb.util.FileUtils;
import com.owsb.util.IDGenerator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PRService {
    private static final String PR_FILE = "data/purchase_requisitions.txt";
    private static final String PR_LINES_FILE = "data/pr_lines.txt";
    
    public PurchaseRequisition createPR(String requestedBy, String department, String priority, 
                                       String justification, List<PurchaseRequisitionLine> lines) {
        String prId = IDGenerator.generatePRId();
        PurchaseRequisition pr = new PurchaseRequisition(prId, requestedBy, LocalDate.now(),
                                                        department, priority, justification, "PENDING");
        
        try {
            List<String> prLines = FileUtils.readAllLines(PR_FILE);
            prLines.add(pr.toCSV());
            FileUtils.writeAllLines(PR_FILE, prLines);
            
            List<String> lineFileLines = FileUtils.readAllLines(PR_LINES_FILE);
            for (PurchaseRequisitionLine line : lines) {
                line.setPrId(prId);
                lineFileLines.add(line.toCSV());
            }
            FileUtils.writeAllLines(PR_LINES_FILE, lineFileLines);
            
            pr.setLines(lines);
            return pr;
        } catch (Exception e) {
            throw new RuntimeException("Error creating PR: " + e.getMessage());
        }
    }
    
    public String createPRForLowStockItem(String itemId, int quantity, String requestedBy, String priority) {
        try {
            String prId = IDGenerator.generatePRId();
            PurchaseRequisition pr = new PurchaseRequisition(prId, requestedBy, LocalDate.now(),
                                                            "INVENTORY", priority, "Low stock alert", "PENDING");
            
            // Save PR
            List<String> prLines = FileUtils.readAllLines(PR_FILE);
            prLines.add(pr.toCSV());
            FileUtils.writeAllLines(PR_FILE, prLines);
            
            // Get item details for PR line
            ItemService itemService = new ItemService();
            com.owsb.domain.Item item = itemService.findById(itemId);
            
            // Create PR line for the item
            PurchaseRequisitionLine prLine = new PurchaseRequisitionLine(prId, itemId, 
                item != null ? item.getItemCode() : "", 
                item != null ? item.getItemName() : "", 
                quantity, "pieces", 0.0, "Low stock replenishment");
            
            // Save PR line
            List<String> lineFileLines = FileUtils.readAllLines(PR_LINES_FILE);
            lineFileLines.add(prLine.toCSV());
            FileUtils.writeAllLines(PR_LINES_FILE, lineFileLines);
            
            return prId;
        } catch (Exception e) {
            System.err.println("Error creating PR for low stock item: " + e.getMessage());
            return null;
        }
    }
    
    public List<PurchaseRequisition> listPRs() {
        List<PurchaseRequisition> prs = new ArrayList<>();
        
        try {
            List<String> lines = FileUtils.readAllLines(PR_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 7) {
                    PurchaseRequisition pr = createPRFromCSV(parts);
                    pr.setLines(loadPRLines(pr.getPrId()));
                    prs.add(pr);
                }
            }
        } catch (Exception e) {
            System.err.println("Error listing PRs: " + e.getMessage());
        }
        
        return prs;
    }
    
    public boolean updatePR(PurchaseRequisition pr) {
        try {
            List<String> lines = FileUtils.readAllLines(PR_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 7 && parts[0].equals(pr.getPrId())) {
                    lines.set(i, pr.toCSV());
                    FileUtils.writeAllLines(PR_FILE, lines);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating PR: " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean deletePR(String prId) {
        try {
            List<String> lines = FileUtils.readAllLines(PR_FILE);
            List<String> newLines = new ArrayList<>();
            newLines.add(lines.get(0));
            
            boolean found = false;
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 7 && !parts[0].equals(prId)) {
                    newLines.add(lines.get(i));
                } else if (parts[0].equals(prId)) {
                    found = true;
                }
            }
            
            if (found) {
                FileUtils.writeAllLines(PR_FILE, newLines);
                deletePRLines(prId);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error deleting PR: " + e.getMessage());
        }
        
        return false;
    }
    
    public PurchaseRequisition findPRById(String prId) {
        try {
            List<String> lines = FileUtils.readAllLines(PR_FILE);
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 7 && parts[0].equals(prId)) {
                    PurchaseRequisition pr = createPRFromCSV(parts);
                    pr.setLines(loadPRLines(prId));
                    return pr;
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding PR: " + e.getMessage());
        }
        
        return null;
    }
    
    public List<PurchaseRequisition> getPendingPRs() {
        List<PurchaseRequisition> allPRs = listPRs();
        List<PurchaseRequisition> pendingPRs = new ArrayList<>();
        
        for (PurchaseRequisition pr : allPRs) {
            if ("PENDING".equals(pr.getStatus())) {
                pendingPRs.add(pr);
            }
        }
        
        return pendingPRs;
    }
    
    public boolean approvePR(String prId, String approvedBy) {
        PurchaseRequisition pr = findPRById(prId);
        if (pr != null && "PENDING".equals(pr.getStatus())) {
            pr.setStatus("APPROVED");
            return updatePR(pr);
        }
        return false;
    }
    
    public boolean rejectPR(String prId, String rejectedBy) {
        PurchaseRequisition pr = findPRById(prId);
        if (pr != null && "PENDING".equals(pr.getStatus())) {
            pr.setStatus("REJECTED");
            return updatePR(pr);
        }
        return false;
    }
    
    public List<PurchaseRequisition> getPRsByStatus(String status) {
        List<PurchaseRequisition> allPRs = listPRs();
        List<PurchaseRequisition> filteredPRs = new ArrayList<>();
        
        for (PurchaseRequisition pr : allPRs) {
            if (status.equalsIgnoreCase(pr.getStatus())) {
                filteredPRs.add(pr);
            }
        }
        
        return filteredPRs;
    }
    
    public List<PurchaseRequisition> getPRsByUser(String username) {
        List<PurchaseRequisition> allPRs = listPRs();
        List<PurchaseRequisition> userPRs = new ArrayList<>();
        
        for (PurchaseRequisition pr : allPRs) {
            if (username.equals(pr.getRequestedBy())) {
                userPRs.add(pr);
            }
        }
        
        return userPRs;
    }
    
    public boolean updatePRLines(String prId, List<PurchaseRequisitionLine> newLines) {
        try {
            deletePRLines(prId);
            
            List<String> lineFileLines = FileUtils.readAllLines(PR_LINES_FILE);
            for (PurchaseRequisitionLine line : newLines) {
                line.setPrId(prId);
                lineFileLines.add(line.toCSV());
            }
            FileUtils.writeAllLines(PR_LINES_FILE, lineFileLines);
            
            return true;
        } catch (Exception e) {
            System.err.println("Error updating PR lines: " + e.getMessage());
            return false;
        }
    }
    
    private List<PurchaseRequisitionLine> loadPRLines(String prId) {
        List<PurchaseRequisitionLine> lines = new ArrayList<>();
        
        try {
            List<String> fileLines = FileUtils.readAllLines(PR_LINES_FILE);
            
            for (int i = 1; i < fileLines.size(); i++) {
                String[] parts = fileLines.get(i).split(",");
                if (parts.length >= 8 && parts[0].equals(prId)) {
                    PurchaseRequisitionLine line = createPRLineFromCSV(parts);
                    lines.add(line);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading PR lines: " + e.getMessage());
        }
        
        return lines;
    }
    
    private void deletePRLines(String prId) {
        try {
            List<String> lines = FileUtils.readAllLines(PR_LINES_FILE);
            List<String> newLines = new ArrayList<>();
            newLines.add(lines.get(0));
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 8 && !parts[0].equals(prId)) {
                    newLines.add(lines.get(i));
                }
            }
            
            FileUtils.writeAllLines(PR_LINES_FILE, newLines);
        } catch (Exception e) {
            System.err.println("Error deleting PR lines: " + e.getMessage());
        }
    }
    
    private PurchaseRequisition createPRFromCSV(String[] parts) {
        String prId = parts[0];
        String requestedBy = parts[1];
        LocalDate requestDate = LocalDate.parse(parts[2]);
        String department = parts[3];
        String priority = parts[4];
        String justification = parts[5];
        String status = parts[6];
        
        return new PurchaseRequisition(prId, requestedBy, requestDate, department, 
                                     priority, justification, status);
    }
    
    private PurchaseRequisitionLine createPRLineFromCSV(String[] parts) {
        String prId = parts[0];
        String itemId = parts[1];
        String itemCode = parts[2];
        String itemName = parts[3];
        int quantity = Integer.parseInt(parts[4]);
        String unit = parts[5];
        double estimatedPrice = Double.parseDouble(parts[6]);
        String specifications = parts[7];
        
        return new PurchaseRequisitionLine(prId, itemId, itemCode, itemName,
                                         quantity, unit, estimatedPrice, specifications);
    }
    
    public PurchaseRequisition getPRById(String prId) {
        return findPRById(prId);
    }
    
    public List<PurchaseRequisitionLine> getPRLines(String prId) {
        return loadPRLines(prId);
    }
    
    public boolean updatePRStatus(String prId, String status) {
        PurchaseRequisition pr = findPRById(prId);
        if (pr != null) {
            pr.setStatus(status);
            return updatePR(pr);
        }
        return false;
    }
}