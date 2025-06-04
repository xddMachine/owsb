package com.owsb.ui;

import com.owsb.domain.Item;
import com.owsb.domain.PurchaseRequisitionLine;
import com.owsb.domain.User;
import com.owsb.service.ItemService;
import com.owsb.service.PRService;
import com.owsb.util.UIPermissionUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class PRCreationForm extends JDialog {
    private JTextField departmentField;
    private JComboBox<String> priorityCombo;
    private JTextArea justificationArea;
    private DefaultTableModel lineTableModel;
    private JTable lineTable;
    private JComboBox<Item> itemCombo;
    private JTextField quantityField;
    private JTextField priceField;
    private JTextField specField;
    
    private PRService prService;
    private ItemService itemService;
    private User currentUser;
    
    public PRCreationForm(JFrame parent, User user) {
        super(parent, "Create Purchase Requisition", true);
        this.currentUser = user;
        this.prService = new PRService();
        this.itemService = new ItemService();
        initializeComponents();
        loadItems();
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
        headerPanel.add(new JLabel("Requested By:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(currentUser.getUsername()), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        headerPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        departmentField = new JTextField(20);
        headerPanel.add(departmentField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        headerPanel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1;
        priorityCombo = new JComboBox<>(new String[]{"LOW", "MEDIUM", "HIGH", "URGENT"});
        headerPanel.add(priorityCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        headerPanel.add(new JLabel("Justification:"), gbc);
        gbc.gridx = 1;
        justificationArea = new JTextArea(3, 20);
        justificationArea.setLineWrap(true);
        headerPanel.add(new JScrollPane(justificationArea), gbc);
        
        // Line items panel
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Line Items"));
        
        // Add item panel
        JPanel addItemPanel = new JPanel(new FlowLayout());
        addItemPanel.add(new JLabel("Item:"));
        itemCombo = new JComboBox<>();
        itemCombo.setPreferredSize(new Dimension(150, 25));
        addItemPanel.add(itemCombo);
        
        addItemPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField(8);
        addItemPanel.add(quantityField);
        
        addItemPanel.add(new JLabel("Est. Price:"));
        priceField = new JTextField(8);
        addItemPanel.add(priceField);
        
        addItemPanel.add(new JLabel("Specs:"));
        specField = new JTextField(12);
        addItemPanel.add(specField);
        
        JButton addLineButton = new JButton("Add Item");
        addLineButton.addActionListener(this::addLineItem);
        addItemPanel.add(addLineButton);
        
        // Items table
        String[] columns = {"Item Code", "Item Name", "Quantity", "Unit", "Est. Price", "Specifications", "Total"};
        lineTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        lineTable = new JTable(lineTableModel);
        
        JScrollPane tableScrollPane = new JScrollPane(lineTable);
        tableScrollPane.setPreferredSize(new Dimension(750, 200));
        
        JPanel tableButtonPanel = new JPanel(new FlowLayout());
        JButton removeLineButton = new JButton("Remove Selected");
        removeLineButton.addActionListener(this::removeLineItem);
        tableButtonPanel.add(removeLineButton);
        
        itemsPanel.add(addItemPanel, BorderLayout.NORTH);
        itemsPanel.add(tableScrollPane, BorderLayout.CENTER);
        itemsPanel.add(tableButtonPanel, BorderLayout.SOUTH);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton submitButton = new JButton("Submit PR");
        submitButton.addActionListener(this::submitPR);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(itemsPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Apply consistent styling to all buttons
        UIPermissionUtils.styleAllButtonsInContainer(mainPanel);
        
        add(mainPanel);
    }
    
    private void loadItems() {
        List<Item> items = itemService.listItems();
        for (Item item : items) {
            itemCombo.addItem(item);
        }
        itemCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Item) {
                    Item item = (Item) value;
                    setText(item.getItemCode() + " - " + item.getItemName());
                }
                return this;
            }
        });
    }
    
    private void addLineItem(ActionEvent e) {
        try {
            Item selectedItem = (Item) itemCombo.getSelectedItem();
            if (selectedItem == null) {
                JOptionPane.showMessageDialog(this, "Please select an item.");
                return;
            }
            
            int quantity = Integer.parseInt(quantityField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());
            String specs = specField.getText().trim();
            
            if (quantity <= 0 || price < 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive and price cannot be negative.");
                return;
            }
            
            Object[] rowData = {
                selectedItem.getItemCode(),
                selectedItem.getItemName(),
                quantity,
                "pcs",
                String.format("%.2f", price),
                specs,
                String.format("%.2f", quantity * price)
            };
            
            lineTableModel.addRow(rowData);
            
            quantityField.setText("");
            priceField.setText("");
            specField.setText("");
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for quantity and price.");
        }
    }
    
    private void removeLineItem(ActionEvent e) {
        int selectedRow = lineTable.getSelectedRow();
        if (selectedRow >= 0) {
            lineTableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a line item to remove.");
        }
    }
    
    private void submitPR(ActionEvent e) {
        try {
            String department = departmentField.getText().trim();
            String priority = (String) priorityCombo.getSelectedItem();
            String justification = justificationArea.getText().trim();
            
            if (department.isEmpty() || justification.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
                return;
            }
            
            if (lineTableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Please add at least one line item.");
                return;
            }
            
            List<PurchaseRequisitionLine> lines = new ArrayList<>();
            for (int i = 0; i < lineTableModel.getRowCount(); i++) {
                String itemCode = (String) lineTableModel.getValueAt(i, 0);
                String itemName = (String) lineTableModel.getValueAt(i, 1);
                int quantity = (Integer) lineTableModel.getValueAt(i, 2);
                String unit = (String) lineTableModel.getValueAt(i, 3);
                double price = Double.parseDouble(((String) lineTableModel.getValueAt(i, 4)));
                String specifications = (String) lineTableModel.getValueAt(i, 5);
                
                Item item = itemService.findByCode(itemCode);
                String itemId = item != null ? item.getItemId() : "";
                
                PurchaseRequisitionLine line = new PurchaseRequisitionLine(
                    "", itemId, itemCode, itemName, quantity, unit, price, specifications);
                lines.add(line);
            }
            
            prService.createPR(currentUser.getUsername(), department, priority, justification, lines);
            
            JOptionPane.showMessageDialog(this, "Purchase Requisition created successfully!");
            dispose();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error creating PR: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}