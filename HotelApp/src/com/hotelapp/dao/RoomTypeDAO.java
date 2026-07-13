package com.hotelapp.dao;

import com.hotelapp.model.RoomType;
import java.util.List;

public interface RoomTypeDAO {
    RoomType save(RoomType roomType);
    boolean update(RoomType roomType);
    boolean delete(int id);
    RoomType findById(int id);
    List<RoomType> findAll();
}
