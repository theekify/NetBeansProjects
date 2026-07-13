package com.hotelapp.dao;

import com.hotelapp.model.Invoice;
import com.hotelapp.model.InvoiceItem;
import com.hotelapp.model.Service;
import com.hotelapp.util.DBConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAOImpl implements InvoiceDAO {

    private Connection getConn() {
        return DBConnectionManager.getInstance().getConnection();
    }

    @Override
    public Invoice save(Invoice invoice) {
        String sql = "INSERT INTO invoices (booking_id, room_charge, service_charge, total_amount, payment_status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, invoice.getBookingId());
            ps.setBigDecimal(2, invoice.getRoomCharge());
            ps.setBigDecimal(3, invoice.getServiceCharge());
            ps.setBigDecimal(4, invoice.getTotalAmount());
            ps.setString(5, invoice.getPaymentStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) invoice.setId(keys.getInt(1));
            }

            // Persist line items
            if (invoice.getItems() != null) {
                String itemSql = "INSERT INTO invoice_items (invoice_id, service_id, quantity, line_total) VALUES (?, ?, ?, ?)";
                try (PreparedStatement itemPs = getConn().prepareStatement(itemSql)) {
                    for (InvoiceItem item : invoice.getItems()) {
                        itemPs.setInt(1, invoice.getId());
                        itemPs.setInt(2, item.getServiceId());
                        itemPs.setInt(3, item.getQuantity());
                        itemPs.setBigDecimal(4, item.getLineTotal());
                        itemPs.addBatch();
                    }
                    itemPs.executeBatch();
                }
            }
            return invoice;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save invoice: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updatePaymentStatus(int invoiceId, Invoice.PaymentStatus status) {
        String sql = "UPDATE invoices SET payment_status=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, invoiceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update payment status: " + e.getMessage(), e);
        }
    }

    @Override
    public Invoice findById(int id) {
        String sql = "SELECT * FROM invoices WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invoice inv = mapRow(rs);
                    inv.setItems(findItems(inv.getId()));
                    return inv;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find invoice: " + e.getMessage(), e);
        }
    }

    @Override
    public Invoice findByBookingId(int bookingId) {
        String sql = "SELECT * FROM invoices WHERE booking_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invoice inv = mapRow(rs);
                    inv.setItems(findItems(inv.getId()));
                    return inv;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find invoice by booking: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Invoice> findAll() {
        String sql = "SELECT * FROM invoices ORDER BY generated_at DESC";
        List<Invoice> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch invoices: " + e.getMessage(), e);
        }
    }

    @Override
    public BigDecimal getMonthlyRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM invoices " +
                     "WHERE payment_status='PAID' AND MONTH(generated_at) = MONTH(CURDATE()) " +
                     "AND YEAR(generated_at) = YEAR(CURDATE())";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
            return BigDecimal.ZERO;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to compute monthly revenue: " + e.getMessage(), e);
        }
    }

    private List<InvoiceItem> findItems(int invoiceId) {
        String sql = "SELECT ii.*, s.service_name, s.price FROM invoice_items ii " +
                     "JOIN services s ON ii.service_id = s.id WHERE ii.invoice_id=?";
        List<InvoiceItem> items = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvoiceItem item = new InvoiceItem();
                    item.setId(rs.getInt("id"));
                    item.setInvoiceId(rs.getInt("invoice_id"));
                    item.setServiceId(rs.getInt("service_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setLineTotal(rs.getBigDecimal("line_total"));
                    Service svc = new Service();
                    svc.setId(rs.getInt("service_id"));
                    svc.setServiceName(rs.getString("service_name"));
                    svc.setPrice(rs.getBigDecimal("price"));
                    item.setService(svc);
                    items.add(item);
                }
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch invoice items: " + e.getMessage(), e);
        }
    }

    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setId(rs.getInt("id"));
        inv.setBookingId(rs.getInt("booking_id"));
        inv.setRoomCharge(rs.getBigDecimal("room_charge"));
        inv.setServiceCharge(rs.getBigDecimal("service_charge"));
        inv.setTotalAmount(rs.getBigDecimal("total_amount"));
        inv.setPaymentStatus(Invoice.PaymentStatus.valueOf(rs.getString("payment_status")));
        Timestamp ts = rs.getTimestamp("generated_at");
        if (ts != null) inv.setGeneratedAt(ts.toLocalDateTime());
        return inv;
    }
}
