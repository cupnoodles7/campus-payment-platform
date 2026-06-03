package com.campus.exception;

//Wraps a checked SQLException (or other low-level DB failure) as an unchecked exception
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(String message) {
        super(message);
    }
}
