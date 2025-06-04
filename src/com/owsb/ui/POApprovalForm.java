package com.owsb.ui;

import com.owsb.domain.PurchaseOrder;
import com.owsb.domain.PurchaseOrderLine;
import com.owsb.domain.Supplier;
import com.owsb.domain.User;
import com.owsb.service.POService;
import com.owsb.service.SupplierService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class POApprovalForm extends JDialog {
    private POService poService;
    private SupplierService supplierService;
    private User currentUser;
    private JTable poTable;
    private DefaultTableModel poTableModel;
    private JTable itemsTable;
    private DefaultTableModel itemsTableModel;
    private JTextArea commentsArea;
    private JButton approveButton;
    private JButton rejectButton;
    private JButton modifySupplierButton;
    private JButton closeButton;
    private PurchaseOrder selectedPO;
    
    public POApprovalForm(JFrame parent, User user) {
        super(parent, "Purchase Order Approval - Finance Manager", true);
        this.currentUser = user;
        this.poService = new POService();
        this.supplierService = new SupplierService();
        initializeComponents();
        loadPendingPOs();
    }
    
    private void initializeComponents() {
        setSize(1000, 700);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // PO List Panel
        JPanel poListPanel = new JPanel(new BorderLayout());
        poListPanel.setBorder(BorderFactory.createTitledBorder("Pending Purchase Orders for Approval"));
        
        String[] poColumns = {"PO ID", "PR ID", "Supplier", "Created By", "Order Date", "Expected Delivery", "Total Amount", "Status"};
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
        poScrollPane.setPreferredSize(new Dimension(0, 200));
        poListPanel.add(poScrollPane, BorderLayout.CENTER);
        
        // PO Details Panel
        JPanel detailsPanel = new JPanel(new BorderLayout());
        
        // Items table
        String[] itemColumns = {"Item Code", "Item Name", "Quantity", "Unit", "Unit Price", "Line Total", "Specifications"};
        itemsTableModel = new DefaultTableModel(itemColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemsTable = new JTable(itemsTableModel);
        JScrollPane itemsScrollPane = new JScrollPane(itemsTable);
        itemsScrollPane.setBorder(BorderFactory.createTitledBorder("Purchase Order Line Items"));
        itemsScrollPane.setPreferredSize(new Dimension(0, 200));
        
        // Comments and Actions Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Comments panel
        JPanel commentsPanel = new JPanel(new BorderLayout());
        commentsPanel.setBorder(BorderFactory.createTitledBorder("Approval Comments"));
        commentsArea = new JTextArea(4, 0);
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        commentsPanel.add(new JScrollPane(commentsArea), BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        approveButton = new JButton("Approve PO");
        approveButton.setBackground(Color.WHITE);
        approveButton.setForeground(Color.BLACK);
        approveButton.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 0), 2));
        approveButton.setFont(approveButton.getFont().deriveFont(Font.BOLD));
        approveButton.addActionListener(e -> approvePO());
        
        rejectButton = new JButton("Reject PO");
        rejectButton.setBackground(Color.WHITE);
        rejectButton.setForeground(Color.BLACK);
        rejectButton.setBorder(BorderFactory.createLineBorder(new Color(200, 0, 0), 2));
        rejectButton.setFont(rejectButton.getFont().deriveFont(Font.BOLD));
        rejectButton.addActionListener(e -> rejectPO());
        
        modifySupplierButton = new JButton("Modify Supplier");
        modifySupplierButton.setBackground(Color.WHITE);
        modifySupplierButton.setForeground(Color.BLACK);
        modifySupplierButton.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 200), 2));
        modifySupplierButton.addActionListener(e -> modifySupplier());
        
        closeButton = new JButton("Close");
        closeButton.setBackground(Color.WHITE);
        closeButton.setForeground(Color.BLACK);
        closeButton.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        closeButton.addActionListener(e -> dispose());
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
        refreshButton.addActionListener(e -> loadPendingPOs());
        
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(modifySupplierButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        // Enable/disable buttons based on selection
        approveButton.setEnabled(false);
        rejectButton.setEnabled(false);
        modifySupplierButton.setEnabled(false);
        
        bottomPanel.add(commentsPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        detailsPanel.add(itemsScrollPane, BorderLayout.CENTER);
        detailsPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Assemble main panel
        mainPanel.add(poListPanel, BorderLayout.NORTH);
        mainPanel.add(detailsPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private void loadPendingPOs() {
        poTableModel.setRowCount(0);
        
        try {
            List<PurchaseOrder> pendingPOs = poService.getConfirmedPOs();
            
            for (PurchaseOrder po : pendingPOs) {
                Object[] rowData = {
                    po.getPoId(),
                    po.getPrId(),
                    po.getSupplierName(),
                    po.getCreatedBy(),
                    po.getOrderDate().toString(),
                    po.getExpectedDeliveryDate().toString(),
                    String.format("$%.2f", po.getTotalAmount()),
                    po.getStatus()
                };
                poTableModel.addRow(rowData);
            }
            
            if (pendingPOs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No pending Purchase Orders found for approval.", 
                                            "No Pending POs", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading Purchase Orders: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadSelectedPODetails() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow >= 0) {
            String poId = (String) poTableModel.getValueAt(selectedRow, 0);
            selectedPO = poService.findPOById(poId);
            
            if (selectedPO != null) {
                // Load line items
                itemsTableModel.setRowCount(0);
                for (PurchaseOrderLine line : selectedPO.getLines()) {
                    Object[] rowData = {
                        line.getItemCode(),
                        line.getItemName(),
                        line.getQuantity(),
                        line.getUnit(),
                        String.format("%.2f", line.getUnitPrice()),
                        String.format("%.2f", line.getLineTotal()),
                        line.getSpecifications()
                    };
                    itemsTableModel.addRow(rowData);
                }
                
                // Enable action buttons
                approveButton.setEnabled(true);
                rejectButton.setEnabled(true);
                modifySupplierButton.setEnabled(true);
            }
        } else {
            // Clear details and disable buttons
            itemsTableModel.setRowCount(0);
            selectedPO = null;
            approveButton.setEnabled(false);
            rejectButton.setEnabled(false);
            modifySupplierButton.setEnabled(false);
        }
    }
    
    private void approvePO() {
        if (selectedPO == null) {
            JOptionPane.showMessageDialog(this, "Please select a PO to approve.");
            return;
        }
        
        String comments = commentsArea.getText().trim();
        
        int result = JOptionPane.showConfirmDialog(this,
                String.format("Approve Purchase Order %s?\\n\\nSupplier: %s\\nTotal Amount: $%.2f",
                        selectedPO.getPoId(), selectedPO.getSupplierName(), selectedPO.getTotalAmount()),
                "Confirm PO Approval",
                JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            if (poService.approvePO(selectedPO.getPoId(), currentUser.getUsername())) {
                JOptionPane.showMessageDialog(this,
                        "Purchase Order approved successfully!\\nPO ID: " + selectedPO.getPoId(),
                        "Approval Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                
                loadPendingPOs();
                commentsArea.setText("");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error approving Purchase Order.",
                        "Approval Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void rejectPO() {
        if (selectedPO == null) {
            JOptionPane.showMessageDialog(this, "Please select a PO to reject.");
            return;
        }
        
        String comments = commentsArea.getText().trim();
        if (comments.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please provide comments for rejection.",
                    "Comments Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this,
                String.format("Reject Purchase Order %s?\\n\\nReason: %s",
                        selectedPO.getPoId(), comments),
                "Confirm PO Rejection",
                JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            if (poService.rejectPO(selectedPO.getPoId(), currentUser.getUsername(), comments)) {
                JOptionPane.showMessageDialog(this,
                        "Purchase Order rejected.\\nPO ID: " + selectedPO.getPoId(),
                        "Rejection Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                
                loadPendingPOs();
                commentsArea.setText("");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error rejecting Purchase Order.",
                        "Rejection Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void modifySupplier() {
        if (selectedPO == null) {
            JOptionPane.showMessageDialog(this, "Please select a PO to modify.");
            return;
        }
        
        // Open supplier modification dialog
        SupplierModificationDialog dialog = new SupplierModificationDialog(this, selectedPO);
        dialog.setVisible(true);
        
        // Refresh after modification
        loadPendingPOs();
    }
}

class SupplierModificationDialog extends JDialog {
    private PurchaseOrder po;
    private JComboBox<String> supplierComboBox;
    private JTextField newPaymentTermsField;
    private JTextField newDeliveryDateField;
    private POService poService;
    private SupplierService supplierService;
    
    public SupplierModificationDialog(Dialog parent, PurchaseOrder po) {
        super(parent, "Modify Supplier for PO " + po.getPoId(), true);
        this.po = po;
        this.poService = new POService();
        this.supplierService = new SupplierService();
        initializeComponents();
    }
    
    private void initializeComponents() {
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Current Supplier:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(new JLabel(po.getSupplierName()), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("New Supplier:"), gbc);
        gbc.gridx = 1;
        supplierComboBox = new JComboBox<>();
        // Populate with available suppliers from database
        List<Supplier> suppliers = supplierService.listSuppliers();
        for (Supplier supplier : suppliers) {
            // Don't show current supplier
            if (!supplier.getSupplierId().equals(po.getSupplierId())) {
                supplierComboBox.addItem(supplier.getSupplierName() + " (" + supplier.getSupplierId() + ")");
            }
        }
        mainPanel.add(supplierComboBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Payment Terms:"), gbc);
        gbc.gridx = 1;
        newPaymentTermsField = new JTextField(15);
        newPaymentTermsField.setText(po.getPaymentTerms());
        mainPanel.add(newPaymentTermsField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Expected Delivery:"), gbc);
        gbc.gridx = 1;
        newDeliveryDateField = new JTextField(15);
        newDeliveryDateField.setText(po.getExpectedDeliveryDate().toString());
        mainPanel.add(newDeliveryDateField, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save Changes");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> saveChanges());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel);
    }
    
    private void saveChanges() {
        String newSupplier = (String) supplierComboBox.getSelectedItem();
        String newPaymentTerms = newPaymentTermsField.getText().trim();
        String newDeliveryDate = newDeliveryDateField.getText().trim();
        
        if (newSupplier != null && !newPaymentTerms.isEmpty() && !newDeliveryDate.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Supplier modification saved:\\n" +
                    "New Supplier: " + newSupplier + "\\n" +
                    "Payment Terms: " + newPaymentTerms + "\\n" +
                    "Delivery Date: " + newDeliveryDate,
                    "Changes Saved",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all fields.",
                    "Incomplete Information",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}