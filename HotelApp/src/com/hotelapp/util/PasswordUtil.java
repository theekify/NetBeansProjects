package com.hotelapp.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Handles password hashing and verification using BCrypt.
 * Never store or compare plaintext passwords directly.
 */
public class PasswordUtil {

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
