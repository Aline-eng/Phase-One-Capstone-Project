package com.igirepay.lab1.service;

import com.igirepay.lab1.enums.TransactionType;
import com.igirepay.lab1.exception.DuplicateTransactionException;
import com.igirepay.lab1.exception.InsufficientBalanceException;
import com.igirepay.lab1.exception.InvalidAmountException;
import com.igirepay.lab1.model.Account;
import com.igirepay.lab1.model.Customer;
import com.igirepay.lab1.model.Transaction;

import java.util.*;

public class PaymentService {

    // List - ordered collection of all customers in the system
    private List<Customer> customers = new ArrayList<>();

    // Map - quick lookup of accounts by accountId (key=accountId, value=Account)
    private Map<Integer, Account> accounts = new HashMap<>();

    // List - full history of all transactions ever processed
    private List<Transaction> transactionHistory = new ArrayList<>();

    // Set - stores reference IDs of already processed transactions.
    // Set is used here because it automatically rejects duplicates and lookup is O(1)
    private Set<String> processedReferenceIds = new HashSet<>();

    // List - logs of transactions that failed (e.g. duplicate, insufficient funds)
    private List<String> failedTransactionLogs = new ArrayList<>();

    private int transactionCounter = 1;

    public void registerCustomer(Customer customer) {
        customers.add(customer);
    }

    public void registerAccount(Account account) {
        accounts.put(account.getAccountId(), account);
    }

    public Account getAccount(int accountId) {
        return accounts.get(accountId);
    }

    // Core method: processes a transaction only if the referenceId is new
    public Transaction processTransaction(int accountId, String referenceId, double amount, TransactionType type)
            throws InvalidAmountException, InsufficientBalanceException {

        // Idempotency check: if this referenceId was already processed, reject it
        if (processedReferenceIds.contains(referenceId)) {
            String log = "DUPLICATE rejected: ref=" + referenceId;
            failedTransactionLogs.add(log);
            throw new DuplicateTransactionException("Transaction already processed: " + referenceId);
        }

        Account account = accounts.get(accountId);
        if (account == null) throw new InvalidAmountException("Account not found: " + accountId);

        // Delegate to the account's own processTransaction (polymorphism)
        account.processTransaction(type == TransactionType.WITHDRAW ? -amount : amount);

        // Mark this referenceId as processed so it can never run again
        processedReferenceIds.add(referenceId);

        Transaction transaction = new Transaction(transactionCounter++, referenceId, amount, type);
        transactionHistory.add(transaction);
        return transaction;
    }

    public List<Transaction> getTransactionHistory() { return transactionHistory; }
    public List<Customer> getCustomers() { return customers; }
    public List<String> getFailedTransactionLogs() { return failedTransactionLogs; }
}
