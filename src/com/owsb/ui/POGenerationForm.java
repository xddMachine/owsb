package com.owsb.ui;

import com.owsb.domain.*;
import com.owsb.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;

public class POGenerationForm extends JDialog {
    private JPanel mainPanel;
    private JComboBox<String> supplierCombo;
    private JTextField deliveryDateField;
    private JTextArea termsArea;
    private JTable itemsTable;
    private JButton addItemButton;
    private JButton removeItemButton;
    private JButton generateButton;
    private JButton cancelButton;
    private JLabel totalLabel;
    
    private DefaultTableModel tableModel;
    private ItemService itemService;
    private SupplierService supplierService;
    private POService poService;
    private PRService prService;
    private String currentPRId;
    private PurchaseRequisition currentPR;
    
    public POGenerationForm(JFrame parent) {
        super(parent, "Generate Purchase Order", true);
        initializeServices();
        initializeComponents();
    }
    
    public POGenerationForm(JFrame parent, String prId) {
        super(parent, "Generate Purchase Order from PR", true);
        this.currentPRId = prId;
        initializeServices();
        initializeComponents();
        loadFromPR(prId);
    }
    
    private void initializeServices() {
        this.itemService = new ItemService();
        this.supplierService = new SupplierService();
        this.poService = new POService();
        this.prService = new PRService();
    }
    
    private void initializeComponents() {
        setTitle("Purchase Order Generation");
        setSize(800, 700);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        headerPanel.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1;
        supplierCombo = new JComboBox<>();
        headerPanel.add(supplierCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        headerPanel.add(new JLabel("Expected Delivery:"), gbc);
        gbc.gridx = 1;
        deliveryDateField = new JTextField(15);
        headerPanel.add(deliveryDateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        headerPanel.add(new JLabel("Terms & Conditions:"), gbc);
        gbc.gridx = 1;
        termsArea = new JTextArea(3, 20);
        headerPanel.add(new JScrollPane(termsArea), gbc);
        
        // Items table
        String[] columns = {"Item ID", "Item Name", "Quantity", "Unit Price", "Total"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2 || column == 3; // Only quantity and unit price editable
            }
        };
        itemsTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(itemsTable);
        
        // Load suppliers
        loadSuppliers();
        
        // Items button panel
        JPanel itemButtonPanel = new JPanel(new FlowLayout());
        addItemButton = new JButton("Add Item");
        removeItemButton = new JButton("Remove Item");
        itemButtonPanel.add(addItemButton);
        itemButtonPanel.add(removeItemButton);
        
        // Footer panel
        JPanel footerPanel = new JPanel(new BorderLayout());
        totalLabel = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 14f));
        footerPanel.add(totalLabel, BorderLayout.EAST);
        
        // Main button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        generateButton = new JButton("Generate PO");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(generateButton);
        buttonPanel.add(cancelButton);
        
        // Assemble main panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        centerPanel.add(itemButtonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(footerPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Add action listeners
        addItemButton.addActionListener(e -> addItem());
        removeItemButton.addActionListener(e -> removeItem());
        generateButton.addActionListener(e -> generatePO());
        cancelButton.addActionListener(e -> dispose());
    }
    
    private void loadSuppliers() {
        supplierCombo.removeAllItems();
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        for (Supplier supplier : suppliers) {
            supplierCombo.addItem(supplier.getSupplierId() + " - " + supplier.getSupplierName());
        }
    }
    
    private void loadFromPR(String prId) {
        try {
            currentPR = prService.getPRById(prId);
            if (currentPR != null) {
                List<PurchaseRequisitionLine> lines = prService.getPRLines(prId);
                for (PurchaseRequisitionLine line : lines) {
                    if (line.getSupplierId() != null && !line.getSupplierId().isEmpty()) {
                        Item item = itemService.getItemById(line.getItemId());
                        if (item != null) {
                            Object[] rowData = {
                                line.getItemId(),
                                item.getItemName(),
                                line.getQuantity(),
                                line.getUnitPrice(),
                                line.getQuantity() * line.getUnitPrice()
                            };
                            tableModel.addRow(rowData);
                        }
                    }
                }
                updateTotal();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading PR data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addItem() {
        // Create item selection dialog
        List<Item> items = itemService.getAllItems();
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items available", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String[] itemOptions = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            itemOptions[i] = items.get(i).getItemId() + " - " + items.get(i).getItemName();
        }
        
        String selectedItem = (String) JOptionPane.showInputDialog(
            this,
            "Select an item:",
            "Add Item",
            JOptionPane.QUESTION_MESSAGE,
            null,
            itemOptions,
            itemOptions[0]
        );
        
        if (selectedItem != null) {
            String itemId = selectedItem.split(" - ")[0];
            Item item = itemService.getItemById(itemId);
            
            String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity:", "1");
            if (quantityStr != null && !quantityStr.trim().isEmpty()) {
                try {
                    int quantity = Integer.parseInt(quantityStr.trim());
                    String priceStr = JOptionPane.showInputDialog(this, "Enter unit price:", "0.00");
                    if (priceStr != null && !priceStr.trim().isEmpty()) {
                        double unitPrice = Double.parseDouble(priceStr.trim());
                        
                        Object[] rowData = {
                            item.getItemId(),
                            item.getItemName(),
                            quantity,
                            unitPrice,
                            quantity * unitPrice
                        };
                        tableModel.addRow(rowData);
                        updateTotal();
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid number format", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void removeItem() {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow >= 0) {
            tableModel.removeRow(selectedRow);
            updateTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to remove", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void generatePO() {
        if (validateForm()) {
            try {
                String selectedSupplier = (String) supplierCombo.getSelectedItem();
                if (selectedSupplier == null) {
                    JOptionPane.showMessageDialog(this, "Please select a supplier", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String supplierId = selectedSupplier.split(" - ")[0];
                
                LocalDate deliveryDate;
                try {
                    deliveryDate = LocalDate.parse(deliveryDateField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (DateTimeParseException e) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Create PO
                PurchaseOrder po = new PurchaseOrder();
                po.setPoId(poService.generatePOId());
                po.setSupplierId(supplierId);
                po.setPoDate(LocalDate.now());
                po.setExpectedDeliveryDate(deliveryDate);
                po.setTermsAndConditions(termsArea.getText());
                po.setStatus("PENDING");
                po.setTotalAmount(calculateTotal());
                if (currentPRId != null) {
                    po.setPrId(currentPRId);
                }
                
                // Create PO lines
                List<PurchaseOrderLine> poLines = new ArrayList<>();
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    PurchaseOrderLine line = new PurchaseOrderLine();
                    line.setPoId(po.getPoId());
                    line.setLineNumber(i + 1);
                    line.setItemId((String) tableModel.getValueAt(i, 0));
                    line.setQuantity((Integer) tableModel.getValueAt(i, 2));
                    line.setUnitPrice((Double) tableModel.getValueAt(i, 3));
                    line.setLineTotal((Double) tableModel.getValueAt(i, 4));
                    poLines.add(line);
                }
                
                // Save PO and lines
                poService.createPO(po, poLines);
                
                // Update PR status if this PO was generated from a PR
                if (currentPRId != null) {
                    prService.updatePRStatus(currentPRId, "CONVERTED_TO_PO");
                }
                
                JOptionPane.showMessageDialog(this, "Purchase Order " + po.getPoId() + " generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error generating PO: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private boolean validateForm() {
        if (supplierCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a supplier", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (deliveryDateField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter expected delivery date", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Please add at least one item", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private double calculateTotal() {
        double total = 0.0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            total += (Double) tableModel.getValueAt(i, 4);
        }
        return total;
    }
    
    private void updateTotal() {
        double total = calculateTotal();
        totalLabel.setText(String.format("Total: $%.2f", total));
    }
}