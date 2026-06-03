package com.campus.exception;

//Thrown when a wallet's balance is less than the requested debit/transfer amount
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
