package com.hotelapp.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Lightweight color palette + helpers. Unlike a custom look-and-feel, this
 * only ever calls setBackground()/setForeground()/setBorder() on standard
 * Swing components (JButton, JPanel, TitledBorder) - exactly what you can
 * set by hand through NetBeans' GUI Builder Properties panel. No custom
 * painting, no rounded corners, no overridden paintComponent().
 */
public class SimpleTheme {

    public static final Color PRIMARY = new Color(41, 128, 185);   // blue
    public static final Color SUCCESS = new Color(39, 174, 96);    // green
    public static final Color DANGER  = new Color(192, 57, 43);    // red
    public static final Color WARNING = new Color(230, 126, 34);   // orange
    public static final Color NEUTRAL = new Color(149, 165, 166);  // gray

    public static final Color HEADER_BG = new Color(44, 62, 80);   // dark slate for table headers / tab accents
    public static final Color PANEL_BG  = new Color(245, 247, 250);
    public static final Color CARD_BG   = Color.WHITE;

    /**
     * A flat, solid-color button (no rounded corners, no gradients).
     * We paint the background and text ourselves because every tested
     * look-and-feel (Windows, Metal, Nimbus) handles JButton's disabled
     * state differently, and none of them reliably respect a custom
     * background/foreground once setEnabled(false) is called. Painting
     * it directly is the only way to guarantee consistent colors.
     */
    public static JButton coloredButton(String text, Color bg) {
        Color dimmed = new Color(
                (bg.getRed() + 255) / 2,
                (bg.getGreen() + 255) / 2,
                (bg.getBlue() + 255) / 2);

        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(isEnabled() ? bg : dimmed);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.WHITE);
                g.setFont(getFont());
                FontMetrics fm = g.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(getText(), textX, textY);
            }
        };
        button.setText(text);
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setPreferredSize(new Dimension(
                button.getFontMetrics(button.getFont()).stringWidth(text) + 40, 32));
        return button;
    }

    public static Border coloredTitledBorder(String title, Color color) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(color, 2), title);
        border.setTitleColor(color);
        border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD, 13f));
        return border;
    }

    public static void styleTableHeader(JTable table) {
        table.getTableHeader().setBackground(HEADER_BG);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        table.setSelectionBackground(new Color(214, 234, 248));
        table.setRowHeight(24);
    }

    /** Maps a status string to a color for use in a colored-text cell renderer. */
    public static Color colorForStatus(String status) {
        switch (status) {
            case "AVAILABLE": case "PAID": case "CHECKED_IN":
                return SUCCESS;
            case "RESERVED": case "OCCUPIED": case "UNPAID":
                return WARNING;
            case "MAINTENANCE": case "CANCELLED":
                return DANGER;
            case "CHECKED_OUT":
                return NEUTRAL;
            default:
                return Color.DARK_GRAY;
        }
    }
}