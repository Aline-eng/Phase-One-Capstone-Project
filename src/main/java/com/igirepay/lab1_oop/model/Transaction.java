package com.igirepay.lab1_oop.model;

import com.igirepay.lab1_oop.enums.TransactionType;
import java.time.LocalDateTime;

public class Transaction {
    private int transactionId;
    private String referenceId;   // unique ID used to detect duplicate transactions
    private double amount;
    private TransactionType transactionType;
    private LocalDateTime timestamp;

    public Transaction(int transactionId, String referenceId, double amount, TransactionType transactionType) {
        this.transactionId = transactionId;
        this.referenceId = referenceId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.timestamp = LocalDateTime.now(); // automatically set when transaction is created
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
