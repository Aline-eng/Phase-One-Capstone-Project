package com.igirepay.lab1_oop.model;

import com.igirepay.lab1_oop.exception.InsufficientBalanceException;
import com.igirepay.lab1_oop.exception.InvalidAmountException;

// SavingsAccount has withdrawal restrictions: a fee and a minimum balance requirement
public class SavingsAccount extends Account {

    private static final double WITHDRAWAL_FEE = 100;
    private static final double MINIMUM_BALANCE = 500;

    public SavingsAccount(int accountId, double balance) {
        super(accountId, balance, "Savings");
    }

    @Override
    public void withdraw(double amount) throws InvalidAmountException, InsufficientBalanceException {
        if (amount <= 0) throw new InvalidAmountException("Amount must be greater than 0.");
        // Total deducted = requested amount + fee
        double totalDeducted = amount + WITHDRAWAL_FEE;
        if (getBalance() - totalDeducted < MINIMUM_BALANCE) {
            throw new InsufficientBalanceException(
                "Cannot withdraw. Minimum balance of " + MINIMUM_BALANCE + " must be maintained."
            );
        }
        setBalance(getBalance() - totalDeducted);
    }

    @Override
    public void processTransaction(double amount) throws InvalidAmountException, InsufficientBalanceException {
        if (amount > 0) {
            deposit(amount);
        } else {
            withdraw(Math.abs(amount));
        }
    }
}
