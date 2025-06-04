package com.owsb.ui;

import com.owsb.domain.Item;
import com.owsb.service.ItemService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ItemManagementForm extends JDialog {
    private ItemService itemService;
    private JTable itemTable;
    private DefaultTableModel tableModel;
    private JTextField itemCodeField, itemNameField, descriptionField, categoryField;
    private JTextField unitPriceField, stockQuantityField, reorderLevelField;
    private JComboBox<String> statusCombo;
    
    public ItemManagementForm(Frame parent) {
        super(parent, "Item Management", true);
        this.itemService = new ItemService();
        initializeComponents();
        loadItems();
    }
    
    private void initializeComponents() {
        setSize(1000, 600);
        setLayout(new BorderLayout());
        
        String[] columnNames = {"ID", "Code", "Name", "Description", "Category", 
                               "Unit Price", "Stock Qty", "Reorder Level", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemTable = new JTable(tableModel);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Item Code:"), gbc);
        gbc.gridx = 1;
        itemCodeField = new JTextField(15);
        formPanel.add(itemCodeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1;
        itemNameField = new JTextField(15);
        formPanel.add(itemNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        descriptionField = new JTextField(15);
        formPanel.add(descriptionField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        categoryField = new JTextField(15);
        formPanel.add(categoryField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Unit Price:"), gbc);
        gbc.gridx = 1;
        unitPriceField = new JTextField(15);
        formPanel.add(unitPriceField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Stock Quantity:"), gbc);
        gbc.gridx = 1;
        stockQuantityField = new JTextField(15);
        formPanel.add(stockQuantityField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Reorder Level:"), gbc);
        gbc.gridx = 1;
        reorderLevelField = new JTextField(15);
        formPanel.add(reorderLevelField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        formPanel.add(statusCombo, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Item");
        JButton updateButton = new JButton("Update Item");
        JButton deleteButton = new JButton("Delete Item");
        JButton clearButton = new JButton("Clear");
        JButton closeButton = new JButton("Close");
        
        addButton.addActionListener(e -> addItem());
        updateButton.addActionListener(e -> updateItem());
        deleteButton.addActionListener(e -> deleteItem());
        clearButton.addActionListener(e -> clearForm());
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(closeButton);
        
        itemTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateForm();
            }
        });
        
        add(scrollPane, BorderLayout.CENTER);
        add(formPanel, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setLocationRelativeTo(getParent());
    }
    
    private void loadItems() {
        tableModel.setRowCount(0);
        List<Item> items = itemService.listItems();
        
        for (Item item : items) {
            Object[] row = {
                item.getItemId(),
                item.getItemCode(),
                item.getItemName(),
                item.getDescription(),
                item.getCategory(),
                item.getUnitPrice(),
                item.getStockQuantity(),
                item.getReorderLevel(),
                item.getStatus()
            };
            tableModel.addRow(row);
        }
    }
    
    private void addItem() {
        try {
            String itemCode = itemCodeField.getText().trim();
            String itemName = itemNameField.getText().trim();
            String description = descriptionField.getText().trim();
            String category = categoryField.getText().trim();
            double unitPrice = Double.parseDouble(unitPriceField.getText().trim());
            int stockQuantity = Integer.parseInt(stockQuantityField.getText().trim());
            int reorderLevel = Integer.parseInt(reorderLevelField.getText().trim());
            
            Item newItem = itemService.createItem(itemCode, itemName, description, category,
                                                 unitPrice, stockQuantity, reorderLevel);
            loadItems();
            clearForm();
            JOptionPane.showMessageDialog(this, "Item created successfully!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for price and quantities.", 
                                        "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating item: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateItem() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to update.");
            return;
        }
        
        try {
            String itemId = (String) tableModel.getValueAt(selectedRow, 0);
            Item item = itemService.findById(itemId);
            
            if (item != null) {
                item.setItemCode(itemCodeField.getText().trim());
                item.setItemName(itemNameField.getText().trim());
                item.setDescription(descriptionField.getText().trim());
                item.setCategory(categoryField.getText().trim());
                item.setUnitPrice(Double.parseDouble(unitPriceField.getText().trim()));
                item.setStockQuantity(Integer.parseInt(stockQuantityField.getText().trim()));
                item.setReorderLevel(Integer.parseInt(reorderLevelField.getText().trim()));
                item.setStatus((String) statusCombo.getSelectedItem());
                
                itemService.updateItem(item);
                loadItems();
                clearForm();
                JOptionPane.showMessageDialog(this, "Item updated successfully!");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for price and quantities.", 
                                        "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating item: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteItem() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.");
            return;
        }
        
        String itemId = (String) tableModel.getValueAt(selectedRow, 0);
        String itemName = (String) tableModel.getValueAt(selectedRow, 2);
        
        int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete item: " + itemName + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            if (itemService.deleteItem(itemId)) {
                loadItems();
                clearForm();
                JOptionPane.showMessageDialog(this, "Item deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Error deleting item.", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void populateForm() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow != -1) {
            itemCodeField.setText((String) tableModel.getValueAt(selectedRow, 1));
            itemNameField.setText((String) tableModel.getValueAt(selectedRow, 2));
            descriptionField.setText((String) tableModel.getValueAt(selectedRow, 3));
            categoryField.setText((String) tableModel.getValueAt(selectedRow, 4));
            unitPriceField.setText(String.valueOf(tableModel.getValueAt(selectedRow, 5)));
            stockQuantityField.setText(String.valueOf(tableModel.getValueAt(selectedRow, 6)));
            reorderLevelField.setText(String.valueOf(tableModel.getValueAt(selectedRow, 7)));
            statusCombo.setSelectedItem(tableModel.getValueAt(selectedRow, 8));
        }
    }
    
    private void clearForm() {
        itemCodeField.setText("");
        itemNameField.setText("");
        descriptionField.setText("");
        categoryField.setText("");
        unitPriceField.setText("");
        stockQuantityField.setText("");
        reorderLevelField.setText("");
        statusCombo.setSelectedIndex(0);
        itemTable.clearSelection();
    }
}