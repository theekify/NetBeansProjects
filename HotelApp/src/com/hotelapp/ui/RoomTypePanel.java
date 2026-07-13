package com.hotelapp.ui;

import com.hotelapp.exception.ValidationException;
import com.hotelapp.model.RoomType;
import com.hotelapp.service.RoomService;
import com.hotelapp.util.SimpleTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class RoomTypePanel extends JPanel {

    private final RoomService roomService = new RoomService();

    private JTextField nameField, priceField, capacityField, descField;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, clearButton;

    public RoomTypePanel() {
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
        outer.setBorder(SimpleTheme.coloredTitledBorder("Add Room Type", SimpleTheme.SUCCESS));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(SimpleTheme.CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(15);
        priceField = new JTextField(15);
        capacityField = new JTextField(15);
        descField = new JTextField(15);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Type Name:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(nameField, gbc);

        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Base Price (Rs.):"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        form.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(capacityField, gbc);

        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Description:"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        form.add(descField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(SimpleTheme.CARD_BG);
        addButton = SimpleTheme.coloredButton("Add Room Type", SimpleTheme.SUCCESS);
        clearButton = SimpleTheme.coloredButton("Clear", SimpleTheme.NEUTRAL);
        addButton.addActionListener(e -> onAdd());
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        form.add(buttonPanel, gbc);

        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SimpleTheme.CARD_BG);
        panel.setBorder(SimpleTheme.coloredTitledBorder("Room Types", SimpleTheme.SUCCESS));

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Type Name", "Base Price", "Capacity", "Description"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        SimpleTheme.styleTableHeader(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<RoomType> types = roomService.getAllRoomTypes();
        for (RoomType rt : types) {
            tableModel.addRow(new Object[]{
                    rt.getId(), rt.getTypeName(), "Rs. " + rt.getBasePrice(), rt.getCapacity(), rt.getDescription()
            });
        }
    }

    private void clearForm() {
        nameField.setText("");
        priceField.setText("");
        capacityField.setText("");
        descField.setText("");
    }

    private void onAdd() {
        try {
            BigDecimal price;
            int capacity;
            try {
                price = new BigDecimal(priceField.getText().trim());
            } catch (NumberFormatException nfe) {
                throw new ValidationException("Base price must be a valid number.");
            }
            try {
                capacity = Integer.parseInt(capacityField.getText().trim());
            } catch (NumberFormatException nfe) {
                throw new ValidationException("Capacity must be a valid whole number.");
            }

            RoomType rt = new RoomType(nameField.getText().trim(), price, capacity, descField.getText().trim());
            roomService.addRoomType(rt);
            JOptionPane.showMessageDialog(this, "Room type added successfully.");
            clearForm();
            refreshTable();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Could not save room type. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println(ex.getMessage());
        }
    }
}
