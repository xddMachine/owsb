package com.owsb.ui;

import com.owsb.domain.Permission;
import com.owsb.domain.User;
import com.owsb.service.AuthorizationService;
import com.owsb.util.UIPermissionUtils;
import javax.swing.*;
import java.awt.*;

public class PurchaseManagerDashboard extends JFrame {
    private User currentUser;
    private AuthorizationService authService;
    
    public PurchaseManagerDashboard(User user) {
        this.currentUser = user;
        this.authService = new AuthorizationService();
        initializeComponents();
    }
    
    private void initializeComponents() {
        setTitle("OWSB Purchase Manager Dashboard - " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        
        JPanel mainPanel = new JPanel(new GridLayout(3, 3, 20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JButton viewItemsButton = new JButton("View Items");
        JButton viewSuppliersButton = new JButton("View Suppliers");
        JButton viewPRsButton = new JButton("View Purchase Requisitions");
        JButton generatePOButton = new JButton("Generate Purchase Orders");
        JButton managePOButton = new JButton("Manage Purchase Orders");
        JButton viewPOListButton = new JButton("View PO List");
        JButton confirmPOButton = new JButton("🔄 Confirm Draft POs");
        JButton stockReportsButton = new JButton("Stock Reports");
        JButton logoutButton = new JButton("Logout");
        
        Dimension buttonSize = new Dimension(200, 80);
        viewItemsButton.setPreferredSize(buttonSize);
        viewSuppliersButton.setPreferredSize(buttonSize);
        viewPRsButton.setPreferredSize(buttonSize);
        generatePOButton.setPreferredSize(buttonSize);
        managePOButton.setPreferredSize(buttonSize);
        viewPOListButton.setPreferredSize(buttonSize);
        confirmPOButton.setPreferredSize(buttonSize);
        stockReportsButton.setPreferredSize(buttonSize);
        logoutButton.setPreferredSize(buttonSize);
        
        // Apply permission-based enforcement
        UIPermissionUtils.setButtonPermission(viewItemsButton, currentUser, Permission.ITEM_VIEW_ALL);
        UIPermissionUtils.setButtonPermission(viewSuppliersButton, currentUser, Permission.SUPPLIER_VIEW_ALL);
        UIPermissionUtils.setButtonPermission(viewPRsButton, currentUser, Permission.PR_VIEW_ALL);
        UIPermissionUtils.setButtonPermission(generatePOButton, currentUser, Permission.PO_CREATE);
        UIPermissionUtils.setButtonPermission(managePOButton, currentUser, Permission.PO_UPDATE);
        UIPermissionUtils.setButtonPermission(viewPOListButton, currentUser, Permission.PO_VIEW_ALL);
        UIPermissionUtils.setButtonPermission(confirmPOButton, currentUser, Permission.PO_UPDATE);
        UIPermissionUtils.setButtonPermission(stockReportsButton, currentUser, Permission.STOCK_REPORT_GENERATE);
        
        viewItemsButton.addActionListener(UIPermissionUtils.createPermissionAwareAction(
            currentUser, Permission.ITEM_VIEW_ALL, this::openItemView, this, "view items"));
        viewSuppliersButton.addActionListener(UIPermissionUtils.createPermissionAwareAction(
            currentUser, Permission.SUPPLIER_VIEW_ALL, this::openSupplierView, this, "view suppliers"));
        viewPRsButton.addActionListener(UIPermissionUtils.createPermissionAwareAction(
            currentUser, Permission.PR_VIEW_ALL, this::openPRView, this, "view purchase requisitions"));
        generatePOButton.addActionListener(UIPermissionUtils.createPermissionAwareAction(
            currentUser, Permission.PO_CREATE, this::openPOGeneration, this, "generate purchase orders"));
        managePOButton.addActionListener(UIPermissionUtils.createPermissionAwareAction(
            currentUser, Permission.PO_UPDATE, this::openPOManagement, this, "manage purchase orders"));
        viewPOListButton.addActionListener(UIPermissionUtils.createPermissionAwareAction(
            currentUser, Permission.PO_VIEW_ALL, this::openPOList, this, "view purchase orders"));
        confirmPOButton.addActionListener(UIPermissionUtils.createPermissionAwareAction(
            currentUser, Permission.PO_UPDATE, this::openConfirmPO, this, "confirm draft purchase orders"));
        stockReportsButton.addActionListener(UIPermissionUtils.createPermissionAwareAction(
            currentUser, Permission.STOCK_REPORT_GENERATE, this::openStockReports, this, "view stock reports"));
        logoutButton.addActionListener(e -> logout());
        
        mainPanel.add(viewItemsButton);
        mainPanel.add(viewSuppliersButton);
        mainPanel.add(viewPRsButton);
        mainPanel.add(generatePOButton);
        mainPanel.add(managePOButton);
        mainPanel.add(viewPOListButton);
        mainPanel.add(confirmPOButton);
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
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName() + " (Purchase Manager)");
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
    
    private void openItemView() {
        new ItemViewForm(this).setVisible(true);
    }
    
    private void openSupplierView() {
        new SupplierViewForm(this).setVisible(true);
    }
    
    private void openPRView() {
        new PRListForm(this, currentUser).setVisible(true);
    }
    
    private void openPOGeneration() {
        new POGenerationForm(this).setVisible(true);
    }
    
    private void openPOManagement() {
        new POManagementForm(this, currentUser).setVisible(true);
    }
    
    private void openPOList() {
        new POListForm(this, currentUser).setVisible(true);
    }
    
    private void openConfirmPO() {
        new POConfirmationForm(this, currentUser).setVisible(true);
    }
    
    private void openStockReports() {
        new StockReportsForm(this).setVisible(true);
    }
    
    private void logout() {
        this.dispose();
        new LoginForm().setVisible(true);
    }
}