package com.hotelapp.dao;

import com.hotelapp.model.Invoice;
import java.util.List;

public interface InvoiceDAO {
    Invoice save(Invoice invoice);
    boolean updatePaymentStatus(int invoiceId, Invoice.PaymentStatus status);
    Invoice findById(int id);
    Invoice findByBookingId(int bookingId);
    List<Invoice> findAll();

    /** For dashboard/report: sum of total_amount for PAID invoices generated this month. */
    java.math.BigDecimal getMonthlyRevenue();
}
