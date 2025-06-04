package com.owsb.ui;

import com.owsb.domain.PurchaseRequisition;
import com.owsb.domain.PurchaseRequisitionLine;
import com.owsb.domain.User;
import com.owsb.domain.Role;
import com.owsb.service.PRService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PRListForm extends JDialog {
    private JPanel mainPanel;
    private JTable prTable;
    private JButton viewButton;
    private JButton refreshButton;
    private JButton closeButton;
    private JComboBox<String> statusFilter;
    private DefaultTableModel tableModel;
    private PRService prService;
    private User currentUser;
    
    public PRListForm(JFrame parent, User user) {
        super(parent, "Purchase Requisition List", true);
        this.currentUser = user;
        this.prService = new PRService();
        initializeComponents();
        loadPRData();
    }
    
    
    private void initializeComponents() {
        setTitle("Purchase Requisition List");
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Status Filter:"));
        statusFilter = new JComboBox<>(new String[]{"All", "Pending", "Approved", "Rejected"});
        filterPanel.add(statusFilter);
        
        // Create table
        String[] columns = {"PR ID", "Date Created", "Requested By", "Department", "Priority", "Status", "Total Items", "Total Value"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        prTable = new JTable(tableModel);
        prTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(prTable);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        viewButton = new JButton("View Details");
        refreshButton = new JButton("Refresh");
        closeButton = new JButton("Close");
        
        buttonPanel.add(viewButton);
        
        // Add approve and generate PO buttons only for Purchase Manager
        if (currentUser.getRole() == Role.PURCHASE_MANAGER) {
            JButton approveButton = new JButton("Approve PR");
            JButton generatePOButton = new JButton("Generate PO from PR");
            
            // Style the approve button
            approveButton.setBackground(Color.WHITE);
            approveButton.setForeground(Color.BLACK);
            approveButton.setFont(approveButton.getFont().deriveFont(Font.BOLD));
            approveButton.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 0), 2));
            
            // Style the generate PO button
            generatePOButton.setBackground(Color.WHITE);
            generatePOButton.setForeground(Color.BLACK);
            generatePOButton.setFont(generatePOButton.getFont().deriveFont(Font.BOLD));
            generatePOButton.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 200), 2));
            
            // Add action listeners
            approveButton.addActionListener(e -> approvePR());
            generatePOButton.addActionListener(e -> generatePOFromPR());
            
            buttonPanel.add(approveButton);
            buttonPanel.add(generatePOButton);
        }
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        mainPanel.add(filterPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Add action listeners
        viewButton.addActionListener(e -> viewPRDetails());
        refreshButton.addActionListener(e -> loadPRData());
        closeButton.addActionListener(e -> dispose());
        statusFilter.addActionListener(e -> filterByStatus());
    }
    
    private void viewPRDetails() {
        int selectedRow = prTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a PR to view.");
            return;
        }
        
        String prId = (String) tableModel.getValueAt(selectedRow, 0);
        PurchaseRequisition pr = prService.findPRById(prId);
        
        if (pr != null) {
            PRDetailsForm detailsForm = new PRDetailsForm((JFrame) getParent(), pr);
            detailsForm.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "PR not found.");
        }
    }
    
    private void loadPRData() {
        tableModel.setRowCount(0);
        
        try {
            List<PurchaseRequisition> prs = prService.listPRs();
            String selectedStatus = (String) statusFilter.getSelectedItem();
            
            for (PurchaseRequisition pr : prs) {
                // Apply filter
                if (!"All".equals(selectedStatus) && !selectedStatus.equalsIgnoreCase(pr.getStatus())) {
                    continue;
                }
                
                Object[] rowData = {
                    pr.getPrId(),
                    pr.getRequestDate().toString(),
                    pr.getRequestedBy(),
                    pr.getDepartment(),
                    pr.getPriority(),
                    pr.getStatus(),
                    pr.getLines().size(),
                    String.format("%.2f", pr.getTotalAmount())
                };
                tableModel.addRow(rowData);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading PR data: " + e.getMessage());
        }
    }
    
    private void filterByStatus() {
        loadPRData();
    }
    
    private void approvePR() {
        int selectedRow = prTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a PR to approve.");
            return;
        }
        
        String prId = (String) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 5);
        
        if (!"PENDING".equals(status)) {
            JOptionPane.showMessageDialog(this, "Only pending PRs can be approved.\nCurrent status: " + status);
            return;
        }
        
        PurchaseRequisition pr = prService.findPRById(prId);
        if (pr != null) {
            // Open the PR Approval Form with supplier selection
            PRApprovalForm approvalForm = new PRApprovalForm((JFrame) getParent(), pr, currentUser);
            approvalForm.setVisible(true);
            // Refresh the list after approval form is closed
            loadPRData();
        } else {
            JOptionPane.showMessageDialog(this, "PR not found.");
        }
    }
    
    private void generatePOFromPR() {
        int selectedRow = prTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a PR to generate PO from.");
            return;
        }
        
        String prId = (String) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 5);
        
        if (!"APPROVED".equals(status)) {
            JOptionPane.showMessageDialog(this, "Only approved PRs can be converted to POs.\nCurrent status: " + status);
            return;
        }
        
        PurchaseRequisition pr = prService.findPRById(prId);
        if (pr != null) {
            // Check if PR has already been converted to PO
            if ("CONVERTED_TO_PO".equals(pr.getStatus())) {
                JOptionPane.showMessageDialog(this, "This PR has already been converted to a PO.");
                return;
            }
            
            // Open PO Generation Form with PR data pre-populated
            POGenerationForm poForm = new POGenerationForm((JFrame) getParent(), prId);
            poForm.setVisible(true);
            
            // Refresh the list after PO generation
            loadPRData();
        } else {
            JOptionPane.showMessageDialog(this, "PR not found.");
        }
    }
}