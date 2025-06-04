package com.owsb.service;

import com.owsb.domain.Permission;
import com.owsb.domain.Role;
import com.owsb.domain.User;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Enhanced authorization service with granular permissions
 * Implements the authoritative RBAC specification
 */
public class AuthorizationService {
    private static final Map<Role, Set<Permission>> ROLE_PERMISSIONS = new HashMap<>();
    private AuditService auditService;
    
    static {
        initializeRolePermissions();
    }
    
    public AuthorizationService() {
        this.auditService = new AuditService();
    }
    
    /**
     * Initialize role-based permissions according to RBAC spec
     */
    private static void initializeRolePermissions() {
        // ADMIN - ALL permissions
        ROLE_PERMISSIONS.put(Role.ADMIN, EnumSet.allOf(Permission.class));
        
        // SALES_MANAGER (SM) permissions - removed PR_APPROVE and PO generation rights
        ROLE_PERMISSIONS.put(Role.SALES_MANAGER, EnumSet.of(
            Permission.ITEM_CREATE, Permission.ITEM_READ, Permission.ITEM_UPDATE, Permission.ITEM_DELETE,
            Permission.SUPPLIER_CREATE, Permission.SUPPLIER_READ, Permission.SUPPLIER_UPDATE, Permission.SUPPLIER_DELETE,
            Permission.SALESENTRY_CREATE, Permission.SALESENTRY_READ, Permission.SALESENTRY_UPDATE, Permission.SALESENTRY_DELETE,
            Permission.PR_CREATE, Permission.PR_READ, Permission.PR_UPDATE, Permission.PR_DELETE,
            Permission.PR_VIEW_ALL, Permission.PO_VIEW_ALL
        ));
        
        // PURCHASE_MANAGER (PM) permissions - added PR_APPROVE
        ROLE_PERMISSIONS.put(Role.PURCHASE_MANAGER, EnumSet.of(
            Permission.ITEM_VIEW_ALL, Permission.SUPPLIER_VIEW_ALL, Permission.PR_VIEW_ALL, Permission.PR_APPROVE,
            Permission.PO_CREATE, Permission.PO_READ, Permission.PO_UPDATE, Permission.PO_DELETE,
            Permission.PO_VIEW_ALL
        ));
        
        // INVENTORY_MANAGER (IM) permissions
        ROLE_PERMISSIONS.put(Role.INVENTORY_MANAGER, EnumSet.of(
            Permission.ITEM_VIEW_ALL, Permission.STOCK_UPDATE_FROM_PO, 
            Permission.STOCK_LEVEL_MANAGE, Permission.STOCK_REPORT_GENERATE,
            Permission.PO_VIEW_ALL
        ));
        
        // FINANCE_MANAGER (FM) permissions - added PR_APPROVE
        ROLE_PERMISSIONS.put(Role.FINANCE_MANAGER, EnumSet.of(
            Permission.PO_APPROVE, Permission.STOCK_UPDATE_VERIFY,
            Permission.SUPPLIER_PAYMENT_PROCESS, Permission.FIN_REPORT_GENERATE,
            Permission.PR_VIEW_ALL, Permission.PR_APPROVE, Permission.PO_VIEW_ALL
        ));
    }
    
    /**
     * Check if user has specific permission
     * @param user The user to check
     * @param permission The permission to verify
     * @return true if user has permission, false otherwise
     */
    public boolean hasPermission(User user, Permission permission) {
        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            auditService.logPermissionDenied(user, permission, "User inactive or null");
            return false;
        }
        
        Role userRole = user.getRole();
        Set<Permission> rolePermissions = ROLE_PERMISSIONS.get(userRole);
        
        boolean hasPermission = rolePermissions != null && rolePermissions.contains(permission);
        
        if (hasPermission) {
            auditService.logPermissionGranted(user, permission);
        } else {
            auditService.logPermissionDenied(user, permission, "Role does not have permission");
        }
        
        return hasPermission;
    }
    
    /**
     * Check multiple permissions at once
     * @param user The user to check
     * @param permissions Array of permissions to verify
     * @return true if user has ALL permissions, false otherwise
     */
    public boolean hasAllPermissions(User user, Permission... permissions) {
        for (Permission permission : permissions) {
            if (!hasPermission(user, permission)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if user has any of the specified permissions
     * @param user The user to check
     * @param permissions Array of permissions to verify
     * @return true if user has ANY permission, false otherwise
     */
    public boolean hasAnyPermission(User user, Permission... permissions) {
        for (Permission permission : permissions) {
            if (hasPermission(user, permission)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get all permissions for a role
     * @param role The role to get permissions for
     * @return Set of permissions for the role
     */
    public Set<Permission> getPermissionsForRole(Role role) {
        return ROLE_PERMISSIONS.getOrDefault(role, EnumSet.noneOf(Permission.class));
    }
    
    /**
     * Check if user is valid and active
     * @param user The user to validate
     * @return true if user is valid and active
     */
    public boolean isValidUser(User user) {
        return user != null && "ACTIVE".equals(user.getStatus());
    }
    
    /**
     * Log sensitive action with audit trail
     * @param user User performing the action
     * @param action Description of the action
     * @param resource Resource being accessed
     */
    public void logSensitiveAction(User user, String action, String resource) {
        auditService.logSensitiveAction(user, action, resource);
    }
}