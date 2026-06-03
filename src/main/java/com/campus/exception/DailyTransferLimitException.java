package com.campus.exception;

//hrown when todayTransferred + amount would exceed the wallet's daily transfer limit.
public class DailyTransferLimitException extends RuntimeException {

    public DailyTransferLimitException(String message) {
        super(message);
    }
}
