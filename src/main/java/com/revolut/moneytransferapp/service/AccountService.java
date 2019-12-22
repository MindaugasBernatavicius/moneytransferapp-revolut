package com.revolut.moneytransferapp.service;

import com.revolut.moneytransferapp.model.Account;
import com.revolut.moneytransferapp.repository.AccountRepository;
import com.revolut.moneytransferapp.service.serviceexception.AccountNotFoundException;

import java.math.BigDecimal;
import java.util.List;

public class AccountService {

    private AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    public List<Account> getAccounts() { return accountRepository.getAll(); }

    public Account getAccountById(int id) throws AccountNotFoundException {
        Account account = accountRepository.getById(id);
        if (account == null) throw new AccountNotFoundException();
        return account;
    }

    public synchronized int createAccount(){
        Account account = new Account(new BigDecimal("0.0"));
        return accountRepository.save(account);
    }

    public synchronized void updateAccount(Account updateObject) throws AccountNotFoundException {
        Integer id = updateObject.getId();
        if(getAccountById(id) == null)
            throw new AccountNotFoundException();
        accountRepository.update(updateObject);
    }
}
