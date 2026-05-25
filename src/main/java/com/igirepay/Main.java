package com.igirepay;

import com.igirepay.lab1.Lab1Runner;
import com.igirepay.lab2.Lab2Runner;
import com.igirepay.lab3.Lab3Runner;

public class Main {
    public static void main(String[] args) {
        // Run each lab in sequence.
        // Once Lab 3 is complete, only Lab3Runner.run() will be needed (it launches the JavaFX GUI).
        Lab1Runner.run();
        Lab2Runner.run();
        Lab3Runner.run();
    }
}
