package com.owsb.ui;

import javax.swing.*;
import java.awt.*;

public class SupplierViewForm extends JDialog {
    private JPanel mainPanel;
    private JLabel supplierIdLabel;
    private JLabel supplierNameLabel;
    private JLabel contactPersonLabel;
    private JLabel phoneLabel;
    private JLabel emailLabel;
    private JLabel addressLabel;
    private JButton closeButton;
    
    public SupplierViewForm(JFrame parent, String supplierId) {
        super(parent, "Supplier Details", true);
        initializeComponents();
        loadSupplierDetails(supplierId);
    }
    
    public SupplierViewForm(JFrame parent) {
        super(parent, "Supplier Details", true);
        initializeComponents();
        loadSupplierDetails("ALL");
    }
    
    private void initializeComponents() {
        setTitle("Supplier View");
        setSize(450, 350);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Supplier details labels
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Supplier ID:"), gbc);
        gbc.gridx = 1;
        supplierIdLabel = new JLabel();
        mainPanel.add(supplierIdLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Company Name:"), gbc);
        gbc.gridx = 1;
        supplierNameLabel = new JLabel();
        mainPanel.add(supplierNameLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Contact Person:"), gbc);
        gbc.gridx = 1;
        contactPersonLabel = new JLabel();
        mainPanel.add(contactPersonLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        phoneLabel = new JLabel();
        mainPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailLabel = new JLabel();
        mainPanel.add(emailLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        mainPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        addressLabel = new JLabel();
        mainPanel.add(addressLabel, gbc);
        
        // Close button
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        closeButton = new JButton("Close");
        mainPanel.add(closeButton, gbc);
        
        add(mainPanel);
        
        closeButton.addActionListener(e -> dispose());
    }
    
    private void loadSupplierDetails(String supplierId) {
        // Placeholder method
        supplierIdLabel.setText(supplierId);
        supplierNameLabel.setText("Sample Supplier Inc.");
        contactPersonLabel.setText("John Doe");
        phoneLabel.setText("(555) 123-4567");
        emailLabel.setText("contact@supplier.com");
        addressLabel.setText("123 Business St, City, State");
    }
}