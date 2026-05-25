package com.igirepay.lab1_oop.util;

import java.time.LocalDate;

public class TransactionFee {
    public static double getWalletFee(double amount) {
        if (amount <= 5000) {
            return 20;
        } else if (amount <= 10000) {
            return 100;
        } else if (amount <= 100000) {
            return 200;
        } else if (amount <= 500000) {
            return 400;
        } else {
            return 500;
        }
    }
    public static double getSavingsFee(double amount) {
        LocalDate today = LocalDate.now();
        int lastDayOfMonth = today.lengthOfMonth();
        int today_day = today.getDayOfMonth();

        if (today_day >= lastDayOfMonth - 4) {
            return 0;
        }
        return amount * 0.08;
    }
}
