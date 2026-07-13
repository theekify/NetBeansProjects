package com.hotelapp.service;

import com.hotelapp.dao.InvoiceDAO;
import com.hotelapp.dao.InvoiceDAOImpl;
import com.hotelapp.dao.ServiceDAO;
import com.hotelapp.dao.ServiceDAOImpl;
import com.hotelapp.exception.ValidationException;
import com.hotelapp.model.Booking;
import com.hotelapp.model.Invoice;
import com.hotelapp.model.InvoiceItem;
import com.hotelapp.model.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Generates and manages invoices for a booking.
 * Room charge = nights stayed x room type base price.
 * Service charge = sum of selected extra services x quantity.
 */
public class BillingService {

    private final InvoiceDAO invoiceDAO = new InvoiceDAOImpl();
    private final ServiceDAO serviceDAO = new ServiceDAOImpl();

    /**
     * @param booking        the checked-out booking to bill
     * @param serviceQtyMap  map of serviceId -> quantity selected (can be empty)
     */
    public Invoice generateInvoice(Booking booking, Map<Integer, Integer> serviceQtyMap) throws ValidationException {
        if (booking.getRoom() == null || booking.getRoom().getRoomType() == null) {
            throw new ValidationException("Booking is missing room/room-type details.");
        }

        long nights = booking.getNumberOfNights();
        if (nights <= 0) nights = 1; // safety fallback

        BigDecimal roomCharge = booking.getRoom().getRoomType().getBasePrice()
                .multiply(BigDecimal.valueOf(nights));

        BigDecimal serviceCharge = BigDecimal.ZERO;
        Invoice invoice = new Invoice();

        if (serviceQtyMap != null) {
            for (Map.Entry<Integer, Integer> entry : serviceQtyMap.entrySet()) {
                int serviceId = entry.getKey();
                int qty = entry.getValue();
                if (qty <= 0) continue;

                Service svc = serviceDAO.findById(serviceId);
                if (svc == null) continue;

                BigDecimal lineTotal = svc.getPrice().multiply(BigDecimal.valueOf(qty));
                serviceCharge = serviceCharge.add(lineTotal);

                InvoiceItem item = new InvoiceItem(serviceId, qty, lineTotal);
                invoice.getItems().add(item);
            }
        }

        BigDecimal total = roomCharge.add(serviceCharge);

        invoice.setBookingId(booking.getId());
        invoice.setRoomCharge(roomCharge);
        invoice.setServiceCharge(serviceCharge);
        invoice.setTotalAmount(total);
        invoice.setPaymentStatus(Invoice.PaymentStatus.UNPAID);

        return invoiceDAO.save(invoice);
    }

    public boolean markAsPaid(int invoiceId) {
        return invoiceDAO.updatePaymentStatus(invoiceId, Invoice.PaymentStatus.PAID);
    }

    public Invoice getInvoiceByBooking(int bookingId) {
        return invoiceDAO.findByBookingId(bookingId);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceDAO.findAll();
    }

    public List<Service> getAllServices() {
        return serviceDAO.findAll();
    }

    public BigDecimal getMonthlyRevenue() {
        return invoiceDAO.getMonthlyRevenue();
    }
}
