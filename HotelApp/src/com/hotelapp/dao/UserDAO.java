package com.hotelapp.dao;

import com.hotelapp.model.User;

public interface UserDAO {
    User findByUsername(String username);
}
