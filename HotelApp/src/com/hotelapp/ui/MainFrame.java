package com.hotelapp.ui;

import com.hotelapp.util.Session;
import com.hotelapp.util.SimpleTheme;

import javax.swing.*;
import java.awt.*;

/**
 * Main application shell shown after login.
 * Each module (Dashboard, Guests, Rooms, Bookings, Billing) is added
 * as a tab here as it gets built in later steps.
 */
public class MainFrame extends JFrame {

    private final JTabbedPane tabbedPane;

    public MainFrame() {
        setTitle("Boutique Hotel Management System");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setJMenuBar(buildMenuBar());

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(tabbedPane.getFont().deriveFont(Font.BOLD, 13f));
        addColoredTab("Dashboard", new DashboardPanel(), SimpleTheme.PRIMARY);
        addColoredTab("Guests", new GuestPanel(), SimpleTheme.PRIMARY);
        addColoredTab("Room Types", new RoomTypePanel(), SimpleTheme.SUCCESS);
        addColoredTab("Rooms", new RoomPanel(), SimpleTheme.SUCCESS);
        addColoredTab("Bookings", new BookingPanel(), SimpleTheme.WARNING);
        addColoredTab("Billing", new BillingPanel(), SimpleTheme.WARNING);
        addColoredTab("Reports", new ReportsPanel(), SimpleTheme.PRIMARY);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void addColoredTab(String title, JComponent panel, Color accent) {
        tabbedPane.addTab(title, panel);
        int index = tabbedPane.getTabCount() - 1;
        tabbedPane.setBackgroundAt(index, new Color(
                accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
        tabbedPane.setForegroundAt(index, accent.darker());
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(SimpleTheme.HEADER_BG);

        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(Color.WHITE);
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Right-aligned label showing who's logged in
        menuBar.add(Box.createHorizontalGlue());
        String name = Session.getInstance().getCurrentUser() != null
                ? Session.getInstance().getCurrentUser().getFullName()
                : "Guest";
        JLabel userLabel = new JLabel("Logged in as: " + name + "  ");
        userLabel.setForeground(Color.WHITE);
        menuBar.add(userLabel);

        return menuBar;
    }

    /** Adds or replaces a tab - used by later steps to plug in each module panel. */
    public void addModuleTab(String title, JComponent panel) {
        tabbedPane.addTab(title, panel);
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            Session.getInstance().clear();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        }
    }
}
