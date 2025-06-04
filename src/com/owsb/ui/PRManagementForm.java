package com.owsb.ui;

import com.owsb.domain.Permission;
import com.owsb.domain.PurchaseRequisition;
import com.owsb.domain.Role;
import com.owsb.domain.User;
import com.owsb.service.AuthorizationService;
import com.owsb.service.PRService;
import com.owsb.util.UIPermissionUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PRManagementForm extends JDialog {
    private JPanel mainPanel;
    private JButton createButton;
    private JButton editButton;
    private JButton approveButton;
    private JButton generatePOButton;
    private JButton deleteButton;
    private JButton closeButton;
    private JTable prTable;
    private DefaultTableModel tableModel;
    private PRService prService;
    private User currentUser;
    private AuthorizationService authService;
    
    public PRManagementForm(JFrame parent, User user) {
        super(parent, "Purchase Requisition Management", true);
        this.currentUser = user;
        this.prService = new PRService();
        this.authService = new AuthorizationService();
        initializeComponents();
        setupPermissions();
        loadPRData();
    }
    
    
    private void initializeComponents() {
        setTitle("Purchase Requisition Management");
        setSize(900, 650);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        
        // Create table
        String[] columns = {"PR ID", "Date", "Requested By", "Department", "Priority", "Status", "Total Amount"};
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
        createButton = new JButton("Create PR");
        editButton = new JButton("Edit PR");
        deleteButton = new JButton("Delete PR");
        closeButton = new JButton("Close");
        
        // Add approve and generate PO buttons only for Purchase Manager
        if (currentUser.getRole() == Role.PURCHASE_MANAGER) {
            approveButton = new JButton("Approve PR");
            generatePOButton = new JButton("Generate PO");
        }
        
        buttonPanel.add(createButton);
        buttonPanel.add(editButton);
        
        // Add approve/generate buttons only for Purchase Manager
        if (currentUser.getRole() == Role.PURCHASE_MANAGER) {
            buttonPanel.add(approveButton);
            buttonPanel.add(generatePOButton);
        }
        
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Apply consistent styling to all buttons
        UIPermissionUtils.styleAllButtonsInContainer(buttonPanel);
        
        // Add action listeners
        createButton.addActionListener(e -> createPR());
        editButton.addActionListener(e -> editPR());
        
        // Add action listeners only for Purchase Manager buttons
        if (currentUser.getRole() == Role.PURCHASE_MANAGER) {
            approveButton.addActionListener(e -> approvePR());
            generatePOButton.addActionListener(e -> generatePO());
        }
        
        deleteButton.addActionListener(e -> deletePR());
        closeButton.addActionListener(e -> dispose());
        
        // Add refresh button
        JButton refreshButton = UIPermissionUtils.createStyledButton("Refresh");
        refreshButton.addActionListener(e -> loadPRData());
        buttonPanel.add(refreshButton, buttonPanel.getComponentCount() - 1);
    }
    
    private void setupPermissions() {
        // Apply permission-based access control
        UIPermissionUtils.setButtonPermission(createButton, currentUser, Permission.PR_CREATE);
        UIPermissionUtils.setButtonPermission(editButton, currentUser, Permission.PR_UPDATE);
        UIPermissionUtils.setButtonPermission(deleteButton, currentUser, Permission.PR_DELETE);
        
        // Add permissions only for Purchase Manager buttons
        if (currentUser.getRole() == Role.PURCHASE_MANAGER) {
            UIPermissionUtils.setButtonPermission(approveButton, currentUser, Permission.PR_APPROVE);
            UIPermissionUtils.setButtonPermission(generatePOButton, currentUser, Permission.PO_CREATE);
        }
    }
    
    private void createPR() {
        PRCreationForm createForm = new PRCreationForm((JFrame) getParent(), currentUser);
        createForm.setVisible(true);
        loadPRData();
    }
    
    private void editPR() {
        int selectedRow = prTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a PR to edit.");
            return;
        }
        
        String prId = (String) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 5);
        
        if (!"PENDING".equals(status)) {
            JOptionPane.showMessageDialog(this, "Only pending PRs can be edited.");
            return;
        }
        
        PurchaseRequisition pr = prService.findPRById(prId);
        if (pr != null) {
            PREditForm editForm = new PREditForm((JFrame) getParent(), pr, currentUser);
            editForm.setVisible(true);
            loadPRData();
        }
    }
    
    private void approvePR() {
        // Check permission first
        if (!authService.hasPermission(currentUser, Permission.PR_APPROVE)) {
            JOptionPane.showMessageDialog(this, 
                "Access Denied: Only Finance Manager, Purchase Manager, or Admin can approve Purchase Requisitions.",
                "Insufficient Privileges", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int selectedRow = prTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a PR to approve.");
            return;
        }
        
        String prId = (String) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 5);
        
        if (!"PENDING".equals(status)) {
            JOptionPane.showMessageDialog(this, "Only pending PRs can be approved.");
            return;
        }
        
        PurchaseRequisition pr = prService.findPRById(prId);
        if (pr != null) {
            PRApprovalForm approvalForm = new PRApprovalForm((JFrame) getParent(), pr, currentUser);
            approvalForm.setVisible(true);
            loadPRData();
        }
    }
    
    private void generatePO() {
        // Check permission first
        if (!authService.hasPermission(currentUser, Permission.PO_CREATE)) {
            JOptionPane.showMessageDialog(this, 
                "Access Denied: Only Purchase Manager or Admin can generate Purchase Orders.",
                "Insufficient Privileges", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int selectedRow = prTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a PR to generate PO from.");
            return;
        }
        
        String prId = (String) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 5);
        
        if (!"APPROVED".equals(status)) {
            JOptionPane.showMessageDialog(this, "Only approved PRs can be used to generate Purchase Orders.");
            return;
        }
        
        PurchaseRequisition pr = prService.findPRById(prId);
        if (pr != null) {
            POFromPRForm poForm = new POFromPRForm((JFrame) getParent(), pr, currentUser);
            poForm.setVisible(true);
        }
    }
    
    private void deletePR() {
        int selectedRow = prTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a PR to delete.");
            return;
        }
        
        String prId = (String) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 5);
        
        if ("APPROVED".equals(status)) {
            JOptionPane.showMessageDialog(this, "Cannot delete approved PRs.");
            return;
        }
        
        int choice = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this Purchase Requisition?", 
            "Confirm Deletion", 
            JOptionPane.YES_NO_OPTION);
            
        if (choice == JOptionPane.YES_OPTION) {
            if (prService.deletePR(prId)) {
                JOptionPane.showMessageDialog(this, "PR deleted successfully!");
                loadPRData();
            } else {
                JOptionPane.showMessageDialog(this, "Error deleting PR.");
            }
        }
    }
    
    private void loadPRData() {
        tableModel.setRowCount(0);
        
        try {
            List<PurchaseRequisition> prs = prService.listPRs();
            for (PurchaseRequisition pr : prs) {
                Object[] rowData = {
                    pr.getPrId(),
                    pr.getRequestDate().toString(),
                    pr.getRequestedBy(),
                    pr.getDepartment(),
                    pr.getPriority(),
                    pr.getStatus(),
                    String.format("%.2f", pr.getTotalAmount())
                };
                tableModel.addRow(rowData);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading PR data: " + e.getMessage());
        }
    }
}