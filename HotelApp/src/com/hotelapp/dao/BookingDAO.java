package com.hotelapp.dao;

import com.hotelapp.model.Booking;
import java.time.LocalDate;
import java.util.List;

public interface BookingDAO {
    Booking save(Booking booking);
    boolean update(Booking booking);
    boolean delete(int id);
    Booking findById(int id);
    List<Booking> findAll();
    List<Booking> findByStatus(Booking.Status status);
    List<Booking> findTodayCheckIns();
    List<Booking> findTodayCheckOuts();

    /** True if the room already has a RESERVED/CHECKED_IN booking overlapping this date range. */
    boolean hasOverlappingBooking(int roomId, LocalDate checkIn, LocalDate checkOut, Integer excludeBookingId);

    boolean updateStatus(int bookingId, Booking.Status status);
}
