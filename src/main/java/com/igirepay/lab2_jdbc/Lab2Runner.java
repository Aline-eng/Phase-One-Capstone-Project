package com.igirepay.lab2_jdbc;

import com.igirepay.lab1_oop.enums.TransactionType;
import com.igirepay.lab1_oop.model.*;
import com.igirepay.lab2_jdbc.service.JdbcWalletService;

import java.util.List;
import java.util.Scanner;

// Lab2Runner is the entry point for Lab 2.
// It handles all user input and output - nothing else.
// All business logic lives in JdbcWalletService.
// All database logic lives in the DAOs.
// This separation is called layered architecture.
public class Lab2Runner {

    public static void run() {
        Scanner scanner = new Scanner(System.in);
        JdbcWalletService service = new JdbcWalletService();

        System.out.println("========== IgirePay - Lab 2 (JDBC) ==========");

        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            // One try-catch wraps the whole switch so every operation's
            // exceptions are caught in one place - avoids repeating
            // try-catch inside every single case.
            try {
                switch (choice) {

                    // ---- CUSTOMER MANAGEMENT ----

                    case "1" -> {
                        System.out.print("Full name: ");
                        String name = scanner.nextLine();
                        System.out.print("Email: ");
                        String email = scanner.nextLine();
                        System.out.print("Phone number: ");
                        String phone = scanner.nextLine();
                        // Customer ID is 0 here because the database will assign the real ID
                        int id = service.registerCustomer(new Customer(0, name, email, phone));
                        System.out.println("Customer registered successfully. ID: " + id);
                    }

                    case "2" -> {
                        System.out.print("Customer ID to update: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        // First check the customer exists before asking for new details
                        Customer existing = service.findCustomer(id);
                        if (existing == null) {
                            System.out.println("Customer not found.");
                            break;
                        }
                        System.out.print("New full name: ");
                        String name = scanner.nextLine();
                        System.out.print("New email: ");
                        String email = scanner.nextLine();
                        System.out.print("New phone number: ");
                        String phone = scanner.nextLine();
                        service.updateCustomer(new Customer(id, name, email, phone));
                        System.out.println("Customer updated successfully.");
                    }

                    case "3" -> {
                        System.out.print("Customer ID: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        Customer c = service.findCustomer(id);
                        System.out.println(c != null ? c : "Customer not found.");
                    }

                    case "4" -> {
                        List<Customer> customers = service.findAllCustomers();
                        if (customers.isEmpty()) {
                            System.out.println("No customers registered yet.");
                        } else {
                            customers.forEach(System.out::println);
                        }
                    }

                    case "5" -> {
                        System.out.print("Customer ID to delete: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        service.deleteCustomer(id);
                        System.out.println("Customer deleted successfully.");
                    }

                    // ---- ACCOUNT MANAGEMENT ----

                    case "6" -> {
                        System.out.print("Customer ID: ");
                        int cid = Integer.parseInt(scanner.nextLine());
                        System.out.print("Initial balance (RWF): ");
                        double bal = Double.parseDouble(scanner.nextLine());
                        // Account ID is 0 - the database assigns the real ID
                        int aid = service.createAccount(cid, new WalletAccount(0, bal));
                        System.out.println("Wallet account created. Account ID: " + aid);
                    }

                    case "7" -> {
                        System.out.print("Customer ID: ");
                        int cid = Integer.parseInt(scanner.nextLine());
                        System.out.print("Initial balance (RWF): ");
                        double bal = Double.parseDouble(scanner.nextLine());
                        int aid = service.createAccount(cid, new SavingsAccount(0, bal));
                        System.out.println("Savings account created. Account ID: " + aid);
                    }

                    case "8" -> {
                        System.out.print("Account ID: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        Account acc = service.findAccount(id);
                        if (acc == null) {
                            System.out.println("Account not found.");
                        } else {
                            System.out.println("Type: " + acc.getAccountType()
                                    + " | Balance: " + acc.getBalance() + " RWF");
                        }
                    }

                    case "9" -> {
                        System.out.print("Customer ID: ");
                        int cid = Integer.parseInt(scanner.nextLine());
                        List<Account> accounts = service.findAccountsByCustomer(cid);
                        if (accounts.isEmpty()) {
                            System.out.println("No accounts found for this customer.");
                        } else {
                            accounts.forEach(a -> System.out.println(
                                    "ID: " + a.getAccountId()
                                    + " | Type: " + a.getAccountType()
                                    + " | Balance: " + a.getBalance() + " RWF"));
                        }
                    }

                    case "10" -> {
                        System.out.print("Account ID to delete: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        service.deleteAccount(id);
                        System.out.println("Account deleted successfully.");
                    }

                    // ---- TRANSACTION MANAGEMENT ----

                    case "11" -> {
                        System.out.print("Account ID: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        System.out.print("Amount (RWF): ");
                        double amount = Double.parseDouble(scanner.nextLine());
                        System.out.print("Reference ID: ");
                        String ref = scanner.nextLine();
                        service.processTransaction(id, ref, amount, TransactionType.DEPOSIT);
                        System.out.println("Deposit successful! New balance: "
                                + service.findAccount(id).getBalance() + " RWF");
                    }

                    case "12" -> {
                        System.out.print("Account ID: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        System.out.print("Amount (RWF): ");
                        double amount = Double.parseDouble(scanner.nextLine());
                        System.out.print("Reference ID: ");
                        String ref = scanner.nextLine();
                        service.processTransaction(id, ref, amount, TransactionType.WITHDRAW);
                        System.out.println("Withdrawal successful! New balance: "
                                + service.findAccount(id).getBalance() + " RWF");
                    }

                    case "13" -> {
                        System.out.print("Sender account ID: ");
                        int from = Integer.parseInt(scanner.nextLine());
                        System.out.print("Receiver account ID: ");
                        int to = Integer.parseInt(scanner.nextLine());
                        System.out.print("Amount (RWF): ");
                        double amount = Double.parseDouble(scanner.nextLine());
                        System.out.print("Reference ID: ");
                        String ref = scanner.nextLine();
                        service.transfer(from, to, ref, amount);
                        System.out.println("Transfer successful!");
                        System.out.println("Sender balance:   " + service.findAccount(from).getBalance() + " RWF");
                        System.out.println("Receiver balance: " + service.findAccount(to).getBalance() + " RWF");
                    }

                    case "14" -> {
                        System.out.print("Account ID: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        List<Transaction> history = service.getTransactionHistory(id);
                        if (history.isEmpty()) {
                            System.out.println("No transactions found for account " + id);
                        } else {
                            System.out.println("--- Transaction History (newest first) ---");
                            history.forEach(System.out::println);
                        }
                    }

                    case "15" -> {
                        List<String> processed = service.getAllProcessedRequests();
                        if (processed.isEmpty()) {
                            System.out.println("No processed requests yet.");
                        } else {
                            System.out.println("--- Processed Requests (Idempotency Log) ---");
                            processed.forEach(System.out::println);
                        }
                    }

                    case "16" -> {
                        System.out.println("Goodbye!");
                        running = false;
                    }

                    default -> System.out.println("Invalid option. Please choose 1-16.");
                }

            } catch (NumberFormatException e) {
                // Thrown when the user types letters where a number is expected
                System.out.println("Invalid input. Please enter a valid number.");
            } catch (Exception e) {
                // Catches SQLException, DuplicateTransactionException,
                // InsufficientBalanceException, InvalidAmountException
                System.out.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    // Extracted into its own method to keep the run() method clean.
    // Printing the menu is a separate concern from handling the choice.
    private static void printMenu() {
        System.out.println("\n--- MENU ---");
        System.out.println("-- Customer Management --");
        System.out.println(" 1.  Register customer");
        System.out.println(" 2.  Update customer");
        System.out.println(" 3.  Find customer by ID");
        System.out.println(" 4.  View all customers");
        System.out.println(" 5.  Delete customer");
        System.out.println("-- Account Management --");
        System.out.println(" 6.  Create wallet account");
        System.out.println(" 7.  Create savings account");
        System.out.println(" 8.  Check account balance");
        System.out.println(" 9.  View customer accounts");
        System.out.println(" 10. Delete account");
        System.out.println("-- Transactions --");
        System.out.println(" 11. Deposit");
        System.out.println(" 12. Withdraw");
        System.out.println(" 13. Transfer");
        System.out.println(" 14. View transaction history");
        System.out.println(" 15. View processed requests (idempotency log)");
        System.out.println(" 16. Exit");
        System.out.print("Choose: ");
    }
}
