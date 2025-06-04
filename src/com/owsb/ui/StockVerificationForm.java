package com.owsb.ui;

import javax.swing.*;
import java.awt.*;

public class StockVerificationForm extends JDialog {
    private JPanel mainPanel;
    private JTextField itemIdField;
    private JTextField itemNameField;
    private JTextField systemStockField;
    private JTextField physicalStockField;
    private JTextField varianceField;
    private JComboBox<String> reasonCombo;
    private JTextArea remarksArea;
    private JButton searchItemButton;
    private JButton verifyButton;
    private JButton adjustButton;
    private JButton nextItemButton;
    private JButton closeButton;
    private JTable verificationTable;
    private JLabel verificationStatusLabel;
    
    public StockVerificationForm(JFrame parent, com.owsb.domain.User user) {
        this(parent);
    }
    
    public StockVerificationForm(JFrame parent) {
        super(parent, "Stock Verification", true);
        initializeComponents();
    }
    
    private void initializeComponents() {
        setTitle("Physical Stock Verification");
        setSize(900, 700);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        
        // Item verification panel
        JPanel verificationPanel = new JPanel(new GridBagLayout());
        verificationPanel.setBorder(BorderFactory.createTitledBorder("Item Verification"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        verificationPanel.add(new JLabel("Item ID:"), gbc);
        gbc.gridx = 1;
        itemIdField = new JTextField(15);
        verificationPanel.add(itemIdField, gbc);
        gbc.gridx = 2;
        searchItemButton = new JButton("Search");
        verificationPanel.add(searchItemButton, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        verificationPanel.add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        itemNameField = new JTextField(20);
        itemNameField.setEditable(false);
        verificationPanel.add(itemNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        verificationPanel.add(new JLabel("System Stock:"), gbc);
        gbc.gridx = 1;
        systemStockField = new JTextField(15);
        systemStockField.setEditable(false);
        systemStockField.setBackground(Color.LIGHT_GRAY);
        verificationPanel.add(systemStockField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        verificationPanel.add(new JLabel("Physical Count:"), gbc);
        gbc.gridx = 1;
        physicalStockField = new JTextField(15);
        physicalStockField.setBackground(Color.YELLOW);
        verificationPanel.add(physicalStockField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        verificationPanel.add(new JLabel("Variance:"), gbc);
        gbc.gridx = 1;
        varianceField = new JTextField(15);
        varianceField.setEditable(false);
        varianceField.setFont(varianceField.getFont().deriveFont(Font.BOLD));
        verificationPanel.add(varianceField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        verificationPanel.add(new JLabel("Reason for Variance:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        reasonCombo = new JComboBox<>(new String[]{
            "None", "Damaged goods", "Theft/Loss", "Counting error", 
            "Received but not recorded", "System error", "Other"
        });
        verificationPanel.add(reasonCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        verificationPanel.add(new JLabel("Remarks:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        remarksArea = new JTextArea(3, 20);
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        verificationPanel.add(new JScrollPane(remarksArea), gbc);
        
        // Verification status
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
        verificationStatusLabel = new JLabel("Status: Ready for verification");
        verificationStatusLabel.setFont(verificationStatusLabel.getFont().deriveFont(Font.BOLD));
        verificationPanel.add(verificationStatusLabel, gbc);
        
        // Action buttons for current item
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 3;
        JPanel itemButtonPanel = new JPanel(new FlowLayout());
        verifyButton = new JButton("Verify Item");
        adjustButton = new JButton("Adjust Stock");
        nextItemButton = new JButton("Next Item");
        
        verifyButton.setBackground(new Color(46, 125, 50));
        verifyButton.setForeground(Color.WHITE);
        adjustButton.setBackground(new Color(255, 152, 0));
        adjustButton.setForeground(Color.WHITE);
        
        itemButtonPanel.add(verifyButton);
        itemButtonPanel.add(adjustButton);
        itemButtonPanel.add(nextItemButton);
        verificationPanel.add(itemButtonPanel, gbc);
        
        // Verification history table
        String[] columns = {"Item ID", "Item Name", "System Stock", "Physical Count", "Variance", "Status", "Verified By"};
        Object[][] data = {};
        verificationTable = new JTable(data, columns);
        JScrollPane tableScrollPane = new JScrollPane(verificationTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Verification History"));
        tableScrollPane.setPreferredSize(new Dimension(0, 250));
        
        // Main button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        closeButton = new JButton("Close");
        buttonPanel.add(closeButton);
        
        // Assemble main panel
        mainPanel.add(verificationPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Add action listeners
        searchItemButton.addActionListener(e -> searchItem());
        verifyButton.addActionListener(e -> verifyItem());
        adjustButton.addActionListener(e -> adjustStock());
        nextItemButton.addActionListener(e -> nextItem());
        closeButton.addActionListener(e -> dispose());
        
        // Add document listener to calculate variance
        physicalStockField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calculateVariance(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calculateVariance(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calculateVariance(); }
        });
        
        // Initially disable action buttons
        setActionButtonsEnabled(false);
    }
    
    private void searchItem() {
        // Placeholder method
        String itemId = itemIdField.getText().trim();
        if (!itemId.isEmpty()) {
            loadItemForVerification(itemId);
        } else {
            JOptionPane.showMessageDialog(this, "Please enter an Item ID");
        }
    }
    
    private void loadItemForVerification(String itemId) {
        // Placeholder method
        itemNameField.setText("Sample Item - " + itemId);
        systemStockField.setText("100");
        physicalStockField.setText("");
        varianceField.setText("");
        reasonCombo.setSelectedIndex(0);
        remarksArea.setText("");
        verificationStatusLabel.setText("Status: Ready for verification");
        verificationStatusLabel.setForeground(Color.BLACK);
        setActionButtonsEnabled(true);
        physicalStockField.requestFocus();
    }
    
    private void calculateVariance() {
        try {
            if (!systemStockField.getText().isEmpty() && !physicalStockField.getText().isEmpty()) {
                int systemStock = Integer.parseInt(systemStockField.getText());
                int physicalStock = Integer.parseInt(physicalStockField.getText());
                int variance = physicalStock - systemStock;
                
                varianceField.setText(String.valueOf(variance));
                
                // Color code the variance
                if (variance == 0) {
                    varianceField.setForeground(new Color(46, 125, 50)); // Green
                } else if (Math.abs(variance) <= 5) {
                    varianceField.setForeground(new Color(255, 152, 0)); // Orange
                } else {
                    varianceField.setForeground(new Color(211, 47, 47)); // Red
                }
                
                // Update reason combo visibility
                reasonCombo.setEnabled(variance != 0);
            }
        } catch (NumberFormatException e) {
            varianceField.setText("");
        }
    }
    
    private void verifyItem() {
        // Validate input
        if (physicalStockField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter physical count");
            return;
        }
        
        try {
            int variance = Integer.parseInt(varianceField.getText());
            if (variance != 0 && reasonCombo.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(this, "Please select a reason for variance");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid physical count");
            return;
        }
        
        // Placeholder verification
        JOptionPane.showMessageDialog(this, "Verify item functionality not implemented yet");
        verificationStatusLabel.setText("Status: Verified");
        verificationStatusLabel.setForeground(new Color(46, 125, 50));
        setActionButtonsEnabled(false);
    }
    
    private void adjustStock() {
        // Validate input
        if (physicalStockField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter physical count");
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Adjust system stock to match physical count?", 
            "Confirm Stock Adjustment", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "Adjust stock functionality not implemented yet");
            verificationStatusLabel.setText("Status: Adjusted");
            verificationStatusLabel.setForeground(new Color(255, 152, 0));
        }
    }
    
    private void nextItem() {
        // Clear current item and prepare for next
        itemIdField.setText("");
        itemNameField.setText("");
        systemStockField.setText("");
        physicalStockField.setText("");
        varianceField.setText("");
        reasonCombo.setSelectedIndex(0);
        remarksArea.setText("");
        verificationStatusLabel.setText("Status: Ready for verification");
        verificationStatusLabel.setForeground(Color.BLACK);
        setActionButtonsEnabled(false);
        itemIdField.requestFocus();
    }
    
    private void setActionButtonsEnabled(boolean enabled) {
        verifyButton.setEnabled(enabled);
        adjustButton.setEnabled(enabled);
    }
}