package com.owsb.ui;

import com.owsb.domain.User;
import com.owsb.domain.Role;
import com.owsb.service.UserService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class AdminDashboard extends JFrame {
    private User currentUser;
    private UserService userService;
    private JTabbedPane tabbedPane;
    private JTable userTable;
    private DefaultTableModel userTableModel;
    private JTextField userFullNameField, userUsernameField, userEmailField, userPhoneField;
    private JPasswordField userPasswordField;
    private JComboBox<Role> userRoleCombo;
    
    public AdminDashboard(User user) {
        this.currentUser = user;
        this.userService = new UserService();
        initializeComponents();
        loadUsers();
    }
    
    private void initializeComponents() {
        setTitle("OWSB Admin Dashboard - " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        
        tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("User Management", createUserManagementPanel());
        tabbedPane.addTab("Master Data", createMasterDataPanel());
        tabbedPane.addTab("Reports", createReportsPanel());
        tabbedPane.addTab("Audit Log", createAuditLogPanel());
        
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        logoutItem.addActionListener(e -> logout());
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        
        // Add logout button to toolbar
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName() + " (Administrator)");
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        logoutButton.setPreferredSize(new Dimension(80, 30));
        
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.add(logoutButton);
        
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(logoutPanel, BorderLayout.EAST);
        
        setJMenuBar(menuBar);
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }
    
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columnNames = {"User ID", "Role", "Username", "Full Name", "Email", "Phone", "Status"};
        userTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(userTableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        userFullNameField = new JTextField(20);
        formPanel.add(userFullNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        userUsernameField = new JTextField(20);
        formPanel.add(userUsernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        userPasswordField = new JPasswordField(20);
        formPanel.add(userPasswordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        userRoleCombo = new JComboBox<>(new Role[]{Role.SALES_MANAGER, Role.PURCHASE_MANAGER, 
                                                   Role.INVENTORY_MANAGER, Role.FINANCE_MANAGER});
        formPanel.add(userRoleCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        userEmailField = new JTextField(20);
        formPanel.add(userEmailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        userPhoneField = new JTextField(20);
        formPanel.add(userPhoneField, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add User");
        JButton updateButton = new JButton("Update User");
        JButton deleteButton = new JButton("Delete User");
        JButton clearButton = new JButton("Clear");
        
        addButton.addActionListener(e -> addUser());
        updateButton.addActionListener(e -> updateUser());
        deleteButton.addActionListener(e -> deleteUser());
        clearButton.addActionListener(e -> clearUserForm());
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateUserForm();
            }
        });
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(formPanel, BorderLayout.EAST);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createMasterDataPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JButton itemsButton = new JButton("Manage Items");
        JButton suppliersButton = new JButton("Manage Suppliers");
        JButton categoriesButton = new JButton("Manage Categories");
        JButton settingsButton = new JButton("System Settings");
        
        itemsButton.setPreferredSize(new Dimension(200, 100));
        suppliersButton.setPreferredSize(new Dimension(200, 100));
        categoriesButton.setPreferredSize(new Dimension(200, 100));
        settingsButton.setPreferredSize(new Dimension(200, 100));
        
        itemsButton.addActionListener(e -> openItemManagement());
        suppliersButton.addActionListener(e -> openSupplierManagement());
        
        panel.add(itemsButton);
        panel.add(suppliersButton);
        panel.add(categoriesButton);
        panel.add(settingsButton);
        
        return panel;
    }
    
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JButton userReportButton = new JButton("User Report");
        JButton itemReportButton = new JButton("Item Report");
        JButton supplierReportButton = new JButton("Supplier Report");
        JButton salesReportButton = new JButton("Sales Report");
        JButton prReportButton = new JButton("PR Report");
        JButton poReportButton = new JButton("PO Report");
        
        panel.add(userReportButton);
        panel.add(itemReportButton);
        panel.add(supplierReportButton);
        panel.add(salesReportButton);
        panel.add(prReportButton);
        panel.add(poReportButton);
        
        return panel;
    }
    
    private JPanel createAuditLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create audit log table
        String[] columns = {"Timestamp", "User ID", "Username", "Role", "Action", "Permission", "Resource", "Status", "Details"};
        DefaultTableModel auditTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable auditTable = new JTable(auditTableModel);
        auditTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane auditScrollPane = new JScrollPane(auditTable);
        auditScrollPane.setPreferredSize(new Dimension(950, 400));
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Refresh");
        JButton exportButton = new JButton("Export to CSV");
        JLabel recordsLabel = new JLabel("Showing last 100 entries");
        
        controlPanel.add(refreshButton);
        controlPanel.add(exportButton);
        controlPanel.add(recordsLabel);
        
        // Load audit data
        refreshButton.addActionListener(e -> {
            auditTableModel.setRowCount(0);
            com.owsb.service.AuditService auditService = new com.owsb.service.AuditService();
            java.util.List<String> entries = auditService.getRecentAuditEntries(100);
            
            for (String entry : entries) {
                String[] parts = entry.split(",");
                if (parts.length >= 9) {
                    auditTableModel.addRow(parts);
                }
            }
            recordsLabel.setText("Showing last " + entries.size() + " entries");
        });
        
        exportButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Export functionality not implemented yet.", 
                                        "Info", JOptionPane.INFORMATION_MESSAGE);
        });
        
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(auditScrollPane, BorderLayout.CENTER);
        
        // Initial load
        refreshButton.doClick();
        
        return panel;
    }
    
    private void loadUsers() {
        userTableModel.setRowCount(0);
        List<User> users = userService.listUsers();
        
        for (User user : users) {
            Object[] row = {
                user.getUserId(),
                user.getRole(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus()
            };
            userTableModel.addRow(row);
        }
    }
    
    private void addUser() {
        try {
            String fullName = userFullNameField.getText().trim();
            String username = userUsernameField.getText().trim();
            String password = new String(userPasswordField.getPassword());
            Role role = (Role) userRoleCombo.getSelectedItem();
            String email = userEmailField.getText().trim();
            String phone = userPhoneField.getText().trim();
            
            User newUser = userService.createUser(role, username, password, fullName, phone, email);
            loadUsers();
            clearUserForm();
            JOptionPane.showMessageDialog(this, "User created successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating user: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to update.");
            return;
        }
        
        try {
            String userId = (String) userTableModel.getValueAt(selectedRow, 0);
            User user = userService.findUserById(userId);
            
            if (user != null) {
                user.setFullName(userFullNameField.getText().trim());
                user.setUsername(userUsernameField.getText().trim());
                String password = new String(userPasswordField.getPassword());
                if (!password.isEmpty()) {
                    user.setPassword(password);
                }
                user.setRole((Role) userRoleCombo.getSelectedItem());
                user.setEmail(userEmailField.getText().trim());
                user.setPhone(userPhoneField.getText().trim());
                
                userService.updateUser(user);
                loadUsers();
                clearUserForm();
                JOptionPane.showMessageDialog(this, "User updated successfully!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating user: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.");
            return;
        }
        
        String userId = (String) userTableModel.getValueAt(selectedRow, 0);
        String username = (String) userTableModel.getValueAt(selectedRow, 2);
        
        if (userId.equals(currentUser.getUserId())) {
            JOptionPane.showMessageDialog(this, "Cannot delete your own account.");
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete user: " + username + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            if (userService.deleteUser(userId)) {
                loadUsers();
                clearUserForm();
                JOptionPane.showMessageDialog(this, "User deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Error deleting user.", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void populateUserForm() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow != -1) {
            userFullNameField.setText((String) userTableModel.getValueAt(selectedRow, 3));
            userUsernameField.setText((String) userTableModel.getValueAt(selectedRow, 2));
            userPasswordField.setText("");
            userRoleCombo.setSelectedItem(userTableModel.getValueAt(selectedRow, 1));
            userEmailField.setText((String) userTableModel.getValueAt(selectedRow, 4));
            userPhoneField.setText((String) userTableModel.getValueAt(selectedRow, 5));
        }
    }
    
    private void clearUserForm() {
        userFullNameField.setText("");
        userUsernameField.setText("");
        userPasswordField.setText("");
        userRoleCombo.setSelectedIndex(0);
        userEmailField.setText("");
        userPhoneField.setText("");
        userTable.clearSelection();
    }
    
    private void openItemManagement() {
        new ItemManagementForm(this).setVisible(true);
    }
    
    private void openSupplierManagement() {
        new SupplierManagementForm(this).setVisible(true);
    }
    
    private void logout() {
        this.dispose();
        new LoginForm().setVisible(true);
    }
}