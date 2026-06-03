package com.campus.exception;

//Thrown when an amount is <= 0, NaN, or otherwise not a usable money value.
public class InvalidAmountException extends RuntimeException {

    public InvalidAmountException(String message) {
        super(message);
    }
}
