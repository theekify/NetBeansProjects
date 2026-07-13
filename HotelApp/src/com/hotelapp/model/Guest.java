package com.hotelapp.model;

import java.time.LocalDateTime;

public class Guest {
    private int id;
    private String fullName;
    private String nicPassport;
    private String phone;
    private String email;
    private String address;
    private LocalDateTime createdAt;

    public Guest() {
    }

    public Guest(String fullName, String nicPassport, String phone, String email, String address) {
        this.fullName = fullName;
        this.nicPassport = nicPassport;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public Guest(int id, String fullName, String nicPassport, String phone, String email, String address, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.nicPassport = nicPassport;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getNicPassport() { return nicPassport; }
    public void setNicPassport(String nicPassport) { this.nicPassport = nicPassport; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return fullName + " (" + nicPassport + ")";
    }
}
