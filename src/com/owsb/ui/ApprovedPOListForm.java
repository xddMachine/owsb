package com.owsb.ui;

import com.owsb.domain.*;
import com.owsb.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ApprovedPOListForm extends JDialog {
    private JPanel mainPanel;
    private JTable poTable;
    private JButton viewDetailsButton;
    private JButton printButton;
    private JButton refreshButton;
    private JButton closeButton;
    private JLabel statusLabel;
    
    private DefaultTableModel tableModel;
    private POService poService;
    private SupplierService supplierService;
    private User currentUser;
    
    public ApprovedPOListForm(JFrame parent, User user) {
        super(parent, "Approved Purchase Orders", true);
        this.currentUser = user;
        initializeServices();
        initializeComponents();
        loadApprovedPOs();
    }
    
    private void initializeServices() {
        this.poService = new POService();
        this.supplierService = new SupplierService();
    }
    
    private void initializeComponents() {
        setTitle("Approved Purchase Orders - Finance Manager View");
        setSize(950, 700);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        
        // Top panel with title and status
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Approved Purchase Orders", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        statusLabel = new JLabel("Ready", SwingConstants.CENTER);
        statusLabel.setForeground(Color.BLUE);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Create table
        String[] columnNames = {
            "PO ID", "PR ID", "Supplier", "Created By", "Order Date", 
            "Expected Delivery", "Total Amount", "Status", "Approved By", "Approved Date"
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
        
        // Set column widths
        poTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // PO ID
        poTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // PR ID
        poTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Supplier
        poTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Created By
        poTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Order Date
        poTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Expected Delivery
        poTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Total Amount
        poTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Status
        poTable.getColumnModel().getColumn(8).setPreferredWidth(100); // Approved By
        poTable.getColumnModel().getColumn(9).setPreferredWidth(100); // Approved Date
        
        JScrollPane scrollPane = new JScrollPane(poTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Approved Purchase Orders"));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        viewDetailsButton = new JButton("View Details");
        printButton = new JButton("Print PO");
        refreshButton = new JButton("Refresh");
        closeButton = new JButton("Close");
        
        // Style buttons
        viewDetailsButton.setBackground(new Color(70, 130, 180));
        viewDetailsButton.setForeground(Color.WHITE);
        viewDetailsButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        printButton.setBackground(new Color(34, 139, 34));
        printButton.setForeground(Color.WHITE);
        printButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        refreshButton.setBackground(new Color(255, 140, 0));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        closeButton.setBackground(new Color(220, 20, 60));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // Add action listeners
        viewDetailsButton.addActionListener(e -> viewPODetails());
        printButton.addActionListener(e -> printPO());
        refreshButton.addActionListener(e -> {
            statusLabel.setText("Refreshing...");
            loadApprovedPOs();
            statusLabel.setText("Ready");
        });
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(printButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        // Add components to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void loadApprovedPOs() {
        tableModel.setRowCount(0);
        
        try {
            List<PurchaseOrder> allPOs = poService.listPOs();
            int approvedCount = 0;
            
            for (PurchaseOrder po : allPOs) {
                // Only show POs with APPROVED status
                if ("APPROVED".equals(po.getStatus())) {
                    approvedCount++;
                    
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
                        po.getApprovedBy() != null ? po.getApprovedBy() : "N/A",
                        po.getApprovedDate() != null ? po.getApprovedDate().toString() : "N/A"
                    };
                    tableModel.addRow(rowData);
                }
            }
            
            statusLabel.setText("Found " + approvedCount + " approved purchase orders");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading approved Purchase Orders: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Error loading data");
        }
    }
    
    private void viewPODetails() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a Purchase Order to view details.", 
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String poId = (String) tableModel.getValueAt(selectedRow, 0);
        
        try {
            PurchaseOrder po = poService.findPOById(poId);
            if (po != null) {
                new PODetailsViewForm(this, po).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Purchase Order not found.", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading PO details: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void printPO() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a Purchase Order to print.", 
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String poId = (String) tableModel.getValueAt(selectedRow, 0);
        
        // Create a simple print preview dialog
        StringBuilder printContent = new StringBuilder();
        printContent.append("=== PURCHASE ORDER ===\n\n");
        printContent.append("PO ID: ").append(tableModel.getValueAt(selectedRow, 0)).append("\n");
        printContent.append("PR ID: ").append(tableModel.getValueAt(selectedRow, 1)).append("\n");
        printContent.append("Supplier: ").append(tableModel.getValueAt(selectedRow, 2)).append("\n");
        printContent.append("Created By: ").append(tableModel.getValueAt(selectedRow, 3)).append("\n");
        printContent.append("Order Date: ").append(tableModel.getValueAt(selectedRow, 4)).append("\n");
        printContent.append("Expected Delivery: ").append(tableModel.getValueAt(selectedRow, 5)).append("\n");
        printContent.append("Total Amount: ").append(tableModel.getValueAt(selectedRow, 6)).append("\n");
        printContent.append("Status: ").append(tableModel.getValueAt(selectedRow, 7)).append("\n");
        printContent.append("Approved By: ").append(tableModel.getValueAt(selectedRow, 8)).append("\n");
        printContent.append("Approved Date: ").append(tableModel.getValueAt(selectedRow, 9)).append("\n");
        
        JTextArea printArea = new JTextArea(printContent.toString());
        printArea.setEditable(false);
        printArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane printScrollPane = new JScrollPane(printArea);
        printScrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, printScrollPane, "Print Preview - " + poId, 
                                    JOptionPane.INFORMATION_MESSAGE);
    }
}