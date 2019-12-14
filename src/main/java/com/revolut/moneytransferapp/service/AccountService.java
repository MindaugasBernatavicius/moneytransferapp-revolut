package com.revolut.moneytransferapp.service;

import com.revolut.moneytransferapp.model.Account;
import com.revolut.moneytransferapp.repository.AccountRepository;
import com.revolut.moneytransferapp.service.exceptions.AccountNotFoundException;
import com.revolut.moneytransferapp.service.exceptions.InvalidTransferException;

import java.math.BigDecimal;
import java.util.List;

public class AccountService {

    private AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAccounts() {
        return accountRepository.getAccounts();
    }

    public Account getAccountById(int id) {
        return accountRepository.getAccountById(id);
    }

    public synchronized long createAccount(){
        Account account = new Account(new BigDecimal("0.0"));
        return accountRepository.save(account);
    }

    public void updateAccount(int accountId, Account updateObject) throws AccountNotFoundException {
        // TODO :: possibly move, because this is specific to the List backed implementation of repo
        // if another annother implementation would be used this would not need to be here
        if (accountRepository.getAccountCount() < accountId)
            throw new AccountNotFoundException();

        Account account = accountRepository.getAccountById(accountId);
        if (account == null)
            throw new AccountNotFoundException();
        else
            account.setBalance(updateObject.getBalance());
    }

    public void transferMoney(int benefactorId, int beneficiaryId, BigDecimal amountToTransfer) throws InvalidTransferException {
        Account benefactorAccount = accountRepository.getAccountById(benefactorId);
        BigDecimal benefactorBalance = accountRepository.getAccountById(benefactorId).getBalance();
        BigDecimal beneficiaryBalance = accountRepository.getAccountById(benefactorId).getBalance();
        if(benefactorBalance.subtract(amountToTransfer).compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidTransferException();

        Account beneficiaryAccount = accountRepository.getAccountById(beneficiaryId);

        // transfer
        benefactorAccount.setBalance(benefactorBalance.subtract(amountToTransfer));
        beneficiaryAccount.setBalance(beneficiaryBalance.add(amountToTransfer));
    }
}
