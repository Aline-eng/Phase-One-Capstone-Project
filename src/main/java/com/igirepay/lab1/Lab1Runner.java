package com.igirepay.lab1;

import com.igirepay.lab1.enums.TransactionType;
import com.igirepay.lab1.exception.DuplicateTransactionException;
import com.igirepay.lab1.model.*;
import com.igirepay.lab1.service.PaymentService;

public class Lab1Runner {

    public static void run() {
        System.out.println("========== LAB 1: OOP Demo ==========");

        PaymentService service = new PaymentService();

        // Create customers and accounts
        Customer alice = new Customer(1, "Alice Uwase", "alice@email.com", "0781000001");
        WalletAccount wallet = new WalletAccount(101, 2000);
        SavingsAccount savings = new SavingsAccount(102, 3000);

        alice.addAccount(wallet);
        alice.addAccount(savings);
        service.registerCustomer(alice);
        service.registerAccount(wallet);
        service.registerAccount(savings);

        System.out.println("Customer: " + alice);

        // Process a deposit on wallet
        try {
            var t1 = service.processTransaction(101, "REF-001", 500, TransactionType.DEPOSIT);
            System.out.println("Success: " + t1);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        // Try the same referenceId again - should be rejected (idempotency)
        try {
            service.processTransaction(101, "REF-001", 500, TransactionType.DEPOSIT);
        } catch (DuplicateTransactionException e) {
            System.out.println("Duplicate caught: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        // Withdraw from savings - polymorphism: SavingsAccount applies fee + min balance check
        try {
            var t2 = service.processTransaction(102, "REF-002", 1000, TransactionType.WITHDRAW);
            System.out.println("Success: " + t2);
            System.out.println("Savings balance after withdrawal: " + savings.getBalance());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("--- Transaction History ---");
        service.getTransactionHistory().forEach(System.out::println);

        System.out.println("--- Failed Logs ---");
        service.getFailedTransactionLogs().forEach(System.out::println);

        System.out.println("========== LAB 1 END ==========\n");
    }
}
