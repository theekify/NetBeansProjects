package com.hotelapp.model;

import java.math.BigDecimal;

public class Service {
    private int id;
    private String serviceName;
    private BigDecimal price;

    public Service() {
    }

    public Service(String serviceName, BigDecimal price) {
        this.serviceName = serviceName;
        this.price = price;
    }

    public Service(int id, String serviceName, BigDecimal price) {
        this.id = id;
        this.serviceName = serviceName;
        this.price = price;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    @Override
    public String toString() {
        return serviceName + " (Rs. " + price + ")";
    }
}
