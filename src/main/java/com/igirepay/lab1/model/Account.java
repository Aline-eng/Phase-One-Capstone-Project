package com.igirepay.lab1.model;

// Abstract class - cannot be instantiated directly.
// WalletAccount and SavingsAccount will extend this.
public abstract class Account {
    private int accountId;
    private double balance;
    private String accountType;

    public Account(int accountId, double balance, String accountType) {
        this.accountId = accountId;
        this.balance = balance;
        this.accountType = accountType;
    }

    // Deposit is the same for all account types, so we define it here once
    public void deposit(double amount) {
        balance += amount;
    }

    // withdraw and processTransaction behave differently per account type,
    // so subclasses must provide their own implementation (polymorphism)
    public abstract void withdraw(double amount) throws Exception;
    public abstract void processTransaction(double amount) throws Exception;

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    @Override
    public String toString() {
        return "Account{accountId=" + accountId + ", balance=" + balance + ", accountType='" + accountType + "'}";
    }
}
