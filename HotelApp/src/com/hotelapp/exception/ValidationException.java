package com.hotelapp.exception;

/**
 * Thrown when user input fails business-rule validation
 * (empty required fields, bad date ranges, invalid formats, etc.)
 * before it ever reaches the DAO/database layer.
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}
