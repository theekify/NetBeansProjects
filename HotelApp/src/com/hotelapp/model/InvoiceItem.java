package com.hotelapp.model;

import java.math.BigDecimal;

public class InvoiceItem {
    private int id;
    private int invoiceId;
    private int serviceId;
    private Service service; // populated when joined
    private int quantity;
    private BigDecimal lineTotal;

    public InvoiceItem() {
    }

    public InvoiceItem(int serviceId, int quantity, BigDecimal lineTotal) {
        this.serviceId = serviceId;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public Service getService() { return service; }
    public void setService(Service service) { this.service = service; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
