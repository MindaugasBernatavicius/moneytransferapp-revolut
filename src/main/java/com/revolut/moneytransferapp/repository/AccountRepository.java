package com.revolut.moneytransferapp.repository;

import com.revolut.moneytransferapp.model.Account;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AccountRepository {
    private List<Account> accounts;
    private int accountCount;

    public AccountRepository() {
        accounts = new ArrayList<Account>(){{
            add(new Account(0, new BigDecimal("0.01")));
            add(new Account(1, new BigDecimal("1.01")));
            add(new Account(2, new BigDecimal("2.01")));
        }};
        accountCount = accounts.size();
    }

    public List<Account> getAccounts() {
        return this.accounts;
    }

    public Account getAccountById(int id){
        for (Account account : accounts)
            if(account.getId() == id)
                return account;
        return null;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
        this.accountCount = accounts.size();
    }

    public int getAccountCount() {
        return accountCount;
    }

    public int saveAccount(Account account){
        int id = accounts.size();
        account.setId(id);
        accounts.add(account);
        ++accountCount;
        return id;
    }

    public void updateAccount(Account account){
        for (int i = 0; i < accounts.size(); i++){
            Account tmpAcc = accounts.get(i);
            if(tmpAcc.getId().equals(account.getId()))
                tmpAcc.setBalance(account.getBalance());
        }
    }
}
