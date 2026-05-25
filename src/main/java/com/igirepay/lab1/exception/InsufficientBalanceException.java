package com.igirepay.lab1.exception;

// Checked exception - callers must handle or declare it, forcing them to deal with this case
public class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
