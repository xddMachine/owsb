package com.owsb.util;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for initializing global UI settings.
 */
public class UIUtils {

    public static final Color PRIMARY_COLOR = new Color(33, 150, 243);
    public static final Color BACKGROUND_COLOR = Color.WHITE;
    public static final Color TEXT_COLOR = Color.DARK_GRAY;
    public static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 13);

    /**
     * Configure the application look and feel.
     * Attempts to use Nimbus and falls back to the system look and feel.
     */
    public static void setLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // ignore and use default
        }
    }

    /**
     * Apply a global font to all UI components.
     *
     * @param font the font to apply
     */
    public static void setGlobalFont(Font font) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                UIManager.put(key, font);
            }
        }
    }

    /**
     * Configure common colors for UI components via UIManager.
     */
    public static void setGlobalColors() {
        UIManager.put("Button.background", PRIMARY_COLOR);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.margin", new Insets(5, 15, 5, 15));
        UIManager.put("Button.font", DEFAULT_FONT);
        UIManager.put("Button.border", BorderFactory.createLineBorder(PRIMARY_COLOR.darker()));
        UIManager.put("Button.focus", PRIMARY_COLOR.darker());

        UIManager.put("Panel.background", BACKGROUND_COLOR);
        UIManager.put("Label.foreground", TEXT_COLOR);
        UIManager.put("OptionPane.background", BACKGROUND_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
    }

    /**
     * Initialize common UI settings.
     */
    public static void initializeUI() {
        setLookAndFeel();
        setGlobalFont(DEFAULT_FONT);
        setGlobalColors();
    }
}
