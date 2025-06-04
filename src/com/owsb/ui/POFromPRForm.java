package com.owsb.ui;

import com.owsb.domain.PurchaseRequisition;
import com.owsb.domain.Supplier;
import com.owsb.domain.User;
import com.owsb.service.POService;
import com.owsb.service.SupplierService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class POFromPRForm extends JDialog {
    private PurchaseRequisition pr;
    private POService poService;
    private SupplierService supplierService;
    private User currentUser;
    
    private JComboBox<Supplier> supplierCombo;
    private JTextField deliveryDateField;
    private JTextField paymentTermsField;
    private JTextArea deliveryAddressArea;
    
    public POFromPRForm(JFrame parent, PurchaseRequisition pr, User user) {
        super(parent, "Generate Purchase Order from PR - " + pr.getPrId(), true);
        this.pr = pr;
        this.currentUser = user;
        this.poService = new POService();
        this.supplierService = new SupplierService();
        initializeComponents();
        loadSuppliers();
    }
    
    private void initializeComponents() {
        setSize(600, 500);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // PR Info panel
        JPanel prInfoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        prInfoPanel.setBorder(BorderFactory.createTitledBorder("Purchase Requisition Information"));
        
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        prInfoPanel.add(new JLabel("PR ID:"), gbc);
        gbc.gridx = 1;
        prInfoPanel.add(new JLabel(pr.getPrId()), gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        prInfoPanel.add(new JLabel("Requested By:"), gbc);
        gbc.gridx = 3;
        prInfoPanel.add(new JLabel(pr.getRequestedBy()), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        prInfoPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        prInfoPanel.add(new JLabel(pr.getDepartment()), gbc);
        
        gbc.gridx = 2; gbc.gridy = 1;
        prInfoPanel.add(new JLabel("Total Amount:"), gbc);
        gbc.gridx = 3;
        JLabel totalLabel = new JLabel(String.format("$%.2f", pr.getTotalAmount()));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
        totalLabel.setForeground(new Color(0, 120, 0));
        prInfoPanel.add(totalLabel, gbc);
        
        // PO Details panel
        JPanel poDetailsPanel = new JPanel(new GridBagLayout());
        poDetailsPanel.setBorder(BorderFactory.createTitledBorder("Purchase Order Details"));
        
        gbc.gridx = 0; gbc.gridy = 0;
        poDetailsPanel.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1;
        supplierCombo = new JComboBox<>();
        supplierCombo.setPreferredSize(new Dimension(200, 25));
        poDetailsPanel.add(supplierCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        poDetailsPanel.add(new JLabel("Expected Delivery Date:"), gbc);
        gbc.gridx = 1;
        deliveryDateField = new JTextField(15);
        deliveryDateField.setText(LocalDate.now().plusDays(14).format(DateTimeFormatter.ISO_LOCAL_DATE));
        deliveryDateField.setToolTipText("Format: YYYY-MM-DD");
        poDetailsPanel.add(deliveryDateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        poDetailsPanel.add(new JLabel("Payment Terms:"), gbc);
        gbc.gridx = 1;
        paymentTermsField = new JTextField(15);
        paymentTermsField.setText("Net 30");
        poDetailsPanel.add(paymentTermsField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        poDetailsPanel.add(new JLabel("Delivery Address:"), gbc);
        gbc.gridx = 1;
        deliveryAddressArea = new JTextArea(4, 20);
        deliveryAddressArea.setLineWrap(true);
        deliveryAddressArea.setWrapStyleWord(true);
        deliveryAddressArea.setText("Main Warehouse\\n123 Business Street\\nCity, State 12345");
        poDetailsPanel.add(new JScrollPane(deliveryAddressArea), gbc);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Items Summary"));
        
        String summaryText = String.format(
            "This Purchase Order will include %d line items from PR %s\\n" +
            "Total estimated value: $%.2f\\n" +
            "Priority: %s",
            pr.getLines().size(), pr.getPrId(), pr.getTotalAmount(), pr.getPriority()
        );
        
        JTextArea summaryArea = new JTextArea(summaryText);
        summaryArea.setEditable(false);
        summaryArea.setBackground(summaryPanel.getBackground());
        summaryArea.setFont(summaryArea.getFont().deriveFont(Font.PLAIN));
        summaryPanel.add(summaryArea, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton generateButton = new JButton("Generate PO");
        generateButton.setBackground(new Color(0, 120, 215));
        generateButton.setForeground(Color.WHITE);
        generateButton.setFont(generateButton.getFont().deriveFont(Font.BOLD));
        generateButton.addActionListener(this::generatePO);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(generateButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(prInfoPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(poDetailsPanel, BorderLayout.NORTH);
        centerPanel.add(summaryPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void loadSuppliers() {
        List<Supplier> suppliers = supplierService.listSuppliers();
        for (Supplier supplier : suppliers) {
            supplierCombo.addItem(supplier);
        }
        
        supplierCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Supplier) {
                    Supplier supplier = (Supplier) value;
                    setText(supplier.getSupplierName() + " - " + supplier.getContactPerson());
                }
                return this;
            }
        });
    }
    
    private void generatePO(ActionEvent e) {
        try {
            // Validate inputs
            Supplier selectedSupplier = (Supplier) supplierCombo.getSelectedItem();
            if (selectedSupplier == null) {
                JOptionPane.showMessageDialog(this, "Please select a supplier.");
                return;
            }
            
            String deliveryDateStr = deliveryDateField.getText().trim();
            if (deliveryDateStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an expected delivery date.");
                return;
            }
            
            LocalDate deliveryDate;
            try {
                deliveryDate = LocalDate.parse(deliveryDateStr);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD format.");
                return;
            }
            
            String paymentTerms = paymentTermsField.getText().trim();
            if (paymentTerms.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter payment terms.");
                return;
            }
            
            String deliveryAddress = deliveryAddressArea.getText().trim();
            if (deliveryAddress.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter delivery address.");
                return;
            }
            
            // Generate PO
            var generatedPO = poService.generatePOFromPR(
                pr, 
                selectedSupplier.getSupplierId(),
                selectedSupplier.getSupplierName(),
                deliveryDate,
                currentUser.getUsername(),
                paymentTerms,
                deliveryAddress
            );
            
            JOptionPane.showMessageDialog(this, 
                String.format("Purchase Order generated successfully!\\nPO ID: %s\\nTotal Amount: $%.2f",
                    generatedPO.getPoId(), generatedPO.getTotalAmount()),
                "PO Generated", 
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error generating Purchase Order: " + ex.getMessage(),
                "Generation Failed", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}