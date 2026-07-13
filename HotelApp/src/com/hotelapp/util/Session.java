package com.hotelapp.util;

import com.hotelapp.model.User;

/**
 * Holds the currently logged-in user for the duration of the app session.
 * Singleton — there's only ever one active session in a desktop app.
 */
public class Session {

    private static Session instance;
    private User currentUser;

    private Session() {}

    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void clear() {
        this.currentUser = null;
    }
}
