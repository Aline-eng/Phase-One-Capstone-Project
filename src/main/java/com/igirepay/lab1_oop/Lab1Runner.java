package com.igirepay.lab1_oop;

import com.igirepay.lab1_oop.enums.TransactionType;
import com.igirepay.lab1_oop.model.*;
import com.igirepay.lab1_oop.service.WalletService;

import java.util.Scanner;

public class Lab1Runner {

    public static void run() {
        // Scanner reads input from the keyboard (System.in)
        Scanner scanner = new Scanner(System.in);
        WalletService service = new WalletService();

        // Pre-load two customers with accounts so we have data to work with
        Customer alice = new Customer(1, "Alice Uwase", "alice@email.com", "0781000001");
        Customer bob = new Customer(2, "Bob Mugisha", "bob@email.com", "0782000002");

        WalletAccount aliceWallet = new WalletAccount(101, 50000);
        SavingsAccount aliceSavings = new SavingsAccount(102, 20000);
        WalletAccount bobWallet = new WalletAccount(201, 30000);

        alice.addAccount(aliceWallet);
        alice.addAccount(aliceSavings);
        bob.addAccount(bobWallet);

        service.registerCustomer(alice);
        service.registerCustomer(bob);
        service.registerAccount(aliceWallet);
        service.registerAccount(aliceSavings);
        service.registerAccount(bobWallet);

        System.out.println("========== IgirePay - Lab 1 Demo ==========");
        System.out.println("Customers loaded: Alice (Wallet: 101, Savings: 102) | Bob (Wallet: 201)");

        boolean running = true;

        while (running) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. Check account balance");
            System.out.println("2. Deposit money");
            System.out.println("3. Withdraw money");
            System.out.println("4. Transfer money");
            System.out.println("5. View transaction history");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");

            // nextLine() reads the full line the user types including spaces
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> {
                    System.out.print("Enter account ID: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    Account acc = service.getAccount(id);
                    if (acc == null) {
                        System.out.println("Account not found.");
                    } else {
                        System.out.println("Balance: " + acc.getBalance() + " RWF (" + acc.getAccountType() + ")");
                    }
                }

                case "2" -> {
                    System.out.print("Enter account ID: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter amount to deposit: ");
                    double amount = Double.parseDouble(scanner.nextLine());
                    // Reference ID is auto-generated - the user should never set this manually.
                    // UUID.randomUUID() produces a globally unique string like: a3f1c2d4-...
                    String ref = java.util.UUID.randomUUID().toString();
                    try {
                        service.processTransaction(id, ref, amount, TransactionType.DEPOSIT);
                        System.out.println("Deposit successful! New balance: " + service.getAccount(id).getBalance() + " RWF");
                    } catch (Exception e) {
                        System.out.println("Deposit failed: " + e.getMessage());
                    }
                }

                case "3" -> {
                    System.out.print("Enter account ID: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter amount to withdraw: ");
                    double amount = Double.parseDouble(scanner.nextLine());
                    String ref = java.util.UUID.randomUUID().toString();
                    try {
                        service.processTransaction(id, ref, amount, TransactionType.WITHDRAW);
                        System.out.println("Withdrawal successful! New balance: " + service.getAccount(id).getBalance() + " RWF");
                    } catch (Exception e) {
                        System.out.println("Withdrawal failed: " + e.getMessage());
                    }
                }

                case "4" -> {
                    System.out.print("Enter sender account ID: ");
                    int senderId = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter receiver account ID: ");
                    int receiverId = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter amount to transfer: ");
                    double amount = Double.parseDouble(scanner.nextLine());
                    // One UUID for the whole transfer - both sides share the same reference
                    String ref = java.util.UUID.randomUUID().toString();
                    try {
                        service.transfer(senderId, receiverId, ref, amount);
                        System.out.println("Transfer successful!");
                        System.out.println("Sender balance: " + service.getAccount(senderId).getBalance() + " RWF");
                        System.out.println("Receiver balance: " + service.getAccount(receiverId).getBalance() + " RWF");
                    } catch (Exception e) {
                        System.out.println("Transfer failed: " + e.getMessage());
                    }
                }

                case "5" -> {
                    var history = service.getTransactionHistory();
                    if (history.isEmpty()) {
                        System.out.println("No transactions yet.");
                    } else {
                        System.out.println("--- Transaction History ---");
                        // forEach with method reference - prints each transaction using its toString()
                        history.forEach(System.out::println);
                    }
                }

                case "6" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }

                default -> System.out.println("Invalid option. Please choose 1-6.");
            }
        }

        // Always close the scanner when done to release the keyboard resource
        scanner.close();
    }
}
