package com.igirepay.lab1_oop.model;

import com.igirepay.lab1_oop.enums.TransactionType;
import java.time.LocalDateTime;

public class Transaction {
    private int transactionId;
    private String referenceId;
    private double amount;
    private TransactionType transactionType;
    private LocalDateTime timestamp;

    public Transaction(int transactionId, String referenceId, double amount, TransactionType transactionType) {
        this.transactionId = transactionId;
        this.referenceId = referenceId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.timestamp = LocalDateTime.now();
    }

    
    public Transaction(int transactionId, String referenceId, double amount,
                       TransactionType transactionType, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.referenceId = referenceId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.timestamp = timestamp;
    }

    public int getTransactionId() { return transactionId; }
    public String getReferenceId() { return referenceId; }
    public double getAmount() { return amount; }
    public TransactionType getTransactionType() { return transactionType; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Transaction{id=" + transactionId + ", ref='" + referenceId + "', amount=" + amount
                + ", type=" + transactionType + ", time=" + timestamp + "}";
    }
}
