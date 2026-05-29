package com.igirepay.lab2_jdbc.service;

import com.igirepay.lab1_oop.exception.DuplicateTransactionException;
import com.igirepay.lab2_jdbc.dao.ProcessedRequestDAO;

import java.sql.SQLException;
import java.util.List;

public class IdempotencyService {

    private final ProcessedRequestDAO processedRequestDAO = new ProcessedRequestDAO();


    public void validateReference(String referenceId) throws SQLException, DuplicateTransactionException {
        if (processedRequestDAO.exists(referenceId)) {
            throw new DuplicateTransactionException(
                "Transaction already processed with reference: " + referenceId
                + ". Each transaction must use a unique reference ID."
            );
        }
    }

    public void markAsProcessed(String referenceId) throws SQLException {
        processedRequestDAO.save(referenceId);
    }

    public List<String> getAllProcessedRequests() throws SQLException {
        return processedRequestDAO.findAll();
    }
}
