package com.hotelapp.ui;

import com.hotelapp.exception.RoomNotAvailableException;
import com.hotelapp.exception.ValidationException;
import com.hotelapp.model.Booking;
import com.hotelapp.model.Guest;
import com.hotelapp.model.Room;
import com.hotelapp.service.BookingService;
import com.hotelapp.service.GuestService;
import com.hotelapp.service.RoomService;
import com.hotelapp.util.SimpleTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * The main Transaction UI: create a booking, then progress it through
 * RESERVED -> CHECKED_IN -> CHECKED_OUT (or CANCELLED).
 */
public class BookingPanel extends JPanel {

    private final BookingService bookingService = new BookingService();
    private final GuestService guestService = new GuestService();
    private final RoomService roomService = new RoomService();

    private JComboBox<Guest> guestCombo;
    private JSpinner checkInSpinner, checkOutSpinner;
    private JComboBox<Room> availableRoomCombo;
    private JTextField numGuestsField;
    private JButton searchRoomsButton, bookButton;

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton checkInButton, checkOutButton, cancelButton;
    private Integer selectedBookingId = null;

    public BookingPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(SimpleTheme.PANEL_BG);

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        refreshGuests();
        refreshTable();
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(SimpleTheme.CARD_BG);
        outer.setBorder(SimpleTheme.coloredTitledBorder("New Booking", SimpleTheme.PRIMARY));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(SimpleTheme.CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        guestCombo = new JComboBox<>();

        Date today = new Date();
        Date tomorrow = new Date(today.getTime() + 24L * 60 * 60 * 1000);
        checkInSpinner = new JSpinner(new SpinnerDateModel(today, null, null, java.util.Calendar.DAY_OF_MONTH));
        checkOutSpinner = new JSpinner(new SpinnerDateModel(tomorrow, null, null, java.util.Calendar.DAY_OF_MONTH));
        checkInSpinner.setEditor(new JSpinner.DateEditor(checkInSpinner, "yyyy-MM-dd"));
        checkOutSpinner.setEditor(new JSpinner.DateEditor(checkOutSpinner, "yyyy-MM-dd"));

        numGuestsField = new JTextField("1", 5);
        availableRoomCombo = new JComboBox<>();

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Guest:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(guestCombo, gbc);

        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("No. of Guests:"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        form.add(numGuestsField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Check-in Date:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(checkInSpinner, gbc);

        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Check-out Date:"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        form.add(checkOutSpinner, gbc);

        searchRoomsButton = SimpleTheme.coloredButton("Search Available Rooms", SimpleTheme.PRIMARY);
        searchRoomsButton.addActionListener(e -> searchAvailableRooms());
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST;
        form.add(searchRoomsButton, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Available Room:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.WEST;
        form.add(availableRoomCombo, gbc);

        bookButton = SimpleTheme.coloredButton("Create Booking", SimpleTheme.SUCCESS);
        bookButton.addActionListener(e -> onCreateBooking());
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        form.add(bookButton, gbc);

        JButton refreshGuestsButton = SimpleTheme.coloredButton("Refresh Guests", SimpleTheme.NEUTRAL);
        refreshGuestsButton.addActionListener(e -> refreshGuests());
        gbc.gridx = 2; gbc.gridwidth = 2;
        form.add(refreshGuestsButton, gbc);

        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(SimpleTheme.CARD_BG);
        panel.setBorder(SimpleTheme.coloredTitledBorder("All Bookings", SimpleTheme.PRIMARY));

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Guest", "Room", "Check-in", "Check-out", "Guests", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        SimpleTheme.styleTableHeader(table);
        table.getColumnModel().getColumn(6).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
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
                onRowSelected();
            }
        });
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.setBackground(SimpleTheme.CARD_BG);
        checkInButton = SimpleTheme.coloredButton("Check In", SimpleTheme.SUCCESS);
        checkOutButton = SimpleTheme.coloredButton("Check Out", SimpleTheme.PRIMARY);
        cancelButton = SimpleTheme.coloredButton("Cancel Booking", SimpleTheme.DANGER);
        checkInButton.setEnabled(false);
        checkOutButton.setEnabled(false);
        cancelButton.setEnabled(false);

        checkInButton.addActionListener(e -> onCheckIn());
        checkOutButton.addActionListener(e -> onCheckOut());
        cancelButton.addActionListener(e -> onCancel());

        actionPanel.add(checkInButton);
        actionPanel.add(checkOutButton);
        actionPanel.add(cancelButton);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ---------------------------------------------------------------
    // Data loading
    // ---------------------------------------------------------------

    private void refreshGuests() {
        Guest selected = (Guest) guestCombo.getSelectedItem();
        guestCombo.removeAllItems();
        List<Guest> guests = guestService.getAllGuests();
        for (Guest g : guests) guestCombo.addItem(g);
        if (selected != null) guestCombo.setSelectedItem(selected);
    }

    private void searchAvailableRooms() {
        LocalDate checkIn = toLocalDate((Date) checkInSpinner.getValue());
        LocalDate checkOut = toLocalDate((Date) checkOutSpinner.getValue());

        if (!checkOut.isAfter(checkIn)) {
            JOptionPane.showMessageDialog(this, "Check-out date must be after check-in date.",
                    "Invalid Dates", JOptionPane.WARNING_MESSAGE);
            return;
        }

        availableRoomCombo.removeAllItems();
        List<Room> available = roomService.getAvailableRooms(checkIn, checkOut);
        if (available.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No rooms available for the selected dates.",
                    "No Availability", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        for (Room r : available) availableRoomCombo.addItem(r);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Booking> bookings = bookingService.getAllBookings();
        for (Booking b : bookings) {
            tableModel.addRow(new Object[]{
                    b.getId(),
                    b.getGuest() != null ? b.getGuest().getFullName() : "",
                    b.getRoom() != null ? b.getRoom().getRoomNumber() : "",
                    b.getCheckInDate(), b.getCheckOutDate(), b.getNumGuests(), b.getStatus()
            });
        }
    }

    private void onRowSelected() {
        int row = table.getSelectedRow();
        selectedBookingId = (Integer) tableModel.getValueAt(row, 0);
        Booking.Status status = (Booking.Status) tableModel.getValueAt(row, 6);

        checkInButton.setEnabled(status == Booking.Status.RESERVED);
        checkOutButton.setEnabled(status == Booking.Status.CHECKED_IN);
        cancelButton.setEnabled(status == Booking.Status.RESERVED);
    }

    // ---------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------

    private void onCreateBooking() {
        Guest guest = (Guest) guestCombo.getSelectedItem();
        Room room = (Room) availableRoomCombo.getSelectedItem();

        if (guest == null) {
            JOptionPane.showMessageDialog(this, "Please select a guest (register one in the Guests tab if needed).",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (room == null) {
            JOptionPane.showMessageDialog(this, "Please search and select an available room first.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int numGuests;
        try {
            numGuests = Integer.parseInt(numGuestsField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Number of guests must be a whole number.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate checkIn = toLocalDate((Date) checkInSpinner.getValue());
        LocalDate checkOut = toLocalDate((Date) checkOutSpinner.getValue());

        Booking booking = new Booking(guest.getId(), room.getId(), checkIn, checkOut, numGuests);

        try {
            bookingService.createBooking(booking);
            JOptionPane.showMessageDialog(this, "Booking created successfully for " + guest.getFullName()
                    + " in Room " + room.getRoomNumber() + ".");
            availableRoomCombo.removeAllItems();
            refreshTable();
        } catch (RoomNotAvailableException ex) {
            // This is the custom exception - shown clearly, not as a generic error
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Room Not Available", JOptionPane.WARNING_MESSAGE);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Could not create booking. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println(ex.getMessage());
        }
    }

    private void onCheckIn() {
        if (selectedBookingId == null) return;
        boolean ok = bookingService.checkIn(selectedBookingId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Guest checked in.");
        } else {
            JOptionPane.showMessageDialog(this, "Could not check in this booking.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        refreshTable();
        resetActionButtons();
    }

    private void onCheckOut() {
        if (selectedBookingId == null) return;
        boolean ok = bookingService.checkOut(selectedBookingId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Guest checked out. You can now generate an invoice from the Billing tab.");
        } else {
            JOptionPane.showMessageDialog(this, "Could not check out this booking.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        refreshTable();
        resetActionButtons();
    }

    private void onCancel() {
        if (selectedBookingId == null) return;
        int choice = JOptionPane.showConfirmDialog(this, "Cancel this booking?",
                "Confirm Cancel", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        boolean ok = bookingService.cancelBooking(selectedBookingId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Booking cancelled.");
        }
        refreshTable();
        resetActionButtons();
    }

    private void resetActionButtons() {
        selectedBookingId = null;
        table.clearSelection();
        checkInButton.setEnabled(false);
        checkOutButton.setEnabled(false);
        cancelButton.setEnabled(false);
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
