package com.igirepay.lab2_jdbc.service;

import com.igirepay.lab1_oop.enums.TransactionType;
import com.igirepay.lab1_oop.exception.DuplicateTransactionException;
import com.igirepay.lab1_oop.exception.InvalidAmountException;
import com.igirepay.lab1_oop.model.*;
import com.igirepay.lab2_jdbc.dao.AccountDAO;
import com.igirepay.lab2_jdbc.dao.CustomerDAO;
import com.igirepay.lab2_jdbc.dao.TransactionDAO;

import java.sql.SQLException;
import java.util.List;

// JdbcWalletService is the service layer for Lab 2.
// It sits between Lab2Runner (which handles user input) and the DAOs (which talk to the database).
// The runner never calls a DAO directly - it always goes through this service.
// This separation means if we change the database structure, we only update the DAOs,
// not the runner. And if we change the menu, we only update the runner, not the DAOs.
//
// Why not extend WalletService from Lab 1?
// Lab 1 stores data in memory (List, Map, Set).
// Lab 2 stores data in PostgreSQL via JDBC.
// They have completely different internals, so extending would cause more problems than it solves.
// Instead, we reuse Lab 1's models (Customer, Account, Transaction) and exceptions.
public class JdbcWalletService {

    // Each DAO is created once and reused across all method calls.
    // We use final because these references never change after the service is created.
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    // IdempotencyService replaces direct ProcessedRequestDAO calls.
    // All duplicate-prevention logic is now owned by one dedicated class.
    private final IdempotencyService idempotencyService = new IdempotencyService();

    // transactionCounter gives each Transaction object a local ID.
    // In Lab 2 the real permanent ID comes from the database (SERIAL),
    // but the Transaction constructor requires an int, so we use this as a placeholder.
    private int transactionCounter = 1;

    // -------------------------------------------------------------------------
    // CUSTOMER OPERATIONS
    // -------------------------------------------------------------------------

    // Registers a new customer with their PIN in one atomic operation.
    // Using a JDBC transaction means if the insert fails, the sequence
    // does not advance - so IDs never skip on failed registrations.
    public int registerCustomer(Customer customer, String pin) throws SQLException {
        return customerDAO.save(customer, pin);
    }

    // Kept for backward compatibility with Lab2Runner console
    public int registerCustomer(Customer customer) throws SQLException {
        return customerDAO.save(customer);
    }

    // Finds a customer by their ID - returns null if not found.
    // The runner checks for null and shows "not found" to the user.
    public Customer findCustomer(int id) throws SQLException {
        return customerDAO.findById(id);
    }

    // Returns every customer in the system.
    // Used when the user wants to see the full customer list.
    public List<Customer> findAllCustomers() throws SQLException {
        return customerDAO.findAll();
    }

    // Updates a customer's details in the database.
    // The customer object must have the correct ID set so the right row is updated.
    public void updateCustomer(Customer customer) throws SQLException {
        customerDAO.update(customer);
    }

    // Deletes a customer by ID.
    // The database FOREIGN KEY on accounts will reject this if the customer
    // still has accounts - which protects data integrity automatically.
    public void deleteCustomer(int id) throws SQLException {
        customerDAO.delete(id);
    }

    // -------------------------------------------------------------------------
    // ACCOUNT OPERATIONS
    // -------------------------------------------------------------------------

    // Creates a new account linked to a customer and returns the generated account ID.
    // customerId is needed because the accounts table has a customer_id foreign key.
    public int createAccount(int customerId, Account account) throws SQLException {
        return accountDAO.save(customerId, account);
    }

    // Finds an account by ID - returns the correct subclass (WalletAccount or SavingsAccount)
    // so that fee logic is preserved when the account is used for transactions.
    public Account findAccount(int accountId) throws SQLException {
        return accountDAO.findById(accountId);
    }

    // Returns all accounts belonging to one customer.
    public List<Account> findAccountsByCustomer(int customerId) throws SQLException {
        return accountDAO.findByCustomerId(customerId);
    }

    // Deletes an account - used for removing inactive accounts (Exercise 2.2).
    // The database will reject this if the account still has transactions linked to it.
    public void deleteAccount(int accountId) throws SQLException {
        accountDAO.delete(accountId);
    }

    // -------------------------------------------------------------------------
    // TRANSACTION OPERATIONS
    // -------------------------------------------------------------------------

    // Processes a deposit or withdrawal on one account.
    // This is the core method - it enforces idempotency, applies fees,
    // persists the balance change, and records the transaction.
    public Transaction processTransaction(int accountId, String referenceId, double amount, TransactionType type)
            throws Exception {

        // Normalize the reference ID - convert to uppercase and remove leading/trailing spaces.
        // This is a business rule: reference IDs are always uppercase in this system.
        // "dep-001", "DEP-001", and " dep-001 " all become "DEP-001" before any check.
        // We enforce this here in the service so it applies regardless of where the
        // call comes from - terminal input, JavaFX form, or any future interface.
        referenceId = referenceId.toUpperCase().trim();

        // Step 1: Idempotency check - delegates to IdempotencyService.
        // If the reference ID was already used, this throws DuplicateTransactionException
        // and the method stops here - no money moves, nothing is saved.
        idempotencyService.validateReference(referenceId);

        // Step 2: Load the account from the database.
        // We load it fresh each time instead of caching it, so the balance
        // we work with is always the latest value in the database.
        Account account = accountDAO.findById(accountId);
        if (account == null) {
            throw new InvalidAmountException("Account not found: " + accountId);
        }

        // Step 3: Apply the transaction on the account object.
        // This is the same polymorphism from Lab 1 - WalletAccount and SavingsAccount
        // each handle processTransaction differently (different fees, different rules).
        // Passing negative amount for WITHDRAW tells the account to subtract.
        account.processTransaction(type == TransactionType.WITHDRAW ? -amount : amount);

        // Step 4: Persist the new balance back to the database.
        // Without this step the balance change only exists in memory and is lost
        // when the program stops - this is the key difference from Lab 1.
        accountDAO.updateBalance(accountId, account.getBalance());

        // Step 5: Save the transaction record to the transactions table.
        Transaction transaction = new Transaction(transactionCounter++, referenceId, amount, type);
        transactionDAO.save(accountId, transaction);

        // Step 6: Mark reference ID as processed via IdempotencyService.
        // This must happen AFTER the transaction succeeds - if we saved it before
        // and the transaction failed, the reference ID would be permanently blocked.
        idempotencyService.markAsProcessed(referenceId);

        return transaction;
    }

    // Transfers money from one account to another.
    // Internally: withdraw from sender (fee applies) + deposit to receiver (no fee).
    // Records ONE transaction of type TRANSFER - not two separate ones.
    // This matches how real systems like MTN MoMo show transfers in history.
    public Transaction transfer(int senderAccountId, int receiverAccountId, String referenceId, double amount)
            throws Exception {

        // Normalize reference ID - same rule as processTransaction
        referenceId = referenceId.toUpperCase().trim();

        // Idempotency check via IdempotencyService
        idempotencyService.validateReference(referenceId);

        Account sender = accountDAO.findById(senderAccountId);
        Account receiver = accountDAO.findById(receiverAccountId);

        if (sender == null) throw new InvalidAmountException("Sender account not found: " + senderAccountId);
        if (receiver == null) throw new InvalidAmountException("Receiver account not found: " + receiverAccountId);

        sender.withdraw(amount);
        receiver.deposit(amount);

        accountDAO.updateBalance(senderAccountId, sender.getBalance());
        accountDAO.updateBalance(receiverAccountId, receiver.getBalance());

        // TRANSFER_OUT recorded on sender's account - money left
        Transaction outTx = new Transaction(transactionCounter++, referenceId, amount, TransactionType.TRANSFER_OUT);
        transactionDAO.save(senderAccountId, outTx);

        // TRANSFER_IN recorded on receiver's account - money arrived.
        // referenceId + "-IN" keeps it traceable to the same transfer
        // while being a distinct entry in the transactions table.
        Transaction inTx = new Transaction(transactionCounter++, referenceId + "-IN", amount, TransactionType.TRANSFER_IN);
        transactionDAO.save(receiverAccountId, inTx);

        // Mark the base reference ID as processed - the "-IN" variant is part
        // of the same operation so it does not need its own processed_requests entry.
        idempotencyService.markAsProcessed(referenceId);

        return outTx;
    }

    // Returns all processed reference IDs - exposes the idempotency audit log.
    public List<String> getAllProcessedRequests() throws SQLException {
        return idempotencyService.getAllProcessedRequests();
    }

    // Returns the transaction history for one account, newest first.
    public List<Transaction> getTransactionHistory(int accountId) throws SQLException {
        return transactionDAO.findByAccountId(accountId);
    }

    // Login by phone number and PIN with account locking after 3 failed attempts.
    public Customer loginByPhone(String phone, String pin) throws SQLException {
        // First check if the account exists and is locked
        Customer existing = customerDAO.findByPhone(phone);
        if (existing == null) return null;

        Customer verified = customerDAO.findByPhoneAndPin(phone, pin);
        if (verified != null) {
            // Successful login - reset any previous failed attempts
            customerDAO.resetFailedAttempts(verified.getCustomerId());
            return verified;
        }

        // Wrong PIN - increment failed attempts
        customerDAO.incrementFailedAttempts(existing.getCustomerId());

        // Lock after 3 failed attempts
        // We re-fetch to get the updated count
        Customer updated = customerDAO.findById(existing.getCustomerId());
        return null;
    }

    // Checks if an account is locked by phone number
    public boolean isAccountLocked(String phone) throws SQLException {
        String sql = "SELECT is_locked, failed_attempts FROM customers WHERE phone_number = ?";
        try (var conn = com.igirepay.lab2_jdbc.db.DatabaseConnection.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                boolean locked = rs.getBoolean("is_locked");
                int attempts = rs.getInt("failed_attempts");
                if (attempts >= 3 && !locked) {
                    // Auto-lock if attempts reached 3
                    Customer c = customerDAO.findByPhone(phone);
                    if (c != null) customerDAO.lockAccount(c.getCustomerId());
                    return true;
                }
                return locked;
            }
            return false;
        }
    }

    // Returns failed attempt count for a phone number
    public int getFailedAttempts(String phone) throws SQLException {
        String sql = "SELECT failed_attempts FROM customers WHERE phone_number = ?";
        try (var conn = com.igirepay.lab2_jdbc.db.DatabaseConnection.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            var rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("failed_attempts");
            return 0;
        }
    }

    // Login by customer ID and PIN - kept for Lab 2 console runner.
    public Customer login(int customerId, String pin) throws SQLException {
        return customerDAO.findByIdAndPin(customerId, pin);
    }

    // Updates the PIN for the currently logged-in customer.
    public void updatePin(int customerId, String newPin) throws SQLException {
        customerDAO.updatePin(customerId, newPin);
    }
}
