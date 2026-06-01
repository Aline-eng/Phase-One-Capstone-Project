package com.igirepay.lab2_jdbc.service;

import com.igirepay.lab1_oop.enums.TransactionType;
import com.igirepay.lab1_oop.exception.DuplicateTransactionException;
import com.igirepay.lab1_oop.exception.InvalidAmountException;
import com.igirepay.lab1_oop.model.*;
import com.igirepay.lab2_jdbc.dao.AccountDAO;
import com.igirepay.lab2_jdbc.dao.CustomerDAO;
import com.igirepay.lab2_jdbc.dao.LoanDAO;
import com.igirepay.lab2_jdbc.dao.TransactionDAO;
import com.igirepay.lab2_jdbc.model.Loan;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class JdbcWalletService {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final IdempotencyService idempotencyService = new IdempotencyService();
    private final LoanDAO loanDAO = new LoanDAO();

    private int transactionCounter = 1;

    public int registerCustomer(Customer customer, String pin) throws SQLException {
        return customerDAO.save(customer, pin);
    }

    public int registerCustomer(Customer customer) throws SQLException {
        return customerDAO.save(customer);
    }

    public Customer findCustomer(int id) throws SQLException {
        return customerDAO.findById(id);
    }

    public List<Customer> findAllCustomers() throws SQLException {
        return customerDAO.findAll();
    }

    public void updateCustomer(Customer customer) throws SQLException {
        customerDAO.update(customer);
    }

    public void deleteCustomer(int id) throws SQLException {
        customerDAO.delete(id);
    }

    public int createAccount(int customerId, Account account) throws SQLException {
        return accountDAO.save(customerId, account);
    }

    public Account findAccount(int accountId) throws SQLException {
        return accountDAO.findById(accountId);
    }

    public List<Account> findAccountsByCustomer(int customerId) throws SQLException {
        return accountDAO.findByCustomerId(customerId);
    }

    public void deleteAccount(int accountId) throws SQLException {
        accountDAO.delete(accountId);
    }

    public Transaction processTransaction(int accountId, String referenceId, double amount, TransactionType type)
            throws Exception {

        referenceId = referenceId.toUpperCase().trim();
        idempotencyService.validateReference(referenceId);

        Account account = accountDAO.findById(accountId);
        if (account == null) throw new InvalidAmountException("Account not found: " + accountId);

        account.processTransaction(type == TransactionType.WITHDRAW ? -amount : amount);
        accountDAO.updateBalance(accountId, account.getBalance());

        Transaction transaction = new Transaction(transactionCounter++, referenceId, amount, type);
        transactionDAO.save(accountId, transaction);
        idempotencyService.markAsProcessed(referenceId);
        return transaction;
    }

    // Ownership-enforced version used by the UI — ensures the account belongs to the customer
    public Transaction processTransaction(int customerId, int accountId, String referenceId, double amount, TransactionType type)
            throws Exception {

        List<Account> owned = accountDAO.findByCustomerId(customerId);
        boolean owns = owned.stream().anyMatch(a -> a.getAccountId() == accountId);
        if (!owns) throw new Exception("Access denied: account " + accountId + " does not belong to your profile.");
        return processTransaction(accountId, referenceId, amount, type);
    }

    public Transaction transfer(int senderCustomerId, int senderAccountId, String receiverPhone, String referenceId, double amount)
            throws Exception {

        // Ownership check — sender must own the source account
        List<Account> owned = accountDAO.findByCustomerId(senderCustomerId);
        boolean owns = owned.stream().anyMatch(a -> a.getAccountId() == senderAccountId);
        if (!owns) throw new Exception("Access denied: account " + senderAccountId + " does not belong to your profile.");

        // Resolve receiver by phone number
        Customer receiver = customerDAO.findByPhone(receiverPhone);
        if (receiver == null) throw new Exception("No account found for phone number: " + receiverPhone);

        // Get receiver's primary (first) account
        List<Account> receiverAccounts = accountDAO.findByCustomerId(receiver.getCustomerId());
        if (receiverAccounts.isEmpty()) throw new Exception(receiver.getFullName() + " has no accounts to receive money.");
        int receiverAccountId = receiverAccounts.get(0).getAccountId();

        return transfer(senderAccountId, receiverAccountId, referenceId, amount);
    }

    // Lookup a customer by phone for transfer confirmation screen
    public Customer findCustomerByPhone(String phone) throws Exception {
        Customer c = customerDAO.findByPhone(phone);
        if (c == null) throw new Exception("No customer found with phone number: " + phone);
        return c;
    }

    public Transaction transfer(int senderAccountId, int receiverAccountId, String referenceId, double amount)
            throws Exception {

        referenceId = referenceId.toUpperCase().trim();

        idempotencyService.validateReference(referenceId);

        Account sender = accountDAO.findById(senderAccountId);
        Account receiver = accountDAO.findById(receiverAccountId);

        if (sender == null) throw new InvalidAmountException("Sender account not found: " + senderAccountId);
        if (receiver == null) throw new InvalidAmountException("Receiver account not found: " + receiverAccountId);

        sender.withdraw(amount);
        receiver.deposit(amount);

        accountDAO.updateBalance(senderAccountId, sender.getBalance());
        accountDAO.updateBalance(receiverAccountId, receiver.getBalance());

        Transaction outTx = new Transaction(transactionCounter++, referenceId, amount, TransactionType.TRANSFER_OUT);
        transactionDAO.save(senderAccountId, outTx);

        Transaction inTx = new Transaction(transactionCounter++, referenceId + "-IN", amount, TransactionType.TRANSFER_IN);
        transactionDAO.save(receiverAccountId, inTx);

        idempotencyService.markAsProcessed(referenceId);

        return outTx;
    }

    public Map<String, double[]> getDailySummary(int accountId) throws SQLException {
        return transactionDAO.getDailySummary(accountId);
    }

    public List<String> getAllProcessedRequests() throws SQLException {
        return idempotencyService.getAllProcessedRequests();
    }

    public List<Transaction> getTransactionHistory(int accountId) throws SQLException {
        return transactionDAO.findByAccountId(accountId);
    }

    public Customer loginByPhone(String phone, String pin) throws SQLException {
        Customer existing = customerDAO.findByPhone(phone);
        if (existing == null) return null;

        Customer verified = customerDAO.findByPhoneAndPin(phone, pin);
        if (verified != null) {
            try { customerDAO.resetFailedAttempts(verified.getCustomerId()); } catch (SQLException ignored) {}
            return verified;
        }

        try { customerDAO.incrementFailedAttempts(existing.getCustomerId()); } catch (SQLException ignored) {}
        return null;
    }

    public boolean isAccountLocked(String phone) throws SQLException {
        try {
            String sql = "SELECT is_locked, failed_attempts FROM customers WHERE phone_number = ?";
            try (var conn = com.igirepay.lab2_jdbc.db.DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, phone);
                var rs = stmt.executeQuery();
                if (rs.next()) {
                    boolean locked = rs.getBoolean("is_locked");
                    int attempts = rs.getInt("failed_attempts");
                    if (attempts >= 3 && !locked) {
                        Customer c = customerDAO.findByPhone(phone);
                        if (c != null) customerDAO.lockAccount(c.getCustomerId());
                        return true;
                    }
                    return locked;
                }
                return false;
            }
        } catch (SQLException e) {
            if (e.getMessage().toLowerCase().contains("column")) return false;
            throw e;
        }
    }

    public int getFailedAttempts(String phone) throws SQLException {
        try {
            String sql = "SELECT failed_attempts FROM customers WHERE phone_number = ?";
            try (var conn = com.igirepay.lab2_jdbc.db.DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, phone);
                var rs = stmt.executeQuery();
                if (rs.next()) return rs.getInt("failed_attempts");
                return 0;
            }
        } catch (SQLException e) {
            return 0;
        }
    }

    public String getRoleByPhone(String phone) throws SQLException {
        return customerDAO.findRoleByPhone(phone);
    }

    public void unlockAccount(int customerId) throws SQLException {
        customerDAO.resetFailedAttempts(customerId);
    }

    public Customer login(int customerId, String pin) throws SQLException {
        return customerDAO.findByIdAndPin(customerId, pin);
    }

    public void updatePin(int customerId, String newPin) throws SQLException {
        customerDAO.updatePin(customerId, newPin);
    }

    // Submits a loan request for a customer
    public int requestLoan(int customerId, double amount, String reason) throws SQLException {
        return loanDAO.save(customerId, amount, reason);
    }

    // Returns all loan requests for one customer
    public List<Loan> getLoansByCustomer(int customerId) throws SQLException {
        return loanDAO.findByCustomerId(customerId);
    }

    // Admin - returns all loan requests in the system
    public List<Loan> getAllLoans() throws SQLException {
        return loanDAO.findAll();
    }

    // Admin - approves or rejects a loan
    public void updateLoanStatus(int loanId, String status) throws SQLException {
        loanDAO.updateStatus(loanId, status);
    }
}
