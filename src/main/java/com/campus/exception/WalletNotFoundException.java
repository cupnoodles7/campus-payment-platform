package com.campus.exception;

//Thrown when no wallet row exists for a given student id.
public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(String message) {
        super(message);
    }
}
