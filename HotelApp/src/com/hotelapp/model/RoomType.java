package com.hotelapp.model;

import java.math.BigDecimal;

public class RoomType {
    private int id;
    private String typeName;
    private BigDecimal basePrice;
    private int capacity;
    private String description;

    public RoomType() {
    }

    public RoomType(String typeName, BigDecimal basePrice, int capacity, String description) {
        this.typeName = typeName;
        this.basePrice = basePrice;
        this.capacity = capacity;
        this.description = description;
    }

    public RoomType(int id, String typeName, BigDecimal basePrice, int capacity, String description) {
        this.id = id;
        this.typeName = typeName;
        this.basePrice = basePrice;
        this.capacity = capacity;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return typeName;
    }
}
