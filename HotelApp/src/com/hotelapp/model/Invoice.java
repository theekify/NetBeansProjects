package com.hotelapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Invoice {

    public enum PaymentStatus {
        UNPAID, PAID
    }

    private int id;
    private int bookingId;
    private Booking booking; // populated when joined
    private BigDecimal roomCharge;
    private BigDecimal serviceCharge;
    private BigDecimal totalAmount;
    private PaymentStatus paymentStatus;
    private LocalDateTime generatedAt;
    private List<InvoiceItem> items = new ArrayList<>();

    public Invoice() {
    }

    public Invoice(int bookingId, BigDecimal roomCharge, BigDecimal serviceCharge, BigDecimal totalAmount) {
        this.bookingId = bookingId;
        this.roomCharge = roomCharge;
        this.serviceCharge = serviceCharge;
        this.totalAmount = totalAmount;
        this.paymentStatus = PaymentStatus.UNPAID;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public BigDecimal getRoomCharge() { return roomCharge; }
    public void setRoomCharge(BigDecimal roomCharge) { this.roomCharge = roomCharge; }

    public BigDecimal getServiceCharge() { return serviceCharge; }
    public void setServiceCharge(BigDecimal serviceCharge) { this.serviceCharge = serviceCharge; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }
}
