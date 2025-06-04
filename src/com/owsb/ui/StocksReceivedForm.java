package com.owsb.ui;

import com.owsb.domain.*;
import com.owsb.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class StocksReceivedForm extends JDialog {
    private JPanel mainPanel;
    private JTable stocksTable;
    private JButton updateStockButton;
    private JButton viewDetailsButton;
    private JButton refreshButton;
    private JButton closeButton;
    private JComboBox<String> statusFilter;
    private JLabel statusLabel;
    
    private DefaultTableModel tableModel;
    private POService poService;
    private ItemService itemService;
    private SupplierService supplierService;
    private User currentUser;
    
    public StocksReceivedForm(JFrame parent, User user) {
        super(parent, "Stocks Received", true);
        this.currentUser = user;
        initializeServices();
        initializeComponents();
        loadStocksReceived();
    }
    
    private void initializeServices() {
        this.poService = new POService();
        this.itemService = new ItemService();
        this.supplierService = new SupplierService();
    }
    
    private void initializeComponents() {
        setTitle("Stocks Received - Inventory Manager");
        setSize(1000, 700);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        
        // Top panel with title and filters
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Stocks Received from Purchase Orders", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel filterPanel = new JPanel(new FlowLayout());
        filterPanel.add(new JLabel("Filter by Status:"));
        statusFilter = new JComboBox<>(new String[]{"All", "RECEIVED", "PARTIALLY_RECEIVED", "PENDING"});
        statusFilter.addActionListener(e -> loadStocksReceived());
        filterPanel.add(statusFilter);
        
        statusLabel = new JLabel("Ready", SwingConstants.CENTER);
        statusLabel.setForeground(Color.BLUE);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.CENTER);
        topPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Create table
        String[] columnNames = {
            "PO ID", "PO Status", "Item ID", "Item Name", "Supplier", "Ordered Qty", 
            "Received Qty", "Pending Qty", "Unit Price", "Total Value", "Item Status", "Last Updated"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        stocksTable = new JTable(tableModel);
        stocksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stocksTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Set column widths
        stocksTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // PO ID
        stocksTable.getColumnModel().getColumn(1).setPreferredWidth(90);  // PO Status
        stocksTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Item ID
        stocksTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Item Name
        stocksTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Supplier
        stocksTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Ordered Qty
        stocksTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Received Qty
        stocksTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Pending Qty
        stocksTable.getColumnModel().getColumn(8).setPreferredWidth(80);  // Unit Price
        stocksTable.getColumnModel().getColumn(9).setPreferredWidth(100); // Total Value
        stocksTable.getColumnModel().getColumn(10).setPreferredWidth(90); // Item Status
        stocksTable.getColumnModel().getColumn(11).setPreferredWidth(100); // Last Updated
        
        JScrollPane scrollPane = new JScrollPane(stocksTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Stock Receipts"));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        updateStockButton = new JButton("Update Stock");
        viewDetailsButton = new JButton("View PO Details");
        refreshButton = new JButton("Refresh");
        closeButton = new JButton("Close");
        
        // Style buttons
        updateStockButton.setBackground(new Color(34, 139, 34));
        updateStockButton.setForeground(Color.WHITE);
        updateStockButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
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
        updateStockButton.addActionListener(e -> updateSelectedStock());
        viewDetailsButton.addActionListener(e -> viewPODetails());
        refreshButton.addActionListener(e -> {
            statusLabel.setText("Refreshing...");
            loadStocksReceived();
            statusLabel.setText("Ready");
        });
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(updateStockButton);
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        // Add components to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void loadStocksReceived() {
        tableModel.setRowCount(0);
        
        try {
            List<PurchaseOrder> approvedPOs = poService.getApprovedPOs();
            String selectedStatus = (String) statusFilter.getSelectedItem();
            int totalRecords = 0;
            
            for (PurchaseOrder po : approvedPOs) {
                List<PurchaseOrderLine> poLines = poService.getPOLines(po.getPoId());
                Supplier supplier = supplierService.getSupplierById(po.getSupplierId());
                String supplierName = (supplier != null) ? supplier.getSupplierName() : "Unknown";
                
                for (PurchaseOrderLine line : poLines) {
                    // Calculate status
                    int orderedQty = line.getQuantity();
                    int receivedQty = line.getReceivedQuantity();
                    int pendingQty = orderedQty - receivedQty;
                    
                    String lineStatus;
                    if (receivedQty == 0) {
                        lineStatus = "PENDING";
                    } else if (receivedQty >= orderedQty) {
                        lineStatus = "RECEIVED";
                    } else {
                        lineStatus = "PARTIALLY_RECEIVED";
                    }
                    
                    // Apply filter
                    if (!selectedStatus.equals("All") && !lineStatus.equals(selectedStatus)) {
                        continue;
                    }
                    
                    totalRecords++;
                    
                    // Get item details
                    Item item = itemService.getItemById(line.getItemId());
                    String itemName = (item != null) ? item.getItemName() : "Unknown";
                    
                    Object[] rowData = {
                        po.getPoId(),
                        po.getStatus(),
                        line.getItemId(),
                        itemName,
                        supplierName,
                        orderedQty,
                        receivedQty,
                        pendingQty,
                        String.format("₹%.2f", line.getUnitPrice()),
                        String.format("₹%.2f", receivedQty * line.getUnitPrice()),
                        lineStatus,
                        po.getOrderDate().toString()
                    };
                    tableModel.addRow(rowData);
                }
            }
            
            statusLabel.setText("Found " + totalRecords + " stock receipt records");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading stocks received: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Error loading data");
        }
    }
    
    private void updateSelectedStock() {
        int selectedRow = stocksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a stock item to update.", 
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String poId = (String) tableModel.getValueAt(selectedRow, 0);
        String itemId = (String) tableModel.getValueAt(selectedRow, 2);
        String itemName = (String) tableModel.getValueAt(selectedRow, 3);
        int orderedQty = (Integer) tableModel.getValueAt(selectedRow, 5);
        int currentReceived = (Integer) tableModel.getValueAt(selectedRow, 6);
        
        // Open stock update dialog
        new StockReceiptUpdateDialog(this, poId, itemId, itemName, orderedQty, currentReceived, 
                                   () -> loadStocksReceived()).setVisible(true);
    }
    
    private void viewPODetails() {
        int selectedRow = stocksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to view PO details.", 
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String poId = (String) tableModel.getValueAt(selectedRow, 0);
        
        try {
            PurchaseOrder po = poService.findPOById(poId);
            if (po != null) {
                new PODetailsViewForm(this, po).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Purchase Order not found.", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading PO details: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}