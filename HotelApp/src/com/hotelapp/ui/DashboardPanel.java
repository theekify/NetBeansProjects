package com.hotelapp.ui;

import com.hotelapp.model.Booking;
import com.hotelapp.model.Room;
import com.hotelapp.service.BillingService;
import com.hotelapp.service.BookingEventListener;
import com.hotelapp.service.BookingService;
import com.hotelapp.service.RoomService;
import com.hotelapp.util.SimpleTheme;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Landing screen showing key stats for decision-making:
 * occupancy, today's check-ins/outs, and monthly revenue.
 *
 * Implements BookingEventListener (Observer pattern) so it refreshes
 * automatically whenever a booking changes anywhere else in the app.
 */
public class DashboardPanel extends JPanel implements BookingEventListener {

    private final BookingService bookingService = new BookingService();
    private final RoomService roomService = new RoomService();
    private final BillingService billingService = new BillingService();

    private JLabel occupancyValueLabel;
    private JLabel revenueValueLabel;
    private JLabel availableRoomsValueLabel;
    private DefaultListModel<String> checkInListModel;
    private DefaultListModel<String> checkOutListModel;

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(SimpleTheme.PANEL_BG);

        add(buildStatsRow(), BorderLayout.NORTH);
        add(buildListsRow(), BorderLayout.CENTER);

        JButton refreshButton = SimpleTheme.coloredButton("Refresh Now", SimpleTheme.PRIMARY);
        refreshButton.addActionListener(e -> refreshAll());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(SimpleTheme.PANEL_BG);
        bottom.add(refreshButton);
        add(bottom, BorderLayout.SOUTH);

        BookingService.addListener(this);
        refreshAll();
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 15, 0));
        row.setBackground(SimpleTheme.PANEL_BG);

        row.add(buildStatCard("Occupancy Rate", occupancyValueLabel = new JLabel("--"), SimpleTheme.PRIMARY));
        row.add(buildStatCard("Available Rooms", availableRoomsValueLabel = new JLabel("--"), SimpleTheme.SUCCESS));
        row.add(buildStatCard("Revenue This Month", revenueValueLabel = new JLabel("--"), SimpleTheme.WARNING));

        return row;
    }

    private JPanel buildStatCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(SimpleTheme.CARD_BG);
        // A colored left-edge stripe via a standard MatteBorder - the kind
        // of border you can pick directly in NetBeans' Properties panel.
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, accent),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        titleLabel.setForeground(Color.GRAY);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        valueLabel.setForeground(accent);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildListsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 15, 0));
        row.setBackground(SimpleTheme.PANEL_BG);

        checkInListModel = new DefaultListModel<>();
        checkOutListModel = new DefaultListModel<>();

        JPanel checkInPanel = new JPanel(new BorderLayout());
        checkInPanel.setBackground(SimpleTheme.CARD_BG);
        checkInPanel.setBorder(SimpleTheme.coloredTitledBorder("Today's Check-ins", SimpleTheme.SUCCESS));
        checkInPanel.add(new JScrollPane(new JList<>(checkInListModel)), BorderLayout.CENTER);

        JPanel checkOutPanel = new JPanel(new BorderLayout());
        checkOutPanel.setBackground(SimpleTheme.CARD_BG);
        checkOutPanel.setBorder(SimpleTheme.coloredTitledBorder("Today's Check-outs", SimpleTheme.WARNING));
        checkOutPanel.add(new JScrollPane(new JList<>(checkOutListModel)), BorderLayout.CENTER);

        row.add(checkInPanel);
        row.add(checkOutPanel);
        return row;
    }

    private void refreshAll() {
        refreshOccupancy();
        refreshRevenue();
        refreshTodayLists();
    }

    private void refreshOccupancy() {
        List<Room> rooms = roomService.getAllRooms();
        long total = rooms.size();
        long occupied = rooms.stream().filter(r -> r.getStatus() == Room.Status.OCCUPIED).count();
        long available = rooms.stream().filter(r -> r.getStatus() == Room.Status.AVAILABLE).count();

        double occupancyPct = total == 0 ? 0 : (occupied * 100.0 / total);
        occupancyValueLabel.setText(String.format("%.0f%%", occupancyPct));
        availableRoomsValueLabel.setText(String.valueOf(available));
    }

    private void refreshRevenue() {
        BigDecimal revenue = billingService.getMonthlyRevenue();
        revenueValueLabel.setText("Rs. " + revenue.toPlainString());
    }

    private void refreshTodayLists() {
        checkInListModel.clear();
        List<Booking> checkIns = bookingService.getTodayCheckIns();
        if (checkIns.isEmpty()) {
            checkInListModel.addElement("No check-ins scheduled for today.");
        } else {
            for (Booking b : checkIns) {
                checkInListModel.addElement(b.getGuest().getFullName() + " - Room "
                        + b.getRoom().getRoomNumber() + " (" + b.getNumGuests() + " guest(s))");
            }
        }

        checkOutListModel.clear();
        List<Booking> checkOuts = bookingService.getTodayCheckOuts();
        if (checkOuts.isEmpty()) {
            checkOutListModel.addElement("No check-outs scheduled for today.");
        } else {
            for (Booking b : checkOuts) {
                checkOutListModel.addElement(b.getGuest().getFullName() + " - Room "
                        + b.getRoom().getRoomNumber());
            }
        }
    }

    @Override
    public void onBookingChanged() {
        // Called automatically by BookingService whenever a booking is
        // created, checked in/out, or cancelled - runs on the Swing
        // event thread since all our service calls originate from UI actions.
        refreshAll();
    }
}
