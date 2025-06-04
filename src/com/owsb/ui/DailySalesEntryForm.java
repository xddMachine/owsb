package com.owsb.ui;

import com.owsb.domain.DailySalesRecord;
import com.owsb.domain.Item;
import com.owsb.domain.User;
import com.owsb.service.SalesService;
import com.owsb.service.ItemService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class DailySalesEntryForm extends JDialog {
    private JPanel mainPanel;
    private JTextField dateField;
    private JComboBox<String> itemComboBox;
    private JTextField quantityField;
    private JTextField priceField;
    private JButton addItemButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JTable salesItemsTable;
    private DefaultTableModel salesTableModel;
    private User currentUser;
    private SalesService salesService;
    private ItemService itemService;
    private double totalAmount = 0.0;
    private JLabel totalLabel;
    
    public DailySalesEntryForm(JFrame parent, User user) {
        super(parent, "Daily Sales Entry", true);
        this.currentUser = user;
        this.salesService = new SalesService();
        this.itemService = new ItemService();
        initializeComponents();
        loadItems();
        setCurrentDate();
    }
    
    private void initializeComponents() {
        setTitle("Daily Sales Entry");
        setSize(700, 600);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        
        // Input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1;
        dateField = new JTextField(15);
        dateField.setEditable(false);
        inputPanel.add(dateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Item:"), gbc);
        gbc.gridx = 1;
        itemComboBox = new JComboBox<>();
        itemComboBox.setPreferredSize(new Dimension(200, 25));
        inputPanel.add(itemComboBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        quantityField = new JTextField(15);
        inputPanel.add(quantityField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Unit Price:"), gbc);
        gbc.gridx = 1;
        priceField = new JTextField(15);
        inputPanel.add(priceField, gbc);
        
        gbc.gridx = 1; gbc.gridy = 4;
        addItemButton = new JButton("Add Item");
        inputPanel.add(addItemButton, gbc);
        
        // Table for sales items
        String[] columns = {"Item Code", "Item Name", "Quantity", "Unit Price", "Total"};
        salesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        salesItemsTable = new JTable(salesTableModel);
        JScrollPane scrollPane = new JScrollPane(salesItemsTable);
        scrollPane.setPreferredSize(new Dimension(650, 200));
        
        // Total and button panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalLabel = new JLabel("Total Amount: $0.00");
        totalLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        totalPanel.add(totalLabel);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        saveButton = new JButton("Save Sales Record");
        cancelButton = new JButton("Cancel");
        JButton clearButton = new JButton("Clear All");
        
        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(cancelButton);
        
        bottomPanel.add(totalPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Add action listeners
        itemComboBox.addActionListener(e -> updatePriceField());
        addItemButton.addActionListener(e -> addSalesItem());
        saveButton.addActionListener(e -> saveSales());
        cancelButton.addActionListener(e -> dispose());
        
        // Clear all button
        clearButton.addActionListener(e -> {
            salesTableModel.setRowCount(0);
            totalAmount = 0.0;
            updateTotalLabel();
            clearInputFields();
        });
    }
    
    private void loadItems() {
        itemComboBox.removeAllItems();
        itemComboBox.addItem("-- Select Item --");
        
        List<Item> items = itemService.listItems();
        for (Item item : items) {
            if ("ACTIVE".equals(item.getStatus())) {
                itemComboBox.addItem(item.getItemCode() + " - " + item.getItemName());
            }
        }
    }
    
    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        dateField.setText(sdf.format(new Date()));
    }
    
    private void updatePriceField() {
        if (itemComboBox.getSelectedIndex() > 0) {
            String selectedItem = (String) itemComboBox.getSelectedItem();
            String itemCode = selectedItem.split(" - ")[0];
            Item item = itemService.findByCode(itemCode);
            if (item != null) {
                priceField.setText(String.format("%.2f", item.getUnitPrice()));
            }
        } else {
            priceField.setText("");
        }
    }
    
    private void addSalesItem() {
        try {
            if (itemComboBox.getSelectedIndex() <= 0) {
                JOptionPane.showMessageDialog(this, "Please select an item.");
                return;
            }
            
            String selectedItem = (String) itemComboBox.getSelectedItem();
            String itemCode = selectedItem.split(" - ")[0];
            Item item = itemService.findByCode(itemCode);
            
            if (item == null) {
                JOptionPane.showMessageDialog(this, "Item not found.");
                return;
            }
            
            int quantity = Integer.parseInt(quantityField.getText().trim());
            double unitPrice = Double.parseDouble(priceField.getText().trim());
            
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.");
                return;
            }
            
            if (unitPrice <= 0) {
                JOptionPane.showMessageDialog(this, "Price must be greater than 0.");
                return;
            }
            
            // Check if item has enough stock
            if (quantity > item.getStockQuantity()) {
                JOptionPane.showMessageDialog(this, 
                    "Insufficient stock. Available: " + item.getStockQuantity(), 
                    "Stock Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            double itemTotal = quantity * unitPrice;
            
            // Check if item already exists in the table
            for (int i = 0; i < salesTableModel.getRowCount(); i++) {
                if (salesTableModel.getValueAt(i, 0).equals(itemCode)) {
                    JOptionPane.showMessageDialog(this, "Item already added. Please modify the existing entry.");
                    return;
                }
            }
            
            Object[] row = {itemCode, item.getItemName(), quantity, 
                           String.format("%.2f", unitPrice), String.format("%.2f", itemTotal)};
            salesTableModel.addRow(row);
            
            totalAmount += itemTotal;
            updateTotalLabel();
            clearInputFields();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for quantity and price.", 
                                        "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveSales() {
        if (salesTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Please add at least one item to save.");
            return;
        }
        
        try {
            String dateStr = dateField.getText();
            LocalDate salesDate = LocalDate.parse(dateStr);
            
            // Process each item in the sales table
            boolean allSaved = true;
            for (int i = 0; i < salesTableModel.getRowCount(); i++) {
                String itemCode = (String) salesTableModel.getValueAt(i, 0);
                String itemName = (String) salesTableModel.getValueAt(i, 1);
                int quantity = Integer.parseInt(salesTableModel.getValueAt(i, 2).toString());
                double unitPrice = Double.parseDouble(salesTableModel.getValueAt(i, 3).toString());
                
                Item item = itemService.findByCode(itemCode);
                if (item != null) {
                    // Create individual sales record for each item
                    String recordId = salesService.generateSalesRecordId();
                    DailySalesRecord salesRecord = new DailySalesRecord(recordId, salesDate, 
                        item.getItemId(), itemCode, itemName, quantity, unitPrice, 
                        currentUser.getUserId(), "Walk-in Customer");
                    
                    // Save the sales record
                    if (salesService.saveDailySalesRecord(salesRecord)) {
                        // Update stock after successful save
                        int newStock = item.getStockQuantity() - quantity;
                        itemService.updateStock(item.getItemId(), newStock);
                    } else {
                        allSaved = false;
                    }
                }
            }
            
            // Check if all items were saved successfully
            if (allSaved) {
                JOptionPane.showMessageDialog(this, "Sales record saved successfully!");
                
                // Clear the form
                salesTableModel.setRowCount(0);
                totalAmount = 0.0;
                updateTotalLabel();
                clearInputFields();
                loadItems(); // Refresh item list
            } else {
                JOptionPane.showMessageDialog(this, "Error saving some sales records.", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving sales record: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateTotalLabel() {
        totalLabel.setText(String.format("Total Amount: $%.2f", totalAmount));
    }
    
    private void clearInputFields() {
        itemComboBox.setSelectedIndex(0);
        quantityField.setText("");
        priceField.setText("");
    }
}