package com.revolut.moneytransferapp.model;

import java.math.BigDecimal;

public class Transfer extends Model {
    private Integer benefactor;
    private Integer beneficiary;
    private BigDecimal amount;

    public Transfer(Integer id) {
        super(id);
    }

    public Transfer(Integer benefactor, Integer beneficiary, BigDecimal amount) {
        this.benefactor = benefactor;
        this.beneficiary = beneficiary;
        this.amount = amount;
    }

    public Integer getBenefactorId() {
        return benefactor;
    }

    public Integer getBeneficiaryId() {
        return beneficiary;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Transfer{" +
                "benefactor=" + benefactor +
                ", beneficiary=" + beneficiary +
                ", amount=" + amount + "}";
    }
}
