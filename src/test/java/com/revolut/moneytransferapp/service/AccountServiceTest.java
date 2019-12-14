package com.revolut.moneytransferapp.service;

import com.revolut.moneytransferapp.model.Account;
import com.revolut.moneytransferapp.repository.AccountRepository;
import com.revolut.moneytransferapp.service.exceptions.AccountNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

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
    public void updateAccount__whenNoAccountsPresent__throwsException(){
        int accId = 1;
        BigDecimal b = new BigDecimal(1);
        assertThrows(AccountNotFoundException.class,() ->
                accountService.updateAccount(accId, new Account(accId, b)));
    }

    @Test
    public void updateAccount__givenExistingAccountId__updatesAccCorrectly() throws AccountNotFoundException {
        // given
        int accId = 1;
        BigDecimal b = new BigDecimal(1);
        accountRepository.setAccounts(new ArrayList<Account>(){{ add(new Account(accId, b)); }});

        // when
        Account updatedAccount = new Account(1, new BigDecimal(5));
        accountService.updateAccount(1, updatedAccount);

        // then
        assertEquals(new BigDecimal(5), accountService.getAccountById(accId).getBalance());
    }
}
