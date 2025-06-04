package com.owsb.ui;

import com.owsb.domain.Supplier;
import com.owsb.domain.Item;
import com.owsb.service.SupplierService;
import com.owsb.service.SupplierItemService;
import com.owsb.service.ItemService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SupplierManagementForm extends JDialog {
    private SupplierService supplierService;
    private SupplierItemService supplierItemService;
    private ItemService itemService;
    private JTable supplierTable;
    private DefaultTableModel tableModel;
    private JTextField supplierCodeField, supplierNameField, contactPersonField;
    private JTextField phoneField, emailField, addressField, paymentTermsField;
    private JComboBox<String> statusCombo;
    private JButton manageItemsButton;
    
    public SupplierManagementForm(Frame parent) {
        super(parent, "Supplier Management", true);
        this.supplierService = new SupplierService();
        this.supplierItemService = new SupplierItemService();
        this.itemService = new ItemService();
        initializeComponents();
        loadSuppliers();
    }
    
    private void initializeComponents() {
        setSize(1200, 700);
        setLayout(new BorderLayout());
        
        String[] columnNames = {"ID", "Code", "Name", "Contact Person", "Phone", "Email", "Address", "Payment Terms", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        supplierTable = new JTable(tableModel);
        supplierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(supplierTable);
        scrollPane.setPreferredSize(new Dimension(700, 300));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Supplier Code:"), gbc);
        gbc.gridx = 1;
        supplierCodeField = new JTextField(15);
        formPanel.add(supplierCodeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Supplier Name:"), gbc);
        gbc.gridx = 1;
        supplierNameField = new JTextField(15);
        formPanel.add(supplierNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Contact Person:"), gbc);
        gbc.gridx = 1;
        contactPersonField = new JTextField(15);
        formPanel.add(contactPersonField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(15);
        formPanel.add(phoneField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(15);
        formPanel.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        addressField = new JTextField(15);
        formPanel.add(addressField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Payment Terms:"), gbc);
        gbc.gridx = 1;
        paymentTermsField = new JTextField(15);
        formPanel.add(paymentTermsField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        formPanel.add(statusCombo, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Supplier");
        JButton updateButton = new JButton("Update Supplier");
        JButton deleteButton = new JButton("Delete Supplier");
        JButton clearButton = new JButton("Clear");
        manageItemsButton = new JButton("Manage Items");
        JButton closeButton = new JButton("Close");
        
        addButton.addActionListener(e -> addSupplier());
        updateButton.addActionListener(e -> updateSupplier());
        deleteButton.addActionListener(e -> deleteSupplier());
        clearButton.addActionListener(e -> clearForm());
        manageItemsButton.addActionListener(e -> manageSupplierItems());
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(manageItemsButton);
        buttonPanel.add(closeButton);
        
        supplierTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateForm();
                manageItemsButton.setEnabled(supplierTable.getSelectedRow() != -1);
            }
        });
        
        manageItemsButton.setEnabled(false);
        
        add(scrollPane, BorderLayout.CENTER);
        add(formPanel, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setLocationRelativeTo(getParent());
    }
    
    private void loadSuppliers() {
        tableModel.setRowCount(0);
        List<Supplier> suppliers = supplierService.listSuppliers();
        
        for (Supplier supplier : suppliers) {
            Object[] row = {
                supplier.getSupplierId(),
                supplier.getSupplierCode(),
                supplier.getSupplierName(),
                supplier.getContactPerson(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getAddress(),
                supplier.getPaymentTerms(),
                supplier.getStatus()
            };
            tableModel.addRow(row);
        }
    }
    
    private void addSupplier() {
        try {
            String supplierCode = supplierCodeField.getText().trim();
            String supplierName = supplierNameField.getText().trim();
            String contactPerson = contactPersonField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();
            String paymentTerms = paymentTermsField.getText().trim();
            
            Supplier newSupplier = supplierService.createSupplier(supplierCode, supplierName, 
                contactPerson, phone, email, address, paymentTerms);
            loadSuppliers();
            clearForm();
            JOptionPane.showMessageDialog(this, "Supplier created successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating supplier: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to update.");
            return;
        }
        
        try {
            String supplierId = (String) tableModel.getValueAt(selectedRow, 0);
            Supplier supplier = supplierService.findById(supplierId);
            
            if (supplier != null) {
                supplier.setSupplierCode(supplierCodeField.getText().trim());
                supplier.setSupplierName(supplierNameField.getText().trim());
                supplier.setContactPerson(contactPersonField.getText().trim());
                supplier.setPhone(phoneField.getText().trim());
                supplier.setEmail(emailField.getText().trim());
                supplier.setAddress(addressField.getText().trim());
                supplier.setPaymentTerms(paymentTermsField.getText().trim());
                supplier.setStatus((String) statusCombo.getSelectedItem());
                
                supplierService.updateSupplier(supplier);
                loadSuppliers();
                clearForm();
                JOptionPane.showMessageDialog(this, "Supplier updated successfully!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating supplier: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to delete.");
            return;
        }
        
        String supplierId = (String) tableModel.getValueAt(selectedRow, 0);
        String supplierName = (String) tableModel.getValueAt(selectedRow, 2);
        
        int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete supplier: " + supplierName + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            if (supplierService.deleteSupplier(supplierId)) {
                loadSuppliers();
                clearForm();
                JOptionPane.showMessageDialog(this, "Supplier deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Error deleting supplier.", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void populateForm() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow != -1) {
            supplierCodeField.setText((String) tableModel.getValueAt(selectedRow, 1));
            supplierNameField.setText((String) tableModel.getValueAt(selectedRow, 2));
            contactPersonField.setText((String) tableModel.getValueAt(selectedRow, 3));
            phoneField.setText((String) tableModel.getValueAt(selectedRow, 4));
            emailField.setText((String) tableModel.getValueAt(selectedRow, 5));
            addressField.setText((String) tableModel.getValueAt(selectedRow, 6));
            paymentTermsField.setText((String) tableModel.getValueAt(selectedRow, 7));
            statusCombo.setSelectedItem(tableModel.getValueAt(selectedRow, 8));
        }
    }
    
    private void clearForm() {
        supplierCodeField.setText("");
        supplierNameField.setText("");
        contactPersonField.setText("");
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");
        paymentTermsField.setText("");
        statusCombo.setSelectedIndex(0);
        supplierTable.clearSelection();
        manageItemsButton.setEnabled(false);
    }
    
    private void manageSupplierItems() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a supplier first.");
            return;
        }
        
        String supplierId = (String) tableModel.getValueAt(selectedRow, 0);
        String supplierName = (String) tableModel.getValueAt(selectedRow, 2);
        
        new SupplierItemManagementDialog(this, supplierId, supplierName, 
                                        supplierItemService, itemService).setVisible(true);
    }
}

class SupplierItemManagementDialog extends JDialog {
    private String supplierId;
    private String supplierName;
    private SupplierItemService supplierItemService;
    private ItemService itemService;
    private JTable supplierItemsTable;
    private JTable availableItemsTable;
    private DefaultTableModel supplierItemsModel;
    private DefaultTableModel availableItemsModel;
    
    public SupplierItemManagementDialog(Dialog parent, String supplierId, String supplierName,
                                       SupplierItemService supplierItemService, ItemService itemService) {
        super(parent, "Manage Items for " + supplierName, true);
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.supplierItemService = supplierItemService;
        this.itemService = itemService;
        initializeComponents();
        loadData();
    }
    
    private void initializeComponents() {
        setSize(800, 600);
        setLayout(new BorderLayout());
        
        // Supplier's items table
        String[] supplierItemsColumns = {"Item ID", "Item Code", "Item Name", "Price", "Lead Time"};
        supplierItemsModel = new DefaultTableModel(supplierItemsColumns, 0);
        supplierItemsTable = new JTable(supplierItemsModel);
        JScrollPane supplierItemsScroll = new JScrollPane(supplierItemsTable);
        supplierItemsScroll.setBorder(BorderFactory.createTitledBorder("Current Supplier Items"));
        
        // Available items table
        String[] availableItemsColumns = {"Item ID", "Item Code", "Item Name", "Category"};
        availableItemsModel = new DefaultTableModel(availableItemsColumns, 0);
        availableItemsTable = new JTable(availableItemsModel);
        JScrollPane availableItemsScroll = new JScrollPane(availableItemsTable);
        availableItemsScroll.setBorder(BorderFactory.createTitledBorder("Available Items"));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addItemButton = new JButton("Add Item to Supplier");
        JButton removeItemButton = new JButton("Remove Item from Supplier");
        JButton closeButton = new JButton("Close");
        
        addItemButton.addActionListener(e -> addItemToSupplier());
        removeItemButton.addActionListener(e -> removeItemFromSupplier());
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(addItemButton);
        buttonPanel.add(removeItemButton);
        buttonPanel.add(closeButton);
        
        // Layout
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        tablesPanel.add(supplierItemsScroll);
        tablesPanel.add(availableItemsScroll);
        
        add(tablesPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setLocationRelativeTo(getParent());
    }
    
    private void loadData() {
        // Load supplier's current items
        supplierItemsModel.setRowCount(0);
        List<Item> supplierItems = supplierItemService.getItemsForSupplier(supplierId);
        for (Item item : supplierItems) {
            Double price = supplierItemService.getSupplierItemPrice(supplierId, item.getItemId());
            Object[] row = {item.getItemId(), item.getItemCode(), item.getItemName(), 
                           price != null ? String.format("%.2f", price) : "N/A", "7 days"};
            supplierItemsModel.addRow(row);
        }
        
        // Load all available items
        availableItemsModel.setRowCount(0);
        List<Item> allItems = itemService.listItems();
        for (Item item : allItems) {
            Object[] row = {item.getItemId(), item.getItemCode(), item.getItemName(), item.getCategory()};
            availableItemsModel.addRow(row);
        }
    }
    
    private void addItemToSupplier() {
        int selectedRow = availableItemsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to add.");
            return;
        }
        
        String itemId = (String) availableItemsModel.getValueAt(selectedRow, 0);
        String itemName = (String) availableItemsModel.getValueAt(selectedRow, 2);
        
        String priceStr = JOptionPane.showInputDialog(this, 
                "Enter price for " + itemName + ":", "Price", JOptionPane.QUESTION_MESSAGE);
        
        if (priceStr != null && !priceStr.trim().isEmpty()) {
            try {
                double price = Double.parseDouble(priceStr.trim());
                if (supplierItemService.addSupplierItem(supplierId, itemId, price, 7)) {
                    loadData();
                    JOptionPane.showMessageDialog(this, "Item added to supplier successfully!");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid price.", 
                                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void removeItemFromSupplier() {
        int selectedRow = supplierItemsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.");
            return;
        }
        
        String itemId = (String) supplierItemsModel.getValueAt(selectedRow, 0);
        String itemName = (String) supplierItemsModel.getValueAt(selectedRow, 2);
        
        int result = JOptionPane.showConfirmDialog(this, 
                "Remove " + itemName + " from " + supplierName + "?",
                "Confirm Remove", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            if (supplierItemService.removeSupplierItem(supplierId, itemId)) {
                loadData();
                JOptionPane.showMessageDialog(this, "Item removed from supplier successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Error removing item from supplier.", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}