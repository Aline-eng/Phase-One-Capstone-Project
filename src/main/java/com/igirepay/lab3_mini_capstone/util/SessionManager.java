package com.igirepay.lab3_mini_capstone.util;

import com.igirepay.lab1_oop.model.Customer;

public class SessionManager {

    private static SessionManager instance;
    private Customer currentCustomer;
    private String currentRole = "USER";

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public Customer getCurrentCustomer() { return currentCustomer; }

    public void setCurrentCustomer(Customer customer) {
        this.currentCustomer = customer;
    }

    public String getRole() { return currentRole; }

    public void setRole(String role) {
        this.currentRole = role != null ? role : "USER";
    }

    public boolean isAdmin() {
        return "ADMIN".equals(currentRole);
    }

    public void clear() {
        this.currentCustomer = null;
        this.currentRole = "USER";
    }
}
