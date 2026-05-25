package com.igirepay.lab1_oop.exception;

// RuntimeException because duplicate transactions are a programming/logic error,
// not something the caller is expected to recover from gracefully
public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String message) {
        super(message);
    }
}
