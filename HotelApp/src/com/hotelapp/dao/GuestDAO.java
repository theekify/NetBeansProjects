package com.hotelapp.dao;

import com.hotelapp.model.Guest;
import java.util.List;

/**
 * DAO interface for Guest persistence.
 * Why an interface (viva point): the service/UI layers depend on this
 * abstraction, not the concrete implementation. Lets us swap the storage
 * mechanism (e.g. different DB, or a mock for testing) without touching
 * any calling code.
 */
public interface GuestDAO {
    Guest save(Guest guest);
    boolean update(Guest guest);
    boolean delete(int id);
    Guest findById(int id);
    List<Guest> findAll();
    List<Guest> search(String keyword);
}
