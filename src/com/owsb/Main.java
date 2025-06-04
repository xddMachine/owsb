package com.owsb;

import com.owsb.ui.LoginForm;
import com.owsb.util.FileUtils;
import com.owsb.util.UIUtils;
import com.owsb.service.DataInitializationService;
import com.owsb.service.SampleDataService;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        FileUtils.initializeDataFiles();
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // Initialize sample data after GUI thread starts
                    DataInitializationService dataService = new DataInitializationService();
                    dataService.initializeSampleData();
                    
                    // Initialize supplier-item relationships
                    SampleDataService sampleDataService = new SampleDataService();
                    sampleDataService.initializeSampleData();

                    // Apply global UI settings before showing the login form
                    UIUtils.initializeUI();

                    new LoginForm().setVisible(true);
                } catch (Exception e) {
                    System.err.println("Error starting application: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}