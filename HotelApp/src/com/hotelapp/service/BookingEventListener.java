package com.hotelapp.service;

/**
 * Observer interface - implemented by any UI component that needs to
 * react when a booking's state changes (created, checked in/out, cancelled).
 *
 * Used by the Dashboard to auto-refresh without polling the database
 * or requiring a manual "Refresh" click.
 */
public interface BookingEventListener {
    void onBookingChanged();
}
