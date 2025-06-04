package com.owsb.service;

import com.owsb.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuthorizationService
 */
public class AuthorizationServiceTest {
    private AuthorizationService authService;
    private User adminUser;
    private User salesManagerUser;
    private User purchaseManagerUser;
    private User inventoryManagerUser;
    private User financeManagerUser;
    private User inactiveUser;
    
    @BeforeEach
    void setUp() {
        authService = new AuthorizationService();
        
        // Create test users for each role
        adminUser = new Admin("A001", "admin", "password", "Admin User", "123", "admin@test.com", "ACTIVE");
        salesManagerUser = new SalesManager("S001", "sales_mgr", "password", "Sales Manager", "123", "sales@test.com", "ACTIVE");
        purchaseManagerUser = new PurchaseManager("P001", "purchase_mgr", "password", "Purchase Manager", "123", "purchase@test.com", "ACTIVE");
        inventoryManagerUser = new InventoryManager("I001", "inventory_mgr", "password", "Inventory Manager", "123", "inventory@test.com", "ACTIVE");
        financeManagerUser = new FinanceManager("F001", "finance_mgr", "password", "Finance Manager", "123", "finance@test.com", "ACTIVE");
        inactiveUser = new SalesManager("S002", "inactive", "password", "Inactive User", "123", "inactive@test.com", "INACTIVE");
    }
    
    @Test
    void testAdminHasAllPermissions() {
        // Admin should have all permissions
        for (Permission permission : Permission.values()) {
            assertTrue(authService.hasPermission(adminUser, permission), 
                      "Admin should have permission: " + permission);
        }
    }
    
    @Test
    void testSalesManagerPermissions() {
        // Sales Manager should have these permissions
        assertTrue(authService.hasPermission(salesManagerUser, Permission.ITEM_CREATE));
        assertTrue(authService.hasPermission(salesManagerUser, Permission.ITEM_READ));
        assertTrue(authService.hasPermission(salesManagerUser, Permission.ITEM_UPDATE));
        assertTrue(authService.hasPermission(salesManagerUser, Permission.ITEM_DELETE));
        assertTrue(authService.hasPermission(salesManagerUser, Permission.SUPPLIER_CREATE));
        assertTrue(authService.hasPermission(salesManagerUser, Permission.SUPPLIER_READ));
        assertTrue(authService.hasPermission(salesManagerUser, Permission.SUPPLIER_UPDATE));
        assertTrue(authService.hasPermission(salesManagerUser, Permission.SUPPLIER_DELETE));
        assertTrue(authService.hasPermission(salesManagerUser, Permission.SALESENTRY_CREATE));
        assertTrue(authService.hasPermission(salesManagerUser, Permission.PR_CREATE));
        assertTrue(authService.hasPermission(salesManagerUser, Permission.PR_READ));
        assertTrue(authService.hasPermission(salesManagerUser, Permission.PR_UPDATE));
        assertTrue(authService.hasPermission(salesManagerUser, Permission.PR_DELETE));
        assertTrue(authService.hasPermission(salesManagerUser, Permission.PR_VIEW_ALL));
        assertTrue(authService.hasPermission(salesManagerUser, Permission.PO_VIEW_ALL));
        
        // Sales Manager should NOT have these permissions
        assertFalse(authService.hasPermission(salesManagerUser, Permission.USER_CREATE));
        assertFalse(authService.hasPermission(salesManagerUser, Permission.PO_CREATE));
        assertFalse(authService.hasPermission(salesManagerUser, Permission.PO_APPROVE));
        assertFalse(authService.hasPermission(salesManagerUser, Permission.PR_APPROVE));
        assertFalse(authService.hasPermission(salesManagerUser, Permission.STOCK_UPDATE_FROM_PO));
    }
    
    @Test
    void testPurchaseManagerPermissions() {
        // Purchase Manager should have these permissions
        assertTrue(authService.hasPermission(purchaseManagerUser, Permission.ITEM_VIEW_ALL));
        assertTrue(authService.hasPermission(purchaseManagerUser, Permission.SUPPLIER_VIEW_ALL));
        assertTrue(authService.hasPermission(purchaseManagerUser, Permission.PR_VIEW_ALL));
        assertTrue(authService.hasPermission(purchaseManagerUser, Permission.PR_APPROVE));
        assertTrue(authService.hasPermission(purchaseManagerUser, Permission.PO_CREATE));
        assertTrue(authService.hasPermission(purchaseManagerUser, Permission.PO_READ));
        assertTrue(authService.hasPermission(purchaseManagerUser, Permission.PO_UPDATE));
        assertTrue(authService.hasPermission(purchaseManagerUser, Permission.PO_DELETE));
        assertTrue(authService.hasPermission(purchaseManagerUser, Permission.PO_VIEW_ALL));
        
        // Purchase Manager should NOT have these permissions
        assertFalse(authService.hasPermission(purchaseManagerUser, Permission.ITEM_CREATE));
        assertFalse(authService.hasPermission(purchaseManagerUser, Permission.SUPPLIER_CREATE));
        assertFalse(authService.hasPermission(purchaseManagerUser, Permission.PO_APPROVE));
        assertFalse(authService.hasPermission(purchaseManagerUser, Permission.SALESENTRY_CREATE));
    }
    
    @Test
    void testInventoryManagerPermissions() {
        // Inventory Manager should have these permissions
        assertTrue(authService.hasPermission(inventoryManagerUser, Permission.ITEM_VIEW_ALL));
        assertTrue(authService.hasPermission(inventoryManagerUser, Permission.STOCK_UPDATE_FROM_PO));
        assertTrue(authService.hasPermission(inventoryManagerUser, Permission.STOCK_LEVEL_MANAGE));
        assertTrue(authService.hasPermission(inventoryManagerUser, Permission.STOCK_REPORT_GENERATE));
        assertTrue(authService.hasPermission(inventoryManagerUser, Permission.PO_VIEW_ALL));
        
        // Inventory Manager should NOT have these permissions
        assertFalse(authService.hasPermission(inventoryManagerUser, Permission.ITEM_CREATE));
        assertFalse(authService.hasPermission(inventoryManagerUser, Permission.PO_CREATE));
        assertFalse(authService.hasPermission(inventoryManagerUser, Permission.PO_APPROVE));
        assertFalse(authService.hasPermission(inventoryManagerUser, Permission.SUPPLIER_CREATE));
    }
    
    @Test
    void testFinanceManagerPermissions() {
        // Finance Manager should have these permissions
        assertTrue(authService.hasPermission(financeManagerUser, Permission.PO_APPROVE));
        assertTrue(authService.hasPermission(financeManagerUser, Permission.STOCK_UPDATE_VERIFY));
        assertTrue(authService.hasPermission(financeManagerUser, Permission.SUPPLIER_PAYMENT_PROCESS));
        assertTrue(authService.hasPermission(financeManagerUser, Permission.FIN_REPORT_GENERATE));
        assertTrue(authService.hasPermission(financeManagerUser, Permission.PR_VIEW_ALL));
        assertTrue(authService.hasPermission(financeManagerUser, Permission.PR_APPROVE));
        assertTrue(authService.hasPermission(financeManagerUser, Permission.PO_VIEW_ALL));
        
        // Finance Manager should NOT have these permissions
        assertFalse(authService.hasPermission(financeManagerUser, Permission.ITEM_CREATE));
        assertFalse(authService.hasPermission(financeManagerUser, Permission.PO_CREATE));
        assertFalse(authService.hasPermission(financeManagerUser, Permission.SUPPLIER_CREATE));
        assertFalse(authService.hasPermission(financeManagerUser, Permission.SALESENTRY_CREATE));
    }
    
    @Test
    void testInactiveUserDenied() {
        // Inactive user should be denied all permissions
        for (Permission permission : Permission.values()) {
            assertFalse(authService.hasPermission(inactiveUser, permission),
                       "Inactive user should not have permission: " + permission);
        }
    }
    
    @Test
    void testNullUserDenied() {
        // Null user should be denied all permissions
        for (Permission permission : Permission.values()) {
            assertFalse(authService.hasPermission(null, permission),
                       "Null user should not have permission: " + permission);
        }
    }
    
    @Test
    void testHasAllPermissions() {
        // Admin should have all permissions
        assertTrue(authService.hasAllPermissions(adminUser, 
                  Permission.ITEM_CREATE, Permission.USER_CREATE, Permission.PO_APPROVE));
        
        // Sales Manager should have some but not all
        assertTrue(authService.hasAllPermissions(salesManagerUser, 
                  Permission.ITEM_CREATE, Permission.PR_CREATE));
        assertFalse(authService.hasAllPermissions(salesManagerUser, 
                   Permission.ITEM_CREATE, Permission.PO_APPROVE));
    }
    
    @Test
    void testHasAnyPermission() {
        // Sales Manager should have any of these
        assertTrue(authService.hasAnyPermission(salesManagerUser, 
                  Permission.ITEM_CREATE, Permission.PO_APPROVE));
        
        // But should not have any of these
        assertFalse(authService.hasAnyPermission(salesManagerUser, 
                   Permission.USER_CREATE, Permission.PO_APPROVE));
    }
    
    @Test
    void testIsValidUser() {
        assertTrue(authService.isValidUser(adminUser));
        assertTrue(authService.isValidUser(salesManagerUser));
        assertFalse(authService.isValidUser(inactiveUser));
        assertFalse(authService.isValidUser(null));
    }
    
    @Test
    void testGetPermissionsForRole() {
        var adminPermissions = authService.getPermissionsForRole(Role.ADMIN);
        assertEquals(Permission.values().length, adminPermissions.size());
        
        var salesPermissions = authService.getPermissionsForRole(Role.SALES_MANAGER);
        assertTrue(salesPermissions.contains(Permission.ITEM_CREATE));
        assertTrue(salesPermissions.contains(Permission.PR_CREATE));
        assertFalse(salesPermissions.contains(Permission.PO_APPROVE));
        assertFalse(salesPermissions.contains(Permission.USER_CREATE));
    }
}