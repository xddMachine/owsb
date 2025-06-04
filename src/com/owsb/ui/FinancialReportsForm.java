package com.owsb.ui;

import javax.swing.*;
import java.awt.*;

public class FinancialReportsForm extends JDialog {
    private JPanel mainPanel;
    private JComboBox<String> reportTypeCombo;
    private JTextField fromDateField;
    private JTextField toDateField;
    private JComboBox<String> departmentFilter;
    private JComboBox<String> supplierFilter;
    private JTable reportTable;
    private JButton generateButton;
    private JButton exportButton;
    private JButton printButton;
    private JButton closeButton;
    private JLabel summaryLabel;
    private JPanel chartPanel;
    
    public FinancialReportsForm(JFrame parent) {
        super(parent, "Financial Reports", true);
        initializeComponents();
    }
    
    private void initializeComponents() {
        setTitle("Financial Reports");
        setSize(1200, 800);
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
            "Purchase Summary", 
            "Payment Status", 
            "Supplier Performance", 
            "Budget Analysis", 
            "Cash Flow", 
            "Outstanding Invoices",
            "Purchase Trends"
        });
        filterPanel.add(reportTypeCombo, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        filterPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 3;
        departmentFilter = new JComboBox<>(new String[]{"All Departments", "IT", "HR", "Finance", "Operations"});
        filterPanel.add(departmentFilter, gbc);
        
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
        
        gbc.gridx = 0; gbc.gridy = 2;
        filterPanel.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        supplierFilter = new JComboBox<>(new String[]{"All Suppliers", "Supplier A", "Supplier B", "Supplier C"});
        filterPanel.add(supplierFilter, gbc);
        
        gbc.gridx = 3; gbc.gridy = 2; gbc.gridwidth = 1;
        generateButton = new JButton("Generate Report");
        filterPanel.add(generateButton, gbc);
        
        // Create tabbed pane for report display
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Table tab
        String[] columns = {"Date", "Description", "Amount", "Type", "Supplier", "Status"};
        Object[][] data = {};
        reportTable = new JTable(data, columns);
        JScrollPane tableScrollPane = new JScrollPane(reportTable);
        tabbedPane.addTab("Detailed View", tableScrollPane);
        
        // Chart tab (placeholder)
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.add(new JLabel("Chart View - Not Implemented", SwingConstants.CENTER), BorderLayout.CENTER);
        chartPanel.setBackground(Color.WHITE);
        tabbedPane.addTab("Chart View", chartPanel);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryLabel = new JLabel("No data to display", SwingConstants.CENTER);
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.BOLD, 14f));
        summaryPanel.add(summaryLabel, BorderLayout.CENTER);
        summaryPanel.setPreferredSize(new Dimension(0, 40));
        
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
        centerPanel.add(tabbedPane, BorderLayout.CENTER);
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
        reportTypeCombo.addActionListener(e -> updateFilterVisibility());
        
        // Initial setup
        updateFilterVisibility();
    }
    
    private void updateFilterVisibility() {
        // Show/hide filters based on report type
        String reportType = (String) reportTypeCombo.getSelectedItem();
        boolean needsSupplier = "Supplier Performance".equals(reportType) || "Outstanding Invoices".equals(reportType);
        supplierFilter.setEnabled(needsSupplier);
        
        boolean needsDepartment = "Budget Analysis".equals(reportType) || "Purchase Summary".equals(reportType);
        departmentFilter.setEnabled(needsDepartment);
    }
    
    private void generateReport() {
        // Placeholder method
        String reportType = (String) reportTypeCombo.getSelectedItem();
        String department = (String) departmentFilter.getSelectedItem();
        String supplier = (String) supplierFilter.getSelectedItem();
        String fromDate = fromDateField.getText();
        String toDate = toDateField.getText();
        
        // Validate date range
        if (fromDate.isEmpty() || toDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both from and to dates");
            return;
        }
        
        JOptionPane.showMessageDialog(this, 
            "Generate financial report functionality not implemented yet\n" +
            "Report Type: " + reportType + "\n" +
            "Department: " + department + "\n" +
            "Supplier: " + supplier + "\n" +
            "Date Range: " + fromDate + " to " + toDate);
        
        // Update summary with placeholder data
        updateSummary(reportType);
    }
    
    private void updateSummary(String reportType) {
        // Placeholder summary based on report type
        switch (reportType) {
            case "Purchase Summary":
                summaryLabel.setText("Total Purchases: $125,450 | Orders: 45 | Average Order: $2,788");
                break;
            case "Payment Status":
                summaryLabel.setText("Paid: $98,200 | Pending: $27,250 | Overdue: $5,500");
                break;
            case "Cash Flow":
                summaryLabel.setText("Inflow: $250,000 | Outflow: $198,750 | Net: $51,250");
                break;
            case "Outstanding Invoices":
                summaryLabel.setText("Total Outstanding: $32,750 | Count: 12 | Average Age: 18 days");
                break;
            default:
                summaryLabel.setText("Report generated successfully");
        }
    }
    
    private void exportReport() {
        // Placeholder method
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("financial_report.xlsx"));
        int result = fileChooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(this, "Export functionality not implemented yet");
        }
    }
    
    private void printReport() {
        // Placeholder method
        JOptionPane.showMessageDialog(this, "Print functionality not implemented yet");
    }
}