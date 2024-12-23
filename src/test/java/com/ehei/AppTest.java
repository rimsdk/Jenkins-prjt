package com.ehei;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BankAccountTest {
    @Test
    void testDeposit() {
        BankAccount account = new BankAccount("John Doe", 1000.0);
        account.deposit(500.0);
        assertEquals(1500.0, account.getBalance());
    }

    @Test
    void testWithdraw() {
        BankAccount account = new BankAccount("John Doe", 1000.0);
        account.withdraw(300.0);
        assertEquals(700.0, account.getBalance());
    }

    @Test
    void testWithdrawInsufficientFunds() {
        BankAccount account = new BankAccount("John Doe", 1000.0);
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(1500.0));
    }
}
