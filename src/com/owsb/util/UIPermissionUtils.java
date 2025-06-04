package com.owsb.util;

import com.owsb.domain.Permission;
import com.owsb.domain.User;
import com.owsb.service.AuthorizationService;
import javax.swing.*;
import java.awt.*;

/**
 * Utility class for enforcing permissions in UI components
 */
public class UIPermissionUtils {
    private static AuthorizationService authService = new AuthorizationService();
    
    /**
     * Enable/disable button based on user permission with consistent styling
     * @param button The button to control
     * @param user Current user
     * @param permission Required permission
     */
    public static void setButtonPermission(JButton button, User user, Permission permission) {
        boolean hasPermission = authService.hasPermission(user, permission);
        
        // Apply consistent styling to all buttons
        applyConsistentButtonStyle(button);
        
        button.setEnabled(hasPermission);
        
        if (!hasPermission) {
            button.setToolTipText("Access denied - insufficient privileges");
            // Keep consistent colors even when disabled
            button.setBackground(Color.LIGHT_GRAY);
            button.setForeground(Color.DARK_GRAY);
        } else {
            button.setToolTipText(null);
        }
    }
    
    /**
     * Enable/disable menu item based on user permission with consistent styling
     * @param menuItem The menu item to control
     * @param user Current user
     * @param permission Required permission
     */
    public static void setMenuItemPermission(JMenuItem menuItem, User user, Permission permission) {
        boolean hasPermission = authService.hasPermission(user, permission);
        menuItem.setEnabled(hasPermission);
        
        if (!hasPermission) {
            menuItem.setToolTipText("Access denied - insufficient privileges");
            menuItem.setForeground(Color.DARK_GRAY);
        } else {
            menuItem.setForeground(Color.BLACK);
        }
    }
    
    /**
     * Show/hide component based on user permission
     * @param component The component to control
     * @param user Current user
     * @param permission Required permission
     */
    public static void setComponentVisibility(JComponent component, User user, Permission permission) {
        boolean hasPermission = authService.hasPermission(user, permission);
        component.setVisible(hasPermission);
    }
    
    /**
     * Check permission and show error if denied
     * @param user Current user
     * @param permission Required permission
     * @param parentComponent Parent component for error dialog
     * @param actionDescription Description of the action being attempted
     * @return true if permission granted, false if denied
     */
    public static boolean checkPermissionWithDialog(User user, Permission permission, 
                                                   Component parentComponent, String actionDescription) {
        if (authService.hasPermission(user, permission)) {
            return true;
        } else {
            JOptionPane.showMessageDialog(parentComponent,
                "Access Denied: You do not have permission to " + actionDescription + ".",
                "Insufficient Privileges",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Create a permission-aware action listener wrapper
     * @param user Current user
     * @param permission Required permission
     * @param action The action to execute if permission is granted
     * @param parentComponent Parent component for error dialogs
     * @param actionDescription Description of the action
     * @return ActionListener that checks permissions before executing
     */
    public static java.awt.event.ActionListener createPermissionAwareAction(
            User user, Permission permission, Runnable action, 
            Component parentComponent, String actionDescription) {
        return e -> {
            if (checkPermissionWithDialog(user, permission, parentComponent, actionDescription)) {
                authService.logSensitiveAction(user, actionDescription, "UI_ACTION");
                action.run();
            }
        };
    }
    
    /**
     * Apply role-based styling to component while maintaining consistency
     * @param component Component to style
     * @param user Current user
     * @param permission Required permission
     */
    public static void applyPermissionStyling(JComponent component, User user, Permission permission) {
        boolean hasPermission = authService.hasPermission(user, permission);
        
        if (component instanceof JButton) {
            applyConsistentButtonStyle((JButton) component);
        }
        
        if (!hasPermission) {
            component.setEnabled(false);
            component.setToolTipText("Access denied - insufficient privileges");
            
            if (component instanceof JButton) {
                // Keep consistent styling even when disabled
                component.setBackground(Color.LIGHT_GRAY);
                component.setForeground(Color.DARK_GRAY);
            }
        } else {
            component.setToolTipText(null);
        }
    }
    
    /**
     * Create permission-based component with automatic enforcement
     * @param text Button text
     * @param user Current user
     * @param permission Required permission
     * @param action Action to execute
     * @param parentComponent Parent for error dialogs
     * @return Permission-enforced button
     */
    public static JButton createPermissionButton(String text, User user, Permission permission, 
                                                Runnable action, Component parentComponent) {
        JButton button = new JButton(text);
        setButtonPermission(button, user, permission);
        
        button.addActionListener(createPermissionAwareAction(user, permission, action, 
                                                            parentComponent, text.toLowerCase()));
        
        return button;
    }
    
    /**
     * Apply consistent styling to all buttons
     * @param button The button to style
     */
    public static void applyConsistentButtonStyle(JButton button) {
        // Set consistent colors
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        
        // Set consistent border and appearance
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        
        // Set consistent font
        Font currentFont = button.getFont();
        button.setFont(new Font(currentFont.getName(), Font.PLAIN, currentFont.getSize()));
        
        // Ensure button respects background color
        button.setContentAreaFilled(true);
    }
    
    /**
     * Create a consistently styled button
     * @param text Button text
     * @return Styled button
     */
    public static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        applyConsistentButtonStyle(button);
        return button;
    }
    
    /**
     * Apply consistent styling to all buttons in a container
     * @param container The container with buttons
     */
    public static void styleAllButtonsInContainer(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton) {
                applyConsistentButtonStyle((JButton) component);
            } else if (component instanceof Container) {
                styleAllButtonsInContainer((Container) component);
            }
        }
    }
}