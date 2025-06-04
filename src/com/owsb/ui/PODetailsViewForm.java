package com.owsb.ui;

import com.owsb.domain.*;
import com.owsb.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PODetailsViewForm extends JDialog {
    private PurchaseOrder po;
    private POService poService;
    private SupplierService supplierService;
    private ItemService itemService;
    
    public PODetailsViewForm(JDialog parent, PurchaseOrder po) {
        super(parent, "Purchase Order Details - " + po.getPoId(), true);
        this.po = po;
        initializeServices();
        initializeComponents();
    }
    
    private void initializeServices() {
        this.poService = new POService();
        this.supplierService = new SupplierService();
        this.itemService = new ItemService();
    }
    
    private void initializeComponents() {
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Header panel with PO information
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBorder(BorderFactory.createTitledBorder("Purchase Order Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Get supplier info
        Supplier supplier = supplierService.getSupplierById(po.getSupplierId());
        String supplierName = (supplier != null) ? supplier.getSupplierName() : "Unknown";
        
        // First row
        gbc.gridx = 0; gbc.gridy = 0;
        headerPanel.add(new JLabel("PO ID:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(po.getPoId()), gbc);
        
        gbc.gridx = 2;
        headerPanel.add(new JLabel("PR ID:"), gbc);
        gbc.gridx = 3;
        headerPanel.add(new JLabel(po.getPrId() != null ? po.getPrId() : "N/A"), gbc);
        
        // Second row
        gbc.gridx = 0; gbc.gridy = 1;
        headerPanel.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(supplierName), gbc);
        
        gbc.gridx = 2;
        headerPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 3;
        JLabel statusLabel = new JLabel(po.getStatus());
        if ("APPROVED".equals(po.getStatus())) {
            statusLabel.setForeground(new Color(34, 139, 34));
        }
        headerPanel.add(statusLabel, gbc);
        
        // Third row
        gbc.gridx = 0; gbc.gridy = 2;
        headerPanel.add(new JLabel("Order Date:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(po.getOrderDate().toString()), gbc);
        
        gbc.gridx = 2;
        headerPanel.add(new JLabel("Expected Delivery:"), gbc);
        gbc.gridx = 3;
        headerPanel.add(new JLabel(po.getExpectedDeliveryDate() != null ? po.getExpectedDeliveryDate().toString() : "N/A"), gbc);
        
        // Fourth row
        gbc.gridx = 0; gbc.gridy = 3;
        headerPanel.add(new JLabel("Created By:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(po.getCreatedBy() != null ? po.getCreatedBy() : "System"), gbc);
        
        gbc.gridx = 2;
        headerPanel.add(new JLabel("Total Amount:"), gbc);
        gbc.gridx = 3;
        JLabel totalLabel = new JLabel(String.format("₹%.2f", po.getTotalAmount()));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
        totalLabel.setForeground(new Color(0, 100, 0));
        headerPanel.add(totalLabel, gbc);
        
        // Fifth row - Approval information
        if (po.getApprovedBy() != null) {
            gbc.gridx = 0; gbc.gridy = 4;
            headerPanel.add(new JLabel("Approved By:"), gbc);
            gbc.gridx = 1;
            headerPanel.add(new JLabel(po.getApprovedBy()), gbc);
            
            gbc.gridx = 2;
            headerPanel.add(new JLabel("Approved Date:"), gbc);
            gbc.gridx = 3;
            headerPanel.add(new JLabel(po.getApprovedDate() != null ? po.getApprovedDate().toString() : "N/A"), gbc);
        }
        
        // Line items table
        String[] columnNames = {"Item ID", "Item Name", "Quantity", "Unit Price", "Total Price"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable lineTable = new JTable(tableModel);
        lineTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Load PO lines
        try {
            List<PurchaseOrderLine> poLines = poService.getPOLines(po.getPoId());
            for (PurchaseOrderLine line : poLines) {
                Item item = itemService.getItemById(line.getItemId());
                String itemName = (item != null) ? item.getItemName() : "Unknown";
                
                Object[] rowData = {
                    line.getItemId(),
                    itemName,
                    line.getQuantity(),
                    String.format("₹%.2f", line.getUnitPrice()),
                    String.format("₹%.2f", line.getLineTotal())
                };
                tableModel.addRow(rowData);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading PO line items: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        JScrollPane scrollPane = new JScrollPane(lineTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Line Items"));
        
        // Terms and conditions
        JTextArea termsArea = new JTextArea(3, 50);
        termsArea.setText(po.getTermsAndConditions() != null ? po.getTermsAndConditions() : "No terms specified");
        termsArea.setEditable(false);
        termsArea.setBackground(getBackground());
        JScrollPane termsScrollPane = new JScrollPane(termsArea);
        termsScrollPane.setBorder(BorderFactory.createTitledBorder("Terms and Conditions"));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.setBackground(new Color(220, 20, 60));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBorder(BorderFactory.createRaisedBevelBorder());
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(closeButton);
        
        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(termsScrollPane, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
}