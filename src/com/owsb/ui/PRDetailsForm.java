package com.owsb.ui;

import com.owsb.domain.PurchaseRequisition;
import com.owsb.domain.PurchaseRequisitionLine;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PRDetailsForm extends JDialog {
    private PurchaseRequisition pr;
    private JTable lineTable;
    
    public PRDetailsForm(JFrame parent, PurchaseRequisition pr) {
        super(parent, "Purchase Requisition Details - " + pr.getPrId(), true);
        this.pr = pr;
        initializeComponents();
    }
    
    private void initializeComponents() {
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Header info panel
        JPanel headerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        headerPanel.setBorder(BorderFactory.createTitledBorder("Requisition Information"));
        
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        headerPanel.add(new JLabel("PR ID:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(pr.getPrId()), gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        headerPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 3;
        JLabel statusLabel = new JLabel(pr.getStatus());
        if ("APPROVED".equals(pr.getStatus())) {
            statusLabel.setForeground(Color.GREEN.darker());
        } else if ("REJECTED".equals(pr.getStatus())) {
            statusLabel.setForeground(Color.RED);
        } else {
            statusLabel.setForeground(Color.ORANGE.darker());
        }
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        headerPanel.add(statusLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        headerPanel.add(new JLabel("Requested By:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(pr.getRequestedBy()), gbc);
        
        gbc.gridx = 2; gbc.gridy = 1;
        headerPanel.add(new JLabel("Request Date:"), gbc);
        gbc.gridx = 3;
        headerPanel.add(new JLabel(pr.getRequestDate().toString()), gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        headerPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(pr.getDepartment()), gbc);
        
        gbc.gridx = 2; gbc.gridy = 2;
        headerPanel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 3;
        JLabel priorityLabel = new JLabel(pr.getPriority());
        if ("HIGH".equals(pr.getPriority()) || "URGENT".equals(pr.getPriority())) {
            priorityLabel.setForeground(Color.RED);
        } else if ("MEDIUM".equals(pr.getPriority())) {
            priorityLabel.setForeground(Color.ORANGE.darker());
        }
        headerPanel.add(priorityLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        headerPanel.add(new JLabel("Total Amount:"), gbc);
        gbc.gridx = 1;
        JLabel totalLabel = new JLabel(String.format("$%.2f", pr.getTotalAmount()));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
        headerPanel.add(totalLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        headerPanel.add(new JLabel("Justification:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        JTextArea justificationArea = new JTextArea(pr.getJustification());
        justificationArea.setEditable(false);
        justificationArea.setLineWrap(true);
        justificationArea.setWrapStyleWord(true);
        justificationArea.setBackground(headerPanel.getBackground());
        headerPanel.add(new JScrollPane(justificationArea), gbc);
        
        // Line items panel
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Line Items"));
        
        String[] columns = {"Item Code", "Item Name", "Quantity", "Unit", "Est. Price", "Specifications", "Line Total"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        for (PurchaseRequisitionLine line : pr.getLines()) {
            Object[] rowData = {
                line.getItemCode(),
                line.getItemName(),
                line.getQuantity(),
                line.getUnit(),
                String.format("%.2f", line.getEstimatedPrice()),
                line.getSpecifications(),
                String.format("%.2f", line.getLineTotal())
            };
            tableModel.addRow(rowData);
        }
        
        lineTable = new JTable(tableModel);
        lineTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane tableScrollPane = new JScrollPane(lineTable);
        tableScrollPane.setPreferredSize(new Dimension(750, 200));
        
        itemsPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summaryPanel.add(new JLabel("Total Items: " + pr.getLines().size()));
        summaryPanel.add(Box.createHorizontalStrut(20));
        summaryPanel.add(new JLabel("Grand Total: $" + String.format("%.2f", pr.getTotalAmount())));
        
        itemsPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        JButton printButton = new JButton("Print");
        printButton.addActionListener(e -> printPR());
        
        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(itemsPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void printPR() {
        try {
            boolean complete = lineTable.print();
            if (complete) {
                JOptionPane.showMessageDialog(this, "Printing completed.");
            } else {
                JOptionPane.showMessageDialog(this, "Printing was cancelled.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error printing: " + e.getMessage());
        }
    }
}