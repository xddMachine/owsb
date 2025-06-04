package com.owsb.ui;

import com.owsb.domain.User;
import javax.swing.*;
import java.awt.*;

public class FinanceManagerDashboard extends JFrame {
    private User currentUser;
    
    public FinanceManagerDashboard(User user) {
        this.currentUser = user;
        initializeComponents();
    }
    
    private void initializeComponents() {
        setTitle("OWSB Finance Manager Dashboard - " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        
        JPanel mainPanel = new JPanel(new GridLayout(3, 3, 20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JButton approvePOButton = new JButton("Approve Purchase Orders");
        JButton managePOButton = new JButton("🔧 Manage Purchase Orders");
        JButton approvedPOsButton = new JButton("📋 View Approved POs");
        JButton processPaymentsButton = new JButton("Process Payments");
        JButton financialReportsButton = new JButton("Financial Reports");
        JButton viewPRsButton = new JButton("View PR List");
        JButton viewPOsButton = new JButton("View All POs");
        JButton verifyStockButton = new JButton("Verify Stock Updates");
        JButton comprehensiveReportsButton = new JButton("System Reports Dashboard");
        JButton stockReportsButton = new JButton("Stock Reports");
        JButton logoutButton = new JButton("Logout");
        
        Dimension buttonSize = new Dimension(200, 80);
        approvePOButton.setPreferredSize(buttonSize);
        managePOButton.setPreferredSize(buttonSize);
        approvedPOsButton.setPreferredSize(buttonSize);
        processPaymentsButton.setPreferredSize(buttonSize);
        financialReportsButton.setPreferredSize(buttonSize);
        viewPRsButton.setPreferredSize(buttonSize);
        viewPOsButton.setPreferredSize(buttonSize);
        verifyStockButton.setPreferredSize(buttonSize);
        comprehensiveReportsButton.setPreferredSize(buttonSize);
        stockReportsButton.setPreferredSize(buttonSize);
        logoutButton.setPreferredSize(buttonSize);
        
        approvePOButton.addActionListener(e -> openPOApproval());
        managePOButton.addActionListener(e -> openPOManagement());
        approvedPOsButton.addActionListener(e -> openApprovedPOsList());
        processPaymentsButton.addActionListener(e -> openPaymentProcessing());
        financialReportsButton.addActionListener(e -> openFinancialReports());
        viewPRsButton.addActionListener(e -> openPRList());
        viewPOsButton.addActionListener(e -> openPOList());
        verifyStockButton.addActionListener(e -> openStockVerification());
        comprehensiveReportsButton.addActionListener(e -> openComprehensiveReports());
        stockReportsButton.addActionListener(e -> openStockReports());
        logoutButton.addActionListener(e -> logout());
        
        mainPanel.add(approvePOButton);
        mainPanel.add(managePOButton);
        mainPanel.add(approvedPOsButton);
        mainPanel.add(processPaymentsButton);
        mainPanel.add(financialReportsButton);
        mainPanel.add(viewPRsButton);
        mainPanel.add(viewPOsButton);
        mainPanel.add(verifyStockButton);
        mainPanel.add(comprehensiveReportsButton);
        mainPanel.add(stockReportsButton);
        mainPanel.add(logoutButton);
        
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
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName() + " (Finance Manager)");
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
    
    private void openPOApproval() {
        new POApprovalForm(this, currentUser).setVisible(true);
    }
    
    private void openPaymentProcessing() {
        new PaymentProcessingForm(this, currentUser).setVisible(true);
    }
    
    private void openFinancialReports() {
        new FinancialReportsForm(this).setVisible(true);
    }
    
    private void openPRList() {
        new PRListForm(this, currentUser).setVisible(true);
    }
    
    private void openPOList() {
        new POListForm(this, currentUser).setVisible(true);
    }
    
    private void openStockVerification() {
        new StockVerificationForm(this, currentUser).setVisible(true);
    }
    
    private void openComprehensiveReports() {
        new ComprehensiveReportsForm(this).setVisible(true);
    }
    
    private void openPOManagement() {
        new POManagementForm(this, currentUser).setVisible(true);
    }
    
    private void openApprovedPOsList() {
        new ApprovedPOListForm(this, currentUser).setVisible(true);
    }
    
    private void openStockReports() {
        new StockReportsForm(this).setVisible(true);
    }
    
    private void logout() {
        this.dispose();
        new LoginForm().setVisible(true);
    }
}