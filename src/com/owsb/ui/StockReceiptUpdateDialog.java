package com.owsb.ui;

import com.owsb.domain.*;
import com.owsb.service.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class StockReceiptUpdateDialog extends JDialog {
    private String poId;
    private String itemId;
    private String itemName;
    private int orderedQuantity;
    private int currentReceivedQuantity;
    private Runnable onUpdateCallback;
    
    private JTextField receivedQtyField;
    private JTextArea notesArea;
    private JLabel statusLabel;
    
    private POService poService;
    private ItemService itemService;
    
    public StockReceiptUpdateDialog(JDialog parent, String poId, String itemId, String itemName, 
                                  int orderedQty, int currentReceived, Runnable callback) {
        super(parent, "Update Stock Receipt", true);
        this.poId = poId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.orderedQuantity = orderedQty;
        this.currentReceivedQuantity = currentReceived;
        this.onUpdateCallback = callback;
        
        initializeServices();
        initializeComponents();
    }
    
    private void initializeServices() {
        this.poService = new POService();
        this.itemService = new ItemService();
    }
    
    private void initializeComponents() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBorder(BorderFactory.createTitledBorder("Stock Receipt Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        headerPanel.add(new JLabel("PO ID:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(poId), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        headerPanel.add(new JLabel("Item ID:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(itemId), gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        headerPanel.add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(itemName), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        headerPanel.add(new JLabel("Ordered Quantity:"), gbc);
        gbc.gridx = 1;
        JLabel orderedLabel = new JLabel(String.valueOf(orderedQuantity));
        orderedLabel.setFont(orderedLabel.getFont().deriveFont(Font.BOLD));
        headerPanel.add(orderedLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        headerPanel.add(new JLabel("Currently Received:"), gbc);
        gbc.gridx = 1;
        JLabel currentLabel = new JLabel(String.valueOf(currentReceivedQuantity));
        currentLabel.setFont(currentLabel.getFont().deriveFont(Font.BOLD));
        if (currentReceivedQuantity >= orderedQuantity) {
            currentLabel.setForeground(new Color(34, 139, 34));
        } else if (currentReceivedQuantity > 0) {
            currentLabel.setForeground(new Color(255, 140, 0));
        } else {
            currentLabel.setForeground(Color.RED);
        }
        headerPanel.add(currentLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        headerPanel.add(new JLabel("Pending Quantity:"), gbc);
        gbc.gridx = 1;
        int pending = orderedQuantity - currentReceivedQuantity;
        JLabel pendingLabel = new JLabel(String.valueOf(pending));
        pendingLabel.setFont(pendingLabel.getFont().deriveFont(Font.BOLD));
        pendingLabel.setForeground(pending > 0 ? Color.RED : new Color(34, 139, 34));
        headerPanel.add(pendingLabel, gbc);
        
        // Update panel
        JPanel updatePanel = new JPanel(new GridBagLayout());
        updatePanel.setBorder(BorderFactory.createTitledBorder("Update Receipt"));
        
        gbc.gridx = 0; gbc.gridy = 0;
        updatePanel.add(new JLabel("New Received Quantity:"), gbc);
        gbc.gridx = 1;
        receivedQtyField = new JTextField(String.valueOf(currentReceivedQuantity), 10);
        updatePanel.add(receivedQtyField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        updatePanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        notesArea = new JTextArea(3, 20);
        notesArea.setBorder(BorderFactory.createEtchedBorder());
        notesArea.setText("Stock updated by Inventory Manager");
        JScrollPane notesScrollPane = new JScrollPane(notesArea);
        updatePanel.add(notesScrollPane, gbc);
        
        // Status panel
        statusLabel = new JLabel("Ready to update", SwingConstants.CENTER);
        statusLabel.setForeground(Color.BLUE);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton updateButton = new JButton("Update Stock");
        JButton cancelButton = new JButton("Cancel");
        
        updateButton.setBackground(new Color(34, 139, 34));
        updateButton.setForeground(Color.WHITE);
        updateButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        cancelButton.setBackground(new Color(220, 20, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        updateButton.addActionListener(e -> updateStock());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);
        
        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(headerPanel, BorderLayout.NORTH);
        centerPanel.add(updatePanel, BorderLayout.CENTER);
        centerPanel.add(statusLabel, BorderLayout.SOUTH);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void updateStock() {
        try {
            // Validate input
            String receivedText = receivedQtyField.getText().trim();
            if (receivedText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter the received quantity.", 
                                            "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int newReceivedQty;
            try {
                newReceivedQty = Integer.parseInt(receivedText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number for quantity.", 
                                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (newReceivedQty < 0) {
                JOptionPane.showMessageDialog(this, "Received quantity cannot be negative.", 
                                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (newReceivedQty > orderedQuantity) {
                int result = JOptionPane.showConfirmDialog(this, 
                    "Received quantity (" + newReceivedQty + ") is greater than ordered quantity (" + 
                    orderedQuantity + "). Do you want to continue?", 
                    "Quantity Exceeds Order", JOptionPane.YES_NO_OPTION);
                
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            statusLabel.setText("Updating stock...");
            
            // Update the PO line received quantity and check PO status
            boolean updated = poService.updatePOLineReceived(poId, itemId, newReceivedQty);
            
            if (updated) {
                // Update the item stock quantity
                Item item = itemService.getItemById(itemId);
                if (item != null) {
                    int stockIncrease = newReceivedQty - currentReceivedQuantity;
                    int newStockLevel = item.getStockQuantity() + stockIncrease;
                    item.setStockQuantity(newStockLevel);
                    itemService.updateItem(item);
                }
                
                statusLabel.setText("Stock updated successfully!");
                statusLabel.setForeground(new Color(34, 139, 34));
                
                // Check if PO status was updated
                PurchaseOrder po = poService.findPOById(poId);
                String statusMessage = "Stock updated successfully!\n" +
                    "Previous received: " + currentReceivedQuantity + "\n" +
                    "New received: " + newReceivedQty + "\n" +
                    "Stock increase: " + (newReceivedQty - currentReceivedQuantity);
                
                if (po != null && "RECEIVED".equals(po.getStatus())) {
                    statusMessage += "\n\n🎉 PO Status Updated: All items have been received!\nPO " + poId + " status changed to RECEIVED.";
                }
                
                // Show success message
                JOptionPane.showMessageDialog(this, statusMessage, 
                    "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                
                // Callback to refresh parent table
                if (onUpdateCallback != null) {
                    onUpdateCallback.run();
                }
                
                dispose();
                
            } else {
                statusLabel.setText("Error: PO line not found");
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "Error: Could not find the PO line to update.", 
                                            "Update Failed", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            statusLabel.setText("Error updating stock");
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "Error updating stock: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}