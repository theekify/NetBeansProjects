package com.hotelapp.util;

import com.hotelapp.exception.ValidationException;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Reusable field-level validation helpers used by the service layer.
 * Keeping this separate avoids repeating regex/empty-check logic
 * in every service class.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{9,15}$");

    public static void requireNonEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
    }

    public static void requireValidEmail(String email, String fieldName) throws ValidationException {
        if (email == null || email.trim().isEmpty()) return; // email optional in schema
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException(fieldName + " is not a valid email address.");
        }
    }

    public static void requireValidPhone(String phone, String fieldName) throws ValidationException {
        requireNonEmpty(phone, fieldName);
        String digitsOnly = phone.replaceAll("[\\s-]", "");
        if (!PHONE_PATTERN.matcher(digitsOnly).matches()) {
            throw new ValidationException(fieldName + " must contain 9-15 digits only.");
        }
    }

    public static void requirePositive(java.math.BigDecimal value, String fieldName) throws ValidationException {
        if (value == null || value.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero.");
        }
    }

    public static void requirePositive(int value, String fieldName) throws ValidationException {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero.");
        }
    }

    public static void requireValidDateRange(LocalDate checkIn, LocalDate checkOut) throws ValidationException {
        if (checkIn == null || checkOut == null) {
            throw new ValidationException("Check-in and check-out dates are required.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new ValidationException("Check-out date must be after check-in date.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new ValidationException("Check-in date cannot be in the past.");
        }
    }
}
