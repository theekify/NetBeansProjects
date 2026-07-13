package com.hotelapp.service;

import com.hotelapp.dao.BookingDAO;
import com.hotelapp.dao.BookingDAOImpl;
import com.hotelapp.dao.RoomDAO;
import com.hotelapp.dao.RoomDAOImpl;
import com.hotelapp.exception.RoomNotAvailableException;
import com.hotelapp.exception.ValidationException;
import com.hotelapp.model.Booking;
import com.hotelapp.model.Room;
import com.hotelapp.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for the booking lifecycle:
 * RESERVED -> CHECKED_IN -> CHECKED_OUT (or CANCELLED at any point before check-in).
 *
 * This is the "Transaction UI" backing service — conflict detection here
 * is what the RoomNotAvailableException protects against.
 *
 * Also acts as the Subject in an Observer pattern: registered listeners
 * (e.g. the Dashboard) are notified whenever a booking's state changes,
 * so the UI can refresh itself automatically instead of polling.
 */
public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAOImpl();
    private final RoomDAO roomDAO = new RoomDAOImpl();

    // Static so every BookingService instance shares the same listener registry -
    // a lightweight in-process event bus for this desktop app.
    private static final List<BookingEventListener> listeners = new ArrayList<>();

    public static void addListener(BookingEventListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(BookingEventListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (BookingEventListener listener : listeners) {
            listener.onBookingChanged();
        }
    }

    public Booking createBooking(Booking booking) throws ValidationException, RoomNotAvailableException {
        // 1. Field-level validation
        ValidationUtil.requirePositive(booking.getGuestId(), "Guest");
        ValidationUtil.requirePositive(booking.getRoomId(), "Room");
        ValidationUtil.requireValidDateRange(booking.getCheckInDate(), booking.getCheckOutDate());
        ValidationUtil.requirePositive(booking.getNumGuests(), "Number of guests");

        // 2. Business-rule validation: no double booking
        boolean overlap = bookingDAO.hasOverlappingBooking(
                booking.getRoomId(), booking.getCheckInDate(), booking.getCheckOutDate(), null);
        if (overlap) {
            Room room = roomDAO.findById(booking.getRoomId());
            String roomNumber = (room != null) ? room.getRoomNumber() : String.valueOf(booking.getRoomId());
            throw new RoomNotAvailableException(roomNumber, booking.getCheckInDate(), booking.getCheckOutDate());
        }

        // 3. Capacity check against the room type
        Room room = roomDAO.findById(booking.getRoomId());
        if (room != null && room.getRoomType() != null
                && booking.getNumGuests() > room.getRoomType().getCapacity()) {
            throw new ValidationException("This room type only accommodates "
                    + room.getRoomType().getCapacity() + " guest(s).");
        }

        booking.setStatus(Booking.Status.RESERVED);
        Booking saved = bookingDAO.save(booking);
        notifyListeners();
        return saved;
    }

    public boolean checkIn(int bookingId) {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null || booking.getStatus() != Booking.Status.RESERVED) {
            return false;
        }
        booking.setStatus(Booking.Status.CHECKED_IN);
        booking.setActualCheckIn(LocalDateTime.now());
        boolean updated = bookingDAO.update(booking);
        if (updated) {
            roomDAO.updateStatus(booking.getRoomId(), Room.Status.OCCUPIED);
            notifyListeners();
        }
        return updated;
    }

    public boolean checkOut(int bookingId) {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null || booking.getStatus() != Booking.Status.CHECKED_IN) {
            return false;
        }
        booking.setStatus(Booking.Status.CHECKED_OUT);
        booking.setActualCheckOut(LocalDateTime.now());
        boolean updated = bookingDAO.update(booking);
        if (updated) {
            roomDAO.updateStatus(booking.getRoomId(), Room.Status.AVAILABLE);
            notifyListeners();
        }
        return updated;
    }

    public boolean cancelBooking(int bookingId) {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null || booking.getStatus() != Booking.Status.RESERVED) {
            return false;
        }
        boolean updated = bookingDAO.updateStatus(bookingId, Booking.Status.CANCELLED);
        if (updated) {
            notifyListeners();
        }
        return updated;
    }

    public Booking getBooking(int id) {
        return bookingDAO.findById(id);
    }

    public List<Booking> getAllBookings() {
        return bookingDAO.findAll();
    }

    public List<Booking> getTodayCheckIns() {
        return bookingDAO.findTodayCheckIns();
    }

    public List<Booking> getTodayCheckOuts() {
        return bookingDAO.findTodayCheckOuts();
    }

    public List<Booking> getActiveBookings() {
        return bookingDAO.findByStatus(Booking.Status.CHECKED_IN);
    }
}
