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
        int accId1 = 1; BigDecimal balance1 = new BigDecimal("1.01");
        int accId2 = 2; BigDecimal balance2 = new BigDecimal("2.01");
        List<Account> accountsIn =  new ArrayList<Account>(){{
            add(new Account(accId1, balance1));
            add(new Account(accId2, balance2));
        }};
        accountRepository.setAccounts(accountsIn);

        // when
        List<Account> accountsOut = accountRepository.getAccounts();

        // then
        assertEquals(accountsIn, accountsOut);
    }

    @Test
    void getAccountById__whenCalledWithExistingID__thenReturnsCorrespondingAccountSuccessfully(){
        // given
        int accId1 = 1; BigDecimal balance1 = new BigDecimal("1.01");
        int accId2 = 2; BigDecimal balance2 = new BigDecimal("2.01");
        List<Account> accountsIn =  new ArrayList<Account>(){{
            add(new Account(accId1, balance1));
            add(new Account(accId2, balance2));
        }};
        accountRepository.setAccounts(accountsIn);

        // when
        Account accountsOut = accountRepository.getAccountById(1);

        // then
        assertEquals(accountsIn.get(0), accountsOut);
    }

    @Test
    void getAccountById__whenCalledWithNonExistingID__thenReturnsNull(){
        // given
        int accId1 = 1; BigDecimal balance1 = new BigDecimal("1.01");
        int accId2 = 2; BigDecimal balance2 = new BigDecimal("2.01");
        List<Account> accountsIn =  new ArrayList<Account>(){{
            add(new Account(accId1, balance1));
            add(new Account(accId2, balance2));
        }};
        accountRepository.setAccounts(accountsIn);

        // when
        Account accountsOut = accountRepository.getAccountById(3);

        // then
        assertEquals(null, accountsOut);
    }
}
