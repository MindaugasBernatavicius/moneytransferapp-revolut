package com.revolut.moneytransferapp.model;

import java.math.BigDecimal;

public class Account extends Model {

    private BigDecimal balance;

    public Account(BigDecimal balance) {
        super(0);
        this.balance = balance;
    }

    public Account(Integer id, BigDecimal balance) {
        super(id);
        this.balance = balance;
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
                "id=" + super.getId() +
                ", balance=" + balance +
                '}';
    }
}
