package com.revolut.moneytransferapp.repository;

import com.revolut.moneytransferapp.model.Account;
import com.revolut.moneytransferapp.repository.repositoryexceptions.OptimisticLockException;
import com.revolut.moneytransferapp.service.serviceexception.AccountNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountRepositoryOptimisticLockingTest {
    AccountRepository accountRepository;

    @BeforeEach
    public void initRepo(){
        accountRepository = new AccountRepository();
    }

    @RepeatedTest(4)
    void updateAccount__whenCalledConcurrently__throwsInterruptedException()
            throws InterruptedException, ExecutionException {
        // given
        var degreeOfParallelism = Runtime.getRuntime().availableProcessors() * 2 + 1;
        var executor = Executors.newFixedThreadPool(degreeOfParallelism);
        var responses = new ArrayList<Future>();

        int accId1 = 1; var balance1 = new BigDecimal("1.01");
        int accId2 = 2; var balance2 = new BigDecimal("2.01");
        var accountsIn = new ArrayList<Account>(){{
            add(new Account(accId1, balance1));
            add(new Account(accId2, balance2));
        }};
        accountRepository.setAccounts(accountsIn);

        // when
        AtomicInteger exceptionCounter = new AtomicInteger(0);
        for (int i = 0; i < degreeOfParallelism; i++){
            var balance = new BigDecimal(Integer.toString(i));
            responses.add(executor.submit(() -> {
                try { accountRepository.update(new Account(1, balance)); }
                catch (OptimisticLockException e) { exceptionCounter.incrementAndGet(); }
                catch (AccountNotFoundException e) { /* skip */ }
            }));
            responses.add(executor.submit(() -> {
                try { accountRepository.update(new Account(1, balance)); }
                catch (OptimisticLockException e) { exceptionCounter.incrementAndGet(); }
                catch (AccountNotFoundException e) { /* skip */ }
            }));
        }
        executor.shutdown();

        for (int i = 0; i < responses.size(); i++)
            responses.get(i).get();

        // then
        assertTrue(exceptionCounter.get() > 0);
        int expectedVersionIncrements = degreeOfParallelism * 2 - exceptionCounter.get();
        assertEquals(accountRepository.getById(1).getVersion(), expectedVersionIncrements);
        assertTrue(accountRepository.getById(1).getBalance().compareTo(new BigDecimal("2")) > 0);
        assertTrue(accountRepository.getById(1).getVersion() > 2);
    }
}
