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

    // List - ordered collection of all registered customers
    private List<Customer> customers = new ArrayList<>();

    // Map - key is accountId, value is the Account object.
    // Chosen over List because we look up accounts by ID, not by position.
    private Map<Integer, Account> accounts = new HashMap<>();

    // List - keeps every successful transaction in the order it happened
    private List<Transaction> transactionHistory = new ArrayList<>();

    // Set - stores reference IDs of already processed transactions.
    // HashSet gives O(1) lookup - checking if a ref exists is instant.
    private Set<String> processedReferenceIds = new HashSet<>();

    private int transactionCounter = 1;

    public void registerCustomer(Customer customer) {
        customers.add(customer);
    }

    public void registerAccount(Account account) {
        // put(key, value) - stores the account mapped to its ID
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

    // Transfer method - moves money from one account to another.
    // Internally it withdraws from sender and deposits to receiver,
    // but records ONE transaction of type TRANSFER (not two separate ones).
    // This matches how real systems like MTN MoMo show transfers in history.
    public Transaction transfer(int senderAccountId, int receiverAccountId, String referenceId, double amount)
            throws Exception {

        // Duplicate check first
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

        // Find sender's customer name for logging
        String senderName = "Unknown";
        for (Customer c : customers) {
            if (c.getAccounts().contains(sender)) {
                senderName = c.getFullName();
                break;
            }
        }

        // Execute: withdraw from sender (fees apply), deposit to receiver (no fee on receiving)
        sender.withdraw(amount);
        receiver.deposit(amount);

        // Mark reference as processed
        processedReferenceIds.add(referenceId);

        // Record TRANSFER_OUT on sender - money left their account
        Transaction outTx = new Transaction(transactionCounter++, referenceId, amount, TransactionType.TRANSFER_OUT);
        transactionHistory.add(outTx);
        TransactionLogger.logSuccess(outTx, senderName);
        TransactionLogger.logStatement(outTx, senderAccountId, senderName);

        // Record TRANSFER_IN on receiver - money arrived in their account
        // We use referenceId + "-IN" so it is a distinct entry but still traceable
        // to the same transfer operation
        Transaction inTx = new Transaction(transactionCounter++, referenceId + "-IN", amount, TransactionType.TRANSFER_IN);
        transactionHistory.add(inTx);

        // Find receiver name for logging
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

    // Core method - the only way a transaction gets processed in this system
    public Transaction processTransaction(int accountId, String referenceId, double amount, TransactionType type)
            throws Exception {

        // Step 1: Duplicate check - reject if this referenceId was already used
        if (processedReferenceIds.contains(referenceId)) {
            TransactionLogger.logFailure(referenceId, "Duplicate transaction rejected");
            throw new DuplicateTransactionException("Transaction already processed: " + referenceId);
        }

        // Step 2: Find the account - reject if it doesn't exist
        Account account = accounts.get(accountId);
        if (account == null) {
            TransactionLogger.logFailure(referenceId, "Account not found: " + accountId);
            throw new InvalidAmountException("Account not found: " + accountId);
        }

        // Step 3: Find the customer name for logging purposes
        // We loop through customers and check whose account list contains this account
        String customerName = "Unknown";
        for (Customer c : customers) {
            if (c.getAccounts().contains(account)) {
                customerName = c.getFullName();
                break;
            }
        }

        // Step 4: Execute the transaction on the account (polymorphism kicks in here -
        // WalletAccount and SavingsAccount each handle this differently)
        // WITHDRAW is passed as negative so the account subtracts, DEPOSIT as positive
        account.processTransaction(type == TransactionType.WITHDRAW ? -amount : amount);

        // Step 5: Mark this referenceId as done so it can never be reused
        processedReferenceIds.add(referenceId);

        // Step 6: Create the transaction record and store it
        Transaction transaction = new Transaction(transactionCounter++, referenceId, amount, type);
        transactionHistory.add(transaction);

        // Step 7: Write to log files
        TransactionLogger.logSuccess(transaction, customerName);
        TransactionLogger.logStatement(transaction, accountId, customerName);

        return transaction;
    }
}
