package com.owsb.ui;

import com.owsb.domain.User;
import javax.swing.*;
import java.awt.*;

public class InventoryManagerDashboard extends JFrame {
    private User currentUser;
    
    public InventoryManagerDashboard(User user) {
        this.currentUser = user;
        initializeComponents();
    }
    
    private void initializeComponents() {
        setTitle("OWSB Inventory Manager Dashboard - " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        
        JPanel mainPanel = new JPanel(new GridLayout(3, 3, 20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JButton viewItemsButton = new JButton("View Items");
        JButton viewStocksReceivedButton = new JButton("📦 View Stocks Received");
        JButton updateStockButton = new JButton("Update Stock");
        JButton lowStockAlertsButton = new JButton("Low Stock Alerts");
        JButton stockReportsButton = new JButton("📊 Stock Reports");
        JButton itemWiseReportsButton = new JButton("📋 Item-wise Reports");
        JButton supplierWiseReportsButton = new JButton("🏢 Supplier-wise Reports");
        JButton receiveGoodsButton = new JButton("Receive Goods");
        JButton viewPOListButton = new JButton("View PO List");
        
        Dimension buttonSize = new Dimension(200, 80);
        viewItemsButton.setPreferredSize(buttonSize);
        viewStocksReceivedButton.setPreferredSize(buttonSize);
        updateStockButton.setPreferredSize(buttonSize);
        lowStockAlertsButton.setPreferredSize(buttonSize);
        stockReportsButton.setPreferredSize(buttonSize);
        itemWiseReportsButton.setPreferredSize(buttonSize);
        supplierWiseReportsButton.setPreferredSize(buttonSize);
        receiveGoodsButton.setPreferredSize(buttonSize);
        viewPOListButton.setPreferredSize(buttonSize);
        
        viewItemsButton.addActionListener(e -> openItemView());
        viewStocksReceivedButton.addActionListener(e -> openStocksReceived());
        updateStockButton.addActionListener(e -> openStockUpdate());
        lowStockAlertsButton.addActionListener(e -> openLowStockAlerts());
        stockReportsButton.addActionListener(e -> openStockReports());
        itemWiseReportsButton.addActionListener(e -> openItemWiseReports());
        supplierWiseReportsButton.addActionListener(e -> openSupplierWiseReports());
        receiveGoodsButton.addActionListener(e -> openReceiveGoods());
        viewPOListButton.addActionListener(e -> openPOList());
        
        mainPanel.add(viewItemsButton);
        mainPanel.add(viewStocksReceivedButton);
        mainPanel.add(updateStockButton);
        mainPanel.add(lowStockAlertsButton);
        mainPanel.add(stockReportsButton);
        mainPanel.add(itemWiseReportsButton);
        mainPanel.add(supplierWiseReportsButton);
        mainPanel.add(receiveGoodsButton);
        mainPanel.add(viewPOListButton);
        
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
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName() + " (Inventory Manager)");
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
        add(mainPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }
    
    private void openItemView() {
        new ItemViewForm(this).setVisible(true);
    }
    
    private void openStockUpdate() {
        new StockUpdateForm(this, currentUser).setVisible(true);
    }
    
    private void openLowStockAlerts() {
        new LowStockAlertsForm(this, currentUser).setVisible(true);
    }
    
    private void openStockReports() {
        new StockReportsForm(this).setVisible(true);
    }
    
    private void openReceiveGoods() {
        new ReceiveGoodsForm(this).setVisible(true);
    }
    
    private void openStocksReceived() {
        new StocksReceivedForm(this, currentUser).setVisible(true);
    }
    
    private void openItemWiseReports() {
        new ItemWiseStockReportsForm(this, currentUser).setVisible(true);
    }
    
    private void openSupplierWiseReports() {
        new SupplierWiseStockReportsForm(this, currentUser).setVisible(true);
    }
    
    private void openPOList() {
        new POListForm(this, currentUser).setVisible(true);
    }
    
    private void logout() {
        this.dispose();
        new LoginForm().setVisible(true);
    }
}