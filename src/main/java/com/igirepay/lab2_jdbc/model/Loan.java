package com.igirepay.lab2_jdbc.model;

import java.time.LocalDateTime;

public class Loan {
    private int id;
    private int customerId;
    private double amount;
    private String reason;
    private String status;
    private LocalDateTime requestedAt;

    public Loan(int id, int customerId, double amount, String reason, String status, LocalDateTime requestedAt) {
        this.id = id;
        this.customerId = customerId;
        this.amount = amount;
        this.reason = reason;
        this.status = status;
        this.requestedAt = requestedAt;
    }

    public int getId() { return id; }
    public int getCustomerId() { return customerId; }
    public double getAmount() { return amount; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getRequestedAt() { return requestedAt; }

    @Override
    public String toString() {
        return "Loan{id=" + id + ", customerId=" + customerId + ", amount=" + amount
                + ", reason='" + reason + "', status='" + status + "', requestedAt=" + requestedAt + "}";
    }
}
