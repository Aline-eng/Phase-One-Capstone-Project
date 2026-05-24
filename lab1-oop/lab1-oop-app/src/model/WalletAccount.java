package model;

public class WalletAccount extends Account{
    public WalletAccount(int accountId, double balance) {
        super(accountId, balance, "Wallet Account");
    }
    @Override
    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Amount must be greater than 0. Invalid withdrawal amount!!");
            return;
        }
        if (amount > getBalance()) {
            System.out.println("Insufficient funds to withdrawal!!");
            return;
        }
        setBalance(getBalance() - amount);
        System.out.println(amount + " withdrawn successfully!!");
    }

    @Override
    public void processTransaction(double amount) {
        System.out.println("Processing wallet transaction!");
        deposit(amount);
        System.out.println("Wallet transaction processed successfully!!");
    }
}
