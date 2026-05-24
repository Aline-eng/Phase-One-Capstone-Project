package model;

public class SavingsAccount extends Account {
    private static final double WITHDRAWAL_FEE = 100;
    private static final double MINIMUM_BALANCE = 500;
    public SavingsAccount(int accountId, double balance, String accountType) {
        super(accountId, balance, "Savings Account");
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Invalid withdrawal amount!");
            return;
        }
        double totalAmount = amount + WITHDRAWAL_FEE;
        if (getBalance() - totalAmount < MINIMUM_BALANCE) {
            System.out.println("Cannot withdraw!");
            System.out.println("Minimum balance requirement violated!");
            return;
        }
        setBalance(getBalance() - totalAmount);
        System.out.println(amount + " withdrawn successfully!");
        System.out.println("Withdrawal fee applied: "+ WITHDRAWAL_FEE);

    }

    @Override
    public void processTransaction(double amount) {
        System.out.println("Processing savings transaction...");
        deposit(amount);
        System.out.println("Savings transaction processed successfully!");
    }
}
