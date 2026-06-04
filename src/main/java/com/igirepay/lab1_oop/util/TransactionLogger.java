package com.igirepay.lab1_oop.util;

import com.igirepay.lab1_oop.model.Transaction;

import java.io.FileWriter;
import java.io.IOException;

public class TransactionLogger {

    private static final String HISTORY_FILE = "transaction_history.txt";
    private static final String FAILED_FILE = "failed_transactions.txt";
    private static final String STATEMENT_FILE = "account_statement.txt";

    public static void logSuccess(Transaction t, String customerName) {
        String line = "[SUCCESS]"
                + " | Customer: " + customerName
                + " | Ref: " + t.getReferenceId()
                + " | Type: " + t.getTransactionType()
                + " | Amount: " + t.getAmount() + " RWF"
                + " | Time: " + t.getTimestamp();
        writeToFile(HISTORY_FILE, line);
    }

    public static void logFailure(String referenceId, String reason) {
        String line = "[FAILED]"
                + " | Ref: " + referenceId
                + " | Reason: " + reason
                + " | Time: " + java.time.LocalDateTime.now();
        writeToFile(FAILED_FILE, line);
    }

    public static void logStatement(Transaction t, int accountId, String customerName) {
        String line = "Account: " + accountId
                + " | Customer: " + customerName
                + " | Ref: " + t.getReferenceId()
                + " | Type: " + t.getTransactionType()
                + " | Amount: " + t.getAmount() + " RWF"
                + " | Time: " + t.getTimestamp();
        writeToFile(STATEMENT_FILE, line);
    }

    private static void writeToFile(String filename, String content) {
        try (FileWriter writer = new FileWriter(filename, true)) {
            writer.write(content + "\n");
        } catch (IOException e) {
            System.out.println("Warning: Could not write to log file [" + filename + "]: " + e.getMessage());
        }
    }
}
