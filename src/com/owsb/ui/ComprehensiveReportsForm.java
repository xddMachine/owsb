package com.owsb.ui;

import com.owsb.domain.*;
import com.owsb.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ComprehensiveReportsForm extends JDialog {
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private JTable prTable;
    private JTable poTable;
    private JTable lowStockTable;
    private JButton exportButton;
    private JButton printButton;
    private JButton refreshButton;
    private JButton closeButton;
    
    private DefaultTableModel prTableModel;
    private DefaultTableModel poTableModel;
    private DefaultTableModel lowStockTableModel;
    
    private PRService prService;
    private POService poService;
    private ItemService itemService;
    private SupplierService supplierService;
    
    public ComprehensiveReportsForm(JFrame parent) {
        super(parent, "Comprehensive System Reports", true);
        initializeServices();
        initializeComponents();
        loadAllReports();
    }
    
    private void initializeServices() {
        this.prService = new PRService();
        this.poService = new POService();
        this.itemService = new ItemService();
        this.supplierService = new SupplierService();
    }
    
    private void initializeComponents() {
        setTitle("System Reports Dashboard");
        setSize(1200, 800);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // PR Reports Tab
        JPanel prPanel = createPRReportPanel();
        tabbedPane.addTab("Purchase Requisitions by Status & Role", prPanel);
        
        // PO Reports Tab
        JPanel poPanel = createPOReportPanel();
        tabbedPane.addTab("Purchase Orders by Status & Role", poPanel);
        
        // Low Stock Items Tab
        JPanel lowStockPanel = createLowStockReportPanel();
        tabbedPane.addTab("Items Below Reorder Level", lowStockPanel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        exportButton = new JButton("Export to CSV");
        printButton = new JButton("Print Current Tab");
        refreshButton = new JButton("Refresh All");
        closeButton = new JButton("Close");
        
        buttonPanel.add(exportButton);
        buttonPanel.add(printButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Add action listeners
        exportButton.addActionListener(e -> exportCurrentTab());
        printButton.addActionListener(e -> printCurrentTab());
        refreshButton.addActionListener(e -> loadAllReports());
        closeButton.addActionListener(e -> dispose());
    }
    
    private JPanel createPRReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Purchase Requisitions Summary"));
        
        // Create table
        String[] columns = {"PR ID", "Created Date", "Requested By", "Department", "Priority", "Status", "Total Items", "Total Value", "Days Pending"};
        prTableModel = new DefaultTableModel(columns, 0);
        prTable = new JTable(prTableModel);
        JScrollPane scrollPane = new JScrollPane(prTable);
        
        panel.add(summaryPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPOReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Purchase Orders Summary"));
        
        // Create table
        String[] columns = {"PO ID", "PR ID", "Created By", "Supplier", "Order Date", "Expected Delivery", "Status", "Total Amount", "Days Since Creation"};
        poTableModel = new DefaultTableModel(columns, 0);
        poTable = new JTable(poTableModel);
        JScrollPane scrollPane = new JScrollPane(poTable);
        
        panel.add(summaryPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createLowStockReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Low Stock Items Summary"));
        
        // Create table
        String[] columns = {"Item ID", "Item Name", "Category", "Current Stock", "Reorder Level", "Stock Status", "Unit Price", "Total Value", "Suggested Action"};
        lowStockTableModel = new DefaultTableModel(columns, 0);
        lowStockTable = new JTable(lowStockTableModel);
        JScrollPane scrollPane = new JScrollPane(lowStockTable);
        
        panel.add(summaryPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadAllReports() {
        loadPRReport();
        loadPOReport();
        loadLowStockReport();
    }
    
    private void loadPRReport() {
        prTableModel.setRowCount(0);
        
        try {
            List<PurchaseRequisition> allPRs = prService.listPRs();
            
            int pendingCount = 0, approvedCount = 0, rejectedCount = 0, convertedCount = 0;
            double totalValue = 0.0;
            
            for (PurchaseRequisition pr : allPRs) {
                // Calculate days pending
                long daysPending = java.time.temporal.ChronoUnit.DAYS.between(pr.getRequestDate(), java.time.LocalDate.now());
                
                // Calculate total value for this PR
                double prValue = 0.0;
                List<PurchaseRequisitionLine> lines = prService.getPRLines(pr.getPrId());
                for (PurchaseRequisitionLine line : lines) {
                    prValue += line.getQuantity() * line.getEstimatedPrice();
                }
                totalValue += prValue;
                
                // Count by status
                switch (pr.getStatus()) {
                    case "PENDING": pendingCount++; break;
                    case "APPROVED": approvedCount++; break;
                    case "REJECTED": rejectedCount++; break;
                    case "CONVERTED_TO_PO": convertedCount++; break;
                }
                
                Object[] rowData = {
                    pr.getPrId(),
                    pr.getRequestDate().toString(),
                    pr.getRequestedBy(),
                    pr.getDepartment(),
                    pr.getPriority(),
                    pr.getStatus(),
                    lines.size(),
                    String.format("$%.2f", prValue),
                    daysPending + " days"
                };
                prTableModel.addRow(rowData);
            }
            
            // Update summary
            updatePRSummary(pendingCount, approvedCount, rejectedCount, convertedCount, totalValue);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading PR report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadPOReport() {
        poTableModel.setRowCount(0);
        
        try {
            List<PurchaseOrder> allPOs = poService.listPOs();
            
            int pendingCount = 0, approvedCount = 0, completedCount = 0, rejectedCount = 0;
            double totalValue = 0.0;
            
            for (PurchaseOrder po : allPOs) {
                // Calculate days since creation
                long daysSinceCreation = java.time.temporal.ChronoUnit.DAYS.between(po.getOrderDate(), java.time.LocalDate.now());
                
                // Get supplier name
                Supplier supplier = supplierService.getSupplierById(po.getSupplierId());
                String supplierName = (supplier != null) ? supplier.getSupplierName() : "Unknown";
                
                totalValue += po.getTotalAmount();
                
                // Count by status
                switch (po.getStatus()) {
                    case "PENDING": 
                    case "PENDING_APPROVAL": pendingCount++; break;
                    case "APPROVED": approvedCount++; break;
                    case "COMPLETED": 
                    case "PARTIALLY_RECEIVED": completedCount++; break;
                    case "REJECTED": rejectedCount++; break;
                }
                
                Object[] rowData = {
                    po.getPoId(),
                    po.getPrId() != null ? po.getPrId() : "N/A",
                    po.getCreatedBy() != null ? po.getCreatedBy() : "System",
                    supplierName,
                    po.getOrderDate().toString(),
                    po.getExpectedDeliveryDate() != null ? po.getExpectedDeliveryDate().toString() : "N/A",
                    po.getStatus(),
                    String.format("$%.2f", po.getTotalAmount()),
                    daysSinceCreation + " days"
                };
                poTableModel.addRow(rowData);
            }
            
            // Update summary
            updatePOSummary(pendingCount, approvedCount, completedCount, rejectedCount, totalValue);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading PO report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadLowStockReport() {
        lowStockTableModel.setRowCount(0);
        
        try {
            List<Item> allItems = itemService.getAllItems();
            
            int outOfStockCount = 0, lowStockCount = 0;
            double totalValueAtRisk = 0.0;
            
            for (Item item : allItems) {
                if (item.getStockQuantity() <= item.getReorderLevel()) {
                    String stockStatus;
                    String suggestedAction;
                    
                    if (item.getStockQuantity() == 0) {
                        stockStatus = "OUT OF STOCK";
                        suggestedAction = "URGENT: Create PR immediately";
                        outOfStockCount++;
                    } else if (item.getStockQuantity() <= item.getReorderLevel()) {
                        stockStatus = "LOW STOCK";
                        suggestedAction = "Create PR for restocking";
                        lowStockCount++;
                    } else {
                        stockStatus = "NORMAL";
                        suggestedAction = "No action needed";
                    }
                    
                    double itemValue = item.getStockQuantity() * item.getUnitPrice();
                    totalValueAtRisk += itemValue;
                    
                    Object[] rowData = {
                        item.getItemId(),
                        item.getItemName(),
                        item.getCategory(),
                        item.getStockQuantity(),
                        item.getReorderLevel(),
                        stockStatus,
                        String.format("$%.2f", item.getUnitPrice()),
                        String.format("$%.2f", itemValue),
                        suggestedAction
                    };
                    lowStockTableModel.addRow(rowData);
                }
            }
            
            // Update summary
            updateLowStockSummary(outOfStockCount, lowStockCount, totalValueAtRisk);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading low stock report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updatePRSummary(int pending, int approved, int rejected, int converted, double totalValue) {
        JPanel tabPanel = (JPanel) tabbedPane.getComponentAt(0);
        JPanel summaryPanel = (JPanel) tabPanel.getComponent(0);
        summaryPanel.removeAll();
        summaryPanel.add(new JLabel(String.format("Total PRs: %d | Pending: %d | Approved: %d | Rejected: %d | Converted to PO: %d | Total Value: $%.2f",
                pending + approved + rejected + converted, pending, approved, rejected, converted, totalValue)));
        summaryPanel.revalidate();
    }
    
    private void updatePOSummary(int pending, int approved, int completed, int rejected, double totalValue) {
        JPanel tabPanel = (JPanel) tabbedPane.getComponentAt(1);
        JPanel summaryPanel = (JPanel) tabPanel.getComponent(0);
        summaryPanel.removeAll();
        summaryPanel.add(new JLabel(String.format("Total POs: %d | Pending: %d | Approved: %d | Completed: %d | Rejected: %d | Total Value: $%.2f",
                pending + approved + completed + rejected, pending, approved, completed, rejected, totalValue)));
        summaryPanel.revalidate();
    }
    
    private void updateLowStockSummary(int outOfStock, int lowStock, double totalValue) {
        JPanel tabPanel = (JPanel) tabbedPane.getComponentAt(2);
        JPanel summaryPanel = (JPanel) tabPanel.getComponent(0);
        summaryPanel.removeAll();
        summaryPanel.add(new JLabel(String.format("Items Requiring Attention: %d | Out of Stock: %d | Low Stock: %d | Total Value at Risk: $%.2f",
                outOfStock + lowStock, outOfStock, lowStock, totalValue)));
        summaryPanel.revalidate();
    }
    
    private void exportCurrentTab() {
        int selectedTab = tabbedPane.getSelectedIndex();
        String tabName = tabbedPane.getTitleAt(selectedTab);
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File(tabName.replaceAll(" ", "_") + "_report.csv"));
        int result = fileChooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                JTable currentTable = getCurrentTable();
                if (currentTable != null) {
                    exportTableToCSV(currentTable, fileChooser.getSelectedFile().getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "Report exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error exporting report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void printCurrentTab() {
        JTable currentTable = getCurrentTable();
        if (currentTable != null) {
            try {
                boolean complete = currentTable.print();
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
    
    private JTable getCurrentTable() {
        int selectedTab = tabbedPane.getSelectedIndex();
        switch (selectedTab) {
            case 0: return prTable;
            case 1: return poTable;
            case 2: return lowStockTable;
            default: return null;
        }
    }
    
    private void exportTableToCSV(JTable table, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            
            // Write header
            for (int i = 0; i < model.getColumnCount(); i++) {
                writer.append(model.getColumnName(i));
                if (i < model.getColumnCount() - 1) {
                    writer.append(",");
                }
            }
            writer.append("\n");
            
            // Write data
            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    Object value = model.getValueAt(i, j);
                    writer.append(value != null ? value.toString() : "");
                    if (j < model.getColumnCount() - 1) {
                        writer.append(",");
                    }
                }
                writer.append("\n");
            }
        }
    }
}