package com.igirepay;

import javafx.application.Application;

// JavaFX needs this separate Launcher class because when using modules,
// the JVM requires the main class to NOT extend Application directly.
// This class simply delegates to MainApp which does extend Application.
public class Launcher {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
