package com.hotelapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Booking {

    public enum Status {
        RESERVED, CHECKED_IN, CHECKED_OUT, CANCELLED
    }

    private int id;
    private int guestId;
    private int roomId;
    private Guest guest;   // populated when joined
    private Room room;     // populated when joined
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime actualCheckIn;
    private LocalDateTime actualCheckOut;
    private Status status;
    private int numGuests;
    private LocalDateTime createdAt;

    public Booking() {
    }

    public Booking(int guestId, int roomId, LocalDate checkInDate, LocalDate checkOutDate, int numGuests) {
        this.guestId = guestId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numGuests = numGuests;
        this.status = Status.RESERVED;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGuestId() { return guestId; }
    public void setGuestId(int guestId) { this.guestId = guestId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public LocalDateTime getActualCheckIn() { return actualCheckIn; }
    public void setActualCheckIn(LocalDateTime actualCheckIn) { this.actualCheckIn = actualCheckIn; }

    public LocalDateTime getActualCheckOut() { return actualCheckOut; }
    public void setActualCheckOut(LocalDateTime actualCheckOut) { this.actualCheckOut = actualCheckOut; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int getNumGuests() { return numGuests; }
    public void setNumGuests(int numGuests) { this.numGuests = numGuests; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** Number of nights booked - used for room charge calculation. */
    public long getNumberOfNights() {
        if (checkInDate == null || checkOutDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    @Override
    public String toString() {
        String guestName = guest != null ? guest.getFullName() : "Guest#" + guestId;
        String roomNumber = room != null ? room.getRoomNumber() : "Room#" + roomId;
        return "Booking #" + id + " - " + guestName + " - Room " + roomNumber
                + " (" + checkInDate + " to " + checkOutDate + ")";
    }
}
