package com.owsb.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class PurchaseOrder {
    private String poId;
    private String prId;
    private String supplierId;
    private String supplierName;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private String createdBy;
    private String approvedBy;
    private LocalDate approvedDate;
    private String status;
    private String paymentTerms;
    private String deliveryAddress;
    private String notes;
    private List<PurchaseOrderLine> lines;
    private String termsAndConditions;
    private double totalAmount;
    
    public PurchaseOrder() {
        this.lines = new ArrayList<>();
    }
    
    public PurchaseOrder(String poId, String prId, String supplierId, String supplierName,
                        LocalDate orderDate, LocalDate expectedDeliveryDate, String createdBy,
                        String status, String paymentTerms, String deliveryAddress) {
        this.poId = poId;
        this.prId = prId;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.orderDate = orderDate;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.createdBy = createdBy;
        this.status = status;
        this.paymentTerms = paymentTerms;
        this.deliveryAddress = deliveryAddress;
        this.lines = new ArrayList<>();
    }
    
    public String getPoId() { return poId; }
    public void setPoId(String poId) { this.poId = poId; }
    
    public String getPrId() { return prId; }
    public void setPrId(String prId) { this.prId = prId; }
    
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    
    public LocalDate getApprovedDate() { return approvedDate; }
    public void setApprovedDate(LocalDate approvedDate) { this.approvedDate = approvedDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public List<PurchaseOrderLine> getLines() { return lines; }
    public void setLines(List<PurchaseOrderLine> lines) { this.lines = lines; }
    
    public String getTermsAndConditions() { return termsAndConditions; }
    public void setTermsAndConditions(String termsAndConditions) { this.termsAndConditions = termsAndConditions; }
    
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public void setPoDate(LocalDate poDate) { this.orderDate = poDate; }
    
    public void addLine(PurchaseOrderLine line) {
        this.lines.add(line);
    }
    
    public double getTotalAmount() {
        return lines.stream().mapToDouble(line -> line.getQuantity() * line.getUnitPrice()).sum();
    }
    
    @Override
    public String toString() {
        return "PurchaseOrder{" +
                "poId='" + poId + '\'' +
                ", supplierId='" + supplierId + '\'' +
                ", orderDate=" + orderDate +
                ", status='" + status + '\'' +
                ", totalAmount=" + getTotalAmount() +
                '}';
    }
    
    public String toCSV() {
        return String.join(",", 
                          escapeCSV(poId), 
                          escapeCSV(prId), 
                          escapeCSV(supplierId), 
                          escapeCSV(supplierName),
                          escapeCSV(orderDate.toString()), 
                          escapeCSV(expectedDeliveryDate != null ? expectedDeliveryDate.toString() : ""),
                          escapeCSV(createdBy), 
                          escapeCSV(approvedBy != null ? approvedBy : ""),
                          escapeCSV(approvedDate != null ? approvedDate.toString() : ""),
                          escapeCSV(status), 
                          escapeCSV(paymentTerms != null ? paymentTerms : ""), 
                          escapeCSV(deliveryAddress != null ? deliveryAddress : ""),
                          escapeCSV(notes != null ? notes : ""));
    }
    
    private String escapeCSV(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}