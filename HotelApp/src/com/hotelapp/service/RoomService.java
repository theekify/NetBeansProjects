package com.hotelapp.service;

import com.hotelapp.dao.RoomDAO;
import com.hotelapp.dao.RoomDAOImpl;
import com.hotelapp.dao.RoomTypeDAO;
import com.hotelapp.dao.RoomTypeDAOImpl;
import com.hotelapp.exception.ValidationException;
import com.hotelapp.model.Room;
import com.hotelapp.model.RoomType;

import java.time.LocalDate;
import java.util.List;

public class RoomService {

    private final RoomDAO roomDAO = new RoomDAOImpl();
    private final RoomTypeDAO roomTypeDAO = new RoomTypeDAOImpl();

    public Room addRoom(Room room) throws ValidationException {
        if (room.getRoomNumber() == null || room.getRoomNumber().trim().isEmpty()) {
            throw new ValidationException("Room number cannot be empty.");
        }
        if (room.getRoomTypeId() <= 0) {
            throw new ValidationException("A valid room type must be selected.");
        }
        return roomDAO.save(room);
    }

    public boolean updateRoom(Room room) throws ValidationException {
        if (room.getRoomNumber() == null || room.getRoomNumber().trim().isEmpty()) {
            throw new ValidationException("Room number cannot be empty.");
        }
        return roomDAO.update(room);
    }

    public boolean deleteRoom(int id) {
        return roomDAO.delete(id);
    }

    public Room getRoom(int id) {
        return roomDAO.findById(id);
    }

    public List<Room> getAllRooms() {
        return roomDAO.findAll();
    }

    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        return roomDAO.findAvailableRooms(checkIn, checkOut);
    }

    public boolean setRoomStatus(int roomId, Room.Status status) {
        return roomDAO.updateStatus(roomId, status);
    }

    // Room type management
    public RoomType addRoomType(RoomType roomType) throws ValidationException {
        if (roomType.getTypeName() == null || roomType.getTypeName().trim().isEmpty()) {
            throw new ValidationException("Room type name cannot be empty.");
        }
        if (roomType.getBasePrice() == null || roomType.getBasePrice().signum() <= 0) {
            throw new ValidationException("Base price must be greater than zero.");
        }
        if (roomType.getCapacity() <= 0) {
            throw new ValidationException("Capacity must be greater than zero.");
        }
        return roomTypeDAO.save(roomType);
    }

    public List<RoomType> getAllRoomTypes() {
        return roomTypeDAO.findAll();
    }
}
