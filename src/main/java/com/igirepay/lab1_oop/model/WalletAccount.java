package com.igirepay.lab1_oop.model;

import com.igirepay.lab1_oop.exception.InsufficientBalanceException;
import com.igirepay.lab1_oop.exception.InvalidAmountException;

// WalletAccount extends Account - inherits deposit(), balance, accountId
// Allows instant transfers with no fees or minimum balance restrictions
public class WalletAccount extends Account {

    public WalletAccount(int accountId, double balance) {
        super(accountId, balance, "Wallet");
    }

    @Override
    public void withdraw(double amount) throws InvalidAmountException, InsufficientBalanceException {
        if (amount <= 0) throw new InvalidAmountException("Amount must be greater than 0.");
        if (amount > getBalance()) throw new InsufficientBalanceException("Insufficient funds.");
        setBalance(getBalance() - amount);
    }

    @Override
    public void processTransaction(double amount) throws InvalidAmountException, InsufficientBalanceException {
        // Wallet allows instant deposits (positive) or withdrawals (negative)
        if (amount > 0) {
            deposit(amount);
        } else {
            withdraw(Math.abs(amount));
        }
    }
}
