package com.owsb.ui;

import javax.swing.*;
import java.awt.*;

public class StockUpdateForm extends JDialog {
    private JPanel mainPanel;
    private JTextField itemIdField;
    private JTextField currentStockField;
    private JTextField newStockField;
    private JTextField adjustmentField;
    private JComboBox<String> adjustmentTypeCombo;
    private JTextArea reasonArea;
    private JButton updateButton;
    private JButton cancelButton;
    private JButton searchItemButton;
    
    public StockUpdateForm(JFrame parent) {
        super(parent, "Stock Update", true);
        initializeComponents();
    }
    
    public StockUpdateForm(JFrame parent, com.owsb.domain.User user) {
        this(parent, user.getUserId());
    }
    
    public StockUpdateForm(JFrame parent, String itemId) {
        super(parent, "Stock Update", true);
        initializeComponents();
        loadItemDetails(itemId);
    }
    
    private void initializeComponents() {
        setTitle("Stock Level Update");
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Item ID field with search button
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Item ID:"), gbc);
        gbc.gridx = 1;
        itemIdField = new JTextField(15);
        mainPanel.add(itemIdField, gbc);
        gbc.gridx = 2;
        searchItemButton = new JButton("Search");
        mainPanel.add(searchItemButton, gbc);
        
        // Current stock (read-only)
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Current Stock:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        currentStockField = new JTextField(15);
        currentStockField.setEditable(false);
        mainPanel.add(currentStockField, gbc);
        
        // Adjustment type
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Adjustment Type:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        adjustmentTypeCombo = new JComboBox<>(new String[]{"Add Stock", "Remove Stock", "Set Stock Level"});
        mainPanel.add(adjustmentTypeCombo, gbc);
        
        // Adjustment amount
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Adjustment:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        adjustmentField = new JTextField(15);
        mainPanel.add(adjustmentField, gbc);
        
        // New stock level (calculated)
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("New Stock Level:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        newStockField = new JTextField(15);
        newStockField.setEditable(false);
        mainPanel.add(newStockField, gbc);
        
        // Reason for adjustment
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Reason:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        reasonArea = new JTextArea(3, 15);
        mainPanel.add(new JScrollPane(reasonArea), gbc);
        
        // Button panel
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 3;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        updateButton = new JButton("Update Stock");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel);
        
        // Add action listeners
        searchItemButton.addActionListener(e -> searchItem());
        adjustmentField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calculateNewStock(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calculateNewStock(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calculateNewStock(); }
        });
        adjustmentTypeCombo.addActionListener(e -> calculateNewStock());
        updateButton.addActionListener(e -> updateStock());
        cancelButton.addActionListener(e -> dispose());
    }
    
    private void loadItemDetails(String itemId) {
        // Placeholder method
        itemIdField.setText(itemId);
        currentStockField.setText("100"); // Sample current stock
    }
    
    private void searchItem() {
        // Placeholder method
        String itemId = itemIdField.getText().trim();
        if (!itemId.isEmpty()) {
            loadItemDetails(itemId);
        }
    }
    
    private void calculateNewStock() {
        // Placeholder method
        try {
            int currentStock = Integer.parseInt(currentStockField.getText());
            int adjustment = Integer.parseInt(adjustmentField.getText());
            String adjustmentType = (String) adjustmentTypeCombo.getSelectedItem();
            
            int newStock = currentStock;
            switch (adjustmentType) {
                case "Add Stock":
                    newStock = currentStock + adjustment;
                    break;
                case "Remove Stock":
                    newStock = currentStock - adjustment;
                    break;
                case "Set Stock Level":
                    newStock = adjustment;
                    break;
            }
            
            newStockField.setText(String.valueOf(newStock));
        } catch (NumberFormatException e) {
            newStockField.setText("");
        }
    }
    
    private void updateStock() {
        // Placeholder method
        JOptionPane.showMessageDialog(this, "Update stock functionality not implemented yet");
    }
}