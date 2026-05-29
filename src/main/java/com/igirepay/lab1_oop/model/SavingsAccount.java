package com.igirepay.lab1_oop.model;

import com.igirepay.lab1_oop.exception.InsufficientBalanceException;
import com.igirepay.lab1_oop.exception.InvalidAmountException;
import com.igirepay.lab1_oop.util.TransactionFee;

public class SavingsAccount extends Account {

    private static final double WITHDRAWAL_FEE = 100;
    private static final double MINIMUM_BALANCE = 500;

    public SavingsAccount(int accountId, double balance) {
        super(accountId, balance, "Savings");
    }

    @Override
    public void withdraw(double amount) throws InvalidAmountException, InsufficientBalanceException {
        if (amount <= 0) throw new InvalidAmountException("Amount must be greater than 0.");
        double fee = TransactionFee.getSavingsFee(amount);
        double totalDeducted = amount + fee;

        if (getBalance() - totalDeducted < MINIMUM_BALANCE) throw new InsufficientBalanceException(
                "Cannot withdraw. Minimum balance of " + MINIMUM_BALANCE + " RWF must be maintained!"
        );
        setBalance(getBalance() - totalDeducted);
        if (fee > 0) System.out.println("Early withdrawal fee charged: " + fee + " RWF");
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
