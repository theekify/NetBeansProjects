package com.hotelapp.service;

import com.hotelapp.dao.GuestDAO;
import com.hotelapp.dao.GuestDAOImpl;
import com.hotelapp.exception.ValidationException;
import com.hotelapp.model.Guest;
import com.hotelapp.util.ValidationUtil;

import java.util.List;

/**
 * Business logic for Guest management.
 * UI classes call this, never the DAO directly (MVC separation).
 */
public class GuestService {

    private final GuestDAO guestDAO = new GuestDAOImpl();

    public Guest registerGuest(Guest guest) throws ValidationException {
        validate(guest);
        return guestDAO.save(guest);
    }

    public boolean updateGuest(Guest guest) throws ValidationException {
        validate(guest);
        return guestDAO.update(guest);
    }

    public boolean deleteGuest(int id) {
        return guestDAO.delete(id);
    }

    public Guest getGuest(int id) {
        return guestDAO.findById(id);
    }

    public List<Guest> getAllGuests() {
        return guestDAO.findAll();
    }

    public List<Guest> searchGuests(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return guestDAO.findAll();
        }
        return guestDAO.search(keyword.trim());
    }

    private void validate(Guest guest) throws ValidationException {
        ValidationUtil.requireNonEmpty(guest.getFullName(), "Full name");
        ValidationUtil.requireNonEmpty(guest.getNicPassport(), "NIC/Passport number");
        ValidationUtil.requireValidPhone(guest.getPhone(), "Phone number");
        ValidationUtil.requireValidEmail(guest.getEmail(), "Email");
    }
}
