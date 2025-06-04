package com.owsb.ui;

import com.owsb.domain.*;
import com.owsb.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class POSupplierModificationDialog extends JDialog {
    private PurchaseOrder po;
    private Runnable onUpdateCallback;
    
    private JComboBox<String> supplierCombo;
    private JTable itemsTable;
    private JTable pricesTable;
    private JTextArea notesArea;
    private JLabel totalLabel;
    private JLabel statusLabel;
    
    private DefaultTableModel itemsTableModel;
    private DefaultTableModel pricesTableModel;
    private POService poService;
    private SupplierService supplierService;
    private SupplierItemService supplierItemService;
    
    public POSupplierModificationDialog(JDialog parent, PurchaseOrder po, Runnable callback) {
        super(parent, "Modify Supplier - " + po.getPoId(), true);
        this.po = po;
        this.onUpdateCallback = callback;
        
        initializeServices();
        initializeComponents();
        loadSuppliers();
        loadPOItems();
    }
    
    private void initializeServices() {
        this.poService = new POService();
        this.supplierService = new SupplierService();
        this.supplierItemService = new SupplierItemService();
    }
    
    private void initializeComponents() {
        setSize(800, 700);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBorder(BorderFactory.createTitledBorder("Purchase Order Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        headerPanel.add(new JLabel("PO ID:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(po.getPoId()), gbc);
        
        gbc.gridx = 2;
        headerPanel.add(new JLabel("Current Supplier:"), gbc);
        gbc.gridx = 3;
        headerPanel.add(new JLabel(po.getSupplierName()), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        headerPanel.add(new JLabel("Order Date:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(po.getOrderDate().toString()), gbc);
        
        gbc.gridx = 2;
        headerPanel.add(new JLabel("Current Total:"), gbc);
        gbc.gridx = 3;
        totalLabel = new JLabel(String.format("₹%.2f", po.getTotalAmount()));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
        totalLabel.setForeground(new Color(0, 100, 0));
        headerPanel.add(totalLabel, gbc);
        
        // Supplier selection panel
        JPanel supplierPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        supplierPanel.setBorder(BorderFactory.createTitledBorder("Select New Supplier"));
        supplierPanel.add(new JLabel("New Supplier:"));
        supplierCombo = new JComboBox<>();
        supplierCombo.addActionListener(e -> updatePricesForSelectedSupplier());
        supplierPanel.add(supplierCombo);
        
        // Items table
        String[] itemColumns = {"Item ID", "Item Name", "Quantity", "Current Unit Price", "Current Total"};
        itemsTableModel = new DefaultTableModel(itemColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemsTable = new JTable(itemsTableModel);
        JScrollPane itemsScrollPane = new JScrollPane(itemsTable);
        itemsScrollPane.setBorder(BorderFactory.createTitledBorder("PO Items"));
        itemsScrollPane.setPreferredSize(new Dimension(750, 200));
        
        // Prices table for new supplier
        String[] priceColumns = {"Item ID", "Item Name", "New Unit Price", "New Total", "Price Difference"};
        pricesTableModel = new DefaultTableModel(priceColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        pricesTable = new JTable(pricesTableModel);
        JScrollPane pricesScrollPane = new JScrollPane(pricesTable);
        pricesScrollPane.setBorder(BorderFactory.createTitledBorder("New Supplier Prices"));
        pricesScrollPane.setPreferredSize(new Dimension(750, 200));
        
        // Notes panel
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setBorder(BorderFactory.createTitledBorder("Modification Notes"));
        notesArea = new JTextArea(3, 50);
        notesArea.setText("Supplier modified by Finance Manager");
        JScrollPane notesScrollPane = new JScrollPane(notesArea);
        notesPanel.add(notesScrollPane);
        
        // Status panel
        statusLabel = new JLabel("Select a supplier to see price comparison", SwingConstants.CENTER);
        statusLabel.setForeground(Color.BLUE);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton modifyButton = new JButton("✅ Modify Supplier");
        JButton cancelButton = new JButton("❌ Cancel");
        
        modifyButton.setBackground(new Color(34, 139, 34));
        modifyButton.setForeground(Color.WHITE);
        modifyButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        cancelButton.setBackground(new Color(220, 20, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        modifyButton.addActionListener(e -> modifySupplier());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(modifyButton);
        buttonPanel.add(cancelButton);
        
        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(itemsScrollPane, BorderLayout.NORTH);
        centerPanel.add(pricesScrollPane, BorderLayout.CENTER);
        centerPanel.add(notesPanel, BorderLayout.SOUTH);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(supplierPanel, BorderLayout.CENTER);
        topPanel.add(statusLabel, BorderLayout.SOUTH);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void loadSuppliers() {
        supplierCombo.removeAllItems();
        supplierCombo.addItem("-- Select New Supplier --");
        
        try {
            List<Supplier> suppliers = supplierService.listSuppliers();
            for (Supplier supplier : suppliers) {
                // Don't show current supplier
                if (!supplier.getSupplierId().equals(po.getSupplierId())) {
                    supplierCombo.addItem(supplier.getSupplierName() + " (" + supplier.getSupplierId() + ")");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading suppliers: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadPOItems() {
        itemsTableModel.setRowCount(0);
        
        try {
            List<PurchaseOrderLine> poLines = poService.getPOLines(po.getPoId());
            for (PurchaseOrderLine line : poLines) {
                Object[] rowData = {
                    line.getItemId(),
                    line.getItemName(),
                    line.getQuantity(),
                    String.format("₹%.2f", line.getUnitPrice()),
                    String.format("₹%.2f", line.getLineTotal())
                };
                itemsTableModel.addRow(rowData);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading PO items: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updatePricesForSelectedSupplier() {
        int selectedIndex = supplierCombo.getSelectedIndex();
        if (selectedIndex <= 0) {
            pricesTableModel.setRowCount(0);
            statusLabel.setText("Select a supplier to see price comparison");
            return;
        }
        
        try {
            String selectedItem = (String) supplierCombo.getSelectedItem();
            String supplierId = selectedItem.substring(selectedItem.lastIndexOf("(") + 1, selectedItem.lastIndexOf(")"));
            
            pricesTableModel.setRowCount(0);
            double newTotal = 0.0;
            double currentTotal = po.getTotalAmount();
            boolean canSupplyAll = true;
            
            List<PurchaseOrderLine> poLines = poService.getPOLines(po.getPoId());
            
            for (PurchaseOrderLine line : poLines) {
                // Get new supplier's price for this item
                Double newPrice = supplierItemService.getSupplierItemPrice(supplierId, line.getItemId());
                
                if (newPrice != null) {
                    double newLineTotal = line.getQuantity() * newPrice;
                    double priceDifference = newLineTotal - line.getLineTotal();
                    newTotal += newLineTotal;
                    
                    Object[] rowData = {
                        line.getItemId(),
                        line.getItemName(),
                        String.format("₹%.2f", newPrice),
                        String.format("₹%.2f", newLineTotal),
                        String.format("%s₹%.2f", (priceDifference >= 0 ? "+" : ""), priceDifference)
                    };
                    pricesTableModel.addRow(rowData);
                } else {
                    canSupplyAll = false;
                    Object[] rowData = {
                        line.getItemId(),
                        line.getItemName(),
                        "N/A",
                        "N/A",
                        "Not Available"
                    };
                    pricesTableModel.addRow(rowData);
                }
            }
            
            if (canSupplyAll) {
                double totalDifference = newTotal - currentTotal;
                statusLabel.setText(String.format("New Total: ₹%.2f | Difference: %s₹%.2f", 
                    newTotal, (totalDifference >= 0 ? "+" : ""), totalDifference));
                
                if (totalDifference < 0) {
                    statusLabel.setForeground(new Color(34, 139, 34)); // Green for savings
                } else if (totalDifference > 0) {
                    statusLabel.setForeground(new Color(220, 20, 60)); // Red for increase
                } else {
                    statusLabel.setForeground(Color.BLUE); // Blue for same price
                }
            } else {
                statusLabel.setText("⚠️ Selected supplier cannot provide all items");
                statusLabel.setForeground(new Color(255, 140, 0)); // Orange for warning
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating prices: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void modifySupplier() {
        int selectedIndex = supplierCombo.getSelectedIndex();
        if (selectedIndex <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a new supplier.", 
                                        "No Supplier Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String selectedItem = (String) supplierCombo.getSelectedItem();
            String newSupplierId = selectedItem.substring(selectedItem.lastIndexOf("(") + 1, selectedItem.lastIndexOf(")"));
            String newSupplierName = selectedItem.substring(0, selectedItem.lastIndexOf("(")).trim();
            
            // Verify supplier can supply all items
            List<PurchaseOrderLine> poLines = poService.getPOLines(po.getPoId());
            for (PurchaseOrderLine line : poLines) {
                Double price = supplierItemService.getSupplierItemPrice(newSupplierId, line.getItemId());
                if (price == null) {
                    JOptionPane.showMessageDialog(this, 
                        "Cannot modify supplier: " + newSupplierName + " does not supply item " + line.getItemName(), 
                        "Supplier Cannot Supply All Items", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Calculate new total
            double newTotal = 0.0;
            for (PurchaseOrderLine line : poLines) {
                Double newPrice = supplierItemService.getSupplierItemPrice(newSupplierId, line.getItemId());
                newTotal += line.getQuantity() * newPrice;
            }
            
            // Confirm modification
            int result = JOptionPane.showConfirmDialog(this,
                String.format("Modify supplier for PO %s?\n\nFrom: %s\nTo: %s\n\nOld Total: ₹%.2f\nNew Total: ₹%.2f\nDifference: %s₹%.2f",
                    po.getPoId(), po.getSupplierName(), newSupplierName, 
                    po.getTotalAmount(), newTotal, 
                    (newTotal - po.getTotalAmount() >= 0 ? "+" : ""), (newTotal - po.getTotalAmount())),
                "Confirm Supplier Modification",
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                // Update PO with new supplier
                po.setSupplierId(newSupplierId);
                po.setSupplierName(newSupplierName);
                po.setTotalAmount(newTotal);
                po.setNotes(notesArea.getText().trim());
                
                // Update PO lines with new prices
                for (PurchaseOrderLine line : poLines) {
                    Double newPrice = supplierItemService.getSupplierItemPrice(newSupplierId, line.getItemId());
                    line.setUnitPrice(newPrice);
                }
                
                if (poService.updatePO(po)) {
                    JOptionPane.showMessageDialog(this,
                        "✅ Supplier modified successfully!\n\nPO ID: " + po.getPoId() + 
                        "\nNew Supplier: " + newSupplierName + 
                        "\nNew Total: ₹" + String.format("%.2f", newTotal),
                        "Modification Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    if (onUpdateCallback != null) {
                        onUpdateCallback.run();
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "❌ Error updating Purchase Order.",
                        "Modification Failed",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error modifying supplier: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}