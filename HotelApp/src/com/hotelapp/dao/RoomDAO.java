package com.hotelapp.dao;

import com.hotelapp.model.Room;
import java.time.LocalDate;
import java.util.List;

public interface RoomDAO {
    Room save(Room room);
    boolean update(Room room);
    boolean delete(int id);
    Room findById(int id);
    List<Room> findAll();

    /** Rooms not currently AVAILABLE-blocked by an overlapping RESERVED/CHECKED_IN booking. */
    List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut);

    boolean updateStatus(int roomId, Room.Status status);
}
