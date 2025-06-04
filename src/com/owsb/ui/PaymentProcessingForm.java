package com.owsb.ui;

import javax.swing.*;
import java.awt.*;

public class PaymentProcessingForm extends JDialog {
    private JPanel mainPanel;
    private JTextField invoiceNumberField;
    private JTextField supplierField;
    private JTextField invoiceAmountField;
    private JTextField poNumberField;
    private JTextField dueDateField;
    private JComboBox<String> paymentMethodCombo;
    private JTextField paymentAmountField;
    private JTextField referenceNumberField;
    private JTextArea notesArea;
    private JButton processPaymentButton;
    private JButton partialPaymentButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private JCheckBox reconcileCheckbox;
    
    public PaymentProcessingForm(JFrame parent, com.owsb.domain.User user) {
        super(parent, "Payment Processing", true);
        initializeComponents();
        loadInvoiceDetails(user.getUserId());
    }
    
    private void initializeComponents() {
        setTitle("Process Payment");
        setSize(600, 650);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        
        // Invoice details panel
        JPanel invoicePanel = new JPanel(new GridBagLayout());
        invoicePanel.setBorder(BorderFactory.createTitledBorder("Invoice Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        invoicePanel.add(new JLabel("Invoice Number:"), gbc);
        gbc.gridx = 1;
        invoiceNumberField = new JTextField(15);
        invoiceNumberField.setEditable(false);
        invoicePanel.add(invoiceNumberField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        invoicePanel.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1;
        supplierField = new JTextField(15);
        supplierField.setEditable(false);
        invoicePanel.add(supplierField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        invoicePanel.add(new JLabel("PO Number:"), gbc);
        gbc.gridx = 1;
        poNumberField = new JTextField(15);
        poNumberField.setEditable(false);
        invoicePanel.add(poNumberField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        invoicePanel.add(new JLabel("Invoice Amount:"), gbc);
        gbc.gridx = 1;
        invoiceAmountField = new JTextField(15);
        invoiceAmountField.setEditable(false);
        invoiceAmountField.setFont(invoiceAmountField.getFont().deriveFont(Font.BOLD));
        invoicePanel.add(invoiceAmountField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        invoicePanel.add(new JLabel("Due Date:"), gbc);
        gbc.gridx = 1;
        dueDateField = new JTextField(15);
        dueDateField.setEditable(false);
        invoicePanel.add(dueDateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        statusLabel = new JLabel("Status: Pending Payment");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        statusLabel.setForeground(Color.ORANGE);
        invoicePanel.add(statusLabel, gbc);
        
        // Payment details panel
        JPanel paymentPanel = new JPanel(new GridBagLayout());
        paymentPanel.setBorder(BorderFactory.createTitledBorder("Payment Details"));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        paymentPanel.add(new JLabel("Payment Method:"), gbc);
        gbc.gridx = 1;
        paymentMethodCombo = new JComboBox<>(new String[]{
            "Bank Transfer", "Check", "Credit Card", "Cash", "Wire Transfer"
        });
        paymentPanel.add(paymentMethodCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        paymentPanel.add(new JLabel("Payment Amount:"), gbc);
        gbc.gridx = 1;
        paymentAmountField = new JTextField(15);
        paymentPanel.add(paymentAmountField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        paymentPanel.add(new JLabel("Reference Number:"), gbc);
        gbc.gridx = 1;
        referenceNumberField = new JTextField(15);
        paymentPanel.add(referenceNumberField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        reconcileCheckbox = new JCheckBox("Mark as reconciled");
        paymentPanel.add(reconcileCheckbox, gbc);
        
        // Notes panel
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setBorder(BorderFactory.createTitledBorder("Payment Notes"));
        notesArea = new JTextArea(4, 30);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesPanel.add(new JScrollPane(notesArea), BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        processPaymentButton = new JButton("Process Full Payment");
        processPaymentButton.setBackground(new Color(46, 125, 50));
        processPaymentButton.setForeground(Color.WHITE);
        
        partialPaymentButton = new JButton("Process Partial Payment");
        partialPaymentButton.setBackground(new Color(255, 152, 0));
        partialPaymentButton.setForeground(Color.WHITE);
        
        cancelButton = new JButton("Cancel");
        
        buttonPanel.add(processPaymentButton);
        buttonPanel.add(partialPaymentButton);
        buttonPanel.add(cancelButton);
        
        // Assemble main panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(paymentPanel, BorderLayout.NORTH);
        centerPanel.add(notesPanel, BorderLayout.CENTER);
        
        mainPanel.add(invoicePanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Add action listeners
        processPaymentButton.addActionListener(e -> processFullPayment());
        partialPaymentButton.addActionListener(e -> processPartialPayment());
        cancelButton.addActionListener(e -> dispose());
        
        // Add listener to update payment amount when invoice amount is set
        paymentAmountField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (paymentAmountField.getText().isEmpty() && !invoiceAmountField.getText().isEmpty()) {
                    paymentAmountField.setText(invoiceAmountField.getText());
                }
            }
        });
    }
    
    private void loadInvoiceDetails(String invoiceNumber) {
        // Placeholder method
        invoiceNumberField.setText(invoiceNumber);
        supplierField.setText("Sample Supplier Inc.");
        poNumberField.setText("PO-2024-001");
        invoiceAmountField.setText("$5,250.00");
        dueDateField.setText("2024-02-15");
        paymentAmountField.setText("$5,250.00");
    }
    
    private void processFullPayment() {
        // Validate input
        if (!validatePaymentInput()) {
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Process full payment of " + paymentAmountField.getText() + "?", 
            "Confirm Payment", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "Process full payment functionality not implemented yet");
            statusLabel.setText("Status: Paid");
            statusLabel.setForeground(new Color(46, 125, 50));
            setPaymentButtonsEnabled(false);
        }
    }
    
    private void processPartialPayment() {
        // Validate input
        if (!validatePaymentInput()) {
            return;
        }
        
        // Check if payment amount is less than invoice amount
        try {
            double paymentAmount = Double.parseDouble(paymentAmountField.getText().replace("$", "").replace(",", ""));
            double invoiceAmount = Double.parseDouble(invoiceAmountField.getText().replace("$", "").replace(",", ""));
            
            if (paymentAmount >= invoiceAmount) {
                JOptionPane.showMessageDialog(this, "For partial payment, amount must be less than invoice amount");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount format");
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Process partial payment of " + paymentAmountField.getText() + "?", 
            "Confirm Partial Payment", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "Process partial payment functionality not implemented yet");
            statusLabel.setText("Status: Partially Paid");
            statusLabel.setForeground(new Color(255, 152, 0));
        }
    }
    
    private boolean validatePaymentInput() {
        if (paymentAmountField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter payment amount");
            paymentAmountField.requestFocus();
            return false;
        }
        
        if (referenceNumberField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter reference number");
            referenceNumberField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void setPaymentButtonsEnabled(boolean enabled) {
        processPaymentButton.setEnabled(enabled);
        partialPaymentButton.setEnabled(enabled);
    }
}