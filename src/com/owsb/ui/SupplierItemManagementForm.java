package com.owsb.ui;

import com.owsb.domain.*;
import com.owsb.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SupplierItemManagementForm extends JDialog {
    private JTable relationshipTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> supplierCombo;
    private JComboBox<String> itemCombo;
    private JTextField priceField;
    private JTextField leadTimeField;
    private JButton addButton;
    private JButton removeButton;
    private JButton refreshButton;
    private JButton closeButton;
    
    private SupplierItemService supplierItemService;
    private SupplierService supplierService;
    private ItemService itemService;
    
    public SupplierItemManagementForm(JFrame parent) {
        super(parent, "Manage Supplier-Item Relationships", true);
        initializeServices();
        initializeComponents();
        loadRelationships();
    }
    
    private void initializeServices() {
        this.supplierItemService = new SupplierItemService();
        this.supplierService = new SupplierService();
        this.itemService = new ItemService();
    }
    
    private void initializeComponents() {
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Top panel for adding new relationships
        JPanel addPanel = new JPanel(new GridBagLayout());
        addPanel.setBorder(BorderFactory.createTitledBorder("Add New Supplier-Item Relationship"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        addPanel.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1;
        supplierCombo = new JComboBox<>();
        loadSuppliers();
        addPanel.add(supplierCombo, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        addPanel.add(new JLabel("Item:"), gbc);
        gbc.gridx = 3;
        itemCombo = new JComboBox<>();
        loadItems();
        addPanel.add(itemCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        addPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        priceField = new JTextField(10);
        addPanel.add(priceField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 1;
        addPanel.add(new JLabel("Lead Time (days):"), gbc);
        gbc.gridx = 3;
        leadTimeField = new JTextField(5);
        addPanel.add(leadTimeField, gbc);
        
        gbc.gridx = 4; gbc.gridy = 0; gbc.gridheight = 2;
        addButton = new JButton("Add Relationship");
        addButton.addActionListener(e -> addRelationship());
        addPanel.add(addButton, gbc);
        
        // Table for existing relationships
        String[] columns = {"Supplier ID", "Supplier Name", "Item ID", "Item Name", "Price", "Lead Time (days)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        relationshipTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(relationshipTable);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        removeButton = new JButton("Remove Selected");
        refreshButton = new JButton("Refresh");
        closeButton = new JButton("Close");
        
        removeButton.addActionListener(e -> removeRelationship());
        refreshButton.addActionListener(e -> loadRelationships());
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(removeButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        mainPanel.add(addPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void loadSuppliers() {
        supplierCombo.removeAllItems();
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        for (Supplier supplier : suppliers) {
            supplierCombo.addItem(supplier.getSupplierId() + " - " + supplier.getSupplierName());
        }
    }
    
    private void loadItems() {
        itemCombo.removeAllItems();
        List<Item> items = itemService.getAllItems();
        for (Item item : items) {
            itemCombo.addItem(item.getItemId() + " - " + item.getItemName());
        }
    }
    
    private void loadRelationships() {
        tableModel.setRowCount(0);
        
        try {
            List<Supplier> suppliers = supplierService.getAllSuppliers();
            for (Supplier supplier : suppliers) {
                List<Item> items = supplierItemService.getItemsForSupplier(supplier.getSupplierId());
                for (Item item : items) {
                    Double price = supplierItemService.getSupplierItemPrice(supplier.getSupplierId(), item.getItemId());
                    
                    Object[] rowData = {
                        supplier.getSupplierId(),
                        supplier.getSupplierName(),
                        item.getItemId(),
                        item.getItemName(),
                        price != null ? String.format("$%.2f", price) : "N/A",
                        "N/A" // Lead time not stored in current implementation
                    };
                    tableModel.addRow(rowData);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading relationships: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addRelationship() {
        try {
            String selectedSupplier = (String) supplierCombo.getSelectedItem();
            String selectedItem = (String) itemCombo.getSelectedItem();
            
            if (selectedSupplier == null || selectedItem == null) {
                JOptionPane.showMessageDialog(this, "Please select both supplier and item", 
                                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String supplierId = selectedSupplier.split(" - ")[0];
            String itemId = selectedItem.split(" - ")[0];
            
            double price;
            int leadTime;
            
            try {
                price = Double.parseDouble(priceField.getText().trim());
                leadTime = Integer.parseInt(leadTimeField.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for price and lead time", 
                                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (price <= 0 || leadTime <= 0) {
                JOptionPane.showMessageDialog(this, "Price and lead time must be positive", 
                                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if relationship already exists
            Double existingPrice = supplierItemService.getSupplierItemPrice(supplierId, itemId);
            if (existingPrice != null) {
                int result = JOptionPane.showConfirmDialog(this, 
                    "Relationship already exists with price $" + String.format("%.2f", existingPrice) + 
                    ". Do you want to update it?", 
                    "Relationship Exists", 
                    JOptionPane.YES_NO_OPTION);
                
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
                
                // Remove existing relationship first
                supplierItemService.removeSupplierItem(supplierId, itemId);
            }
            
            // Add new relationship
            if (supplierItemService.addSupplierItem(supplierId, itemId, price, leadTime)) {
                JOptionPane.showMessageDialog(this, "Supplier-Item relationship added successfully!", 
                                            "Success", JOptionPane.INFORMATION_MESSAGE);
                priceField.setText("");
                leadTimeField.setText("");
                loadRelationships();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add relationship", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding relationship: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void removeRelationship() {
        int selectedRow = relationshipTable.getSelectedRow();
        if (selectedRow >= 0) {
            String supplierId = (String) tableModel.getValueAt(selectedRow, 0);
            String itemId = (String) tableModel.getValueAt(selectedRow, 2);
            String supplierName = (String) tableModel.getValueAt(selectedRow, 1);
            String itemName = (String) tableModel.getValueAt(selectedRow, 3);
            
            int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to remove the relationship between " + 
                supplierName + " and " + itemName + "?", 
                "Confirm Removal", 
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                if (supplierItemService.removeSupplierItem(supplierId, itemId)) {
                    JOptionPane.showMessageDialog(this, "Relationship removed successfully!", 
                                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadRelationships();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to remove relationship", 
                                                "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a relationship to remove", 
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
}