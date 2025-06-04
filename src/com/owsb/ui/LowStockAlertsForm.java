package com.owsb.ui;

import com.owsb.domain.Item;
import com.owsb.domain.Supplier;
import com.owsb.domain.User;
import com.owsb.service.ItemService;
import com.owsb.service.SupplierItemService;
import com.owsb.service.PRService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LowStockAlertsForm extends JDialog {
    private ItemService itemService;
    private SupplierItemService supplierItemService;
    private PRService prService;
    private User currentUser;
    private JTable lowStockTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton createPRButton;
    private JLabel alertCountLabel;
    
    public LowStockAlertsForm(Frame parent, User user) {
        super(parent, "Low Stock Alerts", true);
        this.currentUser = user;
        this.itemService = new ItemService();
        this.supplierItemService = new SupplierItemService();
        this.prService = new PRService();
        initializeComponents();
        loadLowStockItems();
    }
    
    private void initializeComponents() {
        setSize(900, 600);
        setLayout(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Low Stock Items Alert", JLabel.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        alertCountLabel = new JLabel("", JLabel.CENTER);
        alertCountLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        alertCountLabel.setForeground(Color.RED);
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(alertCountLabel, BorderLayout.SOUTH);
        
        // Table setup
        String[] columnNames = {"Item Code", "Item Name", "Category", "Current Stock", 
                               "Reorder Level", "Stock Status", "Suppliers Count", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        lowStockTable = new JTable(tableModel);
        lowStockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lowStockTable.setRowHeight(25);
        
        // Set column widths
        lowStockTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        lowStockTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        lowStockTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        lowStockTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        lowStockTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        lowStockTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        lowStockTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        lowStockTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(lowStockTable);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        refreshButton = new JButton("Refresh");
        createPRButton = new JButton("Create PR for Selected Item");
        JButton viewSuppliersButton = new JButton("View Suppliers");
        JButton closeButton = new JButton("Close");
        
        refreshButton.addActionListener(e -> loadLowStockItems());
        createPRButton.addActionListener(e -> createPRForSelectedItem());
        viewSuppliersButton.addActionListener(e -> viewSuppliersForSelectedItem());
        closeButton.addActionListener(e -> dispose());
        
        // Enable/disable buttons based on selection
        lowStockTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = lowStockTable.getSelectedRow() != -1;
            createPRButton.setEnabled(hasSelection);
            viewSuppliersButton.setEnabled(hasSelection);
        });
        
        createPRButton.setEnabled(false);
        viewSuppliersButton.setEnabled(false);
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(createPRButton);
        buttonPanel.add(viewSuppliersButton);
        buttonPanel.add(closeButton);
        
        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        JTextArea infoText = new JTextArea(3, 0);
        infoText.setText("Items shown below have stock levels at or below their reorder level.\n" +
                        "Select an item and click 'Create PR' to generate a Purchase Requisition.\n" +
                        "Ensure items have multiple suppliers for better procurement options.");
        infoText.setEditable(false);
        infoText.setBackground(getBackground());
        infoText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.add(infoText, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setLocationRelativeTo(getParent());
    }
    
    private void loadLowStockItems() {
        tableModel.setRowCount(0);
        List<Item> lowStockItems = itemService.getLowStockItems();
        
        for (Item item : lowStockItems) {
            List<Supplier> suppliers = supplierItemService.getSuppliersForItem(item.getItemId());
            
            String stockStatus;
            if (item.getStockQuantity() == 0) {
                stockStatus = "OUT OF STOCK";
            } else if (item.getStockQuantity() <= item.getReorderLevel()) {
                stockStatus = "LOW STOCK";
            } else {
                stockStatus = "NORMAL";
            }
            
            Object[] row = {
                item.getItemCode(),
                item.getItemName(),
                item.getCategory(),
                item.getStockQuantity(),
                item.getReorderLevel(),
                stockStatus,
                suppliers.size() + " supplier(s)",
                suppliers.size() > 1 ? "Multiple Suppliers" : 
                    (suppliers.size() == 1 ? "Single Supplier" : "No Suppliers")
            };
            tableModel.addRow(row);
        }
        
        // Update alert count
        int alertCount = lowStockItems.size();
        if (alertCount > 0) {
            alertCountLabel.setText("⚠️ " + alertCount + " items require attention!");
        } else {
            alertCountLabel.setText("✅ All items have adequate stock levels.");
            alertCountLabel.setForeground(Color.GREEN);
        }
    }
    
    private void createPRForSelectedItem() {
        int selectedRow = lowStockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item first.");
            return;
        }
        
        String itemCode = (String) tableModel.getValueAt(selectedRow, 0);
        Item item = itemService.findByCode(itemCode);
        
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Item not found.");
            return;
        }
        
        // Check if item has suppliers
        List<Supplier> suppliers = supplierItemService.getSuppliersForItem(item.getItemId());
        if (suppliers.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "This item has no suppliers configured. Please add suppliers first.",
                "No Suppliers", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show quantity input dialog
        String quantityStr = JOptionPane.showInputDialog(this,
                "Enter quantity to requisition for " + item.getItemName() + ":",
                "Create Purchase Requisition",
                JOptionPane.QUESTION_MESSAGE);
        
        if (quantityStr != null && !quantityStr.trim().isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityStr.trim());
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.");
                    return;
                }
                
                // Calculate suggested quantity (reorder level + safety stock)
                int suggestedQty = Math.max(quantity, item.getReorderLevel() * 2);
                
                int result = JOptionPane.showConfirmDialog(this,
                        String.format("Create PR for %s:\n" +
                                     "Requested Quantity: %d\n" +
                                     "Suggested Quantity: %d (2x reorder level)\n" +
                                     "Available Suppliers: %d\n\n" +
                                     "Proceed with requested quantity?",
                                     item.getItemName(), quantity, suggestedQty, suppliers.size()),
                        "Confirm PR Creation",
                        JOptionPane.YES_NO_OPTION);
                
                if (result == JOptionPane.YES_OPTION) {
                    // Create PR using PRService
                    String prId = prService.createPRForLowStockItem(item.getItemId(), quantity, 
                                                                   currentUser.getUserId(), "LOW_STOCK");
                    
                    if (prId != null) {
                        JOptionPane.showMessageDialog(this,
                                "Purchase Requisition created successfully!\n" +
                                "PR ID: " + prId + "\n" +
                                "Status: Pending Approval",
                                "PR Created",
                                JOptionPane.INFORMATION_MESSAGE);
                        
                        // Refresh the table
                        loadLowStockItems();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Error creating Purchase Requisition.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid number.", 
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void viewSuppliersForSelectedItem() {
        int selectedRow = lowStockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item first.");
            return;
        }
        
        String itemCode = (String) tableModel.getValueAt(selectedRow, 0);
        String itemName = (String) tableModel.getValueAt(selectedRow, 1);
        Item item = itemService.findByCode(itemCode);
        
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Item not found.");
            return;
        }
        
        List<Supplier> suppliers = supplierItemService.getSuppliersForItem(item.getItemId());
        
        if (suppliers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No suppliers found for this item.\n" +
                    "Please configure suppliers in Supplier Management.",
                    "No Suppliers",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Show suppliers in a dialog
        StringBuilder supplierInfo = new StringBuilder();
        supplierInfo.append("Suppliers for: ").append(itemName).append("\n\n");
        
        for (int i = 0; i < suppliers.size(); i++) {
            Supplier supplier = suppliers.get(i);
            Double price = supplierItemService.getSupplierItemPrice(supplier.getSupplierId(), item.getItemId());
            
            supplierInfo.append(String.format("%d. %s (%s)\n", 
                                             i + 1, supplier.getSupplierName(), supplier.getSupplierCode()));
            supplierInfo.append(String.format("   Contact: %s\n", supplier.getContactPerson()));
            supplierInfo.append(String.format("   Phone: %s\n", supplier.getPhone()));
            supplierInfo.append(String.format("   Price: $%.2f\n", price != null ? price : 0.0));
            supplierInfo.append("\n");
        }
        
        JTextArea textArea = new JTextArea(supplierInfo.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
                                    "Suppliers for " + itemName, 
                                    JOptionPane.INFORMATION_MESSAGE);
    }
}