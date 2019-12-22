package com.revolut.moneytransferapp.repository;

import com.revolut.moneytransferapp.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountRepositoryTest {

    AccountRepository accountRepository;

    @BeforeEach
    public void initRepo(){
        accountRepository = new AccountRepository();
    }

    @Test
    void getAccounts__whenCalled__thenReturnsAllAccountsSuccessfully(){
        // given
        int accId1 = 1; var balance1 = new BigDecimal("1.01");
        int accId2 = 2; var balance2 = new BigDecimal("2.01");
        var accountsIn =  new ArrayList<Account>(){{
            add(new Account(accId1, balance1));
            add(new Account(accId2, balance2));
        }};
        accountRepository.setAccounts(accountsIn);

        // when
        var accountsOut = accountRepository.getAll();

        // then
        assertEquals(accountsIn, accountsOut);
    }

    @Test
    void getAccountById__whenCalledWithExistingID__thenReturnsCorrespondingAccountSuccessfully(){
        // given
        int accId1 = 1; var balance1 = new BigDecimal("1.01");
        int accId2 = 2; var balance2 = new BigDecimal("2.01");
        var accountsIn =  new ArrayList<Account>(){{
            add(new Account(accId1, balance1));
            add(new Account(accId2, balance2));
        }};
        accountRepository.setAccounts(accountsIn);

        // when
        var accountsOut = accountRepository.getById(1);

        // then
        assertEquals(accountsIn.get(0), accountsOut);
    }

    @Test
    void getAccountById__whenCalledWithNonExistingID__thenReturnsNull(){
        // given
        int accId1 = 1; var balance1 = new BigDecimal("1.01");
        int accId2 = 2; var balance2 = new BigDecimal("2.01");
        var accountsIn =  new ArrayList<Account>(){{
            add(new Account(accId1, balance1));
            add(new Account(accId2, balance2));
        }};
        accountRepository.setAccounts(accountsIn);

        // when
        var accountsOut = accountRepository.getById(3);

        // then
        assertEquals(null, accountsOut);
    }

    @Test
    void saveAccount__whenCalledWithValidAccountRef__thenSuccessfullyCreatesAccountReturnsId(){
        // given
        var accountIn = new Account(new BigDecimal("100.99"));

        // when
        int createdAccId = accountRepository.save(accountIn);

        // then
        var actualBalance = accountRepository.getById(createdAccId).getBalance();
        assertEquals(accountIn.getBalance(), actualBalance);
    }

    @Test
    void updateAccount__whenCalledWithValidAccountRef__thenSuccessfullyModifiesAccount(){
        // given
        int accId1 = 1; var balance1 = new BigDecimal("1.01");
        var accountIn = new Account(accId1, balance1);
        var accountsIn = new ArrayList<Account>(){{ add(accountIn); }};
        accountRepository.setAccounts(accountsIn);

        var balance2 = new BigDecimal("2.01");

        // when
        var updatedAccount = new Account(accId1, balance2);
        accountRepository.update(updatedAccount);

        // then
        var updatedBalance = accountRepository.getById(accId1).getBalance();
        assertEquals(accountIn.getBalance(), updatedBalance);
    }

    @Test
    void updateAccount__whenCalledWithInvalidAccountRef__thenDoesNotModifyAccount(){
        // given
        int existentAccId1 = 1; var balance1 = new BigDecimal("1.01");
        int nonExistentAccId1 = 5; var balance2 = new BigDecimal("5.05");
        var accountIn = new Account(existentAccId1, balance1);
        var accountsIn = new ArrayList<Account>(){{ add(accountIn); }};
        accountRepository.setAccounts(accountsIn);

        // when
        var updatedAccount = new Account(nonExistentAccId1, balance2);
        accountRepository.update(updatedAccount);

        // then
        assertEquals(accountIn.getBalance(), balance1);
    }
}
