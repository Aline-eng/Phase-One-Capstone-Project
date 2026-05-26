package com.igirepay.lab1_oop.enums;

public enum TransactionType {
    DEPOSIT,
    WITHDRAW,
    TRANSFER_OUT,  // money left this account (sender side of a transfer)
    TRANSFER_IN    // money arrived in this account (receiver side of a transfer)
}
