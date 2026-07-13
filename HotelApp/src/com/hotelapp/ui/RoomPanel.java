package com.hotelapp.ui;

import com.hotelapp.exception.ValidationException;
import com.hotelapp.model.Room;
import com.hotelapp.model.RoomType;
import com.hotelapp.service.RoomService;
import com.hotelapp.util.SimpleTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RoomPanel extends JPanel {

    private final RoomService roomService = new RoomService();

    private JTextField roomNumberField, floorField;
    private JComboBox<RoomType> roomTypeCombo;
    private JComboBox<Room.Status> statusCombo;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, updateButton, deleteButton, clearButton;

    private Integer selectedRoomId = null;

    public RoomPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(SimpleTheme.PANEL_BG);

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        refreshRoomTypes();
        refreshTable();
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(SimpleTheme.CARD_BG);
        outer.setBorder(SimpleTheme.coloredTitledBorder("Room Details", SimpleTheme.PRIMARY));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(SimpleTheme.CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        roomNumberField = new JTextField(12);
        floorField = new JTextField(12);
        roomTypeCombo = new JComboBox<>();
        statusCombo = new JComboBox<>(Room.Status.values());

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Room Number:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(roomNumberField, gbc);

        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Floor:"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        form.add(floorField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Room Type:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(roomTypeCombo, gbc);

        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Status:"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        form.add(statusCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(SimpleTheme.CARD_BG);
        addButton = SimpleTheme.coloredButton("Add Room", SimpleTheme.PRIMARY);
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

        JButton refreshTypesButton = SimpleTheme.coloredButton("Refresh Room Types", SimpleTheme.WARNING);
        refreshTypesButton.addActionListener(e -> refreshRoomTypes());
        buttonPanel.add(refreshTypesButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        form.add(buttonPanel, gbc);

        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SimpleTheme.CARD_BG);
        panel.setBorder(SimpleTheme.coloredTitledBorder("Rooms", SimpleTheme.PRIMARY));

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Room No.", "Type", "Floor", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        SimpleTheme.styleTableHeader(table);
        table.getColumnModel().getColumn(4).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                c.setForeground(SimpleTheme.colorForStatus(String.valueOf(value)));
                setFont(getFont().deriveFont(Font.BOLD));
                return c;
            }
        });
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                loadSelectedRowIntoForm();
            }
        });
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshRoomTypes() {
        RoomType selected = (RoomType) roomTypeCombo.getSelectedItem();
        roomTypeCombo.removeAllItems();
        List<RoomType> types = roomService.getAllRoomTypes();
        for (RoomType rt : types) {
            roomTypeCombo.addItem(rt);
        }
        if (selected != null) roomTypeCombo.setSelectedItem(selected);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Room> rooms = roomService.getAllRooms();
        for (Room r : rooms) {
            tableModel.addRow(new Object[]{
                    r.getId(), r.getRoomNumber(),
                    r.getRoomType() != null ? r.getRoomType().getTypeName() : "",
                    r.getFloorNumber(), r.getStatus()
            });
        }
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        selectedRoomId = (Integer) tableModel.getValueAt(row, 0);
        roomNumberField.setText((String) tableModel.getValueAt(row, 1));

        String typeName = (String) tableModel.getValueAt(row, 2);
        for (int i = 0; i < roomTypeCombo.getItemCount(); i++) {
            if (roomTypeCombo.getItemAt(i).getTypeName().equals(typeName)) {
                roomTypeCombo.setSelectedIndex(i);
                break;
            }
        }

        Object floor = tableModel.getValueAt(row, 3);
        floorField.setText(floor != null ? floor.toString() : "");

        statusCombo.setSelectedItem(tableModel.getValueAt(row, 4));

        addButton.setEnabled(false);
        updateButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }

    private void clearForm() {
        selectedRoomId = null;
        roomNumberField.setText("");
        floorField.setText("");
        if (roomTypeCombo.getItemCount() > 0) roomTypeCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        table.clearSelection();
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private Room buildRoomFromForm() throws ValidationException {
        RoomType selectedType = (RoomType) roomTypeCombo.getSelectedItem();
        if (selectedType == null) {
            throw new ValidationException("Please add a Room Type first (see Room Types tab).");
        }

        Room room = new Room();
        if (selectedRoomId != null) room.setId(selectedRoomId);
        room.setRoomNumber(roomNumberField.getText().trim());
        room.setRoomTypeId(selectedType.getId());

        String floorText = floorField.getText().trim();
        if (!floorText.isEmpty()) {
            try {
                room.setFloorNumber(Integer.parseInt(floorText));
            } catch (NumberFormatException e) {
                throw new ValidationException("Floor must be a whole number.");
            }
        }
        room.setStatus((Room.Status) statusCombo.getSelectedItem());
        return room;
    }

    private void onAdd() {
        try {
            roomService.addRoom(buildRoomFromForm());
            JOptionPane.showMessageDialog(this, "Room added successfully.");
            clearForm();
            refreshTable();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Could not save room. Room number may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println(ex.getMessage());
        }
    }

    private void onUpdate() {
        if (selectedRoomId == null) return;
        try {
            roomService.updateRoom(buildRoomFromForm());
            JOptionPane.showMessageDialog(this, "Room updated successfully.");
            clearForm();
            refreshTable();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Could not update room. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println(ex.getMessage());
        }
    }

    private void onDelete() {
        if (selectedRoomId == null) return;
        int choice = JOptionPane.showConfirmDialog(this, "Delete this room? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        try {
            roomService.deleteRoom(selectedRoomId);
            JOptionPane.showMessageDialog(this, "Room deleted.");
            clearForm();
            refreshTable();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not delete room - it may have existing bookings.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
