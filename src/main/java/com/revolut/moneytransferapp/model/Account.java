package com.revolut.moneytransferapp.model;

import java.math.BigDecimal;

public class Account {

    private Integer id;
    private BigDecimal balance;

    public Account(BigDecimal balance) {
        this.balance = balance;
    }

    public Account(Integer id, BigDecimal balance) {
        this.id = id;
        this.balance = balance;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
