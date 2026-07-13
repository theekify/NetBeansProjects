package com.hotelapp.service;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.dao.UserDAOImpl;
import com.hotelapp.exception.ValidationException;
import com.hotelapp.model.User;
import com.hotelapp.util.PasswordUtil;
import com.hotelapp.util.ValidationUtil;

public class AuthService {

    private final UserDAO userDAO = new UserDAOImpl();

    /**
     * @return the authenticated User
     * @throws ValidationException if credentials are empty or invalid
     */
    public User login(String username, String password) throws ValidationException {
        ValidationUtil.requireNonEmpty(username, "Username");
        ValidationUtil.requireNonEmpty(password, "Password");

        User user = userDAO.findByUsername(username.trim());
        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new ValidationException("Invalid username or password.");
        }
        return user;
    }
}
