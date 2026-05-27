package com.igirepay.lab1_oop.service;

import com.igirepay.lab1_oop.enums.TransactionType;
import com.igirepay.lab1_oop.exception.DuplicateTransactionException;
import com.igirepay.lab1_oop.exception.InvalidAmountException;
import com.igirepay.lab1_oop.model.Account;
import com.igirepay.lab1_oop.model.Customer;
import com.igirepay.lab1_oop.model.Transaction;
import com.igirepay.lab1_oop.util.TransactionLogger;

import java.util.*;

public class WalletService {

    private List<Customer> customers = new ArrayList<>();
    private Map<Integer, Account> accounts = new HashMap<>();
    private List<Transaction> transactionHistory = new ArrayList<>();
    private Set<String> processedReferenceIds = new HashSet<>();
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

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public Transaction transfer(int senderAccountId, int receiverAccountId, String referenceId, double amount)
            throws Exception {

        if (processedReferenceIds.contains(referenceId)) {
            TransactionLogger.logFailure(referenceId, "Duplicate transfer rejected");
            throw new DuplicateTransactionException("Transfer already processed: " + referenceId);
        }

        Account sender = accounts.get(senderAccountId);
        Account receiver = accounts.get(receiverAccountId);

        if (sender == null) {
            TransactionLogger.logFailure(referenceId, "Sender account not found: " + senderAccountId);
            throw new InvalidAmountException("Sender account not found: " + senderAccountId);
        }
        if (receiver == null) {
            TransactionLogger.logFailure(referenceId, "Receiver account not found: " + receiverAccountId);
            throw new InvalidAmountException("Receiver account not found: " + receiverAccountId);
        }

        String senderName = "Unknown";
        for (Customer c : customers) {
            if (c.getAccounts().contains(sender)) {
                senderName = c.getFullName();
                break;
            }
        }

        sender.withdraw(amount);
        receiver.deposit(amount);

        processedReferenceIds.add(referenceId);

        Transaction outTx = new Transaction(transactionCounter++, referenceId, amount, TransactionType.TRANSFER_OUT);
        transactionHistory.add(outTx);
        TransactionLogger.logSuccess(outTx, senderName);
        TransactionLogger.logStatement(outTx, senderAccountId, senderName);

        Transaction inTx = new Transaction(transactionCounter++, referenceId + "-IN", amount, TransactionType.TRANSFER_IN);
        transactionHistory.add(inTx);

        String receiverName = "Unknown";
        for (Customer c : customers) {
            if (c.getAccounts().contains(receiver)) {
                receiverName = c.getFullName();
                break;
            }
        }
        TransactionLogger.logSuccess(inTx, receiverName);
        TransactionLogger.logStatement(inTx, receiverAccountId, receiverName);

        return outTx;
    }

    public Transaction processTransaction(int accountId, String referenceId, double amount, TransactionType type)
            throws Exception {

        if (processedReferenceIds.contains(referenceId)) {
            TransactionLogger.logFailure(referenceId, "Duplicate transaction rejected");
            throw new DuplicateTransactionException("Transaction already processed: " + referenceId);
        }

        Account account = accounts.get(accountId);
        if (account == null) {
            TransactionLogger.logFailure(referenceId, "Account not found: " + accountId);
            throw new InvalidAmountException("Account not found: " + accountId);
        }

        String customerName = "Unknown";
        for (Customer c : customers) {
            if (c.getAccounts().contains(account)) {
                customerName = c.getFullName();
                break;
            }
        }

        account.processTransaction(type == TransactionType.WITHDRAW ? -amount : amount);

        processedReferenceIds.add(referenceId);

        Transaction transaction = new Transaction(transactionCounter++, referenceId, amount, type);
        transactionHistory.add(transaction);

        TransactionLogger.logSuccess(transaction, customerName);
        TransactionLogger.logStatement(transaction, accountId, customerName);

        return transaction;
    }
}
