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

public class ReceiveGoodsForm extends JDialog {
    private JPanel mainPanel;
    private JTextField poNumberField;
    private JTextField deliveryDateField;
    private JTextField supplierField;
    private JTable receivedItemsTable;
    private JButton searchPOButton;
    private JButton receiveAllButton;
    private JButton partialReceiveButton;
    private JButton completeButton;
    private JButton cancelButton;
    private JLabel poStatusLabel;
    private JTextArea notesArea;
    
    private DefaultTableModel tableModel;
    private POService poService;
    private ItemService itemService;
    private SupplierService supplierService;
    private PurchaseOrder currentPO;
    private List<PurchaseOrderLine> currentPOLines;
    
    public ReceiveGoodsForm(JFrame parent) {
        super(parent, "Receive Goods", true);
        initializeServices();
        initializeComponents();
    }
    
    public ReceiveGoodsForm(JFrame parent, String poNumber) {
        super(parent, "Receive Goods", true);
        initializeServices();
        initializeComponents();
        loadPODetails(poNumber);
    }
    
    private void initializeServices() {
        this.poService = new POService();
        this.itemService = new ItemService();
        this.supplierService = new SupplierService();
    }
    
    private void initializeComponents() {
        setTitle("Goods Receipt");
        setSize(900, 700);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBorder(BorderFactory.createTitledBorder("Purchase Order Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        headerPanel.add(new JLabel("PO Number:"), gbc);
        gbc.gridx = 1;
        poNumberField = new JTextField(15);
        headerPanel.add(poNumberField, gbc);
        gbc.gridx = 2;
        searchPOButton = new JButton("Search PO");
        headerPanel.add(searchPOButton, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        headerPanel.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        supplierField = new JTextField(20);
        supplierField.setEditable(false);
        headerPanel.add(supplierField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        headerPanel.add(new JLabel("Delivery Date:"), gbc);
        gbc.gridx = 1;
        deliveryDateField = new JTextField(15);
        headerPanel.add(deliveryDateField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 2;
        poStatusLabel = new JLabel("Status: Not Loaded");
        poStatusLabel.setFont(poStatusLabel.getFont().deriveFont(Font.BOLD));
        headerPanel.add(poStatusLabel, gbc);
        
        // Items table
        String[] columns = {"Item ID", "Item Name", "Ordered Qty", "Received Qty", "Remaining", "Unit Price", "Receive Now", "Condition"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 6) return Integer.class; // Receive Now column
                return Object.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6 || column == 7; // Only "Receive Now" and "Condition" editable
            }
        };
        receivedItemsTable = new JTable(tableModel);
        
        // Add combobox for condition column
        JComboBox<String> conditionCombo = new JComboBox<>(new String[]{"Good", "Damaged", "Defective"});
        receivedItemsTable.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(conditionCombo));
        
        JScrollPane tableScrollPane = new JScrollPane(receivedItemsTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Items to Receive"));
        
        // Notes panel
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setBorder(BorderFactory.createTitledBorder("Delivery Notes"));
        notesArea = new JTextArea(4, 30);
        notesPanel.add(new JScrollPane(notesArea), BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        receiveAllButton = new JButton("Receive All");
        partialReceiveButton = new JButton("Partial Receipt");
        completeButton = new JButton("Complete Receipt");
        cancelButton = new JButton("Cancel");
        
        buttonPanel.add(receiveAllButton);
        buttonPanel.add(partialReceiveButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(cancelButton);
        
        // Assemble main panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        centerPanel.add(notesPanel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Add action listeners
        searchPOButton.addActionListener(e -> searchPO());
        receiveAllButton.addActionListener(e -> receiveAll());
        partialReceiveButton.addActionListener(e -> partialReceive());
        completeButton.addActionListener(e -> completeReceipt());
        cancelButton.addActionListener(e -> dispose());
        
        // Initially disable receipt buttons
        setReceiptButtonsEnabled(false);
    }
    
    private void loadPODetails(String poNumber) {
        poNumberField.setText(poNumber);
        searchPO();
    }
    
    private void searchPO() {
        String poNumber = poNumberField.getText().trim();
        if (poNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a PO number", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            currentPO = poService.findPOById(poNumber);
            if (currentPO == null) {
                JOptionPane.showMessageDialog(this, "Purchase Order not found", "Error", JOptionPane.ERROR_MESSAGE);
                clearForm();
                return;
            }
            
            if (!"APPROVED".equals(currentPO.getStatus())) {
                JOptionPane.showMessageDialog(this, "Only approved POs can be received", "Error", JOptionPane.ERROR_MESSAGE);
                clearForm();
                return;
            }
            
            // Load supplier details
            Supplier supplier = supplierService.getSupplierById(currentPO.getSupplierId());
            if (supplier != null) {
                supplierField.setText(supplier.getSupplierName());
            }
            
            deliveryDateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            poStatusLabel.setText("Status: " + currentPO.getStatus());
            
            // Load PO lines
            currentPOLines = poService.getPOLines(poNumber);
            loadPOLines();
            
            setReceiptButtonsEnabled(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading PO: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            clearForm();
        }
    }
    
    private void loadPOLines() {
        tableModel.setRowCount(0);
        
        for (PurchaseOrderLine line : currentPOLines) {
            Item item = itemService.getItemById(line.getItemId());
            String itemName = (item != null) ? item.getItemName() : "Unknown";
            
            int remaining = line.getQuantity() - line.getReceivedQuantity();
            
            Object[] rowData = {
                line.getItemId(),
                itemName,
                line.getQuantity(),
                line.getReceivedQuantity(),
                remaining,
                line.getUnitPrice(),
                0, // Receive Now - default to 0
                "Good" // Default condition
            };
            tableModel.addRow(rowData);
        }
    }
    
    private void receiveAll() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int remaining = (Integer) tableModel.getValueAt(i, 4);
            tableModel.setValueAt(remaining, i, 6); // Set "Receive Now" to remaining quantity
        }
        JOptionPane.showMessageDialog(this, "Set all items to receive full remaining quantities", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void partialReceive() {
        if (validateReceiptQuantities()) {
            processReceipt(false);
        }
    }
    
    private void completeReceipt() {
        int result = JOptionPane.showConfirmDialog(this, 
            "Complete the goods receipt? This will update inventory and cannot be undone.", 
            "Confirm Receipt", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            if (validateReceiptQuantities()) {
                processReceipt(true);
            }
        }
    }
    
    private boolean validateReceiptQuantities() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object receiveNowObj = tableModel.getValueAt(i, 6);
            if (receiveNowObj == null) {
                JOptionPane.showMessageDialog(this, "Please enter receive quantities for all items", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            try {
                int receiveNow = Integer.parseInt(receiveNowObj.toString());
                int remaining = (Integer) tableModel.getValueAt(i, 4);
                
                if (receiveNow < 0) {
                    JOptionPane.showMessageDialog(this, "Receive quantity cannot be negative", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                
                if (receiveNow > remaining) {
                    JOptionPane.showMessageDialog(this, "Cannot receive more than remaining quantity for item: " + 
                        tableModel.getValueAt(i, 0), "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid receive quantity format", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        return true;
    }
    
    private void processReceipt(boolean isComplete) {
        try {
            LocalDate deliveryDate = LocalDate.parse(deliveryDateField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            boolean allItemsFullyReceived = true;
            
            // Update each item's received quantity and stock
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String itemId = (String) tableModel.getValueAt(i, 0);
                int receiveNow = Integer.parseInt(tableModel.getValueAt(i, 6).toString());
                String condition = (String) tableModel.getValueAt(i, 7);
                
                if (receiveNow > 0) {
                    // Update PO line received quantity
                    PurchaseOrderLine line = currentPOLines.get(i);
                    int newReceivedQty = line.getReceivedQuantity() + receiveNow;
                    
                    // Only update stock for items in good condition
                    if ("Good".equals(condition)) {
                        poService.updateStockOnReceive(currentPO.getPoId(), itemId, receiveNow);
                    }
                    
                    // Update line status
                    if (newReceivedQty >= line.getQuantity()) {
                        // Fully received
                        line.setReceivedQuantity(line.getQuantity());
                        line.setStatus("COMPLETED");
                    } else {
                        line.setReceivedQuantity(newReceivedQty);
                        line.setStatus("PARTIALLY_RECEIVED");
                        allItemsFullyReceived = false;
                    }
                }
                
                // Check if item is fully received
                if (currentPOLines.get(i).getReceivedQuantity() < currentPOLines.get(i).getQuantity()) {
                    allItemsFullyReceived = false;
                }
            }
            
            // Update PO status
            if (allItemsFullyReceived || isComplete) {
                currentPO.setStatus("COMPLETED");
            } else {
                currentPO.setStatus("PARTIALLY_RECEIVED");
            }
            
            // Save updates
            poService.updatePO(currentPO);
            
            String message = isComplete ? "Goods receipt completed successfully!" : "Partial goods receipt processed successfully!";
            JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh the display
            loadPOLines();
            poStatusLabel.setText("Status: " + currentPO.getStatus());
            
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid delivery date format. Use YYYY-MM-DD", "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error processing receipt: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        supplierField.setText("");
        deliveryDateField.setText("");
        poStatusLabel.setText("Status: Not Loaded");
        tableModel.setRowCount(0);
        currentPO = null;
        currentPOLines = null;
        setReceiptButtonsEnabled(false);
    }
    
    private void setReceiptButtonsEnabled(boolean enabled) {
        receiveAllButton.setEnabled(enabled);
        partialReceiveButton.setEnabled(enabled);
        completeButton.setEnabled(enabled);
    }
}