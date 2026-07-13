package com.hotelapp.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Centralized color palette, fonts, and styling helpers so every screen
 * looks consistent instead of default gray Swing. Applied by every UI
 * class rather than each panel inventing its own colors.
 */
public class UITheme {

    // Palette
    public static final Color SIDEBAR_BG = new Color(0x1E2A3A);
    public static final Color SIDEBAR_HOVER = new Color(0x2C3E50);
    public static final Color SIDEBAR_ACTIVE = new Color(0x16A085);
    public static final Color SIDEBAR_TEXT = new Color(0xECF0F1);
    public static final Color SIDEBAR_TEXT_MUTED = new Color(0x95A5A6);

    public static final Color BG = new Color(0xF4F6F8);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER = new Color(0xE0E4E8);

    public static final Color PRIMARY = new Color(0x2980B9);
    public static final Color PRIMARY_DARK = new Color(0x21618C);
    public static final Color ACCENT_TEAL = new Color(0x16A085);
    public static final Color SUCCESS = new Color(0x27AE60);
    public static final Color WARNING = new Color(0xF39C12);
    public static final Color DANGER = new Color(0xE74C3C);
    public static final Color DANGER_DARK = new Color(0xC0392B);

    public static final Color TEXT_DARK = new Color(0x2C3E50);
    public static final Color TEXT_MUTED = new Color(0x7F8C8D);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_STAT = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    // ---------------------------------------------------------------
    // Button styling
    // ---------------------------------------------------------------

    public static JButton primaryButton(String text) {
        return styledButton(text, PRIMARY, Color.WHITE, PRIMARY_DARK);
    }

    public static JButton successButton(String text) {
        return styledButton(text, SUCCESS, Color.WHITE, new Color(0x1E8449));
    }

    public static JButton dangerButton(String text) {
        return styledButton(text, DANGER, Color.WHITE, DANGER_DARK);
    }

    public static JButton neutralButton(String text) {
        return styledButton(text, new Color(0xECF0F1), TEXT_DARK, new Color(0xD5DBDB));
    }

    private static JButton styledButton(String text, Color bg, Color fg, Color hoverBg) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hoverBg : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BODY_BOLD);
        button.setForeground(fg);
        button.setBorder(new EmptyBorder(9, 18, 9, 18));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // ---------------------------------------------------------------
    // Text field styling
    // ---------------------------------------------------------------

    public static void styleTextField(javax.swing.text.JTextComponent field) {
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(6, 8, 6, 8)));
    }

    // ---------------------------------------------------------------
    // Table styling
    // ---------------------------------------------------------------

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xD6EAF8));
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(SIDEBAR_BG);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 34));
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF7F9FA));
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }

    // ---------------------------------------------------------------
    // Layout helpers
    // ---------------------------------------------------------------

    /** A white rounded "card" container used throughout the app for forms/sections. */
    public static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)));
        return panel;
    }

    public static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SECTION);
        label.setForeground(TEXT_DARK);
        return label;
    }

    public static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_MUTED);
        return label;
    }

    /** Small colored pill used for status values (e.g. RESERVED, PAID) in tables/lists. */
    public static JLabel statusBadge(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(color);
        label.setForeground(Color.WHITE);
        label.setFont(FONT_SMALL);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(new EmptyBorder(3, 10, 3, 10));
        return label;
    }

    public static Color colorForBookingStatus(String status) {
        switch (status) {
            case "RESERVED": return WARNING;
            case "CHECKED_IN": return ACCENT_TEAL;
            case "CHECKED_OUT": return TEXT_MUTED;
            case "CANCELLED": return DANGER;
            default: return TEXT_MUTED;
        }
    }

    public static Color colorForRoomStatus(String status) {
        switch (status) {
            case "AVAILABLE": return SUCCESS;
            case "OCCUPIED": return WARNING;
            case "MAINTENANCE": return DANGER;
            default: return TEXT_MUTED;
        }
    }

    public static Color colorForPaymentStatus(String status) {
        return "PAID".equals(status) ? SUCCESS : WARNING;
    }
}
