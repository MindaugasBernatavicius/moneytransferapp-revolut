package com.revolut.moneytransferapp.model;

import java.math.BigDecimal;

public class Account {

    private int id;
    private BigDecimal balance;

    public Account(BigDecimal balance) {
        this.balance = balance;
    }

    public Account(int id, BigDecimal balance) {
        this.id = id;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", balance=" + balance +
                '}';
    }
}
