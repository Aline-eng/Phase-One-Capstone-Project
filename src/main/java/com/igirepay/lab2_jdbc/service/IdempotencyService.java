package com.igirepay.lab2_jdbc.service;

import com.igirepay.lab1_oop.exception.DuplicateTransactionException;
import com.igirepay.lab2_jdbc.dao.ProcessedRequestDAO;

import java.sql.SQLException;
import java.util.List;

// IdempotencyService owns all idempotency logic in one place.
//
// What is idempotency?
// An operation is idempotent if running it multiple times produces the same result
// as running it once. In payments this means: if the same transaction request
// arrives twice (network retry, user double-click, system error), it must only
// be processed ONCE. The second attempt must be silently rejected.
//
// How we implement it:
// Every transaction has a unique reference ID chosen by the user.
// Before processing, we check if that reference ID already exists in the
// processed_requests table. If it does, we reject the request immediately.
// If it does not, we process the transaction and then save the reference ID
// so future attempts with the same ID are rejected.
//
// Why a separate class instead of keeping this inside JdbcWalletService?
// Single Responsibility Principle - JdbcWalletService coordinates the full
// transaction flow. IdempotencyService has one job: guard against duplicates.
// If we ever change how idempotency works (e.g. add expiry time to reference IDs),
// we only change this class, nothing else.
public class IdempotencyService {

    private final ProcessedRequestDAO processedRequestDAO = new ProcessedRequestDAO();

    // Checks if the reference ID was already used and throws if it was.
    // Called at the START of every transaction before any money moves.
    // We throw DuplicateTransactionException so the caller knows exactly
    // what went wrong and can show the right message to the user.
    public void validateReference(String referenceId) throws SQLException, DuplicateTransactionException {
        if (processedRequestDAO.exists(referenceId)) {
            throw new DuplicateTransactionException(
                "Transaction already processed with reference: " + referenceId
                + ". Each transaction must use a unique reference ID."
            );
        }
    }

    // Marks a reference ID as processed after a transaction succeeds.
    // Called at the END of every transaction after all operations complete.
    // This order matters - if we marked it before and the transaction failed,
    // the reference ID would be permanently blocked even though nothing happened.
    public void markAsProcessed(String referenceId) throws SQLException {
        processedRequestDAO.save(referenceId);
    }

    // Returns all processed reference IDs - useful for auditing and debugging.
    // An admin can see every reference ID that was ever processed in the system.
    public List<String> getAllProcessedRequests() throws SQLException {
        return processedRequestDAO.findAll();
    }
}
