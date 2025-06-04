package com.owsb.ui;

import com.owsb.domain.*;
import com.owsb.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDate;

public class SupplierWiseStockReportsForm extends JDialog {
    private JPanel mainPanel;
    private JTable stockTable;
    private JButton exportButton;
    private JButton viewDetailsButton;
    private JButton refreshButton;
    private JButton closeButton;
    private JComboBox<String> supplierFilter;
    private JLabel statusLabel;
    private JLabel summaryLabel;
    
    private DefaultTableModel tableModel;
    private ItemService itemService;
    private POService poService;
    private SupplierService supplierService;
    private SupplierItemService supplierItemService;
    private User currentUser;
    
    public SupplierWiseStockReportsForm(JFrame parent, User user) {
        super(parent, "Supplier-wise Stock Reports", true);
        this.currentUser = user;
        initializeServices();
        initializeComponents();
        loadSupplierFilter();
        loadSupplierWiseStockReport();
    }
    
    private void initializeServices() {
        this.itemService = new ItemService();
        this.poService = new POService();
        this.supplierService = new SupplierService();
        this.supplierItemService = new SupplierItemService();
    }
    
    private void initializeComponents() {
        setTitle("Supplier-wise Stock Reports - Inventory Manager");
        setSize(1200, 750);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        
        // Top panel with title and filters
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Supplier-wise Stock Report", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel filterPanel = new JPanel(new FlowLayout());
        filterPanel.add(new JLabel("Filter by Supplier:"));
        supplierFilter = new JComboBox<>();
        supplierFilter.addActionListener(e -> loadSupplierWiseStockReport());
        filterPanel.add(supplierFilter);
        
        statusLabel = new JLabel("Ready", SwingConstants.CENTER);
        statusLabel.setForeground(Color.BLUE);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        summaryLabel = new JLabel("", SwingConstants.CENTER);
        summaryLabel.setFont(new Font("Arial", Font.BOLD, 12));
        summaryLabel.setForeground(new Color(34, 139, 34));
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.CENTER);
        
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(summaryLabel, BorderLayout.SOUTH);
        topPanel.add(statusPanel, BorderLayout.SOUTH);
        
        // Create table
        String[] columnNames = {
            "Supplier", "Item ID", "Item Name", "Category", "Current Stock", "Total Ordered",
            "Total Received", "Pending Receipt", "Last Order Date", "Last Unit Price", "Total Value"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        stockTable = new JTable(tableModel);
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stockTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Set column widths
        stockTable.getColumnModel().getColumn(0).setPreferredWidth(120); // Supplier
        stockTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Item ID
        stockTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Item Name
        stockTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Category
        stockTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Current Stock
        stockTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Total Ordered
        stockTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Total Received
        stockTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Pending Receipt
        stockTable.getColumnModel().getColumn(8).setPreferredWidth(100); // Last Order Date
        stockTable.getColumnModel().getColumn(9).setPreferredWidth(80);  // Last Unit Price
        stockTable.getColumnModel().getColumn(10).setPreferredWidth(100); // Total Value
        
        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Supplier Stock Details"));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        exportButton = new JButton("Export to CSV");
        viewDetailsButton = new JButton("View Supplier Details");
        refreshButton = new JButton("Refresh");
        closeButton = new JButton("Close");
        
        // Style buttons
        exportButton.setBackground(new Color(34, 139, 34));
        exportButton.setForeground(Color.WHITE);
        exportButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        viewDetailsButton.setBackground(new Color(70, 130, 180));
        viewDetailsButton.setForeground(Color.WHITE);
        viewDetailsButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        refreshButton.setBackground(new Color(255, 140, 0));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        closeButton.setBackground(new Color(220, 20, 60));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // Add action listeners
        exportButton.addActionListener(e -> exportToCSV());
        viewDetailsButton.addActionListener(e -> viewSupplierDetails());
        refreshButton.addActionListener(e -> {
            statusLabel.setText("Refreshing...");
            loadSupplierWiseStockReport();
            statusLabel.setText("Ready");
        });
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(exportButton);
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        // Add components to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void loadSupplierFilter() {
        supplierFilter.removeAllItems();
        supplierFilter.addItem("All Suppliers");
        
        try {
            List<Supplier> suppliers = supplierService.listSuppliers();
            for (Supplier supplier : suppliers) {
                supplierFilter.addItem(supplier.getSupplierName());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading suppliers: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadSupplierWiseStockReport() {
        tableModel.setRowCount(0);
        
        try {
            String selectedSupplier = (String) supplierFilter.getSelectedItem();
            List<Supplier> suppliersToProcess = new ArrayList<>();
            
            // Get suppliers to process based on selection
            if ("All Suppliers".equals(selectedSupplier)) {
                suppliersToProcess = supplierService.listSuppliers();
            } else {
                // Find the selected supplier
                List<Supplier> allSuppliers = supplierService.listSuppliers();
                for (Supplier supplier : allSuppliers) {
                    if (supplier.getSupplierName().equals(selectedSupplier)) {
                        suppliersToProcess.add(supplier);
                        break;
                    }
                }
            }
            
            int totalRecords = 0;
            double totalStockValue = 0;
            int totalItemsSupplied = 0;
            
            // Process each supplier
            for (Supplier supplier : suppliersToProcess) {
                String supplierName = supplier.getSupplierName();
                String supplierId = supplier.getSupplierId();
                
                // Get all items this supplier can supply
                List<Item> supplierItems = supplierItemService.getItemsForSupplier(supplierId);
                
                for (Item item : supplierItems) {
                    String itemId = item.getItemId();
                    
                    // Get PO history for this supplier-item combination
                    SupplierItemData poData = getPODataForSupplierItem(supplierId, itemId);
                    
                    int currentStock = item.getStockQuantity();
                    int pendingReceipt = poData.totalOrdered - poData.totalReceived;
                    double unitPrice = poData.lastUnitPrice > 0 ? poData.lastUnitPrice : item.getUnitPrice();
                    double totalValue = currentStock * unitPrice;
                    
                    totalRecords++;
                    totalStockValue += totalValue;
                    totalItemsSupplied++;
                    
                    Object[] rowData = {
                        supplierName,
                        itemId,
                        item.getItemName(),
                        item.getCategory(),
                        currentStock,
                        poData.totalOrdered,
                        poData.totalReceived,
                        pendingReceipt,
                        poData.lastOrderDate != null ? poData.lastOrderDate.toString() : "Never",
                        String.format("₹%.2f", unitPrice),
                        String.format("₹%.2f", totalValue)
                    };
                    tableModel.addRow(rowData);
                }
            }
            
            // Update summary
            summaryLabel.setText(String.format(
                "Records: %d | Items Supplied: %d | Total Stock Value: ₹%.2f",
                totalRecords, totalItemsSupplied, totalStockValue));
            
            statusLabel.setText("Report generated successfully");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating supplier-wise stock report: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Error loading data");
        }
    }
    
    private SupplierItemData getPODataForSupplierItem(String supplierId, String itemId) {
        SupplierItemData data = new SupplierItemData();
        
        try {
            List<PurchaseOrder> allPOs = poService.listPOs();
            
            for (PurchaseOrder po : allPOs) {
                if (!po.getSupplierId().equals(supplierId)) {
                    continue;
                }
                
                List<PurchaseOrderLine> poLines = poService.getPOLines(po.getPoId());
                
                for (PurchaseOrderLine line : poLines) {
                    if (line.getItemId().equals(itemId)) {
                        data.totalOrdered += line.getQuantity();
                        data.totalReceived += line.getReceivedQuantity();
                        
                        // Update last order date and price
                        if (data.lastOrderDate == null || po.getOrderDate().isAfter(data.lastOrderDate)) {
                            data.lastOrderDate = po.getOrderDate();
                            data.lastUnitPrice = line.getUnitPrice();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting PO data for supplier-item: " + e.getMessage());
        }
        
        return data;
    }
    
    private void viewSupplierDetails() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to view supplier details.", 
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String supplierName = (String) tableModel.getValueAt(selectedRow, 0);
        
        try {
            List<Supplier> suppliers = supplierService.listSuppliers();
            Supplier selectedSupplier = null;
            
            for (Supplier supplier : suppliers) {
                if (supplier.getSupplierName().equals(supplierName)) {
                    selectedSupplier = supplier;
                    break;
                }
            }
            
            if (selectedSupplier != null) {
                new SupplierViewForm((JFrame) getParent(), selectedSupplier.getSupplierId()).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Supplier details not found.", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading supplier details: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportToCSV() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.", 
                                        "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        StringBuilder csvContent = new StringBuilder();
        
        // Add headers
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            csvContent.append(tableModel.getColumnName(i));
            if (i < tableModel.getColumnCount() - 1) {
                csvContent.append(",");
            }
        }
        csvContent.append("\n");
        
        // Add data
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                Object value = tableModel.getValueAt(row, col);
                csvContent.append(value != null ? value.toString() : "");
                if (col < tableModel.getColumnCount() - 1) {
                    csvContent.append(",");
                }
            }
            csvContent.append("\n");
        }
        
        // Create export preview dialog
        JTextArea exportArea = new JTextArea(csvContent.toString());
        exportArea.setEditable(false);
        exportArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        
        JScrollPane exportScrollPane = new JScrollPane(exportArea);
        exportScrollPane.setPreferredSize(new Dimension(600, 400));
        
        JOptionPane.showMessageDialog(this, exportScrollPane, 
                                    "CSV Export Preview - Supplier-wise Stock Report", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Helper class to store supplier item data
    private static class SupplierItemData {
        int totalOrdered = 0;
        int totalReceived = 0;
        LocalDate lastOrderDate = null;
        double lastUnitPrice = 0.0;
    }
}