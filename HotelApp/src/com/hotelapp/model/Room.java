package com.hotelapp.model;

public class Room {

    public enum Status {
        AVAILABLE, OCCUPIED, MAINTENANCE
    }

    private int id;
    private String roomNumber;
    private int roomTypeId;
    private RoomType roomType; // populated when joined
    private Integer floorNumber;
    private Status status;

    public Room() {
    }

    public Room(String roomNumber, int roomTypeId, Integer floorNumber, Status status) {
        this.roomNumber = roomNumber;
        this.roomTypeId = roomTypeId;
        this.floorNumber = floorNumber;
        this.status = status;
    }

    public Room(int id, String roomNumber, int roomTypeId, Integer floorNumber, Status status) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomTypeId = roomTypeId;
        this.floorNumber = floorNumber;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(int roomTypeId) { this.roomTypeId = roomTypeId; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    @Override
    public String toString() {
        return "Room " + roomNumber;
    }
}
