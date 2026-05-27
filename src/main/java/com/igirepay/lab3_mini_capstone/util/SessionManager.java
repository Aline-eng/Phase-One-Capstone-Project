package com.igirepay.lab3_mini_capstone.util;

import com.igirepay.lab1_oop.model.Customer;

// SessionManager holds the currently logged-in customer.
// It uses the Singleton pattern - only one instance exists in the whole app.
// Any controller can call SessionManager.getInstance().getCurrentCustomer()
// to know who is logged in without passing the customer object around.
public class SessionManager {
    // The single instance - created once, reused everywhere
    private static SessionManager instance;
    // The customer who is currently logged in
    private Customer currentCustomer;
    // Private constructor prevents anyone from doing new SessionManager()
    private SessionManager() {}
    // Returns the single instance, creating it if it doesn't exist yet
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    public Customer getCurrentCustomer() { return currentCustomer; }

    public void setCurrentCustomer(Customer customer) { this.currentCustomer = customer; }

    // Called on logout - clears the session
    public void clear() { this.currentCustomer = null; }

}
