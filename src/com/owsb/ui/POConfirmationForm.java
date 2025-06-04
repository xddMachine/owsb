package com.owsb.ui;

import com.owsb.domain.*;
import com.owsb.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class POConfirmationForm extends JDialog {
    private JTable poTable;
    private JTable itemsTable;
    private DefaultTableModel poTableModel;
    private DefaultTableModel itemsTableModel;
    private JTextField deliveryDateField;
    private JTextArea notesArea;
    private JLabel totalAmountLabel;
    private JButton confirmButton;
    private JButton rejectButton;
    private JButton refreshButton;
    private JButton closeButton;
    
    private POService poService;
    private SupplierService supplierService;
    private User currentUser;
    private PurchaseOrder selectedPO;
    
    public POConfirmationForm(JFrame parent, User user) {
        super(parent, "Confirm Purchase Orders", true);
        this.currentUser = user;
        initializeServices();
        initializeComponents();
        loadDraftPOs();
    }
    
    private void initializeServices() {
        this.poService = new POService();
        this.supplierService = new SupplierService();
    }
    
    private void initializeComponents() {
        setSize(1200, 800);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create split pane for POs and details
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        // Top panel - Draft POs list
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Draft Purchase Orders Awaiting Confirmation"));
        
        String[] poColumns = {"PO ID", "PR ID", "Supplier", "Created By", "Date Created", "Expected Delivery", "Total Amount", "Status"};
        poTableModel = new DefaultTableModel(poColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        poTable = new JTable(poTableModel);
        poTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        poTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedPODetails();
            }
        });
        
        JScrollPane poScrollPane = new JScrollPane(poTable);
        poScrollPane.setPreferredSize(new Dimension(1150, 300));
        topPanel.add(poScrollPane, BorderLayout.CENTER);
        
        // Bottom panel - PO details
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Purchase Order Details"));
        
        // Left side - Items table
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Items"));
        
        String[] itemColumns = {"Item Code", "Item Name", "Quantity", "Unit", "Unit Price", "Line Total", "Specifications"};
        itemsTableModel = new DefaultTableModel(itemColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemsTable = new JTable(itemsTableModel);
        JScrollPane itemsScrollPane = new JScrollPane(itemsTable);
        itemsScrollPane.setPreferredSize(new Dimension(700, 200));
        itemsPanel.add(itemsScrollPane, BorderLayout.CENTER);
        
        // Right side - PO details and controls
        JPanel detailsPanel = new JPanel(new BorderLayout());
        
        // PO information
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("PO Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        infoPanel.add(new JLabel("Expected Delivery Date:"), gbc);
        gbc.gridx = 1;
        deliveryDateField = new JTextField(15);
        infoPanel.add(deliveryDateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        infoPanel.add(new JLabel("Total Amount:"), gbc);
        gbc.gridx = 1;
        totalAmountLabel = new JLabel("$0.00");
        totalAmountLabel.setFont(totalAmountLabel.getFont().deriveFont(Font.BOLD));
        totalAmountLabel.setForeground(new Color(0, 120, 0));
        infoPanel.add(totalAmountLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        infoPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        infoPanel.add(new JScrollPane(notesArea), gbc);
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout());
        confirmButton = new JButton("✅ Confirm PO");
        rejectButton = new JButton("❌ Reject PO");
        refreshButton = new JButton("🔄 Refresh");
        closeButton = new JButton("Close");
        
        // Style buttons
        confirmButton.setBackground(Color.WHITE);
        confirmButton.setForeground(Color.BLACK);
        confirmButton.setFont(confirmButton.getFont().deriveFont(Font.BOLD));
        confirmButton.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 0), 2));
        
        rejectButton.setBackground(Color.WHITE);
        rejectButton.setForeground(Color.BLACK);
        rejectButton.setFont(rejectButton.getFont().deriveFont(Font.BOLD));
        rejectButton.setBorder(BorderFactory.createLineBorder(new Color(200, 0, 0), 2));
        
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 200), 2));
        
        closeButton.setBackground(Color.WHITE);
        closeButton.setForeground(Color.BLACK);
        closeButton.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        
        actionPanel.add(confirmButton);
        actionPanel.add(rejectButton);
        actionPanel.add(refreshButton);
        actionPanel.add(closeButton);
        
        detailsPanel.add(infoPanel, BorderLayout.CENTER);
        detailsPanel.add(actionPanel, BorderLayout.SOUTH);
        
        // Combine details panel
        JPanel combinedBottom = new JPanel(new BorderLayout());
        combinedBottom.add(itemsPanel, BorderLayout.CENTER);
        combinedBottom.add(detailsPanel, BorderLayout.EAST);
        bottomPanel.add(combinedBottom, BorderLayout.CENTER);
        
        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(bottomPanel);
        splitPane.setDividerLocation(350);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel);
        
        // Add action listeners
        confirmButton.addActionListener(e -> confirmPO());
        rejectButton.addActionListener(e -> rejectPO());
        refreshButton.addActionListener(e -> loadDraftPOs());
        closeButton.addActionListener(e -> dispose());
        
        // Initially disable action buttons
        setActionButtonsEnabled(false);
    }
    
    private void loadDraftPOs() {
        poTableModel.setRowCount(0);
        
        try {
            List<PurchaseOrder> draftPOs = poService.getPOsByStatus("DRAFT");
            
            for (PurchaseOrder po : draftPOs) {
                Supplier supplier = supplierService.getSupplierById(po.getSupplierId());
                String supplierName = (supplier != null) ? supplier.getSupplierName() : "Unknown";
                
                Object[] rowData = {
                    po.getPoId(),
                    po.getPrId() != null ? po.getPrId() : "N/A",
                    supplierName,
                    po.getCreatedBy() != null ? po.getCreatedBy() : "System",
                    po.getOrderDate().toString(),
                    po.getExpectedDeliveryDate() != null ? po.getExpectedDeliveryDate().toString() : "Not set",
                    String.format("$%.2f", po.getTotalAmount()),
                    po.getStatus()
                };
                poTableModel.addRow(rowData);
            }
            
            if (draftPOs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No draft Purchase Orders found for confirmation.", 
                                            "No Draft POs", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading draft POs: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadSelectedPODetails() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow >= 0) {
            String poId = (String) poTableModel.getValueAt(selectedRow, 0);
            selectedPO = poService.findPOById(poId);
            
            if (selectedPO != null) {
                // Load PO details
                deliveryDateField.setText(selectedPO.getExpectedDeliveryDate() != null ? 
                    selectedPO.getExpectedDeliveryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
                totalAmountLabel.setText(String.format("$%.2f", selectedPO.getTotalAmount()));
                notesArea.setText(selectedPO.getNotes() != null ? selectedPO.getNotes() : "");
                
                // Load line items
                itemsTableModel.setRowCount(0);
                List<PurchaseOrderLine> lines = poService.getPOLines(poId);
                for (PurchaseOrderLine line : lines) {
                    Object[] rowData = {
                        line.getItemCode(),
                        line.getItemName(),
                        line.getQuantity(),
                        line.getUnit(),
                        String.format("$%.2f", line.getUnitPrice()),
                        String.format("$%.2f", line.getLineTotal()),
                        line.getSpecifications()
                    };
                    itemsTableModel.addRow(rowData);
                }
                
                setActionButtonsEnabled(true);
            }
        } else {
            setActionButtonsEnabled(false);
            selectedPO = null;
        }
    }
    
    private void confirmPO() {
        if (selectedPO == null) {
            JOptionPane.showMessageDialog(this, "Please select a PO to confirm", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validate delivery date
        String deliveryDateText = deliveryDateField.getText().trim();
        LocalDate deliveryDate = null;
        
        if (!deliveryDateText.isEmpty()) {
            try {
                deliveryDate = LocalDate.parse(deliveryDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (deliveryDate.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(this, "Delivery date cannot be in the past", "Invalid Date", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD", "Invalid Date", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to confirm this Purchase Order?\n\n" +
            "PO ID: " + selectedPO.getPoId() + "\n" +
            "Supplier: " + selectedPO.getSupplierName() + "\n" +
            "Total Amount: $" + String.format("%.2f", selectedPO.getTotalAmount()) + "\n\n" +
            "This will send the PO to the supplier and cannot be undone.", 
            "Confirm Purchase Order", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                // Update PO details
                if (deliveryDate != null) {
                    selectedPO.setExpectedDeliveryDate(deliveryDate);
                }
                selectedPO.setNotes(notesArea.getText().trim());
                selectedPO.setStatus("CONFIRMED");
                selectedPO.setApprovedBy(currentUser.getUsername());
                selectedPO.setApprovedDate(LocalDate.now());
                
                // Save updated PO
                if (poService.updatePO(selectedPO)) {
                    JOptionPane.showMessageDialog(this, 
                        "✅ Purchase Order confirmed successfully!\n\n" +
                        "PO ID: " + selectedPO.getPoId() + "\n" +
                        "Status: CONFIRMED\n" +
                        "Next step: PO will be sent to supplier for fulfillment", 
                        "PO Confirmed", 
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    loadDraftPOs(); // Refresh the list
                    setActionButtonsEnabled(false);
                    selectedPO = null;
                } else {
                    JOptionPane.showMessageDialog(this, "Error confirming PO", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error confirming PO: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void rejectPO() {
        if (selectedPO == null) {
            JOptionPane.showMessageDialog(this, "Please select a PO to reject", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String reason = JOptionPane.showInputDialog(this, 
            "Please provide a reason for rejecting this PO:", 
            "Reject Purchase Order", 
            JOptionPane.QUESTION_MESSAGE);
        
        if (reason != null && !reason.trim().isEmpty()) {
            int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to reject this Purchase Order?\n\n" +
                "PO ID: " + selectedPO.getPoId() + "\n" +
                "Reason: " + reason + "\n\n" +
                "This action cannot be undone.", 
                "Reject Purchase Order", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                try {
                    selectedPO.setStatus("REJECTED");
                    selectedPO.setApprovedBy(currentUser.getUsername());
                    selectedPO.setApprovedDate(LocalDate.now());
                    selectedPO.setNotes((selectedPO.getNotes() != null ? selectedPO.getNotes() + "\n" : "") + 
                                       "REJECTED: " + reason);
                    
                    if (poService.updatePO(selectedPO)) {
                        JOptionPane.showMessageDialog(this, 
                            "❌ Purchase Order rejected successfully!\n\n" +
                            "PO ID: " + selectedPO.getPoId() + "\n" +
                            "Reason: " + reason, 
                            "PO Rejected", 
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        loadDraftPOs(); // Refresh the list
                        setActionButtonsEnabled(false);
                        selectedPO = null;
                    } else {
                        JOptionPane.showMessageDialog(this, "Error rejecting PO", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error rejecting PO: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void setActionButtonsEnabled(boolean enabled) {
        confirmButton.setEnabled(enabled);
        rejectButton.setEnabled(enabled);
    }
}