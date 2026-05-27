package com.igirepay;

import com.igirepay.lab1_oop.Lab1Runner;
import com.igirepay.lab2_jdbc.Lab2Runner;
import com.igirepay.lab3_mini_capstone.Lab3Runner;

// Entry point for the whole project.
// Uncomment the lab you want to run.
// Lab 3 launches the full JavaFX GUI application.
public class Main {
    public static void main(String[] args) {
        // Lab1Runner.run();  // OOP console demo
        // Lab2Runner.run();  // JDBC console demo
        Lab3Runner.run();     // JavaFX GUI - the full application
    }
}
