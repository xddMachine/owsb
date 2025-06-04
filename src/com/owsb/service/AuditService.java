package com.owsb.service;

import com.owsb.domain.Permission;
import com.owsb.domain.User;
import com.owsb.util.FileUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Audit logging service for security and compliance
 */
public class AuditService {
    private static final String AUDIT_LOG_FILE = "data/audit_log.csv";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public AuditService() {
        initializeAuditLog();
    }
    
    /**
     * Initialize audit log file with headers
     */
    private void initializeAuditLog() {
        try {
            List<String> lines = FileUtils.readAllLines(AUDIT_LOG_FILE);
            if (lines.isEmpty()) {
                List<String> headers = new ArrayList<>();
                headers.add("timestamp,userId,username,role,action,permission,resource,status,details");
                FileUtils.writeAllLines(AUDIT_LOG_FILE, headers);
            }
        } catch (Exception e) {
            // Create new audit log if it doesn't exist
            try {
                List<String> headers = new ArrayList<>();
                headers.add("timestamp,userId,username,role,action,permission,resource,status,details");
                FileUtils.writeAllLines(AUDIT_LOG_FILE, headers);
            } catch (Exception ex) {
                System.err.println("Failed to initialize audit log: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Log successful permission grant
     */
    public void logPermissionGranted(User user, Permission permission) {
        logAuditEvent(user, "PERMISSION_CHECK", permission.toString(), "", "GRANTED", "Permission allowed");
    }
    
    /**
     * Log denied permission attempt
     */
    public void logPermissionDenied(User user, Permission permission, String reason) {
        String userId = user != null ? user.getUserId() : "UNKNOWN";
        String username = user != null ? user.getUsername() : "UNKNOWN";
        String role = user != null ? user.getRole().toString() : "UNKNOWN";
        
        logAuditEvent(userId, username, role, "PERMISSION_CHECK", permission.toString(), "", "DENIED", reason);
    }
    
    /**
     * Log sensitive action
     */
    public void logSensitiveAction(User user, String action, String resource) {
        logAuditEvent(user, "SENSITIVE_ACTION", action, resource, "EXECUTED", "");
    }
    
    /**
     * Log user login attempt
     */
    public void logLoginAttempt(String username, boolean success, String details) {
        logAuditEvent("", username, "", "LOGIN", "", "", success ? "SUCCESS" : "FAILED", details);
    }
    
    /**
     * Log data modification
     */
    public void logDataModification(User user, String entity, String entityId, String operation) {
        logAuditEvent(user, "DATA_MODIFICATION", operation, entity + ":" + entityId, "COMPLETED", "");
    }
    
    /**
     * Core audit logging method
     */
    private void logAuditEvent(User user, String action, String permission, String resource, String status, String details) {
        if (user != null) {
            logAuditEvent(user.getUserId(), user.getUsername(), user.getRole().toString(), 
                         action, permission, resource, status, details);
        }
    }
    
    /**
     * Core audit logging method with individual parameters
     */
    private void logAuditEvent(String userId, String username, String role, String action, 
                              String permission, String resource, String status, String details) {
        try {
            String timestamp = LocalDateTime.now().format(DATE_FORMAT);
            String logEntry = String.join(",", 
                timestamp, userId, username, role, action, permission, resource, status, details);
            
            List<String> lines = FileUtils.readAllLines(AUDIT_LOG_FILE);
            lines.add(logEntry);
            FileUtils.writeAllLines(AUDIT_LOG_FILE, lines);
            
        } catch (Exception e) {
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }
    
    /**
     * Get recent audit entries (for admin review)
     */
    public List<String> getRecentAuditEntries(int limit) {
        try {
            List<String> lines = FileUtils.readAllLines(AUDIT_LOG_FILE);
            int startIndex = Math.max(1, lines.size() - limit); // Skip header
            return lines.subList(startIndex, lines.size());
        } catch (Exception e) {
            System.err.println("Failed to read audit log: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}