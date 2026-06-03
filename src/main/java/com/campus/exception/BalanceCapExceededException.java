package com.campus.exception;

//Thrown on deposit when balance + amount would exceed the wallet's balance cap. 
public class BalanceCapExceededException extends RuntimeException {

    public BalanceCapExceededException(String message) {
        super(message);
    }
}
