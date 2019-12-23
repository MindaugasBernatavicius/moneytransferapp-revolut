package com.revolut.moneytransferapp.model;

import com.google.gson.annotations.Expose;

import java.math.BigDecimal;

public class Account extends VersionedEntity {

    @Expose(serialize = true)
    private BigDecimal balance;

    public Account(BigDecimal balance) {
        super(0, 0);
        this.balance = balance;
    }

    public Account(Integer id, BigDecimal balance) {
        super(id, 0);
        this.balance = balance;
    }

    public Account(Integer id, BigDecimal balance, Integer version) {
        super(id, version);
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
                ", version=" + super.getVersion() +
                '}';
    }
}
