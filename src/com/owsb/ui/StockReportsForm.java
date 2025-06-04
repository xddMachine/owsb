package com.owsb.ui;

import com.owsb.domain.*;
import com.owsb.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class StockReportsForm extends JDialog {
    private JPanel mainPanel;
    private JComboBox<String> reportTypeCombo;
    private JTextField fromDateField;
    private JTextField toDateField;
    private JComboBox<String> categoryFilter;
    private JTable reportTable;
    private JButton generateButton;
    private JButton exportButton;
    private JButton printButton;
    private JButton closeButton;
    private JLabel summaryLabel;
    
    private DefaultTableModel tableModel;
    private ItemService itemService;
    private POService poService;
    private SalesService salesService;
    
    public StockReportsForm(JFrame parent) {
        super(parent, "Stock Reports", true);
        initializeServices();
        initializeComponents();
    }
    
    private void initializeServices() {
        this.itemService = new ItemService();
        this.poService = new POService();
        this.salesService = new SalesService();
    }
    
    private void initializeComponents() {
        setTitle("Stock Reports");
        setSize(1000, 700);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        
        // Filter panel
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Report Filters"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        filterPanel.add(new JLabel("Report Type:"), gbc);
        gbc.gridx = 1;
        reportTypeCombo = new JComboBox<>(new String[]{
            "Current Stock Levels", 
            "Stock Movement", 
            "Low Stock Items", 
            "Stock Valuation", 
            "Inventory Turnover"
        });
        filterPanel.add(reportTypeCombo, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        filterPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 3;
        categoryFilter = new JComboBox<>(new String[]{"All Categories", "Electronics", "Stationery", "Furniture"});
        filterPanel.add(categoryFilter, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        filterPanel.add(new JLabel("From Date:"), gbc);
        gbc.gridx = 1;
        fromDateField = new JTextField(10);
        filterPanel.add(fromDateField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 1;
        filterPanel.add(new JLabel("To Date:"), gbc);
        gbc.gridx = 3;
        toDateField = new JTextField(10);
        filterPanel.add(toDateField, gbc);
        
        gbc.gridx = 4; gbc.gridy = 0; gbc.gridheight = 2;
        generateButton = new JButton("Generate Report");
        filterPanel.add(generateButton, gbc);
        
        // Report table
        String[] columns = {"Item ID", "Item Name", "Category", "Current Stock", "Unit Price", "Total Value", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        reportTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reportTable);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        summaryLabel = new JLabel("Total Items: 0 | Total Value: $0.00");
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.BOLD));
        summaryPanel.add(summaryLabel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        exportButton = new JButton("Export to Excel");
        printButton = new JButton("Print Report");
        closeButton = new JButton("Close");
        
        buttonPanel.add(exportButton);
        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);
        
        // Assemble main panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        mainPanel.add(filterPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Add action listeners
        generateButton.addActionListener(e -> generateReport());
        exportButton.addActionListener(e -> exportReport());
        printButton.addActionListener(e -> printReport());
        closeButton.addActionListener(e -> dispose());
        reportTypeCombo.addActionListener(e -> updateDateFieldsVisibility());
        
        // Initial setup
        updateDateFieldsVisibility();
    }
    
    private void updateDateFieldsVisibility() {
        // Enable/disable date fields based on report type
        String reportType = (String) reportTypeCombo.getSelectedItem();
        boolean needsDates = "Stock Movement".equals(reportType) || "Inventory Turnover".equals(reportType);
        fromDateField.setEnabled(needsDates);
        toDateField.setEnabled(needsDates);
    }
    
    private void generateReport() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        String category = (String) categoryFilter.getSelectedItem();
        
        try {
            tableModel.setRowCount(0);
            
            switch (reportType) {
                case "Current Stock Levels":
                    generateCurrentStockReport(category);
                    break;
                case "Low Stock Items":
                    generateLowStockReport(category);
                    break;
                case "Stock Valuation":
                    generateStockValuationReport(category);
                    break;
                case "Stock Movement":
                    generateStockMovementReport(category);
                    break;
                case "Inventory Turnover":
                    generateInventoryTurnoverReport(category);
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Report type not implemented", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generateCurrentStockReport(String category) {
        List<Item> items = itemService.getAllItems();
        
        if (!"All Categories".equals(category)) {
            items = items.stream()
                    .filter(item -> category.equals(item.getCategory()))
                    .collect(Collectors.toList());
        }
        
        double totalValue = 0.0;
        int totalItems = 0;
        
        for (Item item : items) {
            double itemValue = item.getStockQuantity() * item.getUnitPrice();
            totalValue += itemValue;
            totalItems++;
            
            String status = getStockStatus(item);
            
            Object[] rowData = {
                item.getItemId(),
                item.getItemName(),
                item.getCategory(),
                item.getStockQuantity(),
                String.format("$%.2f", item.getUnitPrice()),
                String.format("$%.2f", itemValue),
                status
            };
            tableModel.addRow(rowData);
        }
        
        summaryLabel.setText(String.format("Total Items: %d | Total Value: $%.2f", totalItems, totalValue));
    }
    
    private void generateLowStockReport(String category) {
        List<Item> items = itemService.getAllItems();
        
        if (!"All Categories".equals(category)) {
            items = items.stream()
                    .filter(item -> category.equals(item.getCategory()))
                    .collect(Collectors.toList());
        }
        
        // Filter for low stock items (assuming reorder level of 10 or less)
        items = items.stream()
                .filter(item -> item.getStockQuantity() <= item.getReorderLevel())
                .collect(Collectors.toList());
        
        double totalValue = 0.0;
        int totalItems = 0;
        
        for (Item item : items) {
            double itemValue = item.getStockQuantity() * item.getUnitPrice();
            totalValue += itemValue;
            totalItems++;
            
            String status = getStockStatus(item);
            
            Object[] rowData = {
                item.getItemId(),
                item.getItemName(),
                item.getCategory(),
                item.getStockQuantity(),
                String.format("$%.2f", item.getUnitPrice()),
                String.format("$%.2f", itemValue),
                status
            };
            tableModel.addRow(rowData);
        }
        
        summaryLabel.setText(String.format("Low Stock Items: %d | Total Value: $%.2f", totalItems, totalValue));
    }
    
    private void generateStockValuationReport(String category) {
        List<Item> items = itemService.getAllItems();
        
        if (!"All Categories".equals(category)) {
            items = items.stream()
                    .filter(item -> category.equals(item.getCategory()))
                    .collect(Collectors.toList());
        }
        
        // Sort by value descending
        items.sort((a, b) -> Double.compare(
            b.getStockQuantity() * b.getUnitPrice(),
            a.getStockQuantity() * a.getUnitPrice()
        ));
        
        double totalValue = 0.0;
        int totalItems = 0;
        
        for (Item item : items) {
            double itemValue = item.getStockQuantity() * item.getUnitPrice();
            totalValue += itemValue;
            totalItems++;
            
            String status = String.format("%.1f%% of total", 
                totalValue > 0 ? (itemValue / totalValue) * 100 : 0);
            
            Object[] rowData = {
                item.getItemId(),
                item.getItemName(),
                item.getCategory(),
                item.getStockQuantity(),
                String.format("$%.2f", item.getUnitPrice()),
                String.format("$%.2f", itemValue),
                status
            };
            tableModel.addRow(rowData);
        }
        
        summaryLabel.setText(String.format("Total Items: %d | Total Valuation: $%.2f", totalItems, totalValue));
    }
    
    private void generateStockMovementReport(String category) {
        // This would require transaction history - for now show a simplified version
        List<Item> items = itemService.getAllItems();
        
        if (!"All Categories".equals(category)) {
            items = items.stream()
                    .filter(item -> category.equals(item.getCategory()))
                    .collect(Collectors.toList());
        }
        
        double totalValue = 0.0;
        int totalItems = 0;
        
        for (Item item : items) {
            double itemValue = item.getStockQuantity() * item.getUnitPrice();
            totalValue += itemValue;
            totalItems++;
            
            String status = "Movement data not available";
            
            Object[] rowData = {
                item.getItemId(),
                item.getItemName(),
                item.getCategory(),
                item.getStockQuantity(),
                String.format("$%.2f", item.getUnitPrice()),
                String.format("$%.2f", itemValue),
                status
            };
            tableModel.addRow(rowData);
        }
        
        summaryLabel.setText(String.format("Total Items: %d | Period: %s to %s", 
            totalItems, fromDateField.getText(), toDateField.getText()));
    }
    
    private void generateInventoryTurnoverReport(String category) {
        // This would require historical data - simplified version
        List<Item> items = itemService.getAllItems();
        
        if (!"All Categories".equals(category)) {
            items = items.stream()
                    .filter(item -> category.equals(item.getCategory()))
                    .collect(Collectors.toList());
        }
        
        double totalValue = 0.0;
        int totalItems = 0;
        
        for (Item item : items) {
            double itemValue = item.getStockQuantity() * item.getUnitPrice();
            totalValue += itemValue;
            totalItems++;
            
            String status = "Turnover data not available";
            
            Object[] rowData = {
                item.getItemId(),
                item.getItemName(),
                item.getCategory(),
                item.getStockQuantity(),
                String.format("$%.2f", item.getUnitPrice()),
                String.format("$%.2f", itemValue),
                status
            };
            tableModel.addRow(rowData);
        }
        
        summaryLabel.setText(String.format("Total Items: %d | Analysis Period: %s to %s", 
            totalItems, fromDateField.getText(), toDateField.getText()));
    }
    
    private String getStockStatus(Item item) {
        int stock = item.getStockQuantity();
        int reorderLevel = item.getReorderLevel();
        
        if (stock == 0) {
            return "OUT OF STOCK";
        } else if (stock <= reorderLevel) {
            return "LOW STOCK";
        } else if (stock <= reorderLevel * 2) {
            return "NORMAL";
        } else {
            return "OVERSTOCKED";
        }
    }
    
    private void exportReport() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export. Please generate a report first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("stock_report.csv"));
        int result = fileChooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                exportToCSV(fileChooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Report exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error exporting report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void exportToCSV(String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                writer.append(tableModel.getColumnName(i));
                if (i < tableModel.getColumnCount() - 1) {
                    writer.append(",");
                }
            }
            writer.append("\n");
            
            // Write data
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    Object value = tableModel.getValueAt(i, j);
                    writer.append(value != null ? value.toString() : "");
                    if (j < tableModel.getColumnCount() - 1) {
                        writer.append(",");
                    }
                }
                writer.append("\n");
            }
        }
    }
    
    private void printReport() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to print. Please generate a report first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            boolean complete = reportTable.print();
            if (complete) {
                JOptionPane.showMessageDialog(this, "Report printed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Printing was cancelled.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error printing report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}