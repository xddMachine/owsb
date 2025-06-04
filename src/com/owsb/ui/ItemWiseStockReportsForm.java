package com.owsb.ui;

import com.owsb.domain.*;
import com.owsb.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ItemWiseStockReportsForm extends JDialog {
    private JPanel mainPanel;
    private JTable stockTable;
    private JButton exportButton;
    private JButton refreshButton;
    private JButton closeButton;
    private JComboBox<String> categoryFilter;
    private JLabel statusLabel;
    private JLabel summaryLabel;
    
    private DefaultTableModel tableModel;
    private ItemService itemService;
    private POService poService;
    private SupplierService supplierService;
    private User currentUser;
    
    public ItemWiseStockReportsForm(JFrame parent, User user) {
        super(parent, "Item-wise Stock Reports", true);
        this.currentUser = user;
        initializeServices();
        initializeComponents();
        loadItemWiseStockReport();
    }
    
    private void initializeServices() {
        this.itemService = new ItemService();
        this.poService = new POService();
        this.supplierService = new SupplierService();
    }
    
    private void initializeComponents() {
        setTitle("Item-wise Stock Reports - Inventory Manager");
        setSize(1100, 750);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        
        // Top panel with title and filters
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Item-wise Stock Report", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel filterPanel = new JPanel(new FlowLayout());
        filterPanel.add(new JLabel("Filter by Category:"));
        categoryFilter = new JComboBox<>(new String[]{"All", "Electronics", "Stationery", "Furniture", "Other"});
        categoryFilter.addActionListener(e -> loadItemWiseStockReport());
        filterPanel.add(categoryFilter);
        
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
            "Item ID", "Item Name", "Category", "Current Stock", "Reorder Level", "Stock Status",
            "Total Ordered", "Total Received", "Pending Receipt", "Last Order Date", "Primary Supplier"
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
        stockTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Item ID
        stockTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Item Name
        stockTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Category
        stockTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Current Stock
        stockTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Reorder Level
        stockTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Stock Status
        stockTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Total Ordered
        stockTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Total Received
        stockTable.getColumnModel().getColumn(8).setPreferredWidth(80);  // Pending Receipt
        stockTable.getColumnModel().getColumn(9).setPreferredWidth(100); // Last Order Date
        stockTable.getColumnModel().getColumn(10).setPreferredWidth(120); // Primary Supplier
        
        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Item Stock Details"));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        exportButton = new JButton("Export to CSV");
        refreshButton = new JButton("Refresh");
        closeButton = new JButton("Close");
        
        // Style buttons
        exportButton.setBackground(new Color(34, 139, 34));
        exportButton.setForeground(Color.WHITE);
        exportButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        refreshButton.setBackground(new Color(255, 140, 0));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        closeButton.setBackground(new Color(220, 20, 60));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // Add action listeners
        exportButton.addActionListener(e -> exportToCSV());
        refreshButton.addActionListener(e -> {
            statusLabel.setText("Refreshing...");
            loadItemWiseStockReport();
            statusLabel.setText("Ready");
        });
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(exportButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        // Add components to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void loadItemWiseStockReport() {
        tableModel.setRowCount(0);
        
        try {
            List<Item> items = itemService.listItems();
            String selectedCategory = (String) categoryFilter.getSelectedItem();
            
            // Get PO data for analysis
            List<PurchaseOrder> allPOs = poService.listPOs();
            Map<String, Integer> totalOrderedMap = new HashMap<>();
            Map<String, Integer> totalReceivedMap = new HashMap<>();
            Map<String, String> lastOrderDateMap = new HashMap<>();
            Map<String, String> primarySupplierMap = new HashMap<>();
            
            // Process PO data
            for (PurchaseOrder po : allPOs) {
                List<PurchaseOrderLine> poLines = poService.getPOLines(po.getPoId());
                for (PurchaseOrderLine line : poLines) {
                    String itemId = line.getItemId();
                    
                    // Total ordered
                    totalOrderedMap.put(itemId, 
                        totalOrderedMap.getOrDefault(itemId, 0) + line.getQuantity());
                    
                    // Total received
                    totalReceivedMap.put(itemId, 
                        totalReceivedMap.getOrDefault(itemId, 0) + line.getReceivedQuantity());
                    
                    // Last order date
                    if (!lastOrderDateMap.containsKey(itemId) || 
                        po.getOrderDate().isAfter(java.time.LocalDate.parse(lastOrderDateMap.get(itemId)))) {
                        lastOrderDateMap.put(itemId, po.getOrderDate().toString());
                        
                        // Primary supplier (from most recent order)
                        Supplier supplier = supplierService.getSupplierById(po.getSupplierId());
                        primarySupplierMap.put(itemId, 
                            supplier != null ? supplier.getSupplierName() : "Unknown");
                    }
                }
            }
            
            int totalItems = 0;
            int lowStockItems = 0;
            int outOfStockItems = 0;
            double totalStockValue = 0;
            
            for (Item item : items) {
                // Apply category filter
                if (!selectedCategory.equals("All") && !item.getCategory().equals(selectedCategory)) {
                    continue;
                }
                
                totalItems++;
                
                int currentStock = item.getStockQuantity();
                int reorderLevel = item.getReorderLevel();
                int totalOrdered = totalOrderedMap.getOrDefault(item.getItemId(), 0);
                int totalReceived = totalReceivedMap.getOrDefault(item.getItemId(), 0);
                int pendingReceipt = totalOrdered - totalReceived;
                
                // Determine stock status
                String stockStatus;
                if (currentStock == 0) {
                    stockStatus = "OUT OF STOCK";
                    outOfStockItems++;
                } else if (currentStock <= reorderLevel) {
                    stockStatus = "LOW STOCK";
                    lowStockItems++;
                } else {
                    stockStatus = "ADEQUATE";
                }
                
                totalStockValue += currentStock * item.getUnitPrice();
                
                Object[] rowData = {
                    item.getItemId(),
                    item.getItemName(),
                    item.getCategory(),
                    currentStock,
                    reorderLevel,
                    stockStatus,
                    totalOrdered,
                    totalReceived,
                    pendingReceipt,
                    lastOrderDateMap.getOrDefault(item.getItemId(), "Never"),
                    primarySupplierMap.getOrDefault(item.getItemId(), "N/A")
                };
                tableModel.addRow(rowData);
            }
            
            // Update summary
            summaryLabel.setText(String.format(
                "Total Items: %d | Low Stock: %d | Out of Stock: %d | Total Value: ₹%.2f",
                totalItems, lowStockItems, outOfStockItems, totalStockValue));
            
            statusLabel.setText("Report generated successfully");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating item-wise stock report: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Error loading data");
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
                                    "CSV Export Preview - Item-wise Stock Report", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }
}