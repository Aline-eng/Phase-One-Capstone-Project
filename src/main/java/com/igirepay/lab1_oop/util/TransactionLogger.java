package com.igirepay.lab1_oop.util;

import com.igirepay.lab1_oop.model.Transaction;

import java.io.FileWriter;
import java.io.IOException;

public class TransactionLogger {

    // Each constant holds the name of a separate log file
    private static final String HISTORY_FILE = "transaction_history.txt";
    private static final String FAILED_FILE = "failed_transactions.txt";
    private static final String STATEMENT_FILE = "account_statement.txt";

    // Called when a transaction succeeds - writes to the history file
    public static void logSuccess(Transaction t, String customerName) {
        String line = "[SUCCESS]"
                + " | Customer: " + customerName
                + " | Ref: " + t.getReferenceId()
                + " | Type: " + t.getTransactionType()
                + " | Amount: " + t.getAmount() + " RWF"
                + " | Time: " + t.getTimestamp();
        writeToFile(HISTORY_FILE, line);
    }

    // Called when a transaction fails - writes to the failed file
    public static void logFailure(String referenceId, String reason) {
        String line = "[FAILED]"
                + " | Ref: " + referenceId
                + " | Reason: " + reason
                + " | Time: " + java.time.LocalDateTime.now();
        writeToFile(FAILED_FILE, line);
    }

    // Called after every success - writes to the account statement file
    public static void logStatement(Transaction t, int accountId, String customerName) {
        String line = "Account: " + accountId
                + " | Customer: " + customerName
                + " | Ref: " + t.getReferenceId()
                + " | Type: " + t.getTransactionType()
                + " | Amount: " + t.getAmount() + " RWF"
                + " | Time: " + t.getTimestamp();
        writeToFile(STATEMENT_FILE, line);
    }

    // Private helper used by all three methods above.
    // FileWriter(filename, true) means APPEND mode - new lines are added at the end,
    // existing content is never erased.
    // try-with-resources automatically closes the file even if an error occurs.
    private static void writeToFile(String filename, String content) {
        try (FileWriter writer = new FileWriter(filename, true)) {
            writer.write(content + "\n");
        } catch (IOException e) {
            System.out.println("Warning: Could not write to log file [" + filename + "]: " + e.getMessage());
        }
    }
}
