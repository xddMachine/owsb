package com.owsb.ui;

import com.owsb.domain.*;
import com.owsb.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class POManagementForm extends JDialog {
    private JPanel mainPanel;
    private JTable poTable;
    private JButton approveButton;
    private JButton modifySupplierButton;
    private JButton viewDetailsButton;
    private JButton rejectButton;
    private JButton refreshButton;
    private JButton closeButton;
    private JComboBox<String> statusFilter;
    private JLabel statusLabel;
    
    private DefaultTableModel tableModel;
    private POService poService;
    private SupplierService supplierService;
    private User currentUser;
    private PurchaseOrder selectedPO;
    
    public POManagementForm(JFrame parent, User user) {
        super(parent, "Purchase Order Management", true);
        this.currentUser = user;
        initializeServices();
        initializeComponents();
        loadPOs();
    }
    
    public POManagementForm(JFrame parent) {
        this(parent, null);
    }
    
    private void initializeServices() {
        this.poService = new POService();
        this.supplierService = new SupplierService();
    }
    
    private void initializeComponents() {
        setTitle("Purchase Order Management - " + (currentUser != null ? currentUser.getRole() : ""));
        setSize(1100, 750);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        
        // Top panel with title and filters
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Purchase Order Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel filterPanel = new JPanel(new FlowLayout());
        filterPanel.add(new JLabel("Filter by Status:"));
        statusFilter = new JComboBox<>(new String[]{"All", "DRAFT", "CONFIRMED", "APPROVED", "REJECTED", "RECEIVED"});
        statusFilter.addActionListener(e -> loadPOs());
        filterPanel.add(statusFilter);
        
        statusLabel = new JLabel("Ready", SwingConstants.CENTER);
        statusLabel.setForeground(Color.BLUE);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.CENTER);
        topPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Create table
        String[] columnNames = {
            "PO ID", "PR ID", "Supplier", "Created By", "Order Date", 
            "Expected Delivery", "Total Amount", "Status", "Approved By"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        poTable = new JTable(tableModel);
        poTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        poTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Add selection listener
        poTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedPO();
            }
        });
        
        // Set column widths
        poTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // PO ID
        poTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // PR ID
        poTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Supplier
        poTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Created By
        poTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Order Date
        poTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Expected Delivery
        poTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Total Amount
        poTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Status
        poTable.getColumnModel().getColumn(8).setPreferredWidth(100); // Approved By
        
        JScrollPane scrollPane = new JScrollPane(poTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Purchase Orders"));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        viewDetailsButton = new JButton("View Details");
        refreshButton = new JButton("Refresh");
        closeButton = new JButton("Close");
        
        // Add role-specific buttons
        if (currentUser != null && currentUser.getRole() == Role.FINANCE_MANAGER) {
            approveButton = new JButton("✅ Approve PO");
            modifySupplierButton = new JButton("🔄 Modify Supplier");
            rejectButton = new JButton("❌ Reject PO");
            
            // Style buttons for Finance Manager
            approveButton.setBackground(new Color(34, 139, 34));
            approveButton.setForeground(Color.WHITE);
            approveButton.setBorder(BorderFactory.createRaisedBevelBorder());
            
            modifySupplierButton.setBackground(new Color(255, 140, 0));
            modifySupplierButton.setForeground(Color.WHITE);
            modifySupplierButton.setBorder(BorderFactory.createRaisedBevelBorder());
            
            rejectButton.setBackground(new Color(220, 20, 60));
            rejectButton.setForeground(Color.WHITE);
            rejectButton.setBorder(BorderFactory.createRaisedBevelBorder());
            
            buttonPanel.add(approveButton);
            buttonPanel.add(modifySupplierButton);
            buttonPanel.add(rejectButton);
            
            // Add action listeners
            approveButton.addActionListener(e -> approvePO());
            modifySupplierButton.addActionListener(e -> modifySupplier());
            rejectButton.addActionListener(e -> rejectPO());
            
            // Initially disable buttons until PO is selected
            approveButton.setEnabled(false);
            modifySupplierButton.setEnabled(false);
            rejectButton.setEnabled(false);
        }
        
        // Style common buttons
        viewDetailsButton.setBackground(new Color(70, 130, 180));
        viewDetailsButton.setForeground(Color.WHITE);
        viewDetailsButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        refreshButton.setBackground(new Color(128, 128, 128));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        closeButton.setBackground(new Color(105, 105, 105));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // Add action listeners for common buttons
        viewDetailsButton.addActionListener(e -> viewPODetails());
        refreshButton.addActionListener(e -> {
            statusLabel.setText("Refreshing...");
            loadPOs();
            statusLabel.setText("Ready");
        });
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        // Add components to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void loadPOs() {
        tableModel.setRowCount(0);
        
        try {
            List<PurchaseOrder> allPOs = poService.listPOs();
            String selectedStatus = (String) statusFilter.getSelectedItem();
            int totalRecords = 0;
            
            for (PurchaseOrder po : allPOs) {
                // Apply status filter
                if (!selectedStatus.equals("All") && !po.getStatus().equals(selectedStatus)) {
                    continue;
                }
                
                // For Finance Manager, show only CONFIRMED POs (ready for approval)
                if (currentUser != null && currentUser.getRole() == Role.FINANCE_MANAGER) {
                    if (!po.getStatus().equals("CONFIRMED") && !po.getStatus().equals("APPROVED") && !po.getStatus().equals("REJECTED")) {
                        continue;
                    }
                }
                
                totalRecords++;
                
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
                    String.format("₹%.2f", po.getTotalAmount()),
                    po.getStatus(),
                    po.getApprovedBy() != null ? po.getApprovedBy() : "N/A"
                };
                tableModel.addRow(rowData);
            }
            
            statusLabel.setText("Found " + totalRecords + " purchase orders");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading Purchase Orders: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Error loading data");
        }
    }
    
    private void loadSelectedPO() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow >= 0) {
            String poId = (String) tableModel.getValueAt(selectedRow, 0);
            selectedPO = poService.findPOById(poId);
            
            // Enable/disable buttons based on selection and role
            if (currentUser != null && currentUser.getRole() == Role.FINANCE_MANAGER && selectedPO != null) {
                boolean isConfirmed = "CONFIRMED".equals(selectedPO.getStatus());
                approveButton.setEnabled(isConfirmed);
                modifySupplierButton.setEnabled(isConfirmed);
                rejectButton.setEnabled(isConfirmed);
            }
        } else {
            selectedPO = null;
            if (currentUser != null && currentUser.getRole() == Role.FINANCE_MANAGER) {
                approveButton.setEnabled(false);
                modifySupplierButton.setEnabled(false);
                rejectButton.setEnabled(false);
            }
        }
    }
    
    private void approvePO() {
        if (selectedPO == null) {
            JOptionPane.showMessageDialog(this, "Please select a PO to approve.", 
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!"CONFIRMED".equals(selectedPO.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only confirmed POs can be approved.", 
                                        "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this,
                String.format("Approve Purchase Order %s?\n\nSupplier: %s\nTotal Amount: ₹%.2f",
                        selectedPO.getPoId(), selectedPO.getSupplierName(), selectedPO.getTotalAmount()),
                "Confirm PO Approval",
                JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            if (poService.approvePO(selectedPO.getPoId(), currentUser.getUsername())) {
                JOptionPane.showMessageDialog(this,
                        "✅ Purchase Order approved successfully!\nPO ID: " + selectedPO.getPoId(),
                        "Approval Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                
                loadPOs();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Error approving Purchase Order.",
                        "Approval Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void modifySupplier() {
        if (selectedPO == null) {
            JOptionPane.showMessageDialog(this, "Please select a PO to modify supplier.", 
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!"CONFIRMED".equals(selectedPO.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only confirmed POs can be modified.", 
                                        "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Open supplier modification dialog
        new POSupplierModificationDialog(this, selectedPO, () -> loadPOs()).setVisible(true);
    }
    
    private void rejectPO() {
        if (selectedPO == null) {
            JOptionPane.showMessageDialog(this, "Please select a PO to reject.", 
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!"CONFIRMED".equals(selectedPO.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only confirmed POs can be rejected.", 
                                        "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String reason = JOptionPane.showInputDialog(this, 
            "Please provide a reason for rejecting this PO:", 
            "Rejection Reason", 
            JOptionPane.QUESTION_MESSAGE);
        
        if (reason != null && !reason.trim().isEmpty()) {
            int result = JOptionPane.showConfirmDialog(this,
                    String.format("Reject Purchase Order %s?\n\nReason: %s",
                            selectedPO.getPoId(), reason),
                    "Confirm PO Rejection",
                    JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                selectedPO.setStatus("REJECTED");
                selectedPO.setNotes("Rejected by " + currentUser.getUsername() + ": " + reason);
                
                if (poService.updatePO(selectedPO)) {
                    JOptionPane.showMessageDialog(this,
                            "❌ Purchase Order rejected successfully!\nPO ID: " + selectedPO.getPoId(),
                            "Rejection Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                    
                    loadPOs();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error rejecting Purchase Order.",
                            "Rejection Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void viewPODetails() {
        if (selectedPO == null) {
            JOptionPane.showMessageDialog(this, "Please select a PO to view details.", 
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        new PODetailsViewForm(this, selectedPO).setVisible(true);
    }
}