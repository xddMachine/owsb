package com.owsb.domain;

/**
 * Centralized permission constants for RBAC system
 * Based on the authoritative RBAC specification
 */
public enum Permission {
    // User Management
    USER_CREATE,
    USER_READ,
    USER_UPDATE, 
    USER_DELETE,
    
    // Item Management
    ITEM_CREATE,
    ITEM_READ,
    ITEM_UPDATE,
    ITEM_DELETE,
    ITEM_VIEW_ALL,
    
    // Supplier Management
    SUPPLIER_CREATE,
    SUPPLIER_READ,
    SUPPLIER_UPDATE,
    SUPPLIER_DELETE,
    SUPPLIER_VIEW_ALL,
    
    // Sales Entry
    SALESENTRY_CREATE,
    SALESENTRY_READ,
    SALESENTRY_UPDATE,
    SALESENTRY_DELETE,
    
    // Purchase Requisition
    PR_CREATE,
    PR_READ,
    PR_UPDATE,
    PR_DELETE,
    PR_VIEW_ALL,
    PR_APPROVE,
    
    // Purchase Order
    PO_CREATE,
    PO_READ,
    PO_UPDATE,
    PO_DELETE,
    PO_VIEW_ALL,
    PO_APPROVE,
    
    // Stock Management
    STOCK_UPDATE_FROM_PO,
    STOCK_LEVEL_MANAGE,
    STOCK_REPORT_GENERATE,
    STOCK_UPDATE_VERIFY,
    
    // Payment Processing
    SUPPLIER_PAYMENT_PROCESS,
    
    // Reports
    FIN_REPORT_GENERATE,
    
    // System Administration
    SYSTEM_ADMIN
}