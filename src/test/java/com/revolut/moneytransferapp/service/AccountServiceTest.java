package com.revolut.moneytransferapp.service;

import com.revolut.moneytransferapp.model.Account;
import com.revolut.moneytransferapp.repository.AccountRepository;
import com.revolut.moneytransferapp.repository.TransferRepository;
import com.revolut.moneytransferapp.service.serviceexception.AccountNotFoundException;
import com.revolut.moneytransferapp.service.serviceexception.InvalidTransferException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AccountServiceTest {

    AccountRepository accountRepository;
    AccountService accountService;

    @BeforeEach
    public void initData(){
        accountRepository = new AccountRepository();
        accountRepository.setAccounts(new ArrayList<>());
        accountService = new AccountService(accountRepository);
    }

    @Test
    public void getAccounts__whenCalledWithNoParams__thenReturnsAllAccounts(){
        // given
        var accId1 = 1; BigDecimal balance1 = new BigDecimal("1.01");
        var accId2 = 2; BigDecimal balance2 = new BigDecimal("2.01");
        var accountsIn =  new ArrayList<Account>(){{
            add(new Account(accId1, balance1));
            add(new Account(accId2, balance2));
        }};
        accountRepository.setAccounts(accountsIn);

        // when
        var accountsOut = accountService.getAccounts();

        // then
        assertEquals(accountsIn, accountsOut);
    }

    @Test
    public void getAccountById__whenCalledWithExistingId__thenReturnsAccountForThatId()
            throws AccountNotFoundException {
        // given
        var accId1 = 1; BigDecimal balance1 = new BigDecimal("1.01");
        var accId2 = 2; BigDecimal balance2 = new BigDecimal("2.01");
        var accountsIn =  new ArrayList<Account>(){{
            add(new Account(accId1, balance1));
            add(new Account(accId2, balance2));
        }};
        accountRepository.setAccounts(accountsIn);

        // when
        var accountOut = accountService.getAccountById(1);

        // then
        assertEquals(accountsIn.get(0), accountOut);
    }

    @Test
    public void getAccountById__whenCalledWithNonExistingId__thenThrows() {
        // given
        var accId1 = 1; BigDecimal balance1 = new BigDecimal("1.01");
        var accId2 = 2; BigDecimal balance2 = new BigDecimal("2.01");
        var accountsIn =  new ArrayList<Account>(){{
            add(new Account(accId1, balance1));
            add(new Account(accId2, balance2));
        }};
        accountRepository.setAccounts(accountsIn);

        // when / then
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountById(3));
    }

    @Test
    public void createAccount__whenCalled__thenCreatesAccountReturnsId(){
        // given
        var accId1 = 0; BigDecimal balance1 = new BigDecimal("1.01");
        var accountsIn =  new ArrayList<Account>(){{
            add(new Account(accId1, balance1)); }};
        accountRepository.setAccounts(accountsIn);

        // when
        var createdAccountId = accountService.createAccount();

        // then
        assertEquals(1, createdAccountId);
    }

    @Test
    public void updateAccount__whenCalledWithNonExistentAccId__throwsException(){
        // given
        var accId = 1; BigDecimal balance = new BigDecimal(1);
        var account = new Account(accId, balance);

        // when / then
        assertThrows(AccountNotFoundException.class, () -> accountService.updateAccount(account));
    }

    @Test
    public void updateAccount__givenExistingAccountId__updatesAccCorrectly()
            throws AccountNotFoundException {
        // given
        var accId = 1; BigDecimal balance = new BigDecimal("1");
        var accounts =  new ArrayList<Account>(){{
            add(new Account(accId, balance)); }};
        accountRepository.setAccounts(accounts);

        // when
        var updatedBalance = new BigDecimal("5");
        var updatedAccount = new Account(accId, updatedBalance);
        accountService.updateAccount(updatedAccount);

        // then
        var actualBalance = accountService.getAccountById(accId).getBalance();
        assertEquals(updatedBalance, actualBalance);
    }
}
