package com.hotelapp.dao;

import com.hotelapp.model.Service;
import java.util.List;

public interface ServiceDAO {
    Service save(Service service);
    boolean update(Service service);
    boolean delete(int id);
    Service findById(int id);
    List<Service> findAll();
}
