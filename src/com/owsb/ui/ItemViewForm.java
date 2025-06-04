package com.owsb.ui;

import javax.swing.*;
import java.awt.*;

public class ItemViewForm extends JDialog {
    private JPanel mainPanel;
    private JLabel itemIdLabel;
    private JLabel itemNameLabel;
    private JLabel categoryLabel;
    private JLabel priceLabel;
    private JLabel stockLevelLabel;
    private JButton closeButton;
    
    public ItemViewForm(JFrame parent, String itemId) {
        super(parent, "Item Details", true);
        initializeComponents();
        loadItemDetails(itemId);
    }
    
    public ItemViewForm(JFrame parent) {
        super(parent, "Item Details", true);
        initializeComponents();
        loadItemDetails("ALL");
    }
    
    private void initializeComponents() {
        setTitle("Item View");
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Item details labels
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Item ID:"), gbc);
        gbc.gridx = 1;
        itemIdLabel = new JLabel();
        mainPanel.add(itemIdLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        itemNameLabel = new JLabel();
        mainPanel.add(itemNameLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        categoryLabel = new JLabel();
        mainPanel.add(categoryLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        priceLabel = new JLabel();
        mainPanel.add(priceLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Stock Level:"), gbc);
        gbc.gridx = 1;
        stockLevelLabel = new JLabel();
        mainPanel.add(stockLevelLabel, gbc);
        
        // Close button
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        closeButton = new JButton("Close");
        mainPanel.add(closeButton, gbc);
        
        add(mainPanel);
        
        closeButton.addActionListener(e -> dispose());
    }
    
    private void loadItemDetails(String itemId) {
        // Placeholder method
        itemIdLabel.setText(itemId);
        itemNameLabel.setText("Sample Item");
        categoryLabel.setText("Sample Category");
        priceLabel.setText("$0.00");
        stockLevelLabel.setText("0");
    }
}