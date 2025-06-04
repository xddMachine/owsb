package com.owsb.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class PurchaseRequisition {
    private String prId;
    private String requestedBy;
    private LocalDate requestDate;
    private String department;
    private String priority;
    private String justification;
    private String status;
    private List<PurchaseRequisitionLine> lines;
    
    public PurchaseRequisition() {
        this.lines = new ArrayList<>();
    }
    
    public PurchaseRequisition(String prId, String requestedBy, LocalDate requestDate,
                              String department, String priority, String justification, String status) {
        this.prId = prId;
        this.requestedBy = requestedBy;
        this.requestDate = requestDate;
        this.department = department;
        this.priority = priority;
        this.justification = justification;
        this.status = status;
        this.lines = new ArrayList<>();
    }
    
    public String getPrId() { return prId; }
    public void setPrId(String prId) { this.prId = prId; }
    
    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }
    
    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<PurchaseRequisitionLine> getLines() { return lines; }
    public void setLines(List<PurchaseRequisitionLine> lines) { this.lines = lines; }
    
    public void addLine(PurchaseRequisitionLine line) {
        this.lines.add(line);
    }
    
    public double getTotalAmount() {
        return lines.stream().mapToDouble(line -> line.getQuantity() * line.getEstimatedPrice()).sum();
    }
    
    @Override
    public String toString() {
        return "PurchaseRequisition{" +
                "prId='" + prId + '\'' +
                ", requestedBy='" + requestedBy + '\'' +
                ", requestDate=" + requestDate +
                ", status='" + status + '\'' +
                ", totalAmount=" + getTotalAmount() +
                '}';
    }
    
    public String toCSV() {
        return String.join(",", prId, requestedBy, requestDate.toString(),
                          department, priority, justification, status);
    }
}