package com.owsb.ui;

import com.owsb.domain.PurchaseRequisition;
import com.owsb.domain.PurchaseRequisitionLine;
import com.owsb.domain.PurchaseOrder;
import com.owsb.domain.PurchaseOrderLine;
import com.owsb.domain.User;
import com.owsb.domain.Item;
import com.owsb.domain.Supplier;
import com.owsb.service.PRService;
import com.owsb.service.POService;
import com.owsb.service.ItemService;
import com.owsb.service.SupplierItemService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PRApprovalForm extends JDialog {
    private PurchaseRequisition pr;
    private PRService prService;
    private POService poService;
    private User currentUser;
    private ItemService itemService;
    private SupplierItemService supplierItemService;
    private JTextArea commentsArea;
    private JTable lineTable;
    private DefaultTableModel tableModel;
    
    public PRApprovalForm(JFrame parent, PurchaseRequisition pr, User user) {
        super(parent, "Approve Purchase Requisition - " + pr.getPrId(), true);
        this.pr = pr;
        this.currentUser = user;
        this.prService = new PRService();
        this.poService = new POService();
        this.itemService = new ItemService();
        this.supplierItemService = new SupplierItemService();
        initializeComponents();
    }
    
    private void initializeComponents() {
        setSize(800, 700);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Header info panel
        JPanel headerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        headerPanel.setBorder(BorderFactory.createTitledBorder("Requisition Information"));
        
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        headerPanel.add(new JLabel("PR ID:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(pr.getPrId()), gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        headerPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 3;
        JLabel statusLabel = new JLabel(pr.getStatus());
        statusLabel.setForeground(Color.ORANGE.darker());
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        headerPanel.add(statusLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        headerPanel.add(new JLabel("Requested By:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(pr.getRequestedBy()), gbc);
        
        gbc.gridx = 2; gbc.gridy = 1;
        headerPanel.add(new JLabel("Request Date:"), gbc);
        gbc.gridx = 3;
        headerPanel.add(new JLabel(pr.getRequestDate().toString()), gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        headerPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(pr.getDepartment()), gbc);
        
        gbc.gridx = 2; gbc.gridy = 2;
        headerPanel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 3;
        JLabel priorityLabel = new JLabel(pr.getPriority());
        if ("HIGH".equals(pr.getPriority()) || "URGENT".equals(pr.getPriority())) {
            priorityLabel.setForeground(Color.RED);
        } else if ("MEDIUM".equals(pr.getPriority())) {
            priorityLabel.setForeground(Color.ORANGE.darker());
        }
        headerPanel.add(priorityLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        headerPanel.add(new JLabel("Total Amount:"), gbc);
        gbc.gridx = 1;
        JLabel totalLabel = new JLabel(String.format("$%.2f", pr.getTotalAmount()));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
        totalLabel.setForeground(new Color(0, 120, 0));
        headerPanel.add(totalLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        headerPanel.add(new JLabel("Justification:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        JTextArea justificationArea = new JTextArea(pr.getJustification());
        justificationArea.setEditable(false);
        justificationArea.setLineWrap(true);
        justificationArea.setWrapStyleWord(true);
        justificationArea.setBackground(headerPanel.getBackground());
        justificationArea.setBorder(BorderFactory.createLoweredBevelBorder());
        headerPanel.add(new JScrollPane(justificationArea), gbc);
        
        // Line items panel
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Line Items for Review"));
        
        String[] columns = {"Item Code", "Item Name", "Quantity", "Unit", "Est. Price", "Specifications", "Suppliers", "Select Supplier", "Line Total"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only "Select Supplier" column is editable
            }
        };
        
        for (PurchaseRequisitionLine line : pr.getLines()) {
            // Get suppliers for this item
            Item item = itemService.findByCode(line.getItemCode());
            java.util.List<Supplier> suppliers = item != null ? 
                supplierItemService.getSuppliersForItem(item.getItemId()) : new java.util.ArrayList<>();
            
            String supplierInfo = suppliers.size() + " supplier(s)";
            String selectSupplier = suppliers.size() > 1 ? "▼ Click dropdown to select" : 
                                  suppliers.size() == 1 ? suppliers.get(0).getSupplierName() : "No suppliers";
            
            Object[] rowData = {
                line.getItemCode(),
                line.getItemName(),
                line.getQuantity(),
                line.getUnit(),
                String.format("%.2f", line.getEstimatedPrice()),
                line.getSpecifications(),
                supplierInfo,
                selectSupplier,
                String.format("%.2f", line.getLineTotal())
            };
            tableModel.addRow(rowData);
        }
        
        lineTable = new JTable(tableModel);
        
        // Set up combo box editor for supplier selection column
        setupSupplierComboBoxEditor();
        
        // Add mouse click listener for supplier selection
        lineTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = lineTable.rowAtPoint(e.getPoint());
                    int col = lineTable.columnAtPoint(e.getPoint());
                    if (col == 7 && row >= 0) { // "Select Supplier" column
                        selectSupplierForItem(row);
                    }
                }
            }
        });
        lineTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane tableScrollPane = new JScrollPane(lineTable);
        tableScrollPane.setPreferredSize(new Dimension(750, 200));
        
        itemsPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summaryPanel.add(new JLabel("Total Items: " + pr.getLines().size()));
        summaryPanel.add(Box.createHorizontalStrut(20));
        summaryPanel.add(new JLabel("Grand Total: $" + String.format("%.2f", pr.getTotalAmount())));
        
        itemsPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        // Comments panel
        JPanel commentsPanel = new JPanel(new BorderLayout());
        commentsPanel.setBorder(BorderFactory.createTitledBorder("Approval Comments"));
        commentsArea = new JTextArea(4, 40);
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        commentsPanel.add(new JScrollPane(commentsArea), BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton approveButton = new JButton("Approve with Supplier Selection");
        approveButton.setBackground(Color.WHITE);
        approveButton.setForeground(Color.BLACK);
        approveButton.setFont(approveButton.getFont().deriveFont(Font.BOLD));
        approveButton.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 0), 2));
        approveButton.addActionListener(e -> approvePR());
        
        JButton supplierSelectionButton = new JButton("Select All Suppliers");
        supplierSelectionButton.setBackground(Color.WHITE);
        supplierSelectionButton.setForeground(Color.BLACK);
        supplierSelectionButton.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 200), 2));
        supplierSelectionButton.addActionListener(e -> selectAllSuppliers());
        
        JButton rejectButton = new JButton("Reject");
        rejectButton.setBackground(Color.WHITE);
        rejectButton.setForeground(Color.BLACK);
        rejectButton.setFont(rejectButton.getFont().deriveFont(Font.BOLD));
        rejectButton.setBorder(BorderFactory.createLineBorder(new Color(200, 0, 0), 2));
        rejectButton.addActionListener(e -> rejectPR());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(Color.WHITE);
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(supplierSelectionButton);
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(itemsPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(commentsPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void setupSupplierComboBoxEditor() {
        // Custom cell editor for supplier selection column
        lineTable.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(new JComboBox<String>()) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                String itemCode = (String) table.getValueAt(row, 0);
                Item item = itemService.findByCode(itemCode);
                
                JComboBox<String> comboBox = new JComboBox<>();
                comboBox.setBackground(Color.WHITE);
                comboBox.setForeground(Color.BLACK);
                
                if (item != null) {
                    java.util.List<Supplier> suppliers = supplierItemService.getSuppliersForItem(item.getItemId());
                    
                    if (suppliers.size() > 1) {
                        comboBox.addItem("-- Select Supplier --");
                        for (Supplier supplier : suppliers) {
                            Double price = supplierItemService.getSupplierItemPrice(supplier.getSupplierId(), item.getItemId());
                            String supplierOption = String.format("%s - $%.2f", 
                                supplier.getSupplierName(), 
                                price != null ? price : 0.0);
                            comboBox.addItem(supplierOption);
                        }
                        
                        // Set current selection if exists
                        String currentValue = (String) value;
                        if (currentValue != null && !currentValue.contains("Click dropdown")) {
                            for (int i = 1; i < comboBox.getItemCount(); i++) {
                                String comboItem = comboBox.getItemAt(i);
                                if (comboItem.startsWith(currentValue)) {
                                    comboBox.setSelectedIndex(i);
                                    break;
                                }
                            }
                        }
                    } else if (suppliers.size() == 1) {
                        Supplier supplier = suppliers.get(0);
                        Double price = supplierItemService.getSupplierItemPrice(supplier.getSupplierId(), item.getItemId());
                        String supplierOption = String.format("%s - $%.2f", 
                            supplier.getSupplierName(), 
                            price != null ? price : 0.0);
                        comboBox.addItem(supplierOption);
                        comboBox.setSelectedIndex(0);
                    } else {
                        comboBox.addItem("No suppliers available");
                    }
                } else {
                    comboBox.addItem("Item not found");
                }
                
                return comboBox;
            }
            
            @Override
            public Object getCellEditorValue() {
                JComboBox<String> comboBox = (JComboBox<String>) getComponent();
                String selected = (String) comboBox.getSelectedItem();
                
                if (selected != null && selected.contains(" - $")) {
                    // Extract supplier name before the price
                    return selected.split(" - \\$")[0];
                }
                return selected;
            }
        });
    }
    
    private void selectSupplierForItem(int row) {
        String itemCode = (String) tableModel.getValueAt(row, 0);
        String itemName = (String) tableModel.getValueAt(row, 1);
        
        Item item = itemService.findByCode(itemCode);
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Item not found: " + itemCode);
            return;
        }
        
        java.util.List<Supplier> suppliers = supplierItemService.getSuppliersForItem(item.getItemId());
        
        if (suppliers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No suppliers available for: " + itemName);
            return;
        }
        
        if (suppliers.size() == 1) {
            JOptionPane.showMessageDialog(this, 
                "Only one supplier available: " + suppliers.get(0).getSupplierName());
            return;
        }
        
        // Create supplier selection dialog with better UI
        JDialog supplierDialog = new JDialog(this, "Select Supplier for " + itemName, true);
        supplierDialog.setSize(500, 300);
        supplierDialog.setLocationRelativeTo(this);
        
        JPanel dialogPanel = new JPanel(new BorderLayout());
        
        // Title
        JLabel titleLabel = new JLabel("Select Supplier for: " + itemName, JLabel.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Supplier list
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Supplier supplier : suppliers) {
            Double price = supplierItemService.getSupplierItemPrice(supplier.getSupplierId(), item.getItemId());
            String listItem = String.format("<html><b>%s</b><br/>Price: $%.2f<br/>Contact: %s<br/>Phone: %s</html>", 
                supplier.getSupplierName(), 
                price != null ? price : 0.0,
                supplier.getContactPerson(),
                supplier.getPhone());
            listModel.addElement(listItem);
        }
        
        JList<String> supplierList = new JList<>(listModel);
        supplierList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        supplierList.setSelectedIndex(0);
        supplierList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
        
        JScrollPane listScrollPane = new JScrollPane(supplierList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Available Suppliers"));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton selectButton = new JButton("Select Supplier");
        JButton cancelButton = new JButton("Cancel");
        
        selectButton.setBackground(Color.WHITE);
        selectButton.setForeground(Color.BLACK);
        selectButton.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 0), 2));
        selectButton.setFont(selectButton.getFont().deriveFont(Font.BOLD));
        
        cancelButton.setBackground(Color.WHITE);
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        
        final boolean[] selectionMade = {false};
        
        selectButton.addActionListener(e -> {
            int selectedIndex = supplierList.getSelectedIndex();
            if (selectedIndex >= 0) {
                Supplier selectedSupplier = suppliers.get(selectedIndex);
                tableModel.setValueAt(selectedSupplier.getSupplierName(), row, 7);
                selectionMade[0] = true;
                supplierDialog.dispose();
            }
        });
        
        cancelButton.addActionListener(e -> supplierDialog.dispose());
        
        // Double-click to select
        supplierList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectButton.doClick();
                }
            }
        });
        
        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);
        
        dialogPanel.add(titleLabel, BorderLayout.NORTH);
        dialogPanel.add(listScrollPane, BorderLayout.CENTER);
        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        supplierDialog.add(dialogPanel);
        supplierDialog.setVisible(true);
    }
    
    private void selectAllSuppliers() {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            String itemCode = (String) tableModel.getValueAt(row, 0);
            Item item = itemService.findByCode(itemCode);
            
            if (item != null) {
                java.util.List<Supplier> suppliers = supplierItemService.getSuppliersForItem(item.getItemId());
                
                if (suppliers.size() > 1) {
                    selectSupplierForItem(row);
                }
            }
        }
    }
    
    private void approvePR() {
        String comments = commentsArea.getText().trim();
        
        // Check if all items with multiple suppliers have been selected
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            String selectSupplier = (String) tableModel.getValueAt(row, 7);
            String supplierInfo = (String) tableModel.getValueAt(row, 6);
            
            if (supplierInfo.contains("supplier(s)") && 
                !supplierInfo.startsWith("1 ") && 
                selectSupplier.contains("Click dropdown")) {
                
                String itemName = (String) tableModel.getValueAt(row, 1);
                JOptionPane.showMessageDialog(this, 
                    "Please select a supplier for: " + itemName,
                    "Supplier Selection Required", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        int choice = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to approve this Purchase Requisition with selected suppliers?", 
            "Confirm Approval", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (choice == JOptionPane.YES_OPTION) {
            try {
                // First approve the PR
                if (prService.approvePR(pr.getPrId(), currentUser.getUsername())) {
                    
                    // Update PR lines with selected suppliers
                    updatePRLinesWithSelectedSuppliers();
                    
                    // Automatically create PO from approved PR
                    String poId = createPOFromApprovedPR();
                    
                    if (poId != null) {
                        JOptionPane.showMessageDialog(this, 
                            "✅ Purchase Requisition approved successfully!\n" +
                            "📋 PR ID: " + pr.getPrId() + "\n" +
                            "📦 Purchase Order created automatically: " + poId + "\n" +
                            "🔄 Status: Ready for Purchase Manager confirmation\n\n" +
                            "Next step: Go to 'Manage Purchase Orders' to confirm the PO.", 
                            "PR Approved & PO Created", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "✅ Purchase Requisition approved successfully!\n" +
                            "⚠️ However, PO creation failed. Please create manually.", 
                            "PR Approved", 
                            JOptionPane.WARNING_MESSAGE);
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Error approving the Purchase Requisition.", 
                        "Approval Failed", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error during approval process: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void rejectPR() {
        String comments = commentsArea.getText().trim();
        
        if (comments.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please provide comments for rejection.", 
                "Comments Required", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int choice = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to reject this Purchase Requisition?\nComments: " + comments, 
            "Confirm Rejection", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (choice == JOptionPane.YES_OPTION) {
            if (prService.rejectPR(pr.getPrId(), currentUser.getUsername())) {
                JOptionPane.showMessageDialog(this, 
                    "Purchase Requisition rejected.\nPR ID: " + pr.getPrId(), 
                    "Rejection Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Error rejecting the Purchase Requisition.", 
                    "Rejection Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void updatePRLinesWithSelectedSuppliers() {
        try {
            // Update PR lines with selected supplier information
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                String itemCode = (String) tableModel.getValueAt(row, 0);
                String selectedSupplier = (String) tableModel.getValueAt(row, 7);
                
                // Find the corresponding PR line
                for (PurchaseRequisitionLine line : pr.getLines()) {
                    if (line.getItemCode().equals(itemCode)) {
                        // Extract supplier ID from the selected supplier
                        if (selectedSupplier != null && !selectedSupplier.contains("Click dropdown") && !selectedSupplier.equals("No suppliers")) {
                            // Find the supplier by name
                            String supplierName = selectedSupplier.split(" - ")[0]; // Extract name before price
                            Supplier supplier = findSupplierByName(supplierName);
                            if (supplier != null) {
                                line.setSupplierId(supplier.getSupplierId());
                                // Update price if available from supplier-item relationship
                                Double supplierPrice = supplierItemService.getSupplierItemPrice(supplier.getSupplierId(), line.getItemId());
                                if (supplierPrice != null) {
                                    line.setEstimatedPrice(supplierPrice);
                                }
                            }
                        }
                        break;
                    }
                }
            }
            
            // Update PR lines in the service
            prService.updatePRLines(pr.getPrId(), pr.getLines());
            
        } catch (Exception e) {
            System.err.println("Error updating PR lines with suppliers: " + e.getMessage());
        }
    }
    
    private String createPOFromApprovedPR() {
        try {
            // Group items by supplier to create separate POs for each supplier
            java.util.Map<String, java.util.List<PurchaseRequisitionLine>> supplierGroups = new java.util.HashMap<>();
            
            for (PurchaseRequisitionLine line : pr.getLines()) {
                String supplierId = line.getSupplierId();
                if (supplierId != null && !supplierId.trim().isEmpty()) {
                    supplierGroups.computeIfAbsent(supplierId, k -> new java.util.ArrayList<>()).add(line);
                }
            }
            
            String firstPoId = null;
            
            // Create a PO for each supplier
            for (java.util.Map.Entry<String, java.util.List<PurchaseRequisitionLine>> entry : supplierGroups.entrySet()) {
                String supplierId = entry.getKey();
                java.util.List<PurchaseRequisitionLine> lines = entry.getValue();
                
                // Get supplier details
                Supplier supplier = supplierItemService.findSupplierById(supplierId);
                if (supplier == null) continue;
                
                // Create PO
                String poId = poService.generatePOId();
                if (firstPoId == null) firstPoId = poId;
                
                PurchaseOrder po = new PurchaseOrder();
                po.setPoId(poId);
                po.setPrId(pr.getPrId());
                po.setSupplierId(supplierId);
                po.setSupplierName(supplier.getSupplierName());
                po.setPoDate(java.time.LocalDate.now());
                po.setExpectedDeliveryDate(java.time.LocalDate.now().plusDays(14)); // Default 14 days
                po.setCreatedBy(currentUser.getUsername());
                po.setStatus("DRAFT"); // Created as draft, needs PM confirmation
                po.setPaymentTerms(supplier.getPaymentTerms());
                po.setDeliveryAddress("Default delivery address");
                po.setNotes("Auto-created from approved PR: " + pr.getPrId());
                
                // Create PO lines
                java.util.List<com.owsb.domain.PurchaseOrderLine> poLines = new java.util.ArrayList<>();
                double totalAmount = 0.0;
                
                for (int i = 0; i < lines.size(); i++) {
                    PurchaseRequisitionLine prLine = lines.get(i);
                    
                    com.owsb.domain.PurchaseOrderLine poLine = new com.owsb.domain.PurchaseOrderLine();
                    poLine.setPoId(poId);
                    poLine.setLineNumber(i + 1);
                    poLine.setItemId(prLine.getItemId());
                    poLine.setItemCode(prLine.getItemCode());
                    poLine.setItemName(prLine.getItemName());
                    poLine.setQuantity(prLine.getQuantity());
                    poLine.setUnit(prLine.getUnit());
                    poLine.setUnitPrice(prLine.getEstimatedPrice());
                    poLine.setSpecifications(prLine.getSpecifications());
                    poLine.setReceivedQuantity(0);
                    poLine.setStatus("PENDING");
                    
                    double lineTotal = prLine.getQuantity() * prLine.getEstimatedPrice();
                    poLine.setLineTotal(lineTotal);
                    totalAmount += lineTotal;
                    
                    poLines.add(poLine);
                }
                
                po.setTotalAmount(totalAmount);
                
                // Save PO and lines
                poService.createPO(po, poLines);
            }
            
            // Update PR status to indicate PO has been created
            prService.updatePRStatus(pr.getPrId(), "PO_CREATED");
            
            return firstPoId;
            
        } catch (Exception e) {
            System.err.println("Error creating PO from PR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private Supplier findSupplierByName(String supplierName) {
        try {
            java.util.List<Supplier> suppliers = supplierItemService.getAllSuppliers();
            for (Supplier supplier : suppliers) {
                if (supplier.getSupplierName().equals(supplierName)) {
                    return supplier;
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding supplier by name: " + e.getMessage());
        }
        return null;
    }
}