package com.hotelapp.ui;

import com.hotelapp.exception.ValidationException;
import com.hotelapp.model.Booking;
import com.hotelapp.model.Invoice;
import com.hotelapp.model.Service;
import com.hotelapp.service.BillingService;
import com.hotelapp.service.BookingService;
import com.hotelapp.util.SimpleTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillingPanel extends JPanel {

    private final BillingService billingService = new BillingService();
    private final BookingService bookingService = new BookingService();

    private JComboBox<Booking> bookingCombo;
    private DefaultTableModel serviceTableModel;
    private JTable serviceTable;
    private JLabel summaryLabel;
    private JButton generateButton, refreshBookingsButton;

    private DefaultTableModel invoiceTableModel;
    private JTable invoiceTable;
    private JButton markPaidButton;
    private Integer selectedInvoiceId = null;

    public BillingPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(SimpleTheme.PANEL_BG);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildGeneratePanel(), buildInvoiceListPanel());
        split.setResizeWeight(0.55);
        add(split, BorderLayout.CENTER);

        refreshBookingsWithoutInvoice();
        refreshServiceTable();
        refreshInvoiceTable();
    }

    // ---------------------------------------------------------------
    // Generate invoice panel
    // ---------------------------------------------------------------

    private JPanel buildGeneratePanel() {
        JPanel outer = new JPanel(new BorderLayout(8, 8));
        outer.setBackground(SimpleTheme.CARD_BG);
        outer.setBorder(SimpleTheme.coloredTitledBorder("Generate Invoice", SimpleTheme.SUCCESS));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topRow.setBackground(SimpleTheme.CARD_BG);
        bookingCombo = new JComboBox<>();
        refreshBookingsButton = SimpleTheme.coloredButton("Refresh List", SimpleTheme.NEUTRAL);
        refreshBookingsButton.addActionListener(e -> refreshBookingsWithoutInvoice());
        topRow.add(new JLabel("Checked-out Booking (no invoice yet):"));
        topRow.add(bookingCombo);
        topRow.add(refreshBookingsButton);
        outer.add(topRow, BorderLayout.NORTH);

        serviceTableModel = new DefaultTableModel(new Object[]{"Service ID", "Service", "Price", "Quantity"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 3; // only quantity is editable
            }
            @Override
            public Class<?> getColumnClass(int col) {
                return col == 3 ? Integer.class : Object.class;
            }
        };
        serviceTable = new JTable(serviceTableModel);
        SimpleTheme.styleTableHeader(serviceTable);
        outer.add(new JScrollPane(serviceTable), BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setBackground(SimpleTheme.CARD_BG);
        summaryLabel = new JLabel(" ");
        summaryLabel.setForeground(SimpleTheme.SUCCESS);
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.BOLD));
        generateButton = SimpleTheme.coloredButton("Generate Invoice", SimpleTheme.SUCCESS);
        generateButton.addActionListener(e -> onGenerateInvoice());
        bottomRow.add(summaryLabel, BorderLayout.WEST);
        bottomRow.add(generateButton, BorderLayout.EAST);
        outer.add(bottomRow, BorderLayout.SOUTH);

        return outer;
    }

    private void refreshBookingsWithoutInvoice() {
        Booking selected = (Booking) bookingCombo.getSelectedItem();
        bookingCombo.removeAllItems();
        List<Booking> allBookings = bookingService.getAllBookings();
        for (Booking b : allBookings) {
            if (b.getStatus() == Booking.Status.CHECKED_OUT) {
                Invoice existing = billingService.getInvoiceByBooking(b.getId());
                if (existing == null) {
                    bookingCombo.addItem(b);
                }
            }
        }
        if (selected != null) bookingCombo.setSelectedItem(selected);
    }

    private void refreshServiceTable() {
        serviceTableModel.setRowCount(0);
        List<Service> services = billingService.getAllServices();
        for (Service s : services) {
            serviceTableModel.addRow(new Object[]{s.getId(), s.getServiceName(), "Rs. " + s.getPrice(), 0});
        }
    }

    private void onGenerateInvoice() {
        Booking booking = (Booking) bookingCombo.getSelectedItem();
        if (booking == null) {
            JOptionPane.showMessageDialog(this, "No eligible booking selected. A booking must be Checked-out and not already invoiced.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Map<Integer, Integer> serviceQtyMap = new HashMap<>();
        for (int row = 0; row < serviceTableModel.getRowCount(); row++) {
            int serviceId = (Integer) serviceTableModel.getValueAt(row, 0);
            Object qtyObj = serviceTableModel.getValueAt(row, 3);
            int qty = (qtyObj instanceof Integer) ? (Integer) qtyObj : 0;
            if (qty > 0) serviceQtyMap.put(serviceId, qty);
        }

        try {
            Invoice invoice = billingService.generateInvoice(booking, serviceQtyMap);
            summaryLabel.setText(String.format("Invoice #%d generated - Room: Rs.%s | Services: Rs.%s | Total: Rs.%s",
                    invoice.getId(), invoice.getRoomCharge(), invoice.getServiceCharge(), invoice.getTotalAmount()));
            JOptionPane.showMessageDialog(this, "Invoice generated successfully.\nTotal: Rs. " + invoice.getTotalAmount());

            refreshBookingsWithoutInvoice();
            refreshServiceTable();
            refreshInvoiceTable();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Could not generate invoice. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println(ex.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Invoice list panel
    // ---------------------------------------------------------------

    private JPanel buildInvoiceListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(SimpleTheme.CARD_BG);
        panel.setBorder(SimpleTheme.coloredTitledBorder("All Invoices", SimpleTheme.PRIMARY));

        invoiceTableModel = new DefaultTableModel(
                new Object[]{"Invoice ID", "Booking ID", "Room Charge", "Service Charge", "Total", "Payment Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        invoiceTable = new JTable(invoiceTableModel);
        SimpleTheme.styleTableHeader(invoiceTable);
        invoiceTable.getColumnModel().getColumn(5).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                c.setForeground(SimpleTheme.colorForStatus(String.valueOf(value)));
                setFont(getFont().deriveFont(Font.BOLD));
                return c;
            }
        });
        invoiceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && invoiceTable.getSelectedRow() != -1) {
                int row = invoiceTable.getSelectedRow();
                selectedInvoiceId = (Integer) invoiceTableModel.getValueAt(row, 0);
                String status = (String) invoiceTableModel.getValueAt(row, 5);
                markPaidButton.setEnabled("UNPAID".equals(status));
            }
        });
        panel.add(new JScrollPane(invoiceTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.setBackground(SimpleTheme.CARD_BG);
        markPaidButton = SimpleTheme.coloredButton("Mark as Paid", SimpleTheme.SUCCESS);
        markPaidButton.setEnabled(false);
        markPaidButton.addActionListener(e -> onMarkPaid());
        actionPanel.add(markPaidButton);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshInvoiceTable() {
        invoiceTableModel.setRowCount(0);
        List<Invoice> invoices = billingService.getAllInvoices();
        for (Invoice inv : invoices) {
            invoiceTableModel.addRow(new Object[]{
                    inv.getId(), inv.getBookingId(), "Rs. " + inv.getRoomCharge(),
                    "Rs. " + inv.getServiceCharge(), "Rs. " + inv.getTotalAmount(),
                    inv.getPaymentStatus().name()
            });
        }
    }

    private void onMarkPaid() {
        if (selectedInvoiceId == null) return;
        boolean ok = billingService.markAsPaid(selectedInvoiceId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Invoice marked as paid.");
        } else {
            JOptionPane.showMessageDialog(this, "Could not update payment status.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        refreshInvoiceTable();
        markPaidButton.setEnabled(false);
    }
}
