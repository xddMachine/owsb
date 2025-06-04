package com.owsb.ui;

import com.owsb.domain.User;
import com.owsb.service.AuthService;
import com.owsb.util.FileUtils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton exitButton;
    private AuthService authService;
    
    public LoginForm() {
        initializeComponents();
        this.authService = new AuthService();
        FileUtils.initializeDataFiles();
    }
    
    private void initializeComponents() {
        setTitle("OWSB - Purchase Order Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel titleLabel = new JLabel("Purchase Order Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 30, 20);
        mainPanel.add(titleLabel, gbc);
        
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 20, 5, 10);
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(usernameLabel, gbc);
        
        usernameField = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 5, 20);
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(usernameField, gbc);
        
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.insets = new Insets(5, 20, 10, 10);
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(passwordLabel, gbc);
        
        passwordField = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.insets = new Insets(5, 10, 10, 20);
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(passwordField, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        loginButton = new JButton("Login");
        exitButton = new JButton("Exit");
        
        loginButton.setPreferredSize(new Dimension(80, 30));
        exitButton.setPreferredSize(new Dimension(80, 30));
        
        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 20, 20, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel);
        
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", 
                                        "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        User user = authService.login(username, password);
        
        if (user != null) {
            this.dispose();
            openDashboard(user);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", 
                                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            usernameField.requestFocus();
        }
    }
    
    private void openDashboard(User user) {
        switch (user.getRole()) {
            case ADMIN:
                new AdminDashboard(user).setVisible(true);
                break;
            case SALES_MANAGER:
                new SalesManagerDashboard(user).setVisible(true);
                break;
            case PURCHASE_MANAGER:
                new PurchaseManagerDashboard(user).setVisible(true);
                break;
            case INVENTORY_MANAGER:
                new InventoryManagerDashboard(user).setVisible(true);
                break;
            case FINANCE_MANAGER:
                new FinanceManagerDashboard(user).setVisible(true);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Unknown user role.", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}