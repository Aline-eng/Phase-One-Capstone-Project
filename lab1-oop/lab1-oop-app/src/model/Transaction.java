package model;

import enums.TransacationType;

import java.time.LocalDateTime;

public class Transaction {
    private int transactionId;
    private String referenceId;
    private double amount;
    private TransacationType transacationType;
    private LocalDateTime timestamp;

    public Transaction(int transactionId, String referenceId, double amount, TransacationType transacationType) {
        this.transactionId = transactionId;
        this.referenceId = referenceId;
        this.amount = amount;
        this.transacationType = transacationType;
        this.timestamp = LocalDateTime.now();
    }

    public int getTransactionId() {return transactionId;}
    public void setTransactionId(int transactionId) {this.transactionId = transactionId;}
    public String getReferenceId() {return referenceId;}
    public void setReferenceId(String referenceId) {this.referenceId = referenceId;}
    public double getAmount() {return amount;}
    public void setAmount(double amount) {this.amount = amount;}
    public TransacationType getTransacationType() {return transacationType;}
    public LocalDateTime getTimestamp() {return timestamp;}

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", referenceId='" + referenceId + '\'' +
                ", amount=" + amount +
                ", transactionType=" + transacationType +
                ", timestamp=" + timestamp +
                '}';
    }
}
