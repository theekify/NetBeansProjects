package com.hotelapp.ui;

import com.hotelapp.exception.ValidationException;
import com.hotelapp.model.User;
import com.hotelapp.service.AuthService;
import com.hotelapp.util.Session;
import com.hotelapp.util.SimpleTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginForm extends JFrame {

    private final AuthService authService = new AuthService();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    public LoginForm() {
        setTitle("Boutique Hotel Management System - Login");
        setSize(420, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(SimpleTheme.PANEL_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Hotel Management System");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(SimpleTheme.PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);

        JLabel subLabel = new JLabel("Staff Login");
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subLabel.setForeground(SimpleTheme.NEUTRAL);
        gbc.gridy = 1;
        mainPanel.add(subLabel, gbc);

        gbc.gridwidth = 1;

        JLabel userLabel = new JLabel("Username:");
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(userLabel, gbc);

        usernameField = new JTextField(15);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(usernameField, gbc);

        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(passLabel, gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(passwordField, gbc);
        // Allow pressing Enter in the password field to submit
        passwordField.addActionListener(this::onLogin);

        loginButton = SimpleTheme.coloredButton("Login", SimpleTheme.PRIMARY);
        loginButton.addActionListener(this::onLogin);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(loginButton, gbc);

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(SimpleTheme.DANGER);
        gbc.gridy = 5;
        mainPanel.add(statusLabel, gbc);

        add(mainPanel);
    }

    private void onLogin(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            User user = authService.login(username, password);
            Session.getInstance().setCurrentUser(user);
            statusLabel.setText(" ");

            // Open the main application shell and close the login window
            SwingUtilities.invokeLater(() -> {
                new MainFrame().setVisible(true);
                dispose();
            });

        } catch (ValidationException ex) {
            statusLabel.setText(ex.getMessage());
        } catch (RuntimeException ex) {
            // Covers DB connection failures etc. - never show raw stack traces to the user
            statusLabel.setText("Unable to connect to the system. Please try again.");
            System.err.println("Login error: " + ex.getMessage());
        }
    }
}
