package model;

public abstract class Account {
    private int accountId;
    private double balance;
    private String accountType;

    public Account(int accountId, double balance, String accountType){
        this.accountId = accountId;
        this.balance = balance;
        this.accountType = accountType;
    }
    public void deposit(double amount) {
        balance += amount;
    }
    public abstract void withdraw(double amount);
    public abstract void processTransaction(double amount);
    public int getAccountId() {return accountId;}
    public void setAccountId(int accountId) {this.accountId = accountId;}
    public double getBalance() {return balance;}
    public void setBalance(double balance) {this.balance = balance;}
    public String getAccountType() {return accountType;}
    public void setAccountType(String accountType) {this.accountType = accountType;}

    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                ", balance=" + balance +
                ", accountType='" + accountType + '\'' +
                '}';
    }
}
