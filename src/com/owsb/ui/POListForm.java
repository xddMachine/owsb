package com.owsb.ui;

import com.owsb.domain.*;
import com.owsb.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class POListForm extends JDialog {
    private JPanel mainPanel;
    private JTable poTable;
    private JButton viewButton;
    private JButton printButton;
    private JButton refreshButton;
    private JButton closeButton;
    private JComboBox<String> statusFilter;
    
    private DefaultTableModel tableModel;
    private POService poService;
    private SupplierService supplierService;
    private User currentUser;
    
    public POListForm(JFrame parent, com.owsb.domain.User user) {
        super(parent, "Purchase Order List", true);
        this.currentUser = user;
        initializeServices();
        initializeComponents();
        loadPOs();
    }
    
    public POListForm(JFrame parent) {
        super(parent, "Purchase Order List", true);
        initializeServices();
        initializeComponents();
        loadPOs();
    }
    
    private void initializeServices() {
        this.poService = new POService();
        this.supplierService = new SupplierService();
    }
    
    private void initializeComponents() {
        setTitle("Purchase Order List");
        setSize(900, 650);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Status Filter:"));
        statusFilter = new JComboBox<>(new String[]{"All", "PENDING", "PENDING_APPROVAL", "APPROVED", "PARTIALLY_RECEIVED", "COMPLETED", "REJECTED"});
        filterPanel.add(statusFilter);
        
        // Create table
        String[] columns = {"PO ID", "PR ID", "Supplier", "Created By", "Order Date", "Expected Delivery", "Total Amount", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        poTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(poTable);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        viewButton = new JButton("View Details");
        printButton = new JButton("Print PO");
        refreshButton = new JButton("Refresh");
        closeButton = new JButton("Close");
        
        buttonPanel.add(viewButton);
        buttonPanel.add(printButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        mainPanel.add(filterPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Add action listeners
        viewButton.addActionListener(e -> viewPODetails());
        printButton.addActionListener(e -> printPO());
        refreshButton.addActionListener(e -> refreshList());
        closeButton.addActionListener(e -> dispose());
        statusFilter.addActionListener(e -> filterByStatus());
    }
    
    private void loadPOs() {
        tableModel.setRowCount(0);
        
        try {
            List<PurchaseOrder> allPOs = poService.listPOs();
            
            for (PurchaseOrder po : allPOs) {
                // Get supplier name
                Supplier supplier = supplierService.getSupplierById(po.getSupplierId());
                String supplierName = (supplier != null) ? supplier.getSupplierName() : "Unknown";
                
                Object[] rowData = {
                    po.getPoId(),
                    po.getPrId() != null ? po.getPrId() : "N/A",
                    supplierName,
                    po.getCreatedBy() != null ? po.getCreatedBy() : "System",
                    po.getOrderDate().toString(),
                    po.getExpectedDeliveryDate() != null ? po.getExpectedDeliveryDate().toString() : "N/A",
                    String.format("$%.2f", po.getTotalAmount()),
                    po.getStatus()
                };
                tableModel.addRow(rowData);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading Purchase Orders: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void viewPODetails() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow >= 0) {
            String poId = (String) tableModel.getValueAt(selectedRow, 0);
            
            // Open PO details form or show details in a dialog
            PurchaseOrder po = poService.findPOById(poId);
            if (po != null) {
                showPODetailsDialog(po);
            } else {
                JOptionPane.showMessageDialog(this, "Purchase Order not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a Purchase Order to view", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void showPODetailsDialog(PurchaseOrder po) {
        StringBuilder details = new StringBuilder();
        details.append("PO ID: ").append(po.getPoId()).append("\n");
        details.append("PR ID: ").append(po.getPrId() != null ? po.getPrId() : "N/A").append("\n");
        details.append("Supplier ID: ").append(po.getSupplierId()).append("\n");
        details.append("Order Date: ").append(po.getOrderDate()).append("\n");
        details.append("Expected Delivery: ").append(po.getExpectedDeliveryDate()).append("\n");
        details.append("Status: ").append(po.getStatus()).append("\n");
        details.append("Total Amount: $").append(String.format("%.2f", po.getTotalAmount())).append("\n\n");
        
        details.append("Line Items:\n");
        for (PurchaseOrderLine line : po.getLines()) {
            details.append("- ").append(line.getItemName())
                   .append(" (").append(line.getItemCode()).append(")")
                   .append(" - Qty: ").append(line.getQuantity())
                   .append(" - Price: $").append(String.format("%.2f", line.getUnitPrice())).append("\n");
        }
        
        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Purchase Order Details", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void printPO() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                boolean complete = poTable.print();
                if (complete) {
                    JOptionPane.showMessageDialog(this, "Purchase Order list printed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Printing was cancelled.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error printing: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a Purchase Order to print", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void refreshList() {
        loadPOs();
        filterByStatus(); // Re-apply current filter
        JOptionPane.showMessageDialog(this, "Purchase Order list refreshed", "Refresh", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void filterByStatus() {
        String selectedStatus = (String) statusFilter.getSelectedItem();
        
        tableModel.setRowCount(0);
        
        try {
            List<PurchaseOrder> allPOs = poService.listPOs();
            
            for (PurchaseOrder po : allPOs) {
                // Apply filter
                if ("All".equals(selectedStatus) || selectedStatus.equals(po.getStatus())) {
                    // Get supplier name
                    Supplier supplier = supplierService.getSupplierById(po.getSupplierId());
                    String supplierName = (supplier != null) ? supplier.getSupplierName() : "Unknown";
                    
                    Object[] rowData = {
                        po.getPoId(),
                        po.getPrId() != null ? po.getPrId() : "N/A",
                        supplierName,
                        po.getCreatedBy() != null ? po.getCreatedBy() : "System",
                        po.getOrderDate().toString(),
                        po.getExpectedDeliveryDate() != null ? po.getExpectedDeliveryDate().toString() : "N/A",
                        String.format("$%.2f", po.getTotalAmount()),
                        po.getStatus()
                    };
                    tableModel.addRow(rowData);
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error filtering Purchase Orders: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}