package com.igirepay.lab1_oop.model;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    private int customerId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private List<Account> accounts;

    public Customer(int customerId, String fullName, String email, String phoneNumber) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.accounts = new ArrayList<>();
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public List<Account> getAccounts() { return accounts; }

    @Override
    public String toString() {
        return "Customer{id=" + customerId + ", name='" + fullName + "', email='" + email + "', phone='" + phoneNumber + "'}";
    }
}
