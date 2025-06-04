package com.owsb.ui;

import com.owsb.domain.Permission;
import com.owsb.domain.User;
import com.owsb.service.AuthorizationService;
import com.owsb.util.UIPermissionUtils;
import javax.swing.*;
import java.awt.*;

public class SalesManagerDashboard extends JFrame {
    private User currentUser;
    private AuthorizationService authService;
    
    public SalesManagerDashboard(User user) {
        this.currentUser = user;
        this.authService = new AuthorizationService();
        initializeComponents();
    }
    
    private void initializeComponents() {
        setTitle("OWSB Sales Manager Dashboard - " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        
        JPanel mainPanel = new JPanel(new GridLayout(3, 3, 20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JButton itemManagementButton = new JButton("Item Management");
        JButton supplierManagementButton = new JButton("Supplier Management");
        JButton dailySalesButton = new JButton("Daily Sales Entry");
        JButton prManagementButton = new JButton("Purchase Requisition");
        JButton viewPRButton = new JButton("View PR List");
        JButton viewPOButton = new JButton("View PO List");
        JButton supplierItemButton = new JButton("Manage Supplier-Items");
        JButton stockReportsButton = new JButton("Stock Reports");
        JButton logoutButton = new JButton("Logout");
        
        Dimension buttonSize = new Dimension(200, 80);
        itemManagementButton.setPreferredSize(buttonSize);
        supplierManagementButton.setPreferredSize(buttonSize);
        dailySalesButton.setPreferredSize(buttonSize);
        prManagementButton.setPreferredSize(buttonSize);
        viewPRButton.setPreferredSize(buttonSize);
        viewPOButton.setPreferredSize(buttonSize);
        supplierItemButton.setPreferredSize(buttonSize);
        stockReportsButton.setPreferredSize(buttonSize);
        logoutButton.setPreferredSize(buttonSize);
        
        // Apply permission-based enforcement
        UIPermissionUtils.setButtonPermission(itemManagementButton, currentUser, Permission.ITEM_READ);
        UIPermissionUtils.setButtonPermission(supplierManagementButton, currentUser, Permission.SUPPLIER_READ);
        UIPermissionUtils.setButtonPermission(dailySalesButton, currentUser, Permission.SALESENTRY_CREATE);
        UIPermissionUtils.setButtonPermission(prManagementButton, currentUser, Permission.PR_CREATE);
        UIPermissionUtils.setButtonPermission(viewPRButton, currentUser, Permission.PR_VIEW_ALL);
        UIPermissionUtils.setButtonPermission(viewPOButton, currentUser, Permission.PO_VIEW_ALL);
        UIPermissionUtils.setButtonPermission(supplierItemButton, currentUser, Permission.SUPPLIER_READ);
        UIPermissionUtils.setButtonPermission(stockReportsButton, currentUser, Permission.ITEM_READ);
        
        itemManagementButton.addActionListener(UIPermissionUtils.createPermissionAwareAction(
            currentUser, Permission.ITEM_READ, this::openItemManagement, this, "manage items"));
        supplierManagementButton.addActionListener(UIPermissionUtils.createPermissionAwareAction(
            currentUser, Permission.SUPPLIER_READ, this::openSupplierManagement, this, "manage suppliers"));
        dailySalesButton.addActionListener(UIPermissionUtils.createPermissionAwareAction(
            currentUser, Permission.SALESENTRY_CREATE, this::openDailySalesEntry, this, "enter daily sales"));
        prManagementButton.addActionListener(UIPermissionUtils.createPermissionAwareAction(
            currentUser, Permission.PR_CREATE, this::openPRManagement, this, "manage purchase requisitions"));
        viewPRButton.addActionListener(UIPermissionUtils.createPermissionAwareAction(
            currentUser, Permission.PR_VIEW_ALL, this::openPRList, this, "view purchase requisitions"));
        viewPOButton.addActionListener(UIPermissionUtils.createPermissionAwareAction(
            currentUser, Permission.PO_VIEW_ALL, this::openPOList, this, "view purchase orders"));
        supplierItemButton.addActionListener(UIPermissionUtils.createPermissionAwareAction(
            currentUser, Permission.SUPPLIER_READ, this::openSupplierItemManagement, this, "manage supplier-item relationships"));
        stockReportsButton.addActionListener(UIPermissionUtils.createPermissionAwareAction(
            currentUser, Permission.ITEM_READ, this::openStockReports, this, "view stock reports"));
        logoutButton.addActionListener(e -> logout());
        
        mainPanel.add(itemManagementButton);
        mainPanel.add(supplierManagementButton);
        mainPanel.add(dailySalesButton);
        mainPanel.add(prManagementButton);
        mainPanel.add(viewPRButton);
        mainPanel.add(viewPOButton);
        mainPanel.add(supplierItemButton);
        mainPanel.add(stockReportsButton);
        mainPanel.add(logoutButton);
        
        // Apply consistent styling to all buttons
        UIPermissionUtils.styleAllButtonsInContainer(mainPanel);
        
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
        
        // Add logout button to header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName() + " (Sales Manager)");
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
        
        JButton headerLogoutButton = new JButton("Logout");
        headerLogoutButton.addActionListener(e -> logout());
        headerLogoutButton.setPreferredSize(new Dimension(80, 30));
        
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.add(headerLogoutButton);
        
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(logoutPanel, BorderLayout.EAST);
        
        setJMenuBar(menuBar);
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }
    
    private void openItemManagement() {
        new ItemManagementForm(this).setVisible(true);
    }
    
    private void openSupplierManagement() {
        new SupplierManagementForm(this).setVisible(true);
    }
    
    private void openDailySalesEntry() {
        new DailySalesEntryForm(this, currentUser).setVisible(true);
    }
    
    private void openPRManagement() {
        new PRManagementForm(this, currentUser).setVisible(true);
    }
    
    private void openPRList() {
        new PRListForm(this, currentUser).setVisible(true);
    }
    
    private void openPOList() {
        new POListForm(this, currentUser).setVisible(true);
    }
    
    private void openSupplierItemManagement() {
        new SupplierItemManagementForm(this).setVisible(true);
    }
    
    private void openStockReports() {
        new StockReportsForm(this).setVisible(true);
    }
    
    private void logout() {
        this.dispose();
        new LoginForm().setVisible(true);
    }
}