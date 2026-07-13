package com.hotelapp.exception;

/**
 * Thrown when a booking is attempted for a room that is already
 * reserved/occupied for an overlapping date range.
 *
 * This is the required user-defined exception for the coursework.
 */
public class RoomNotAvailableException extends Exception {

    public RoomNotAvailableException(String message) {
        super(message);
    }

    public RoomNotAvailableException(String roomNumber, java.time.LocalDate checkIn, java.time.LocalDate checkOut) {
        super("Room " + roomNumber + " is not available between " + checkIn + " and " + checkOut
                + " — it overlaps with an existing reservation.");
    }
}
