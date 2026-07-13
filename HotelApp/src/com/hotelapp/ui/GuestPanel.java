package com.hotelapp.ui;

import com.hotelapp.exception.ValidationException;
import com.hotelapp.model.Guest;
import com.hotelapp.service.GuestService;
import com.hotelapp.util.SimpleTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class GuestPanel extends JPanel {

    private final GuestService guestService = new GuestService();

    private JTextField nameField, nicField, phoneField, emailField, addressField;
    private JTextField searchField;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, updateButton, deleteButton, clearButton;

    private Integer selectedGuestId = null; // null = no selection, add mode

    public GuestPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(SimpleTheme.PANEL_BG);

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        refreshTable();
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(SimpleTheme.CARD_BG);
        outer.setBorder(SimpleTheme.coloredTitledBorder("Guest Details", SimpleTheme.PRIMARY));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(SimpleTheme.CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(18);
        nicField = new JTextField(18);
        phoneField = new JTextField(18);
        emailField = new JTextField(18);
        addressField = new JTextField(18);

        addFormRow(form, gbc, 0, "Full Name:", nameField);
        addFormRow(form, gbc, 1, "NIC/Passport:", nicField);
        addFormRow(form, gbc, 2, "Phone:", phoneField);
        addFormRow(form, gbc, 3, "Email:", emailField);
        addFormRow(form, gbc, 4, "Address:", addressField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(SimpleTheme.CARD_BG);
        addButton = SimpleTheme.coloredButton("Add Guest", SimpleTheme.PRIMARY);
        updateButton = SimpleTheme.coloredButton("Update", SimpleTheme.SUCCESS);
        deleteButton = SimpleTheme.coloredButton("Delete", SimpleTheme.DANGER);
        clearButton = SimpleTheme.coloredButton("Clear", SimpleTheme.NEUTRAL);

        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);

        addButton.addActionListener(e -> onAdd());
        updateButton.addActionListener(e -> onUpdate());
        deleteButton.addActionListener(e -> onDelete());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        form.add(buttonPanel, gbc);

        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(field, gbc);
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(SimpleTheme.CARD_BG);
        panel.setBorder(SimpleTheme.coloredTitledBorder("Registered Guests", SimpleTheme.PRIMARY));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(SimpleTheme.CARD_BG);
        searchField = new JTextField(20);
        JButton searchButton = SimpleTheme.coloredButton("Search", SimpleTheme.PRIMARY);
        JButton showAllButton = SimpleTheme.coloredButton("Show All", SimpleTheme.NEUTRAL);
        searchButton.addActionListener(e -> refreshTable());
        showAllButton.addActionListener(e -> { searchField.setText(""); refreshTable(); });
        searchField.addActionListener(e -> refreshTable());
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);
        panel.add(searchPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Full Name", "NIC/Passport", "Phone", "Email", "Address"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        SimpleTheme.styleTableHeader(table);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                loadSelectedRowIntoForm();
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Guest> guests = guestService.searchGuests(searchField.getText());
        for (Guest g : guests) {
            tableModel.addRow(new Object[]{
                    g.getId(), g.getFullName(), g.getNicPassport(), g.getPhone(), g.getEmail(), g.getAddress()
            });
        }
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        selectedGuestId = (Integer) tableModel.getValueAt(row, 0);
        nameField.setText((String) tableModel.getValueAt(row, 1));
        nicField.setText((String) tableModel.getValueAt(row, 2));
        phoneField.setText((String) tableModel.getValueAt(row, 3));
        emailField.setText((String) tableModel.getValueAt(row, 4));
        addressField.setText((String) tableModel.getValueAt(row, 5));

        addButton.setEnabled(false);
        updateButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }

    private void clearForm() {
        selectedGuestId = null;
        nameField.setText("");
        nicField.setText("");
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");
        table.clearSelection();
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private Guest buildGuestFromForm() {
        Guest g = new Guest();
        if (selectedGuestId != null) g.setId(selectedGuestId);
        g.setFullName(nameField.getText().trim());
        g.setNicPassport(nicField.getText().trim());
        g.setPhone(phoneField.getText().trim());
        g.setEmail(emailField.getText().trim());
        g.setAddress(addressField.getText().trim());
        return g;
    }

    private void onAdd() {
        try {
            guestService.registerGuest(buildGuestFromForm());
            JOptionPane.showMessageDialog(this, "Guest registered successfully.");
            clearForm();
            refreshTable();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Could not save guest. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println(ex.getMessage());
        }
    }

    private void onUpdate() {
        if (selectedGuestId == null) return;
        try {
            guestService.updateGuest(buildGuestFromForm());
            JOptionPane.showMessageDialog(this, "Guest updated successfully.");
            clearForm();
            refreshTable();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Could not update guest. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println(ex.getMessage());
        }
    }

    private void onDelete() {
        if (selectedGuestId == null) return;
        int choice = JOptionPane.showConfirmDialog(this, "Delete this guest? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        try {
            boolean deleted = guestService.deleteGuest(selectedGuestId);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Guest deleted.");
            }
            clearForm();
            refreshTable();
        } catch (RuntimeException ex) {
            // Most likely a foreign key constraint (guest has bookings)
            JOptionPane.showMessageDialog(this,
                    "Could not delete guest - they may have existing bookings.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
